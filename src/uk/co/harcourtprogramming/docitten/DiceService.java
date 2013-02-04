package uk.co.harcourtprogramming.docitten;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import uk.co.harcourtprogramming.internetrelaycats.Message;
import uk.co.harcourtprogramming.internetrelaycats.MessageService;
import uk.co.harcourtprogramming.internetrelaycats.RelayCat;
import uk.co.harcourtprogramming.internetrelaycats.Service;
import uk.co.harcourtprogramming.mewler.MessageTokeniser;

/**
 * <p>Service for generating dice rolls for users</p>
 *
 * @author Benedict Harcourt / javajawa
 */
public class DiceService extends Service implements MessageService
{
	/**
	 * <p>Random number generator for dice rolls
	 */
	private final Random r = new Random();
	/**
	 * <p>Pattern which matches all of the dice commands that this service
	 * recognises</p>
	 */
	private final static Pattern dicePattern = Pattern.compile("roll(?<mode> (?:sum|product|base|barrel))?(?<dice>(?: [0-9]*d[0-9]+d?)+)", Pattern.CASE_INSENSITIVE);

	@Override
	protected void shutdown()
	{
		// Nothing to see here. Move along, citizen!
	}

	@Override
	protected void startup(RelayCat r)
	{
		List<HelpService> helpServices = r.getServicesByClass(HelpService.class);

		if (!helpServices.isEmpty())
		{
			HelpService.HelpInfo help = new HelpService.HelpInfo("Dice Service", "DoCitten can be asked to roll dice\r\nDoCitten: roll [mode] <dice>[ <dice> ...]\r\nYou can try 'roll d6', or look at the subtopics for information .");
			help.addChild("mode", new HelpService.HelpInfo("Dice Modes", "The Dice service can combine in one of three ways\r\n \r\nsum:     Dice values are added\r\nproduct: Dice are multiplied together\r\nbase:    Dice are treated each as a digit in base n, when n is the number of sides on the dice."));
			help.addChild("dice", new HelpService.HelpInfo("Dice Specification", "Dice are specified in a simple syntax: [number]d<sides>[d]\r\nFor example, you would ask for a six-sided dice to be rolled four times as '4d6'.\r\nThe final d converts the die to a doubling die."));
			helpServices.get(0).addHelp("dice", help);
		}
	}

	@Override
	public void handle(Message m)
	{
		Matcher dmatch = dicePattern.matcher(m.getMessage());
		if (dmatch.find())
		{
			DiceMode mode    = DiceMode.parse(dmatch.group("mode"));

			if (mode == DiceMode.BARREL)
			{
				m.replyToAll("nnnawaarrwwwhhhwaarrwwwhhh");
				return;
			}

			String[] dice    = dmatch.group("dice").trim().split(" ");

			StringBuilder s = new StringBuilder(48 * dice.length);
			int total = -1;

			switch (mode)
			{
				case SUM: total = 0; break;
				case PRODUCT: total = 1; break;
			}

			for (String d : dice)
			{
				Die die = new Die(d);
				int[] rolls = new int[die.count];
				int roll    = die.roll(mode, rolls);

				s.append("I got ").append(roll).append(" for ");
				s.append(die).append(' ').append(Arrays.toString(rolls)).append('\n');

				switch (mode)
				{
					case SUM: total += roll; break;
					case PRODUCT: total *= roll; break;
				}
			}

			if (dice.length > 1)
			{
				switch (mode)
				{
					case SUM: case PRODUCT: s.append("Total: ").append(total);
				}
			}

			m.replyToAll(s.toString());
		}
	}

	/**
	 * <p>A class that represents a matched group of similar dice.</p>
	 */
	private final class Die
	{
		/**
		 * <p>The number of times this dice is to be rolled</p>
		 */
		final int count;
		/**
		 * <p>The number of sides (faces) each die has</p>
		 */
		final int sides;
		/**
		 * <p>Whether the sides are numbered carindally, or as powers of two</p>
		 */
		final boolean doubling;

		/**
		 * <p>Creates a new Die object based on a specification string</p>
		 * @param spec A string of the form [0-9]*d[0-9]+d?
		 */
		private Die(String spec)
		{
			MessageTokeniser t = new MessageTokeniser(spec);
			t.setConsumeWhitespace(true);

			String c = t.nextToken('d').trim();
			if ("".equalsIgnoreCase(c))
				this.count = 1;
			else
				this.count = Integer.parseInt(c);

			t.consume("d");

			this.doubling = ('d' == t.charAt(t.length() - 1));

			c = t.nextToken('d');
			this.sides = Integer.parseInt(c);
		}

		/**
		 * <p>Rolls these dice</p>
		 * @param mode how the individual rolls are combined
		 * @param rolls an array of ints, at least as long as {@link #count}
		 * @return the total value of the rolls
		 */
		private int roll(DiceMode mode, int[] rolls)
		{
			int result = 0;
			if (mode == DiceMode.PRODUCT)
				result = 1;

			for (int i = 0; i < count; ++i)
			{
				int roll = r.nextInt(this.sides);

				if (mode != DiceMode.BASE)
					roll += 1;

				if (this.doubling)
					roll = (int)Math.pow(2, roll);

				if (rolls != null)
					rolls[i] = roll;

				switch (mode)
				{
					case SUM:
						result += roll; break;
					case PRODUCT:
						result *= roll; break;
					case BASE:
						result *= this.sides;
						result += roll;
						break;
				}
			}

			return result;
		}

		@Override
		public String toString()
		{
			if (doubling)
				return String.format("%d %d-sided doubling dice", count, sides);
			else
				return String.format("%d %d-sided dice", count, sides);
		}
	}

	/**
	 * <p>Different modes for combining individual die rolls</p>
	 */
	private enum DiceMode
	{
		/**
		 * <p>Dice values are added together</p>
		 */
		SUM,
		/**
		 * <p>Dice values are multiplied together</p>
		 */
		PRODUCT,
		/**
		 * <p>Dice values are treated as a string in base n, where n is the
		 * number of sides on the dice</p>
		 */
		BASE,
		BARREL;

		/**
		 * <p>Converts a string name of a DiceMode to the DiceMode value</p>
		 * @param s The String to convert
		 * @return The mode, or SUM if no match was found
		 */
		public static DiceMode parse(String s)
		{
			if (s == null)
				return SUM;
			try
			{
				return DiceMode.valueOf(s.trim().toUpperCase());
			}
			catch (IllegalArgumentException ex)
			{
				return SUM;
			}
		}
	}
}
