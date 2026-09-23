// Project 1 - Health Bar (CS 2114, Group 26)
// Virginia Tech Honor Code Pledge:
//
// As a Hokie, I will conduct myself with honor and integrity at all times.
// I will not lie, cheat, or steal, nor will I accept the actions of those
// who do.
// -- Kaitlyn (906810716)

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Reads and writes the save file (healthbar.txt by default). The only
 * class that touches the file system. The format is plain text, one
 * record per line, so a person can repair a bad line by hand:
 *
 * <pre>
 * GOALS,10000,8,2000,8
 * 09/09/2026,8400,7.5,2100,6
 * </pre>
 *
 * Day columns are the date, then the metrics in enum order.
 *
 * @author Kaitlyn (906810716)
 * @version 2026.09.22
 */
public class LogFile
{
    private static final String GOALS_TAG = "GOALS";
    private final String path;

    /**
     * Creates a LogFile for the given path.
     *
     * @param path
     *            the file to read and write, e.g. "healthbar.txt"
     */
    public LogFile(String path)
    {
        this.path = path;
    }


    /**
     * Gets the path of the save file.
     *
     * @return the path
     */
    public String getPath()
    {
        return path;
    }


    /**
     * Whether a non-empty save file exists. Used by HealthBarApp to choose
     * between "Loaded N days" and "No save file found".
     *
     * @return true if the file exists and is not empty
     */
    public boolean hasSaveData()
    {
        File file = new File(path);
        return file.isFile() && file.length() > 0;
    }


    /**
     * Loads the save file into goals and log. A valid GOALS line fills
     * goals; each valid day line is stored in log. Bad lines are skipped
     * and counted: malformed lines, bad values, bad or future dates,
     * duplicate dates and bad (or repeated) GOALS lines. Blank lines are
     * ignored.
     *
     * @param goals
     *            the goals to fill
     * @param log
     *            the history to fill
     * @return the number of skipped lines; 0 (and nothing loaded) if the
     *         file is missing
     */
    public int load(Goals goals, HealthLog log)
    {
        File file = new File(path);
        if (!file.isFile())
        {
            return 0;
        }
        int skipped = 0;
        boolean goalsSeen = false;
        LocalDate today = LocalDate.now();
        Scanner reader = null;
        try
        {
            reader = new Scanner(file);
            while (reader.hasNextLine())
            {
                String line = reader.nextLine().trim();
                if (line.isEmpty())
                {
                    continue;
                }
                if (line.toUpperCase().startsWith(GOALS_TAG))
                {
                    if (goalsSeen || !loadGoals(line, goals))
                    {
                        skipped++;
                    }
                    else
                    {
                        goalsSeen = true;
                    }
                    continue;
                }
                if (!loadDay(line, log, today))
                {
                    skipped++;
                }
            }
        }
        catch (FileNotFoundException e)
        {
            return 0;
        }
        finally
        {
            if (reader != null)
            {
                reader.close();
            }
        }
        return skipped;
    }


    /**
     * Rewrites the whole file: the GOALS line, then one line per day in
     * date order.
     *
     * @param goals
     *            the goals to save
     * @param log
     *            the history to save
     * @return true if the file was written; false (no exception) if it
     *         could not be
     */
    public boolean save(Goals goals, HealthLog log)
    {
        PrintWriter out = null;
        try
        {
            out = new PrintWriter(new FileWriter(path));
            StringBuilder goalLine = new StringBuilder(GOALS_TAG);
            for (Metric m : Metric.values())
            {
                goalLine.append(',').append(number(goals.get(m)));
            }
            out.println(goalLine);
            for (DayLog day : log.allDays())
            {
                StringBuilder line = new StringBuilder(
                    day.getDate().format(InputParser.DATE_FMT));
                for (Metric m : Metric.values())
                {
                    line.append(',').append(number(day.getValue(m)));
                }
                out.println(line);
            }
            return !out.checkError();
        }
        catch (IOException e)
        {
            return false;
        }
        finally
        {
            if (out != null)
            {
                out.close();
            }
        }
    }


    /**
     * Where the weekly summary goes: the save file's name with "-week"
     * added, e.g. healthbar.txt becomes healthbar-week.txt.
     *
     * @return the export path
     */
    public String getExportPath()
    {
        if (path.endsWith(".txt"))
        {
            return path.substring(0, path.length() - 4) + "-week.txt";
        }
        return path + "-week.txt";
    }


    /**
     * Stretch goal: writes a weekly summary to getExportPath() that a user
     * can send to a friend or coach. It lists today and the six days
     * before it (score and values, or "not logged"), the 7-day averages
     * against the goals, the current streak, the badges and the coaching
     * tip. The file is replaced each time.
     *
     * @param today
     *            the last day of the week to summarize
     * @param goals
     *            the goals
     * @param log
     *            the history
     * @param scorer
     *            the scorer over log and goals
     * @return true if the file was written; false (no exception) if not
     */
    public boolean exportWeek(LocalDate today, Goals goals, HealthLog log,
        HealthScorer scorer)
    {
        PrintWriter out = null;
        try
        {
            out = new PrintWriter(new FileWriter(getExportPath()));
            LocalDate start = today.minusDays(HealthScorer.TUNE_DAYS - 1);
            out.println("Health Bar - weekly summary");
            out.println("Week: " + start.format(InputParser.DATE_FMT)
                + " to " + today.format(InputParser.DATE_FMT));
            out.println();
            for (int i = HealthScorer.TUNE_DAYS - 1; i >= 0; i--)
            {
                out.println(dayLine(today.minusDays(i), log, scorer));
            }
            out.println();
            out.println("7-day averages:");
            for (Metric m : Metric.values())
            {
                out.println("  " + m.label() + ": "
                    + String.format("%,.1f", scorer.sevenDayAverage(m, today))
                    + " (goal " + m.format(goals.get(m)) + ")");
            }
            out.println();
            out.println("Current streak: " + scorer.currentStreak(today)
                + " days");
            out.println("Badges: " + badgeText(scorer));
            Metric weakest = scorer.weakestMetric(today);
            if (weakest == null)
            {
                out.println("Coach: Log a day to unlock coaching");
            }
            else
            {
                out.println("Coach: " + weakest.tip());
            }
            return !out.checkError();
        }
        catch (IOException e)
        {
            return false;
        }
        finally
        {
            if (out != null)
            {
                out.close();
            }
        }
    }


