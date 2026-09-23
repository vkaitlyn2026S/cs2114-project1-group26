// Project 1 - Health Bar (CS 2114, Group 26)
// Virginia Tech Honor Code Pledge:
//
// As a Hokie, I will conduct myself with honor and integrity at all times.
// I will not lie, cheat, or steal, nor will I accept the actions of those
// who do.
// -- Prateek Malekar (prateekm30)

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.Scanner;

/**
 * End-to-end tests for HealthBarApp: each test scripts a whole session
 * with a Scanner over a String and checks what was printed and saved.
 * The tests use their own save file, so a real healthbar.txt is never
 * touched (testMain backs it up and puts it back).
 *
 * @author Prateek Malekar (prateekm30)
 * @version 2026.09.22
 */
public class HealthBarAppTest
    extends student.TestCase
{
    private static final String PATH = "test-app.txt";
    private HealthLog log;
    private Goals goals;
    private String today;

    /**
     * Deletes any old test file and prepares fresh data.
     */
    public void setUp()
    {
        new File(PATH).delete();
        log = new HealthLog();
        goals = new Goals();
        today = LocalDate.now().format(InputParser.DATE_FMT);
    }


    /**
     * Deletes the test file.
     */
    public void tearDown()
    {
        new File(PATH).delete();
    }


    /**
     * Builds an app that reads the given script and saves to path.
     *
     * @param typed
     *            the whole session's input
     * @param path
     *            the save file
     * @return the app
     */
    private HealthBarApp app(String typed, String path)
    {
        return new HealthBarApp(new ConsoleUI(new Scanner(typed)), log,
            goals, new HealthScorer(log, goals), new LogFile(path));
    }


    /**
     * The printed output so far.
     *
     * @return the output
     */
    private String printed()
    {
        return systemOut().getHistory();
    }


    /**
     * Dashboard, then save and quit.
     */
    public void testDashboardAndQuit()
    {
        app("4\n6\n", PATH).run();
        assertTrue(printed().contains("Score: 0 / 100"));
        assertTrue(printed().contains("Saved. Goodbye."));
        assertTrue(new File(PATH).exists());
    }


    /**
     * Junk input, then end of input: the note, then a clean save and exit.
     */
    public void testEndOfInput()
    {
        app("zzz\n", PATH).run();
        assertTrue(printed().contains("That is not one of the options"));
        assertTrue(printed().contains("Saved. Goodbye."));
        assertTrue(new File(PATH).exists());
    }


    /**
     * Logging the same metric twice adds to the total and echoes it.
     */
    public void testLogTwice()
    {
        app("1\n1\n\n4200\n1\n1\n\n4,200\n6\n", PATH).run();
        assertTrue(printed().contains(
            "Steps for " + today + " is now 4,200 (added 4,200)"));
        assertTrue(printed().contains(
            "Steps for " + today + " is now 8,400 (added 4,200)"));
        assertEquals(8400.0, log.getDay(LocalDate.now()).getValue(
            Metric.STEPS), 0.001);
    }


    /**
     * A total over the maximum is refused and the amount asked again.
     */
    public void testLogOverMaximum()
    {
        app("1\n1\n\n60000\n1\n1\n\n60000\n40000\n6\n", PATH).run();
        assertTrue(printed().contains("Adding 60,000 would put Steps at "
            + "120,000, above the maximum of 100,000 steps"));
        assertTrue(printed().contains("is now 100,000 (added 40,000)"));
    }


    /**
     * Logging to a past date, with a bad date first.
     */
    public void testLogPastDate()
    {
        String past = LocalDate.now().minusDays(2)
            .format(InputParser.DATE_FMT);
        app("1\n2\n9/9\n" + past + "\nabc\n7.5\n6\n", PATH).run();
        assertTrue(printed().contains("9/9 is not a valid date"));
        assertTrue(printed().contains("'abc' is not a number"));
        assertTrue(printed().contains(
            "Sleep (hours) for " + past + " is now 7.5 (added 7.5)"));
    }


    /**
     * Adding 0 (Enter) to an unlogged day echoes 0 and stores nothing.
     */
    public void testLogZero()
    {
        app("1\n4\n\n\n6\n", PATH).run();
        assertTrue(printed().contains(
            "Water (glasses) for " + today + " is now 0 (added 0)"));
        assertNull(log.getDay(LocalDate.now()));
    }


    /**
     * 0 at the metric prompt goes back to the menu for all three
     * actions.
     */
    public void testBackToMenu()
    {
        app("1\n0\n2\n0\n3\n0\n6\n", PATH).run();
        assertTrue(printed().contains("Saved. Goodbye."));
        assertTrue(log.allDays().isEmpty());
    }


    /**
     * Edit replaces a value.
     */
    public void testEdit()
    {
        app("1\n1\n\n4200\n2\n1\n\n5000\n6\n", PATH).run();
        assertTrue(printed().contains("Steps for " + today + " is now 5,000"));
        assertEquals(5000.0, log.getDay(LocalDate.now()).getValue(
            Metric.STEPS), 0.001);
    }


    /**
     * Delete sets a value to 0; deleting the last value removes the day.
     */
    public void testDelete()
    {
        app("1\n1\n\n4200\n3\n1\n\n6\n", PATH).run();
        assertTrue(printed().contains("Steps for " + today + " deleted"));
        assertNull(log.getDay(LocalDate.now()));
    }


    /**
     * History prints logged days and averages.
     */
    public void testHistory()
    {
        app("1\n1\n\n4200\n5\n6\n", PATH).run();
        assertTrue(printed().contains(today + "   score "));
        assertTrue(printed().contains("Steps 7-day avg 600.0 (goal 10,000)"));
    }


    /**
     * A save that fails at option 6 warns and shows the menu again; the
     * end-of-input path then warns and exits.
     */
    public void testSaveFails()
    {
        app("6\n", "no-such-folder-hb/healthbar.txt").run();
        assertTrue(printed().contains("Could not write "
            + "no-such-folder-hb/healthbar.txt - check that the folder "
            + "is writable"));
        assertTrue(printed().contains("exiting without saving"));
        assertFalse(printed().contains("Saved. Goodbye."));
    }


    /**
     * First launch: no save file, then the goal prompts.
     */
    public void testStartFresh()
    {
        HealthBarApp app = app("\n\n\n\n", PATH);
        assertTrue(app.start());
        assertTrue(printed().contains("No save file found - starting a fresh "
            + "log"));
        assertTrue(goals.isSet());
    }


    /**
     * Later launch: the file is loaded, bad lines are reported, and the
     * goal prompts are skipped.
     *
     * @throws IOException
     *             if the fixture cannot be written
     */
    public void testStartWithSaveFile()
        throws IOException
    {
        PrintWriter pw = new PrintWriter(PATH);
        pw.println("GOALS,12000,8,2000,8");
        pw.println("09/09/2026,8400,7.5,2100,6");
        pw.println("nonsense");
        pw.println("09/10/2026,4000,6,1800,4");
        pw.close();
        HealthBarApp app = app("", PATH);
        assertTrue(app.start());
        assertTrue(printed().contains(
            "Loaded 2 days (skipped 1 unreadable lines)"));
        assertEquals(12000.0, goals.get(Metric.STEPS), 0.001);
    }


    /**
     * End of input during the goal prompts saves and stops cleanly.
     */
    public void testStartEndOfInput()
    {
        HealthBarApp app = app("12000\n", PATH);
        assertFalse(app.start());
        assertTrue(printed().contains("Saved. Goodbye."));
    }

    /**
     * Option 7 writes the weekly summary next to the save file.
     */
    public void testExportOption()
    {
        app("1\n1\n\n4200\n7\n6\n", PATH).run();
        File export = new File("test-app-week.txt");
        assertTrue(printed().contains(
            "Weekly summary saved to test-app-week.txt"));
        assertTrue(export.exists());
        export.delete();
    }


    /**
     * A failed export warns and goes back to the menu.
     */
    public void testExportFails()
    {
        app("7\n", "no-such-folder-hb/healthbar.txt").run();
        assertTrue(printed().contains("Could not write "
            + "no-such-folder-hb/healthbar-week.txt"));
    }


    /**
     * Seven days of beating the steps goal offers a raise at start-up;
     * "y" accepts it. Sleep, calories and water were missed all week
     * (sleep 5, the others 0), so those get "lower" offers, declined.
     */
    public void testGoalTuningAccepted()
    {
        setAllGoalsToDefault();
        LocalDate now = LocalDate.now();
        for (int i = 1; i <= 7; i++)
        {
            log.addValue(now.minusDays(i), Metric.STEPS, 10000);
            log.addValue(now.minusDays(i), Metric.SLEEP, 5);
        }
        HealthBarApp app = app("y\nn\nn\nn\n", PATH);
        assertTrue(app.start());
        assertTrue(printed().contains("You beat your Steps goal every day "
            + "for the last 7 days. Raise it from 10,000 to 11,000?"));
        assertTrue(printed().contains("Steps goal is now 11,000"));
        assertEquals(11000.0, goals.get(Metric.STEPS), 0.001);
        assertTrue(printed().contains("You missed your Sleep (hours) goal"));
        assertEquals(8.0, goals.get(Metric.SLEEP), 0.001);
    }


    /**
     * Enter declines the offer, and no offer appears without 7 full days.
     */
    public void testGoalTuningDeclinedOrNone()
    {
        setAllGoalsToDefault();
        LocalDate now = LocalDate.now();
        for (int i = 1; i <= 7; i++)
        {
            log.addValue(now.minusDays(i), Metric.WATER, 3);
        }
        assertTrue(app("\n\n\n\n", PATH).start());
        assertEquals(8.0, goals.get(Metric.WATER), 0.001);
        assertFalse(printed().contains("goal is now"));

        systemOut().clearHistory();
        log.setValue(now.minusDays(4), Metric.WATER, 0);
        assertTrue(app("", PATH).start());
        assertFalse(printed().contains("You missed"));
    }


    /**
     * Sets the four goals to their defaults so start() skips the goal
     * prompts.
     */
    private void setAllGoalsToDefault()
    {
        for (Metric m : Metric.values())
        {
            goals.set(m, m.defaultGoal());
        }
    }




    /**
     * The real main: first launch, four default goals, dashboard, quit.
     * Any existing healthbar.txt is moved aside and put back afterwards.
     */
    public void testMain()
    {
        File real = new File(HealthBarApp.SAVE_FILE);
        File backup = new File(HealthBarApp.SAVE_FILE + ".testbackup");
        boolean hadFile = real.exists();
        if (hadFile)
        {
            backup.delete();
            real.renameTo(backup);
        }
        try
        {
            setSystemIn("\n\n\n\n4\n6\n");
            HealthBarApp.main(new String[0]);
            assertTrue(printed().contains("No save file found"));
            assertTrue(printed().contains("Streak: 0 days"));
            assertTrue(printed().contains("Saved. Goodbye."));
            assertTrue(real.exists());
        }
        finally
        {
            real.delete();
            if (hadFile)
            {
                backup.renameTo(real);
            }
        }
    }
}
