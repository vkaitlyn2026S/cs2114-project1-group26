// Project 1 - Health Bar (CS 2114, Group 26)
// Virginia Tech Honor Code Pledge:
//
// As a Hokie, I will conduct myself with honor and integrity at all times.
// I will not lie, cheat, or steal, nor will I accept the actions of those
// who do.
// -- Nikitha (PID)

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Turns one raw line of text into a checked value (amount, goal, date,
 * menu number) or throws an IllegalArgumentException whose message says
 * exactly what was wrong. No console, no state: every call is independent.
 *
 * @author Nikitha (PID)
 * @version 2026.09.22
 */
public class InputParser
{
    /**
     * The one date format used everywhere (screen and save file). STRICT
     * makes 02/31/2026 fail instead of rolling over to March 3. The year
     * is "uuuu", not "yyyy"/"YYYY": in STRICT mode "yyyy" needs an era and
     * "YYYY" is the week-based year, so both would reject valid dates.
     */
    public static final DateTimeFormatter DATE_FMT = DateTimeFormatter
        .ofPattern("MM/dd/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private static final String NOT_AN_OPTION =
        "That is not one of the options";

    /**
     * Private: this class only has static methods.
     */
    private InputParser()
    {
        // not used
    }


    /**
     * Parses an amount to log for a metric. Blank means 0. Commas and
     * spaces are stripped; whole-unit metrics round to the nearest whole
     * number (7500.6 becomes 7501), sleep keeps one decimal.
     *
     * @param raw
     *            the text the user typed
     * @param m
     *            the metric being logged
     * @return the checked amount
     * @throws IllegalArgumentException
     *             if the text is not a number, is negative, or is above
     *             m.maxPerDay()
     */
    public static double parseAmount(String raw, Metric m)
    {
        if (raw == null || raw.trim().isEmpty())
        {
            return 0.0;
        }
        String shown = raw.trim();
        String cleaned = shown.replace(",", "").replace(" ", "");
        if (!isPlainNumber(cleaned))
        {
            throw new IllegalArgumentException("'" + shown
                + "' is not a number; enter digits like 4200");
        }
        double value = Double.parseDouble(cleaned);
        if (value < 0)
        {
            throw new IllegalArgumentException(
                shown + " cannot be negative; enter 0 or more");
        }
        value = roundFor(m, value);
        if (value == 0)
        {
            value = 0; // "-0" would otherwise be stored and shown as -0
        }
        if (value > m.maxPerDay())
        {
            throw new IllegalArgumentException(shown
                + " is above the daily maximum of "
                + m.format(m.maxPerDay()) + " " + m.unit());
        }
        return value;
    }


    /**
     * Parses a daily goal. Blank means the metric's suggested default.
     * Otherwise the same checks as parseAmount, and 0 is refused.
     *
     * @param raw
     *            the text the user typed
     * @param m
     *            the metric
     * @return the checked goal (always more than 0)
     * @throws IllegalArgumentException
     *             for bad input or a goal of 0
     */
    public static double parseGoal(String raw, Metric m)
    {
        if (raw == null || raw.trim().isEmpty())
        {
            return m.defaultGoal();
        }
        double goal = parseAmount(raw, m);
        if (goal == 0)
        {
            throw new IllegalArgumentException("A goal of 0 is not allowed");
        }
        return goal;
    }


    /**
     * Parses a date in MM/DD/YYYY form. Blank means today.
     *
     * @param raw
     *            the text the user typed
     * @param today
     *            today's date
     * @return the checked date (never after today)
     * @throws IllegalArgumentException
     *             for an unreadable or impossible date, or one in the
     *             future
     */
    public static LocalDate parseDate(String raw, LocalDate today)
    {
        if (raw == null || raw.trim().isEmpty())
        {
            return today;
        }
        String shown = raw.trim();
        LocalDate date;
        try
        {
            date = LocalDate.parse(shown, DATE_FMT);
        }
        catch (DateTimeParseException dtpe)
        {
            throw new IllegalArgumentException(shown + " is not a valid "
                + "date; use MM/DD/YYYY, for example 09/09/2026");
        }
        if (date.isAfter(today))
        {
            throw new IllegalArgumentException(shown
                + " is in the future - today is " + today.format(DATE_FMT));
        }
        return date;
    }


    /**
     * Parses a menu choice.
     *
     * @param raw
     *            the text the user typed
     * @param max
     *            the highest option number
     * @return the choice, from 0 to max
     * @throws IllegalArgumentException
     *             "That is not one of the options" for anything else,
     *             including blank input
     */
    public static int parseMenuChoice(String raw, int max)
    {
        if (raw == null)
        {
            throw new IllegalArgumentException(NOT_AN_OPTION);
        }
        int choice;
        try
        {
            choice = Integer.parseInt(raw.trim());
        }
        catch (NumberFormatException nfe)
        {
            // letters, decimals, blank, or a number too big for an int
            throw new IllegalArgumentException(NOT_AN_OPTION);
        }
        if (choice < 0 || choice > max)
        {
            throw new IllegalArgumentException(NOT_AN_OPTION);
        }
        return choice;
    }


    /**
     * Parses a yes/no answer (stretch goal: goal auto-tuning). Blank means
     * no, so pressing Enter never changes anything.
     *
     * @param raw
     *            the text the user typed
     * @return true for "y" or "yes", false for "n", "no" or blank (any
     *         capitalization)
     * @throws IllegalArgumentException
     *             "Please answer y or n" for anything else
     */
    public static boolean parseYesNo(String raw)
    {
        if (raw == null || raw.trim().isEmpty())
        {
            return false;
        }
        String answer = raw.trim().toLowerCase();
        if (answer.equals("y") || answer.equals("yes"))
        {
            return true;
        }
        if (answer.equals("n") || answer.equals("no"))
        {
            return false;
        }
        throw new IllegalArgumentException("Please answer y or n");
    }


    /**
     * Rounds a value the way the metric is stored: whole units to the
     * nearest whole number, sleep to one decimal.
     *
     * @param m
     *            the metric
     * @param value
     *            the raw value
     * @return the rounded value
     */
    private static double roundFor(Metric m, double value)
    {
        if (m.isWholeUnits())
        {
            return Math.round(value);
        }
        return Math.round(value * 10) / 10.0;
    }

    /**
     * Checks that text is a plain decimal number: an optional "-" at the
     * front, digits, and at most one ".". This rejects things that
     * Double.parseDouble would otherwise accept, like "1e5", "NaN" and
     * "Infinity".
     *
     * @param text
     *            the text, with commas and spaces already removed
     * @return true if it is a plain number
     */
    private static boolean isPlainNumber(String text)
    {
        int digits = 0;
        int dots = 0;
        for (int i = 0; i < text.length(); i++)
        {
            char c = text.charAt(i);
            if (c >= '0' && c <= '9')
            {
                digits++;
            }
            else if (c == '.')
            {
                dots++;
            }
            else if (!(c == '-' && i == 0))
            {
                return false;
            }
        }
        return digits > 0 && dots <= 1;
    }

}
