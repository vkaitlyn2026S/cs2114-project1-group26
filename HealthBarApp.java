// Project 1 - Health Bar (CS 2114, Group 26)
// Virginia Tech Honor Code Pledge:
//
// As a Hokie, I will conduct myself with honor and integrity at all times.
// I will not lie, cheat, or steal, nor will I accept the actions of those
// who do.
// -- Prateek Malekar (prateekm30)

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Entry point and menu loop. Builds the other objects, reads one menu
 * choice at a time and hands it to the object that owns that job. Holds no
 * health data, does no math and does no parsing. It is the only class
 * that changes the data (through HealthLog and Goals).
 *
 * @author Prateek Malekar (prateekm30)
 * @version 2026.09.22
 */
public class HealthBarApp
{
    /** The save file used by the real program. */
    public static final String SAVE_FILE = "healthbar.txt";

    private final ConsoleUI ui;
    private final HealthLog log;
    private final Goals goals;
    private final HealthScorer scorer;
    private final LogFile file;

    /**
     * Wires the app together. main uses this with System.in and
     * healthbar.txt; tests pass a scripted Scanner and a test file.
     *
     * @param ui
     *            the console
     * @param log
     *            the history
     * @param goals
     *            the goals
     * @param scorer
     *            the scorer over log and goals
     * @param file
     *            the save file
     */
    public HealthBarApp(ConsoleUI ui, HealthLog log, Goals goals,
        HealthScorer scorer, LogFile file)
    {
        this.ui = ui;
        this.log = log;
        this.goals = goals;
        this.scorer = scorer;
        this.file = file;
    }


    /**
     * Starts Health Bar: loads the save file, asks for goals on first
     * launch, then runs the menu until the user quits.
     *
     * @param args
     *            not used
     */
    public static void main(String[] args)
    {
        Scanner in = new Scanner(System.in);
        ConsoleUI ui = new ConsoleUI(in);
        Goals goals = new Goals();
        HealthLog log = new HealthLog();
        LogFile file = new LogFile(SAVE_FILE);
        HealthScorer scorer = new HealthScorer(log, goals);
        HealthBarApp app = new HealthBarApp(ui, log, goals, scorer, file);
        if (app.start())
        {
            app.run();
        }
    }


    /**
     * Loads the save file, reports what was loaded, runs the goal prompts
     * if the goals are not set yet, then offers any goal auto-tuning.
     *
     * @return true if the menu should run next; false if input ended
     *         during the goal prompts (the data was saved already)
     */
    public boolean start()
    {
        if (file.hasSaveData())
        {
            int skipped = file.load(goals, log);
            ui.showMessage("Loaded " + log.allDays().size() + " days (skipped "
                + skipped + " unreadable lines)");
        }
        else
        {
            ui.showMessage("No save file found - starting a fresh log");
        }
        try
        {
            if (!goals.isSet())
            {
                ui.promptGoals(goals);
            }
            offerGoalTuning(LocalDate.now());
        }
        catch (NoSuchElementException e)
        {
            // end of input (Ctrl-D) while setting or tuning goals
            quit(true);
            return false;
        }
        return true;
    }


    /**
     * The menu loop: 1 Log, 2 Edit, 3 Delete, 4 Dashboard, 5 History &
     * averages, 6 Save & quit, 7 Export weekly summary. End of input
     * (Ctrl-D) is treated as option 6. Never lets an exception escape.
     */
    public void run()
    {
        boolean running = true;
        while (running)
        {
            try
            {
                int choice = ui.showMainMenu();
                LocalDate today = LocalDate.now();
                switch (choice)
                {
                    case 1:
                        logValue(today);
                        break;
                    case 2:
                        editValue(today);
                        break;
                    case 3:
                        deleteValue(today);
                        break;
                    case 4:
                        ui.showDashboard(today, log, goals, scorer);
                        break;
                    case 5:
                        ui.showHistory(today, log, goals, scorer);
                        break;
                    case 7:
                        exportWeek(today);
                        break;
                    default:
                        running = !quit(false);
                        break;
                }
            }
            catch (NoSuchElementException e)
            {
                // end of input (Ctrl-D): save and leave, no stack trace
                quit(true);
                running = false;
            }
            catch (RuntimeException e)
            {
                ui.showMessage("Something went wrong (" + e.getMessage()
                    + ") - back to the menu");
            }
        }
    }


