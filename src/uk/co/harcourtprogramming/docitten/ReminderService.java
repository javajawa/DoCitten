package uk.co.harcourtprogramming.docitten;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import uk.co.harcourtprogramming.internetrelaycats.ExternalService;
import uk.co.harcourtprogramming.internetrelaycats.InternetRelayCat;
import uk.co.harcourtprogramming.internetrelaycats.Message;
import uk.co.harcourtprogramming.internetrelaycats.MessageService;
import uk.co.harcourtprogramming.internetrelaycats.RelayCat;
import uk.co.harcourtprogramming.mewler.MessageTokeniser;

/**
 * <p>Service for create todo list and reminders</p>
 *
 * @author Benedict Harcourt / javajawa
 */
public class ReminderService extends ExternalService implements MessageService
{

	/**
	 * <p>Name of the service, for analysing commands</p>
	 */
	private final static String SERVICE_NAME = "reminder";
	/**
	 * <p>List of the different valid commands</p>
	 */
	private enum Commands
	{

		/**
		 * <p>Add a new times reminder</p>
		 */
		add,
		/**
		 * <p>Remove a reminder or note</p>
		 */
		remove,
		/**
		 * <p>List reminders and notes</p>
		 */
		list,
		/**
		 * <p>Create a new note</p>
		 */
		note,
		/**
		 * <p>Shows the help information</p>
		 */
		help,
		/**
		 * <p>Marker for a non-valid command</p>
		 */
		badcmd
	}
	/**
	 * <p>Comparator that implements the 'natural' ordering for a todo list</p>
	 * <ul>
	 *	<li>Notes sort to the bottom of the list</li>
	 *  <li>Reminders are ordered according how far in the future they occur</li>
	 *  <li>Remaining comparisons are done based on the creation time (oldest to
	 * newest</li>
	 * </ul>
	 */
	private final static Comparator<AbstractReminder> reminderOrderer =
	(
		new Comparator<AbstractReminder>()
		{
			@Override
			public int compare(AbstractReminder o1, AbstractReminder o2)
			{
				if (o1 == o2)
					return 0;

				if (o1 instanceof Note)
				{
					if (o2 instanceof Note)
						return ((Long)o1.setTimestamp).compareTo(o2.setTimestamp);

					return -1;
				}
				if (o2 instanceof Note)
					return 1;

				final Reminder r1 = (Reminder)o1;
				final Reminder r2 = (Reminder)o2;

				if (r1.sendTimestamp == r2.sendTimestamp)
					return ((Long)o1.setTimestamp).compareTo(o2.setTimestamp);

				return ((Long)r1.sendTimestamp).compareTo(r2.sendTimestamp);
			}
		}
	);
	/**
	 * <p>Notes and reminders, catalogued by user</p>
	 */
	private final Map<String, SortedSet<AbstractReminder>> userReminders =
			new TreeMap<String, SortedSet<AbstractReminder>>();
	/**
	 * <p>All the reminders, ordered by when they need to be sent out</p>
	 */
	private final SortedSet<Reminder> globalReminders =
			new TreeSet<Reminder>(reminderOrderer);

	/**
	 * <p>Base class for all reminders stored by the ReminderService</p>
	 */
	private static abstract class AbstractReminder
	{

		/**
		 * <p>The nick that created the reminder</p>
		 */
		protected final String nick;
		/**
		 * <p>The text content of the reminder</p>
		 */
		protected final String data;
		/**
		 * <p>When this AbstractReminder was created</p>
		 */
		protected final long setTimestamp;

		/**
		 * <p>Creates a new instance of an AbstractReminder</p>
		 * @param nick the nick that created the reminder
		 * @param data the text content of the reminder
		 */
		AbstractReminder(String nick, String data)
		{
			this.nick = nick;
			this.data = data;
			this.setTimestamp = System.currentTimeMillis();
		}
	}

	/**
	 * <p>Class representing a todo note</p>
	 */
	private static class Note extends AbstractReminder
	{

		/**
		 * @param nick the nick that created the reminder
		 * @param data the text content of the reminder
		 * @see AbstractReminder
		 */
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

	/**
	 * <p>Class representing an active reminder</p>
	 */
	private static class Reminder extends AbstractReminder
	{

		/**
		 * <p>When to send this reminder</p>
		 */
		final long sendTimestamp;

		/**
		 * <p>Creates a new reminder instance</p>
		 * @param sendTimestamp when to send this reminder
		 * @param nick the nick that created the reminder
		 * @param data the text content of the reminder
		 */
		protected Reminder(long sendTimestamp, String nick, String data)
		{
			super(nick, data);
			this.sendTimestamp = sendTimestamp;
		}

		/**
		 * <p>Creates a blank reminder object, which can be used to partition a
		 * sorted tree into reminders that have occurred and those that have
		 * not</p>
		 */
		protected Reminder()
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

	/**
	 * <p>Creates a new ReminderService instance</p>
	 *
	 * @param inst the IRC interface to attach to
	 */
	public ReminderService(InternetRelayCat inst)
	{
		super(inst);
	}

	@Override
	protected void startup(RelayCat r)
	{
		List<HelpService> helpServices = r.getServicesByClass(HelpService.class);

		if (!helpServices.isEmpty())
		{
			HelpService.HelpInfo help = new HelpService.HelpInfo("Reminder Service",
				  "  add _time_ _message_    Not Yet Implemented\n"
				+ "  note _note text_        Add a static note with the given textm\n"
				+ "  list                    List all notes and alarms\n"
				+ "  remove _index_          Remove a note or alarm, by the index in the list");
			helpServices.get(0).addHelp("reminder", help);
		}
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
	@SuppressWarnings("SleepWhileInLoop")
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
				add(m, tokeniser.toString());
				break;
			case badcmd:
				m.reply("Unknown Command");
			case help:
				m.reply("See 'help reminder'");
				break;
			case list:
				list(m);
				break;
			case note:
				note(m, tokeniser.toString());
				break;
			case remove:
				remove(m, tokeniser.toString());
				break;
		}
	}

	/**
	 * <p>Add a new timed reminder</p>
	 * @param m message that contained this command
	 * @param data parameters to the command
	 */
	private void add(Message m, String data)
	{
		m.reply("Not yet implemented (sorry!)");
	}

	/**
	 * <p>Adds a new note</p>
	 * @param m message that contained this command
	 * @param data parameters to the command
	 */
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

	/**
	 * <p>Lists all the reminders and notes for a user</p>
	 * @param m message that contained this command
	 */
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

	/**
	 * <p>Removes a reminder or note</p>
	 * @param m message that contained this command
	 * @param data parameters to the command
	 */
	private void remove(Message m, String data)
	{
		int index;
		try
		{
			index = Integer.parseInt(data);
		}
		catch (NumberFormatException ex)
		{
			m.reply("The remove function takes a single, numberic parameter.\r\n"
				+ "See 'help reminder' for more information");
			return;
		}

		synchronized (globalReminders)
		{
			SortedSet<AbstractReminder> reminders = userReminders.get(m.getSender());

			if (reminders == null)
			{
				m.reply("You have no reminders to remove");
				return;
			}
			if (index < 1 || index > reminders.size())
			{
				m.reply(String.format("Index %d out of range - see 'reminder list'", index));
				return;
			}

			Iterator<AbstractReminder> it = reminders.iterator();
			AbstractReminder toRemove = null;

			while (--index >= 0)
				toRemove = it.next();

			reminders.remove(toRemove);
			if (toRemove instanceof Reminder)
				globalReminders.remove((Reminder)toRemove);

			m.reply("Reminder removed.");
		}
	}
}
