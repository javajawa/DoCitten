package uk.co.harcourtprogramming.docitten;

import java.io.IOException;
import java.net.UnknownHostException;
import org.junit.Test;
import uk.co.harcourtprogramming.internetrelaycats.Message;
import uk.co.harcourtprogramming.internetrelaycats.TestingRelayCat;
import static org.junit.Assert.*;

public class ReminderServiceTest
{
	public ReminderServiceTest()
	{
	}

	@Test
	public void testListEmptyList() throws UnknownHostException, IOException
	{
		final TestingRelayCat cat = new TestingRelayCat();
		final ReminderService srv = new ReminderService(cat);
		cat.addService(srv);

		cat.inputMessage("bob", "#doc", TestingRelayCat.NAME + ": reminder list");

		Message m = cat.getOutput();
		assertNotNull(m);
		assertEquals("bob", m.getChannel());
		assertEquals("You have no active reminders.", m.getMessage());

		m = cat.getOutput();
		assertNull(m);
	}

	@Test
	public void testNote() throws UnknownHostException, IOException
	{
		final String note = "Hello, world!";
		final TestingRelayCat cat = new TestingRelayCat();
		final ReminderService srv = new ReminderService(cat);
		cat.addService(srv);

		cat.inputMessage("bob", "#doc", TestingRelayCat.NAME + ": reminder note " + note);

		Message m = cat.getOutput();
		assertNotNull(m);
		assertEquals("bob", m.getChannel());
		assertEquals("Note Created", m.getMessage());

		cat.inputMessage("bob", "#doc", TestingRelayCat.NAME + ": reminder list");

		m = cat.getOutput();
		assertNotNull(m);
		assertEquals("Active Reminders for bob (1):", m.getMessage());

		m = cat.getOutput();
		assertNotNull(m);
		assertEquals("  1: " + note, m.getMessage());

		m = cat.getOutput();
		assertNull(m);
	}

	@Test
	public void testNotes()  throws UnknownHostException, IOException, InterruptedException
	{
		final String note1 = "Hello, world!";
		final String note2 = "Hello, computer!";
		final TestingRelayCat cat = new TestingRelayCat();
		final ReminderService srv = new ReminderService(cat);
		Message m;

		cat.addService(srv);

		cat.inputMessage("bob", "#doc", TestingRelayCat.NAME + ": reminder note " + note1);
		cat.inputMessage("bob", "#doc", TestingRelayCat.NAME + ": reminder note " + note2);

		m = cat.getOutput();
		assertNotNull(m);
		assertEquals("bob", m.getChannel());
		assertEquals("Note Created", m.getMessage());

		m = cat.getOutput();
		assertNotNull(m);
		assertEquals("bob", m.getChannel());
		assertEquals("Note Created", m.getMessage());

		cat.inputMessage("bob", "#doc", TestingRelayCat.NAME + ": reminder list");

		m = cat.getOutput();
		assertNotNull(m);
		assertEquals("Active Reminders for bob (2):", m.getMessage());

		m = cat.getOutput();
		assertNotNull(m);
		assertEquals("  1: " + note1, m.getMessage());

		m = cat.getOutput();
		assertNotNull(m);
		assertEquals("  2: " + note2, m.getMessage());

		m = cat.getOutput();
		assertNull(m);
	}

	@Test
	public void UnknownCommand() throws UnknownHostException, IOException
	{
		final TestingRelayCat cat = new TestingRelayCat();
		final ReminderService srv = new ReminderService(cat);
		cat.addService(srv);

		cat.inputMessage("bob", "#doc", TestingRelayCat.NAME + ": reminder flub");

		Message m = cat.getOutput();

		assertNotNull(m);
		assertEquals("Unknown Command", m.getMessage());

		m = cat.getOutput();
		assertNotNull("Help was not also sent", m.getMessage());
	}


	@Test
	public void NullCommand() throws UnknownHostException, IOException
	{
		final TestingRelayCat cat = new TestingRelayCat();
		final ReminderService srv = new ReminderService(cat);
		cat.addService(srv);

		cat.inputMessage("bob", "#doc", TestingRelayCat.NAME + ": reminder");

		Message m = cat.getOutput();

		assertNotNull(m);
		assertEquals("Unknown Command", m.getMessage());

		m = cat.getOutput();
		assertNotNull("Help was not also sent", m.getMessage());
	}
}