    /**
     * Option 1: add to one metric on one day and echo the new total. A
     * total above the daily maximum is refused and the amount is asked for
     * again.
     *
     * @param today
     *            today's date
     */
    private void logValue(LocalDate today)
    {
        Metric m = ui.promptMetric();
        if (m == null)
        {
            return;
        }
        LocalDate date = ui.promptDate(today);
        while (true)
        {
            double amount = ui.promptAmount(m, "to add");
            double total = current(date, m) + amount;
            if (total > m.maxPerDay())
            {
                ui.showMessage("Adding " + m.format(amount) + " would put "
                    + m.label() + " at " + m.format(total)
                    + ", above the maximum of " + m.format(m.maxPerDay())
                    + " " + m.unit());
                continue;
            }
            try
            {
                double newTotal = log.addValue(date, m, amount);
                ui.showMessage(m.label() + " for " + show(date) + " is now "
                    + m.format(newTotal) + " (added " + m.format(amount)
                    + ")");
                return;
            }
            catch (IllegalArgumentException e)
            {
                ui.showMessage(e.getMessage());
            }
        }
    }


    /**
     * Option 2: replace one metric's value on one day.
     *
     * @param today
     *            today's date
     */
    private void editValue(LocalDate today)
    {
        Metric m = ui.promptMetric();
        if (m == null)
        {
            return;
        }
        LocalDate date = ui.promptDate(today);
        double value = ui.promptAmount(m, "new total");
        log.setValue(date, m, value);
        ui.showMessage(m.label() + " for " + show(date) + " is now "
            + m.format(value));
    }


    /**
     * Option 3: set one metric's value on one day back to 0. If that was
     * the day's last non-zero value, the day is removed.
     *
     * @param today
     *            today's date
     */
    private void deleteValue(LocalDate today)
    {
        Metric m = ui.promptMetric();
        if (m == null)
        {
            return;
        }
        LocalDate date = ui.promptDate(today);
        log.setValue(date, m, 0.0);
        ui.showMessage(m.label() + " for " + show(date)
            + " deleted (now 0)");
    }


    /**
     * Stretch goal, goal auto-tuning: for each metric where the last 7 days
     * all beat (or all missed) the goal, asks whether to raise (or lower)
     * it by 10%. Enter or "n" keeps the goal; the offer comes back next
     * launch if the pattern still holds.
     *
     * @param today
     *            today's date
     */
    private void offerGoalTuning(LocalDate today)
    {
        for (Metric m : Metric.values())
        {
            double current = goals.get(m);
            double suggested = scorer.suggestedGoal(m, today);
            if (suggested != current)
            {
                String what = "missed";
                String change = "Lower";
                if (suggested > current)
                {
                    what = "beat";
                    change = "Raise";
                }
                boolean yes = ui.promptYesNo("You " + what + " your "
                    + m.label() + " goal every day for the last "
                    + HealthScorer.TUNE_DAYS + " days. " + change
                    + " it from " + m.format(current) + " to "
                    + m.format(suggested) + "? (y/n, Enter = n):");
                if (yes)
                {
                    goals.set(m, suggested);
                    ui.showMessage(m.label() + " goal is now "
                        + m.format(suggested));
                }
            }
        }
    }


    /**
     * Option 7: writes the weekly summary file.
     *
     * @param today
     *            today's date
     */
    private void exportWeek(LocalDate today)
    {
        if (file.exportWeek(today, goals, log, scorer))
        {
            ui.showMessage("Weekly summary saved to " + file.getExportPath());
        }
        else
        {
            ui.showMessage("Could not write " + file.getExportPath()
                + " - check that the folder is writable");
        }
    }


    /**
     * Option 6 and end of input: save the file.
     *
     * @param endOfInput
     *            true when input has ended, so there is no way to retry
     * @return true if the program should exit
     */
    private boolean quit(boolean endOfInput)
    {
        if (file.save(goals, log))
        {
            ui.showMessage("Saved. Goodbye.");
            return true;
        }
        ui.showMessage("Could not write " + file.getPath()
            + " - check that the folder is writable");
        if (endOfInput)
        {
            ui.showMessage("Input ended, so exiting without saving.");
        }
        return endOfInput;
    }


    /**
     * The value currently stored for one metric on one day.
     *
     * @param date
     *            the date
     * @param m
     *            the metric
     * @return the stored value, or 0 if the day is not logged
     */
    private double current(LocalDate date, Metric m)
    {
        DayLog day = log.getDay(date);
        if (day == null)
        {
            return 0.0;
        }
        return day.getValue(m);
    }


    /**
     * Formats a date for messages.
     *
     * @param date
     *            the date
     * @return the date as MM/DD/YYYY
     */
    private static String show(LocalDate date)
    {
        return date.format(InputParser.DATE_FMT);
    }
}