    /**
     * The badges as one line of text.
     *
     * @param scorer
     *            the scorer
     * @return e.g. "7-Day Streak, Hydration Week", or "none yet"
     */
    public static String badgeText(HealthScorer scorer)
    {
        List<String> earned = scorer.badges();
        if (earned.isEmpty())
        {
            return "none yet";
        }
        String text = earned.get(0);
        for (int i = 1; i < earned.size(); i++)
        {
            text = text + ", " + earned.get(i);
        }
        return text;
    }


    /**
     * One line of the weekly summary.
     *
     * @param date
     *            the day
     * @param log
     *            the history
     * @param scorer
     *            the scorer
     * @return e.g. "09/09/2026  score 81  Steps 8,400  ..." or
     *         "09/10/2026  not logged"
     */
    private static String dayLine(LocalDate date, HealthLog log,
        HealthScorer scorer)
    {
        String line = date.format(InputParser.DATE_FMT);
        DayLog day = log.getDay(date);
        if (day == null)
        {
            return line + "  not logged";
        }
        line = line + "  score " + scorer.dailyScore(day);
        for (Metric m : Metric.values())
        {
            line = line + "  " + m.label() + " " + m.format(day.getValue(m));
        }
        return line;
    }


    /**
     * Turns one day line, e.g. "09/09/2026,8400,7.5,2100,6", into a
     * DayLog.
     *
     * @param line
     *            the line from the file
     * @return the DayLog it describes
     * @throws IllegalArgumentException
     *             for the wrong number of columns, a non-numeric, negative
     *             or over-maximum value, or a date that is not a real
     *             calendar date
     */
    public static DayLog parseDayLine(String line)
    {
        if (line == null)
        {
            throw new IllegalArgumentException("The line is empty");
        }
        String[] parts = line.trim().split(",", -1);
        int expected = 1 + Metric.values().length;
        if (parts.length != expected)
        {
            throw new IllegalArgumentException("Expected " + expected
                + " columns but found " + parts.length);
        }
        LocalDate date;
        try
        {
            date = LocalDate.parse(parts[0].trim(), InputParser.DATE_FMT);
        }
        catch (DateTimeParseException e)
        {
            throw new IllegalArgumentException(
                parts[0].trim() + " is not a real calendar date");
        }
        DayLog day = new DayLog(date);
        Metric[] metrics = Metric.values();
        for (int i = 0; i < metrics.length; i++)
        {
            String cell = parts[i + 1].trim();
            double value;
            try
            {
                value = Double.parseDouble(cell);
            }
            catch (NumberFormatException e)
            {
                throw new IllegalArgumentException(
                    "'" + cell + "' is not a number");
            }
            day.setValue(metrics[i], value);
        }
        return day;
    }


    /**
     * Reads a GOALS line into goals, all or nothing.
     *
     * @param line
     *            the line starting with GOALS
     * @param goals
     *            the goals to fill
     * @return true if all four targets were valid and were set
     */
    private static boolean loadGoals(String line, Goals goals)
    {
        String[] parts = line.split(",", -1);
        Metric[] metrics = Metric.values();
        if (parts.length != 1 + metrics.length
            || !parts[0].trim().equalsIgnoreCase(GOALS_TAG))
        {
            return false;
        }
        double[] targets = new double[metrics.length];
        try
        {
            for (int i = 0; i < metrics.length; i++)
            {
                targets[i] = Double.parseDouble(parts[i + 1].trim());
                if (!(targets[i] > 0)
                    || targets[i] > metrics[i].maxPerDay())
                {
                    return false;
                }
            }
        }
        catch (NumberFormatException e)
        {
            return false;
        }
        for (int i = 0; i < metrics.length; i++)
        {
            goals.set(metrics[i], targets[i]);
        }
        return true;
    }


    /**
     * Reads one day line into log.
     *
     * @param line
     *            the line
     * @param log
     *            the history to add to
     * @param today
     *            today's date (later dates are rejected)
     * @return true if the line was valid and new
     */
    private static boolean loadDay(String line, HealthLog log,
        LocalDate today)
    {
        DayLog day;
        try
        {
            day = parseDayLine(line);
        }
        catch (IllegalArgumentException e)
        {
            return false;
        }
        if (day.getDate().isAfter(today) || log.getDay(day.getDate()) != null)
        {
            return false;
        }
        for (Metric m : Metric.values())
        {
            log.setValue(day.getDate(), m, day.getValue(m));
        }
        return true;
    }


    /**
     * Writes a number the way a person would type it: "8400", not
     * "8400.0"; "7.5" stays "7.5".
     *
     * @param value
     *            the number
     * @return its text form
     */
    private static String number(double value)
    {
        if (value % 1 == 0)
        {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }
}
