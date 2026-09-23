// Project 1 - Health Bar (CS 2114, Group 26)
// Virginia Tech Honor Code Pledge:
//
// As a Hokie, I will conduct myself with honor and integrity at all times.
// I will not lie, cheat, or steal, nor will I accept the actions of those
// who do.
// -- Prateek Malekar (prateekm30)

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

/**
 * The only class that prints to or reads from the console. It draws the
 * menus, bars, dashboard and history, and runs every re-prompt loop (print
 * the error, ask again) until it has a valid value. The Scanner comes in
 * through the constructor, so a test can pass new Scanner("abc\n4200\n").
 *
 * @author Prateek Malekar (prateekm30)
 * @version 2026.09.22
 */
public class ConsoleUI
{
    /** Number of characters inside the brackets of every bar. */
    public static final int BAR_WIDTH = 10;

    private static final int MAIN_OPTIONS = 7;
    private static final int LABEL_WIDTH = 16;
    private static final int HISTORY_DAYS = 7;

    private final Scanner in;

    /**
     * Creates a ConsoleUI that reads from the given source.
     *
     * @param in
     *            System.in for the real program, a Scanner over a String
     *            in tests
     */
    public ConsoleUI(Scanner in)
    {
        this.in = in;
    }


    /**
     * Prints the main menu and reads a choice, redrawing it with a short
     * note until the choice is valid.
     *
     * @return a choice from 1 to 7
     */
    public int showMainMenu()
    {
        while (true)
        {
            System.out.println();
            System.out.println("=== Health Bar ===");
            System.out.println("1 Log a value");
            System.out.println("2 Edit a value");
            System.out.println("3 Delete a value");
            System.out.println("4 Dashboard");
            System.out.println("5 History & averages");
            System.out.println("6 Save & quit");
            System.out.println("7 Export weekly summary");
            String line = ask("Choose 1-" + MAIN_OPTIONS + ":");
            try
            {
                int choice = InputParser.parseMenuChoice(line, MAIN_OPTIONS);
                if (choice >= 1)
                {
                    return choice;
                }
            }
            catch (IllegalArgumentException e)
            {
                // fall through to the note below
            }
            System.out.println(
                "That is not one of the options (1-" + MAIN_OPTIONS + ")");
        }
    }


    /**
     * Asks which metric to work on.
     *
     * @return the chosen Metric, or null for "0 Back to menu"
     */
    public Metric promptMetric()
    {
        Metric[] metrics = Metric.values();
        while (true)
        {
            System.out.println();
            for (int i = 0; i < metrics.length; i++)
            {
                System.out.println((i + 1) + " " + metrics[i].label());
            }
            System.out.println("0 Back to menu");
            String line = ask("Choose 0-" + metrics.length + ":");
            try
            {
                int choice =
                    InputParser.parseMenuChoice(line, metrics.length);
                if (choice == 0)
                {
                    return null;
                }
                return metrics[choice - 1];
            }
            catch (IllegalArgumentException e)
            {
                System.out.println("That is not one of the options (0-"
                    + metrics.length + ")");
            }
        }
    }


    /**
     * Asks for a date until a valid one is entered. Enter means today.
     *
     * @param today
     *            today's date
     * @return the chosen date
     */
    public LocalDate promptDate(LocalDate today)
    {
        while (true)
        {
            String line = ask("Date (MM/DD/YYYY, Enter = today):");
            try
            {
                return InputParser.parseDate(line, today);
            }
            catch (IllegalArgumentException e)
            {
                System.out.println(e.getMessage());
            }
        }
    }


    /**
     * Asks for an amount until a valid one is entered. Enter means 0.
     *
     * @param m
     *            the metric
     * @param verb
     *            what the amount is for, e.g. "to add" gives
     *            "Steps to add (Enter = 0):"
     * @return the checked amount
     */
    public double promptAmount(Metric m, String verb)
    {
        while (true)
        {
            String line = ask(m.label() + " " + verb + " (Enter = 0):");
            try
            {
                return InputParser.parseAmount(line, m);
            }
            catch (IllegalArgumentException e)
            {
                System.out.println(e.getMessage());
            }
        }
    }


    /**
     * Asks for all four daily goals. Enter accepts the suggested default.
     *
     * @param goals
     *            the goals to fill
     */
    public void promptGoals(Goals goals)
    {
        System.out.println();
        System.out.println("Set your daily goals (Enter = suggested).");
        for (Metric m : Metric.values())
        {
            boolean done = false;
            while (!done)
            {
                String line = ask("Daily goal for " + m.label()
                    + " (Enter = " + m.format(m.defaultGoal()) + "):");
                try
                {
                    goals.set(m, InputParser.parseGoal(line, m));
                    done = true;
                }
                catch (IllegalArgumentException e)
                {
                    System.out.println(e.getMessage());
                }
            }
        }
    }


