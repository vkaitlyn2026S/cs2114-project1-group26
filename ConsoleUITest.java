// Project 1 - Health Bar (CS 2114, Group 26)
// Virginia Tech Honor Code Pledge:
//
// As a Hokie, I will conduct myself with honor and integrity at all times.
// I will not lie, cheat, or steal, nor will I accept the actions of those
// who do.
// -- Prateek Malekar (prateekm30)

import java.time.LocalDate;
import java.util.Scanner;

/**
 * Tests for ConsoleUI. Each test hands ConsoleUI a Scanner over a String
 * (the "typed" input) and checks what was printed.
 *
 * @author Prateek Malekar (prateekm30)
 * @version 2026.09.22
 */
public class ConsoleUITest
    extends student.TestCase
{
    private LocalDate today;
    private HealthLog log;
    private Goals goals;
    private HealthScorer scorer;

    /**
     * Sets up an empty log with default goals.
     */
    public void setUp()
    {
        today = LocalDate.of(2026, 9, 15);
        log = new HealthLog();
        goals = new Goals();
        scorer = new HealthScorer(log, goals);
    }


    /**
     * Creates a ConsoleUI that will "read" the given text.
     *
     * @param typed
     *            the input, one answer per line
     * @return the ConsoleUI
     */
    private ConsoleUI ui(String typed)
    {
        return new ConsoleUI(new Scanner(typed));
    }


    /**
     * Counts how often text appears in the printed output.
     *
     * @param text
     *            the text to look for
     * @return the number of times it was printed
     */
    private int count(String text)
    {
        String out = systemOut().getHistory();
        int n = 0;
        int at = out.indexOf(text);
        while (at >= 0)
        {
            n++;
            at = out.indexOf(text, at + text.length());
        }
        return n;
    }


    /**
     * A valid choice prints the menu once and returns it.
     */
    public void testShowMainMenu()
    {
        assertEquals(4, ui("4\n").showMainMenu());
        assertEquals(1, count("6 Save & quit"));
        assertEquals(0, count("not one of the options"));
    }


    /**
     * Bad choices print the note each time and redraw the menu.
     */
    public void testShowMainMenuBad()
    {
        assertEquals(2, ui("x\n99\n0\n\n2\n").showMainMenu());
        assertEquals(4, count("That is not one of the options (1-7)"));
        assertEquals(5, count("6 Save & quit"));
        assertEquals(5, count("7 Export weekly summary"));
    }


    /**
     * promptMetric returns the chosen metric.
     */
    public void testPromptMetric()
    {
        assertEquals(Metric.SLEEP, ui("2\n").promptMetric());
        assertEquals(Metric.STEPS, ui("1\n").promptMetric());
        assertEquals(Metric.WATER, ui("4\n").promptMetric());
        assertTrue(systemOut().getHistory().contains("0 Back to menu"));
    }


    /**
     * A bad choice prints the note; 0 goes back (null).
     */
    public void testPromptMetricBack()
    {
        assertNull(ui("7\n0\n").promptMetric());
        assertEquals(1, count("That is not one of the options (0-4)"));
    }


    /**
     * promptDate returns a typed date, or today for Enter.
     */
    public void testPromptDate()
    {
        assertEquals(LocalDate.of(2026, 9, 9),
            ui("09/09/2026\n").promptDate(today));
        assertEquals(0, count("not a valid date"));
    }


    /**
     * A bad date prints the message, then Enter gives today.
     */
    public void testPromptDateBad()
    {
        assertEquals(today, ui("yesterday\n12/25/2026\n\n").promptDate(
            today));
        assertEquals(1, count("yesterday is not a valid date"));
        assertEquals(1, count("is in the future - today is 09/15/2026"));
    }


    /**
     * Enter at an amount prompt is 0 with no error.
     */
    public void testPromptAmount()
    {
        assertEquals(0.0, ui("\n").promptAmount(Metric.STEPS, "to add"),
            0.001);
        assertEquals(1, count("Steps to add (Enter = 0):"));
        assertEquals(0, count("not a number"));
    }


    /**
     * Two bad amounts print two errors, then the good one is returned.
     */
    public void testPromptAmountBad()
    {
        assertEquals(4200.0, ui("abc\n-1\n4200\n").promptAmount(
            Metric.STEPS, "to add"), 0.001);
        assertEquals(1, count("'abc' is not a number; enter digits like"));
        assertEquals(1, count("cannot be negative"));
    }


    /**
     * Four Enters accept the four defaults.
     */
    public void testPromptGoals()
    {
        ui("\n\n\n\n").promptGoals(goals);
        assertTrue(goals.isSet());
        for (Metric m : Metric.values())
        {
            assertEquals(m.defaultGoal(), goals.get(m), 0.001);
        }
        assertEquals(1, count("Daily goal for Steps (Enter = 10,000):"));
    }


    /**
     * A goal of 0 is refused once, then 12000 is kept.
     */
    public void testPromptGoalsBad()
    {
        ui("0\n12000\n\n\n\n").promptGoals(goals);
        assertEquals(1, count("A goal of 0 is not allowed"));
        assertEquals(12000.0, goals.get(Metric.STEPS), 0.001);
        assertEquals(8.0, goals.get(Metric.SLEEP), 0.001);
        assertTrue(goals.isSet());
    }


    /**
     * The dashboard shows bars, score, streak and one tip.
     */
    public void testShowDashboard()
    {
        log.addValue(today, Metric.STEPS, 6100);
        log.addValue(today, Metric.SLEEP, 7.5);
        ui("").showDashboard(today, log, goals, scorer);
        String out = systemOut().getHistory();
        assertTrue(out.contains("[######----] 6,100 / 10,000"));
        assertTrue(out.contains("[#########-] 7.5 / 8"));
        assertTrue(out.contains("Score: "));
        assertTrue(out.contains("Streak: 0 days"));
        assertEquals(1, count("Coach: "));
        // calories and water are both at 0%; the tie goes to CALORIES
        assertTrue(out.contains(Metric.CALORIES.tip()));
    }


    /**
     * A one-day streak says "day", not "days".
     */
    public void testShowDashboardOneDayStreak()
    {
        for (Metric m : Metric.values())
        {
            log.addValue(today, m, m.defaultGoal());
        }
        ui("").showDashboard(today, log, goals, scorer);
        assertTrue(systemOut().getHistory().contains("Score: 100 / 100"));
        assertTrue(systemOut().getHistory().contains("Streak: 1 day\n")
            || systemOut().getHistory().contains("Streak: 1 day\r\n"));
    }


    /**
     * An empty log still draws a full dashboard with no exception.
     */
    public void testShowDashboardEmpty()
    {
        ui("").showDashboard(today, log, goals, scorer);
        assertEquals(4, count("[----------] 0 / "));
        assertEquals(1, count("Score: 0 / 100"));
        assertEquals(1, count("Streak: 0 days"));
        assertEquals(1, count("Log a day to unlock coaching"));
    }


    /**
     * History lists days newest first, then four average lines.
     */
    public void testShowHistory()
    {
        log.addValue(LocalDate.of(2026, 9, 12), Metric.STEPS, 1000);
        log.addValue(LocalDate.of(2026, 9, 14), Metric.SLEEP, 6.8);
        log.addValue(LocalDate.of(2026, 9, 13), Metric.WATER, 8);
        ui("").showHistory(today, log, goals, scorer);
        String out = systemOut().getHistory();
        int first = out.indexOf("09/14/2026   score 21");
        int second = out.indexOf("09/13/2026   score 25");
        int third = out.indexOf("09/12/2026   score 3");
        assertTrue(first >= 0);
        assertTrue(second > first);
        assertTrue(third > second);
        assertEquals(4, count("7-day avg"));
        assertTrue(out.contains("Sleep (hours) 7-day avg 1.0 (goal 8)"));
    }


    /**
     * History of an empty log says so and shows zero averages.
     */
    public void testShowHistoryEmpty()
    {
        ui("").showHistory(today, log, goals, scorer);
        assertEquals(1, count("No days logged yet."));
        assertTrue(systemOut().getHistory().contains(
            "Steps 7-day avg 0.0 (goal 10,000)"));
    }


    /**
     * showMessage prints one line.
     */
    public void testShowMessage()
    {
        ui("").showMessage("Steps for 09/09/2026 is now 8,400");
        assertEquals(1, count("Steps for 09/09/2026 is now 8,400"));
    }


    /**
     * Bars are always 12 characters wide.
     */
    public void testFormatBar()
    {
        assertEquals("[######----]", ConsoleUI.formatBar(6100, 10000));
        assertEquals("[----------]", ConsoleUI.formatBar(0, 10000));
        assertEquals("[##########]", ConsoleUI.formatBar(12000, 10000));
        assertEquals("[#---------]", ConsoleUI.formatBar(500, 10000));
        assertEquals("[----------]", ConsoleUI.formatBar(5, 0));
        assertEquals(12, ConsoleUI.formatBar(0, 10000).length());
        assertEquals(12, ConsoleUI.formatBar(99999, 1).length());
    }

    /**
     * The dashboard shows the badges line, "none yet" when there are none.
     */
    public void testShowDashboardBadges()
    {
        ui("").showDashboard(today, log, goals, scorer);
        assertEquals(1, count("Badges: none yet"));
        systemOut().clearHistory();
        log.addValue(today, Metric.STEPS, 12000);
        ui("").showDashboard(today, log, goals, scorer);
        assertEquals(1, count("Badges: First 10K Steps Day"));
    }


    /**
     * promptYesNo accepts y / yes / n / no / Enter and re-asks on junk.
     */
    public void testPromptYesNo()
    {
        assertTrue(ui("y\n").promptYesNo("Raise it?"));
        assertTrue(ui("YES\n").promptYesNo("Raise it?"));
        assertFalse(ui("n\n").promptYesNo("Raise it?"));
        assertFalse(ui("\n").promptYesNo("Raise it?"));
        assertTrue(ui("maybe\ny\n").promptYesNo("Raise it?"));
        assertEquals(1, count("Please answer y or n"));
    }

}
