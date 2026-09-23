// Project 1 - Health Bar (CS 2114, Group 26)
// Virginia Tech Honor Code Pledge:
//
// As a Hokie, I will conduct myself with honor and integrity at all times.
// I will not lie, cheat, or steal, nor will I accept the actions of those
// who do.
// -- Kaitlyn (906810716)

/**
 * The user's four daily targets. A target of zero or less is refused, so
 * a percentage can never divide by zero. A value of 0 in a field means
 * "not set yet" (first launch, or a bad GOALS line in the save file).
 *
 * @author Kaitlyn (906810716)
 * @version 2026.09.22
 */
public class Goals
{
    private double steps;
    private double sleep;
    private double calories;
    private double water;

    /**
     * Whether all four targets have been set.
     *
     * @return true once every target is greater than 0
     */
    public boolean isSet()
    {
        return steps > 0 && sleep > 0 && calories > 0 && water > 0;
    }


    /**
     * Gets the target for one metric.
     *
     * @param m
     *            the metric
     * @return the target, or m.defaultGoal() if none is set; never 0 or
     *         less
     */
    public double get(Metric m)
    {
        double target = raw(m);
        if (target > 0)
        {
            return target;
        }
        return m.defaultGoal();
    }


    /**
     * Sets the target for one metric.
     *
     * @param m
     *            the metric
     * @param target
     *            the new daily target
     * @throws IllegalArgumentException
     *             if target is 0 or less, or above m.maxPerDay()
     */
    public void set(Metric m, double target)
    {
        if (Double.isNaN(target) || target <= 0)
        {
            throw new IllegalArgumentException(
                "A goal must be more than 0");
        }
        if (target > m.maxPerDay())
        {
            throw new IllegalArgumentException("A goal of "
                + m.format(target) + " is above the daily maximum of "
                + m.format(m.maxPerDay()) + " " + m.unit());
        }
        switch (m)
        {
            case STEPS:
                steps = target;
                break;
            case SLEEP:
                sleep = target;
                break;
            case CALORIES:
                calories = target;
                break;
            default:
                water = target;
                break;
        }
    }


    /**
     * The stored field for a metric (0 = not set).
     *
     * @param m
     *            the metric
     * @return the stored target
     */
    private double raw(Metric m)
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
}
