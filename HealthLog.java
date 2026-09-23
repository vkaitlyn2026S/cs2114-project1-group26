// Project 1 - Health Bar (CS 2114, Group 26)
// Virginia Tech Honor Code Pledge:
//
// As a Hokie, I will conduct myself with honor and integrity at all times.
// I will not lie, cheat, or steal, nor will I accept the actions of those
// who do.
// -- Kaitlyn (906810716)

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * The whole history: every DayLog, always kept in date order (oldest
 * first), with no two days on the same date. A day is kept only while at
 * least one of its values is non-zero. Knows nothing about scores, screens
 * or files.
 *
 * @author Kaitlyn (906810716)
 * @version 2026.09.22
 */
public class HealthLog
{
    private final ArrayList<DayLog> days;

    /**
     * Creates an empty history.
     */
    public HealthLog()
    {
        days = new ArrayList<DayLog>();
    }


    /**
     * Finds the stored day for a date.
     *
     * @param d
     *            the date to look up
     * @return the stored day, or null if nothing has been logged for it
     */
    public DayLog getDay(LocalDate d)
    {
        for (DayLog day : days)
        {
            if (day.getDate().equals(d))
            {
                return day;
            }
        }
        return null;
    }


    /**
     * Adds an amount to the running total for one metric on one day,
     * creating the day (in date order) if needed. Adding 0 to a day that
     * was never logged creates nothing.
     *
     * @param d
     *            the date
     * @param m
     *            the metric
     * @param amount
     *            the amount to add (0 or more)
     * @return the new total for that metric on that day
     * @throws IllegalArgumentException
     *             for a negative amount or a total above m.maxPerDay();
     *             the total is left unchanged
     */
    public double addValue(LocalDate d, Metric m, double amount)
    {
        if (Double.isNaN(amount) || amount < 0)
        {
            throw new IllegalArgumentException(
                m.label() + " cannot be negative; enter 0 or more");
        }
        DayLog day = getDay(d);
        if (day == null)
        {
            if (amount == 0)
            {
                return 0.0;
            }
            DayLog fresh = new DayLog(d);
            fresh.setValue(m, tidy(m, amount));
            insertInOrder(fresh);
            return fresh.getValue(m);
        }
        double total = tidy(m, day.getValue(m) + amount);
        day.setValue(m, total);
        return total;
    }


    /**
     * Replaces one value (used by Edit and Delete), creating the day if
     * needed. If the day is empty afterwards it is removed from the list.
     *
     * @param d
     *            the date
     * @param m
     *            the metric
     * @param value
     *            the new value (0 deletes it)
     * @throws IllegalArgumentException
     *             for a negative value or one above m.maxPerDay()
     */
    public void setValue(LocalDate d, Metric m, double value)
    {
        DayLog day = getDay(d);
        if (day == null)
        {
            DayLog fresh = new DayLog(d);
            fresh.setValue(m, value);
            if (!fresh.isEmpty())
            {
                insertInOrder(fresh);
            }
            return;
        }
        day.setValue(m, value);
        if (day.isEmpty())
        {
            days.remove(day);
        }
    }


    /**
     * The most recent logged days, newest first.
     *
     * @param n
     *            how many days at most
     * @return up to n days, newest first (empty if none)
     */
    public List<DayLog> lastLoggedDays(int n)
    {
        List<DayLog> result = new ArrayList<DayLog>();
        for (int i = days.size() - 1; i >= 0 && result.size() < n; i--)
        {
            result.add(days.get(i));
        }
        return result;
    }


    /**
     * Every stored day, oldest first (used for saving).
     *
     * @return a copy of the list of days in date order
     */
    public List<DayLog> allDays()
    {
        return new ArrayList<DayLog>(days);
    }


    /**
     * Inserts a new day so the list stays in date order.
     *
     * @param day
     *            the day to insert (its date is not in the list yet)
     */
    private void insertInOrder(DayLog day)
    {
        int index = days.size();
        for (int i = 0; i < days.size(); i++)
        {
            if (days.get(i).getDate().isAfter(day.getDate()))
            {
                index = i;
                break;
            }
        }
        days.add(index, day);
    }


    /**
     * Removes floating-point noise from a running total: whole-unit
     * metrics round to a whole number, sleep to one decimal (so 7.1 + 0.2
     * is stored as 7.3, not 7.300000000000001).
     *
     * @param m
     *            the metric
     * @param value
     *            the raw total
     * @return the tidied total
     */
    private static double tidy(Metric m, double value)
    {
        if (m.isWholeUnits())
        {
            return Math.round(value);
        }
        return Math.round(value * 10) / 10.0;
    }
}