    /**
     * Asks a yes/no question until the answer is y, yes, n, no or blank
     * (blank means no).
     *
     * @param question
     *            the question, e.g. "Raise it to 11,000? (y/n, Enter = n):"
     * @return true for yes
     */
    public boolean promptYesNo(String question)
    {
        while (true)
        {
            String line = ask(question);
            try
            {
                return InputParser.parseYesNo(line);
            }
            catch (IllegalArgumentException e)
            {
                System.out.println(e.getMessage());
            }
        }
    }


    /**
     * Shows today's dashboard: four bars, the score, the streak, the badges
     * earned and one coaching line.
     *
     * @param today
     *            today's date
     * @param log
     *            the history
     * @param goals
     *            the goals
     * @param scorer
     *            the scorer
     */
    public void showDashboard(LocalDate today, HealthLog log, Goals goals,
        HealthScorer scorer)
    {
        DayLog day = log.getDay(today);
        System.out.println();
        System.out.println("Today - " + today.format(InputParser.DATE_FMT));
        for (Metric m : Metric.values())
        {
            double value = 0.0;
            if (day != null)
            {
                value = day.getValue(m);
            }
            double goal = goals.get(m);
            System.out.println(String.format("%-" + LABEL_WIDTH + "s", m
                .label()) + formatBar(value, goal) + " " + m.format(value)
                + " / " + m.format(goal));
        }
        System.out.println("Score: " + scorer.dailyScore(day) + " / 100");
        int streak = scorer.currentStreak(today);
        if (streak == 1)
        {
            System.out.println("Streak: 1 day");
        }
        else
        {
            System.out.println("Streak: " + streak + " days");
        }
        System.out.println("Badges: " + LogFile.badgeText(scorer));
        Metric weakest = scorer.weakestMetric(today);
        if (weakest == null)
        {
            System.out.println("Log a day to unlock coaching");
        }
        else
        {
            System.out.println("Coach: " + weakest.tip());
        }
    }


    /**
     * Shows the last seven logged days (newest first) with their scores,
     * then the 7-day average of each metric.
     *
     * @param today
     *            today's date
     * @param log
     *            the history
     * @param goals
     *            the goals
     * @param scorer
     *            the scorer
     */
    public void showHistory(LocalDate today, HealthLog log, Goals goals,
        HealthScorer scorer)
    {
        System.out.println();
        System.out.println("Last " + HISTORY_DAYS + " logged days");
        List<DayLog> days = log.lastLoggedDays(HISTORY_DAYS);
        if (days.isEmpty())
        {
            System.out.println("No days logged yet.");
        }
        for (DayLog day : days)
        {
            System.out.println(day.getDate().format(InputParser.DATE_FMT)
                + "   score " + scorer.dailyScore(day));
        }
        System.out.println();
        for (Metric m : Metric.values())
        {
            System.out.println(m.label() + " 7-day avg "
                + String.format("%,.1f", scorer.sevenDayAverage(m, today))
                + " (goal " + m.format(goals.get(m)) + ")");
        }
    }


    /**
     * Prints one line of text.
     *
     * @param text
     *            the message, e.g. "Steps for 09/09/2026 is now 8,400"
     */
    public void showMessage(String text)
    {
        System.out.println(text);
    }


    /**
     * Draws a bar exactly 12 characters wide: "[", one "#" for each tenth
     * of the goal reached (rounded, at most 10), "-" for the rest, "]".
     *
     * @param value
     *            the logged value
     * @param goal
     *            the goal
     * @return the bar, e.g. "[######----]"
     */
    public static String formatBar(double value, double goal)
    {
        double ratio = 0.0;
        if (goal > 0 && value > 0)
        {
            ratio = Math.min(value / goal, 1.0);
        }
        int filled = (int) Math.round(BAR_WIDTH * ratio);
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < BAR_WIDTH; i++)
        {
            if (i < filled)
            {
                bar.append('#');
            }
            else
            {
                bar.append('-');
            }
        }
        return bar.append(']').toString();
    }


    /**
     * Prints a prompt and reads one line.
     *
     * @param prompt
     *            the prompt text
     * @return the line typed
     * @throws java.util.NoSuchElementException
     *             at end of input (Ctrl-D); HealthBarApp.run handles it
     */
    private String ask(String prompt)
    {
        System.out.print(prompt + " ");
        return in.nextLine();
    }
}
