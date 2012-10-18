package uk.co.harcourtprogramming.docitten;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import uk.co.harcourtprogramming.internetrelaycats.Message;
import uk.co.harcourtprogramming.internetrelaycats.MessageService;
import uk.co.harcourtprogramming.internetrelaycats.RelayCat;
import uk.co.harcourtprogramming.internetrelaycats.Service;
import uk.co.harcourtprogramming.mewler.MessageTokeniser;

/**
 *
 * @author Ben
 */
public class DiceService extends Service implements MessageService
{
	private final Random r = new Random();
	private static Pattern dicePattern = Pattern.compile("roll(?<mode> (?:sum|product|base))?(?<dice>(?: [0-9]*d[0-9]+d?)+)", Pattern.CASE_INSENSITIVE);

	@Override
	protected void shutdown()
	{
		// Nothing to see here. Move along, citizen!
	}

	@Override
	protected void startup(RelayCat r)
	{
		// Nothing to see here. Move along, citizen!
	}

	@Override
	public void handle(Message m)
	{
		Matcher dmatch = dicePattern.matcher(m.getMessage());
		if (dmatch.find())
		{
			DiceMode mode    = DiceMode.parse(dmatch.group("mode"));
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

			switch (mode)
			{
				case SUM: case PRODUCT: s.append("Total: ").append(total);
			}

			m.replyToAll(s.toString());
		}
	}

	private final class Die
	{
		protected final int count;
		protected final int sides;
		protected final boolean doubling;

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

	protected enum DiceMode
	{
		SUM,
		PRODUCT,
		BASE;

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
