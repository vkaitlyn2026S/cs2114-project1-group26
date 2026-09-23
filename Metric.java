// Project 1 - Health Bar (CS 2114, Group 26)
// Virginia Tech Honor Code Pledge:
//
// As a Hokie, I will conduct myself with honor and integrity at all times.
// I will not lie, cheat, or steal, nor will I accept the actions of those
// who do.
// -- Kaitlyn (PID)

/**
 * The fixed set of four things Health Bar tracks. Every constant (label,
 * default goal, daily maximum, whole-number flag, coaching tip) lives here
 * so there is exactly one place to change it. Metric.values() gives every
 * "for each metric" loop the same order: dashboard rows, prompts and the
 * columns of the save file.
 *
 * @author Kaitlyn (PID)
 * @version 2026.09.22
 */
public enum Metric
{
    /** Steps walked in a day. */
    STEPS,
    /** Hours slept. */
    SLEEP,
    /** Calories eaten. */
    CALORIES,
    /** Glasses of water drunk. */
    WATER;

    /**
     * The name shown on screen.
     *
     * @return the display label
     */
    public String label()
    {
        switch (this)
        {
            case STEPS:
                return "Steps";
            case SLEEP:
                return "Sleep (hours)";
            case CALORIES:
                return "Calories";
            default:
                return "Water (glasses)";
        }
    }


    /**
     * The unit word used in messages, e.g. "above the daily maximum of
     * 24 hours".
     *
     * @return the unit, in lower case
     */
    public String unit()
    {
        switch (this)
        {
            case STEPS:
                return "steps";
            case SLEEP:
                return "hours";
            case CALORIES:
                return "calories";
            default:
                return "glasses";
        }
    }


    /**
     * The goal suggested at first launch (accepted by pressing Enter).
     *
     * @return the default daily goal
     */
    public double defaultGoal()
    {
        switch (this)
        {
            case STEPS:
                return 10000.0;
            case SLEEP:
                return 8.0;
            case CALORIES:
                return 2000.0;
            default:
                return 8.0;
        }
    }


    /**
     * The largest value that can be stored for one day. Anything above
     * this is physically impossible and is rejected.
     *
     * @return the daily maximum
     */
    public double maxPerDay()
    {
        switch (this)
        {
            case STEPS:
                return 100000.0;
            case SLEEP:
                return 24.0;
            case CALORIES:
                return 20000.0;
            default:
                return 40.0;
        }
    }


    /**
     * Whether this metric is counted in whole units (steps, calories,
     * glasses). Only sleep keeps a decimal.
     *
     * @return true for whole-unit metrics
     */
    public boolean isWholeUnits()
    {
        switch (this)
        {
            case SLEEP:
                return false;
            default:
                return true;
        }
    }


    /**
     * The one-sentence coaching tip printed when this is the weakest
     * metric of the week.
     *
     * @return the coaching tip
     */
    public String tip()
    {
        switch (this)
        {
            case STEPS:
                return "Take a 15-minute walk after lunch and after dinner"
                    + " - about 3,000 steps you are leaving on the table.";
            case SLEEP:
                return "Set an alarm 30 minutes before your target bedtime"
                    + " and start winding down when it rings.";
            case CALORIES:
                return "Plan tomorrow's three meals tonight so you fuel up"
                    + " instead of skipping one.";
            default:
                return "Fill a bottle before every class and finish it"
                    + " before the next one.";
        }
    }


    /**
     * Formats a value of this metric for the screen: commas, no decimals
     * for whole-unit metrics, at most one decimal for sleep (and none when
     * the value is whole, so 8.0 shows as "8").
     *
     * @param value
     *            the value to format
     * @return the formatted number, e.g. "6,100" or "7.5"
     */
    public String format(double value)
    {
        if (isWholeUnits() || value % 1 == 0)
        {
            return String.format("%,.0f", value);
        }
        return String.format("%,.1f", value);
    }
}
