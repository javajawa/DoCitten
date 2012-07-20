package uk.co.harcourtprogramming.docitten;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import uk.co.harcourtprogramming.internetrelaycats.ExternalService;
import uk.co.harcourtprogramming.internetrelaycats.InternetRelayCat;
import uk.co.harcourtprogramming.internetrelaycats.Message;
import uk.co.harcourtprogramming.internetrelaycats.MessageService;
import uk.co.harcourtprogramming.internetrelaycats.MessageTokeniser;
import uk.co.harcourtprogramming.internetrelaycats.RelayCat;

/**
 *
 */
public class ReminderService extends ExternalService implements MessageService
{
	private final static String SERVICE_NAME = "reminder";

	private final static Pattern TIME_PATTERN = Pattern.compile(
	    "(\\d\\d?(\\s?h(ou)?(rs)?|:)\\d\\dm?|\\d\\d?(am|pm))",
		Pattern.CASE_INSENSITIVE
	);
	private final static Pattern DATE_PATTERN = Pattern.compile(
	    "(\\d\\d?(st|nd|rd)\\s?(jan(uary)?|feb|mar|apr|may|jun|ju?ly?|aug|sep|oct|nov|dec))",
		Pattern.CASE_INSENSITIVE
	);

	private enum Commands
	{
		add,
		remove,
		list,
		note,
		help,
		badcmd
	}

	private final static Comparator<AbstractReminder> reminderOrderer =
	   new Comparator<AbstractReminder>() {

		@Override
		public int compare(AbstractReminder o1, AbstractReminder o2)
		{
			if (o1 == o2) return 0;
			if (o1 instanceof Note)
			{
				if (o2 instanceof Note)	return ((Long)o1.setTimestamp).compareTo(o2.setTimestamp);
				return -1;
			}
			if (o2 instanceof Note)
				return 1;
			final Reminder r1 = (Reminder)o1;
			final Reminder r2 = (Reminder)o2;
			if (r1.sendTimestamp == r2.sendTimestamp)
			{
				return ((Long)o1.setTimestamp).compareTo(o2.setTimestamp);
			}
			return ((Long)r1.sendTimestamp).compareTo(r2.sendTimestamp);
		}
	};

	private final Map<String, SortedSet<AbstractReminder>> userReminders =
	    new TreeMap<String, SortedSet<AbstractReminder>>();
	private final SortedSet<Reminder> globalReminders =
		new TreeSet<Reminder>(reminderOrderer);

	private static abstract class AbstractReminder
	{
		final String nick;
		final String data;
		final long setTimestamp = System.currentTimeMillis();

		AbstractReminder(String nick, String data)
		{
			this.nick = nick;
			this.data = data;
		}

	}

	private static class Note extends AbstractReminder
	{
		Note(String nick, String data)
		{
			super(nick, data);
		}

		@Override
		public String toString()
		{
			return data;
		}
	}

	private static class Reminder extends AbstractReminder
	{
		final long sendTimestamp;

		Reminder(long sendTimestamp, String nick, String data)
		{
			super(nick, data);
			this.sendTimestamp = sendTimestamp;
		}

		Reminder()
		{
			super(null, null);
			sendTimestamp = System.currentTimeMillis();
		}

		@Override
		public String toString()
		{
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(sendTimestamp);

			return String.format("[%2$tD %2$tR] %1$s", data, c);
		}


	}

	public ReminderService(InternetRelayCat inst)
	{
		super(inst);
	}

	@Override
	protected void startup(RelayCat r)
	{
		// Nothing to see here. Move along, citizen!
	}

	@Override
	public void shutdown()
	{
		synchronized (globalReminders)
		{
			globalReminders.clear();
		}
	}

	@Override
	public void run()
	{
		try
		{
			while (true)
			{
				synchronized (globalReminders)
				{
					// new Reminder() returns reminders reminder that is timestamped now
					// Due to the ordering, the head set of this is all reminders
					// that happened in the past
					for (Reminder r : globalReminders.headSet(new Reminder()))
					{
						this.getInstance().message(r.nick, r.data);
					}
				}
				Thread.sleep(30000);
			}
		}
		catch (InterruptedException ex)
		{
		}
	}

	@Override
	@SuppressWarnings("fallthrough")
	public void handle(Message m)
	{
		MessageTokeniser tokeniser = new MessageTokeniser(m.getMessage());
		tokeniser.setConsumeWhitespace(true);

		if (!tokeniser.consume(m.getNick() + ':') && m.getChannel() != null)
			return;

		if (!tokeniser.consume(SERVICE_NAME))
			return;

		Commands c;
		try
		{
			c = Commands.valueOf(tokeniser.nextToken(' '));
		}
		catch (Exception ex)
		{
			c = Commands.badcmd;
		}

		switch (c)
		{
			case add:
				m.reply("reminder: add command");
				break;
			case badcmd:
				m.reply("Unknown Command");
			case help:
				m.reply("= Reminder Service =\nadd     Not Yet Implemented\nnote    Add a static note\nlist    List all notes and alarms\nremove  Not Yet Implemented\nhelp    This help information");
				break;
			case list:
				list(m);
				break;
			case note:
				note(m, tokeniser.toString());
				break;
			case remove:
				m.reply("reminder: remove command");
				break;
		}
	}

	private void note(Message m, String data)
	{
		Note newNote = new Note(m.getSender(), data);
		synchronized (globalReminders)
		{
			if (!userReminders.containsKey(m.getSender()))
			{
				TreeSet<AbstractReminder> newUserSet = new TreeSet<AbstractReminder>(reminderOrderer);
				newUserSet.add(newNote);
				userReminders.put(m.getSender(), newUserSet);
			}
			else
			{
				userReminders.get(m.getSender()).add(newNote);
			}
		}
		try
		{
			Thread.sleep(2);
		}
		catch (InterruptedException ex)
		{
		}
		m.reply("Note Created");
	}

	private void list(Message m)
	{
		synchronized (globalReminders)
		{
			SortedSet<AbstractReminder> userSet;
			if (!userReminders.containsKey(m.getSender()))
			{
				userSet = new TreeSet<AbstractReminder>();
			}
			else
			{
				userSet = userReminders.get(m.getSender());
			}
			if (userSet.isEmpty())
			{
				m.reply("You have no active reminders.");
			}
			else
			{
				m.reply("Active Reminders for " + m.getSender() + " (" + userSet.size() + "):");
				int i = 0;
				for (AbstractReminder r : userSet)
				{
					m.reply(String.format("%3d: %s", ++i, r));
				}
			}
		}
	}
}
