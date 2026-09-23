// Project 1 - Health Bar (CS 2114, Group 26)
// Virginia Tech Honor Code Pledge:
//
// As a Hokie, I will conduct myself with honor and integrity at all times.
// I will not lie, cheat, or steal, nor will I accept the actions of those
// who do.
// -- Kaitlyn (906810716)

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

/**
 * Tests for LogFile. Each test writes its own small fixture file (good,
 * empty or hand-broken) and deletes it afterwards.
 *
 * @author Kaitlyn (906810716)
 * @version 2026.09.22
 */
public class LogFileTest
    extends student.TestCase
{
    private static final String PATH = "test-logfile.txt";
    private LogFile file;
    private Goals goals;
    private HealthLog log;

    /**
     * Creates fresh objects before each test.
     */
    public void setUp()
    {
        new File(PATH).delete();
        file = new LogFile(PATH);
        goals = new Goals();
        log = new HealthLog();
    }


    /**
     * Removes the fixture file after each test.
     */
    public void tearDown()
    {
        new File(PATH).delete();
    }


    /**
     * A GOALS line and two valid days load with nothing skipped.
     *
     * @throws FileNotFoundException
     *             if the fixture cannot be written
     */
    public void testLoadGood()
        throws FileNotFoundException
    {
        write(new String[] {"GOALS,12000,7.5,2200,10",
            "09/09/2026,8400,7.5,2100,6", "09/10/2026,4000,6,1800,4"});
        assertTrue(file.hasSaveData());
        assertEquals(0, file.load(goals, log));
        assertTrue(goals.isSet());
        assertEquals(12000.0, goals.get(Metric.STEPS), 0.001);
        assertEquals(7.5, goals.get(Metric.SLEEP), 0.001);
        assertEquals(2, log.allDays().size());
        DayLog day = log.getDay(LocalDate.of(2026, 9, 9));
        assertEquals(8400.0, day.getValue(Metric.STEPS), 0.001);
        assertEquals(7.5, day.getValue(Metric.SLEEP), 0.001);
        assertEquals(2100.0, day.getValue(Metric.CALORIES), 0.001);
        assertEquals(6.0, day.getValue(Metric.WATER), 0.001);
    }


    /**
     * A junk line and a duplicate date are skipped and counted; the good
     * line is kept.
     *
     * @throws FileNotFoundException
     *             if the fixture cannot be written
     */
    public void testLoadBroken()
        throws FileNotFoundException
    {
        write(new String[] {"09/09/2026,8400,7.5,2100,6", "hello",
            "09/09/2026,1,1,1,1"});
        assertEquals(2, file.load(goals, log));
        assertEquals(1, log.allDays().size());
        assertEquals(8400.0, log.getDay(LocalDate.of(2026, 9, 9))
            .getValue(Metric.STEPS), 0.001);
        assertFalse(goals.isSet());
    }


    /**
     * Bad values, bad and future dates, a bad GOALS line and a second
     * GOALS line are all skipped; blank lines are ignored.
     *
     * @throws FileNotFoundException
     *             if the fixture cannot be written
     */
    public void testLoadEveryKindOfBadLine()
        throws FileNotFoundException
    {
        String future = LocalDate.now().plusDays(3)
            .format(InputParser.DATE_FMT);
        write(new String[] {"GOALS,0,8,2000,8", "GOALS,10000,8,2000,8",
            "GOALS,9000,8,2000,8", "", "02/31/2026,1,1,1,1",
            "09/09/2026,-5,7,2000,6", "09/09/2026,8400,30,2000,6",
            "09/09/2026,8400,7.5", "09/09/2026,abc,7,2000,6",
            future + ",1,1,1,1", "09/11/2026,5000,7,1500,4"});
        assertEquals(8, file.load(goals, log));
        assertTrue(goals.isSet());
        assertEquals(10000.0, goals.get(Metric.STEPS), 0.001);
        assertEquals(1, log.allDays().size());
    }


    /**
     * A missing file loads nothing and is not an error.
     */
    public void testLoadMissing()
    {
        assertFalse(file.hasSaveData());
        assertEquals(0, file.load(goals, log));
        assertTrue(log.allDays().isEmpty());
        assertFalse(goals.isSet());
    }


    /**
     * An empty file loads nothing and has no save data.
     *
     * @throws FileNotFoundException
     *             if the fixture cannot be written
     */
    public void testLoadEmpty()
        throws FileNotFoundException
    {
        write(new String[0]);
        assertFalse(file.hasSaveData());
        assertEquals(0, file.load(goals, log));
        assertTrue(log.allDays().isEmpty());
    }


    /**
     * Saving and loading into fresh objects gives the same data.
     */
    public void testSaveThenLoad()
    {
        goals.set(Metric.STEPS, 12000);
        goals.set(Metric.SLEEP, 7.5);
        goals.set(Metric.CALORIES, 2200);
        goals.set(Metric.WATER, 10);
        log.addValue(LocalDate.of(2026, 9, 10), Metric.SLEEP, 6.5);
        log.addValue(LocalDate.of(2026, 9, 9), Metric.STEPS, 8400);
        log.addValue(LocalDate.of(2026, 9, 9), Metric.WATER, 6);
        assertTrue(file.save(goals, log));

        Goals goals2 = new Goals();
        HealthLog log2 = new HealthLog();
        assertEquals(0, new LogFile(PATH).load(goals2, log2));
        assertTrue(goals2.isSet());
        for (Metric m : Metric.values())
        {
            assertEquals(goals.get(m), goals2.get(m), 0.001);
        }
        List<DayLog> days = log.allDays();
        List<DayLog> days2 = log2.allDays();
        assertEquals(days.size(), days2.size());
        for (int i = 0; i < days.size(); i++)
        {
            assertEquals(days.get(i).getDate(), days2.get(i).getDate());
            for (Metric m : Metric.values())
            {
                assertEquals(days.get(i).getValue(m),
                    days2.get(i).getValue(m), 0.001);
            }
        }
    }


    /**
     * Saving into a folder that does not exist returns false.
     */
    public void testSaveFails()
    {
        LogFile bad = new LogFile("no-such-folder-hb/healthbar.txt");
        assertFalse(bad.save(goals, log));
        assertEquals("no-such-folder-hb/healthbar.txt", bad.getPath());
    }


    /**
     * A good day line becomes a DayLog.
     */
    public void testParseDayLine()
    {
        DayLog day = LogFile.parseDayLine("09/09/2026,8400,7.5,2100,6");
        assertEquals(LocalDate.of(2026, 9, 9), day.getDate());
        assertEquals(8400.0, day.getValue(Metric.STEPS), 0.001);
        assertEquals(7.5, day.getValue(Metric.SLEEP), 0.001);
        assertEquals(2100.0, day.getValue(Metric.CALORIES), 0.001);
        assertEquals(6.0, day.getValue(Metric.WATER), 0.001);
    }


    /**
     * Bad day lines throw.
     */
    public void testParseDayLineBad()
    {
        assertBadLine("09/09/2026,8400,7.5");
        assertBadLine("02/31/2026,1,1,1,1");
        assertBadLine("09/09/2026,-5,7,2000,6");
        assertBadLine("09/09/2026,x,7,2000,6");
        assertBadLine(null);
    }

    /**
     * The export file sits next to the save file with "-week" added.
     */
    public void testGetExportPath()
    {
        assertEquals("test-logfile-week.txt", file.getExportPath());
        assertEquals("data-week.txt", new LogFile("data").getExportPath());
    }


    /**
     * The weekly summary lists 7 days (logged or not), averages, streak,
     * badges and a coaching tip.
     *
     * @throws FileNotFoundException
     *             if the export cannot be read back
     */
    public void testExportWeek()
        throws FileNotFoundException
    {
        LocalDate today = LocalDate.of(2026, 9, 15);
        log.addValue(today, Metric.STEPS, 12000);
        log.addValue(today.minusDays(2), Metric.SLEEP, 7.5);
        HealthScorer scorer = new HealthScorer(log, goals);
        assertTrue(file.exportWeek(today, goals, log, scorer));

        File out = new File(file.getExportPath());
        java.util.Scanner in = new java.util.Scanner(out);
        String text = "";
        while (in.hasNextLine())
        {
            text = text + in.nextLine() + "\n";
        }
        in.close();
        out.delete();

        assertTrue(text.contains("Week: 09/09/2026 to 09/15/2026"));
        assertTrue(text.contains("09/15/2026  score 25  Steps 12,000"));
        assertTrue(text.contains("09/13/2026  score 23"));
        assertTrue(text.contains("09/14/2026  not logged"));
        assertTrue(text.contains("09/09/2026  not logged"));
        assertFalse(text.contains("09/08/2026"));
        assertTrue(text.contains("Steps: 1,714.3 (goal 10,000)"));
        assertTrue(text.contains("Current streak: 0 days"));
        assertTrue(text.contains("Badges: First 10K Steps Day"));
        assertTrue(text.contains("Coach: " + Metric.CALORIES.tip()));
    }


    /**
     * An empty log still exports; a bad folder returns false.
     */
    public void testExportWeekEmptyAndFailure()
    {
        HealthScorer scorer = new HealthScorer(log, goals);
        assertTrue(file.exportWeek(LocalDate.of(2026, 9, 15), goals, log,
            scorer));
        new File(file.getExportPath()).delete();
        LogFile bad = new LogFile("no-such-folder-hb/healthbar.txt");
        assertFalse(bad.exportWeek(LocalDate.of(2026, 9, 15), goals, log,
            scorer));
    }


    /**
     * badgeText joins badge names with commas, or says "none yet".
     */
    public void testBadgeText()
    {
        HealthScorer scorer = new HealthScorer(log, goals);
        assertEquals("none yet", LogFile.badgeText(scorer));
        LocalDate today = LocalDate.of(2026, 9, 15);
        for (int i = 0; i < 7; i++)
        {
            log.addValue(today.minusDays(i), Metric.WATER, 8);
        }
        log.addValue(today, Metric.STEPS, 10000);
        assertEquals("First 10K Steps Day, Hydration Week",
            LogFile.badgeText(scorer));
    }




    /**
     * Asserts that parseDayLine throws for this line.
     *
     * @param line
     *            the bad line
     */
    private void assertBadLine(String line)
    {
        Exception thrown = null;
        try
        {
            LogFile.parseDayLine(line);
        }
        catch (IllegalArgumentException e)
        {
            thrown = e;
        }
        assertNotNull(thrown);
    }


    /**
     * Writes the fixture file.
     *
     * @param lines
     *            the lines to write
     * @throws FileNotFoundException
     *             if the file cannot be written
     */
    private void write(String[] lines)
        throws FileNotFoundException
    {
        PrintWriter out = new PrintWriter(PATH);
        for (String line : lines)
        {
            out.println(line);
        }
        out.close();
    }
}
