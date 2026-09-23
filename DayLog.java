// Project 1 - Health Bar (CS 2114, Group 26)
// Virginia Tech Honor Code Pledge:
//
// As a Hokie, I will conduct myself with honor and integrity at all times.
// I will not lie, cheat, or steal, nor will I accept the actions of those
// who do.
// -- Kaitlyn (PID)

import java.time.LocalDate;

/**
 * One calendar day: its date and its four logged values. A new day starts
 * with every value at 0.0. The rule for changing a value lives here: it
 * can never be negative and never above the metric's daily maximum.
 *
 * @author Kaitlyn (PID)
 * @version 2026.09.22
 */
public class DayLog
{
    private final LocalDate date;
    private double steps;
    private double sleep;
    private double calories;
    private double water;

    /**
     * Creates an empty day (all four values 0.0).
     *
     * @param date
     *            the calendar day this log is for
     */
    public DayLog(LocalDate date)
    {
        if (date == null)
        {
            throw new IllegalArgumentException("A day needs a date");
        }
        this.date = date;
    }


    /**
     * Gets the date of this day.
     *
     * @return the date
     */
    public LocalDate getDate()
    {
        return date;
    }


    /**
     * Gets the logged value for one metric.
     *
     * @param m
     *            the metric
     * @return the value (0.0 if nothing was logged)
     */
    public double getValue(Metric m)
    {
        switch (m)
        {
            case STEPS:
                return steps;
            case SLEEP:
                return sleep;
            case CALORIES:
                return calories;
            default:
                return water;
        }
    }


    /**
     * Replaces the value for one metric.
     *
     * @param m
     *            the metric
     * @param value
     *            the new value
     * @throws IllegalArgumentException
     *             if value is negative, not a number, or above
     *             m.maxPerDay(); the day is left unchanged
     */
    public void setValue(Metric m, double value)
    {
        if (Double.isNaN(value))
        {
            throw new IllegalArgumentException(
                m.label() + " must be a number");
        }
        if (value < 0)
        {
            throw new IllegalArgumentException(m.label() + " value "
                + m.format(value) + " cannot be negative; enter 0 or more");
        }
        if (value > m.maxPerDay())
        {
            throw new IllegalArgumentException(m.label() + " value "
                + m.format(value) + " is above the daily maximum of "
                + m.format(m.maxPerDay()) + " " + m.unit());
        }
        switch (m)
        {
            case STEPS:
                steps = value;
                break;
            case SLEEP:
                sleep = value;
                break;
            case CALORIES:
                calories = value;
                break;
            default:
                water = value;
                break;
        }
    }


    /**
     * Whether nothing has been logged for this day.
     *
     * @return true while all four values are 0
     */
    public boolean isEmpty()
    {
        return steps == 0 && sleep == 0 && calories == 0 && water == 0;
    }
}
