// Project 1 - Health Bar (CS 2114, Group 26)
// Virginia Tech Honor Code Pledge:
//
// As a Hokie, I will conduct myself with honor and integrity at all times.
// I will not lie, cheat, or steal, nor will I accept the actions of those
// who do.
// -- Kaitlyn (PID)

/**
 * Tests for Metric.
 *
 * @author Kaitlyn (PID)
 * @version 2026.09.22
 */
public class MetricTest
    extends student.TestCase
{
    /**
     * Checks the order of the constants (dashboard rows, file columns).
     */
    public void testOrder()
    {
        Metric[] metrics = Metric.values();
        assertEquals(4, metrics.length);
        assertEquals(Metric.STEPS, metrics[0]);
        assertEquals(Metric.SLEEP, metrics[1]);
        assertEquals(Metric.CALORIES, metrics[2]);
        assertEquals(Metric.WATER, metrics[3]);
    }


    /**
     * Checks the labels and units.
     */
    public void testLabelAndUnit()
    {
        assertEquals("Steps", Metric.STEPS.label());
        assertEquals("Sleep (hours)", Metric.SLEEP.label());
        assertEquals("Calories", Metric.CALORIES.label());
        assertEquals("Water (glasses)", Metric.WATER.label());
        assertEquals("steps", Metric.STEPS.unit());
        assertEquals("hours", Metric.SLEEP.unit());
        assertEquals("calories", Metric.CALORIES.unit());
        assertEquals("glasses", Metric.WATER.unit());
    }


    /**
     * Checks the default goals.
     */
    public void testDefaultGoal()
    {
        assertEquals(10000.0, Metric.STEPS.defaultGoal(), 0.001);
        assertEquals(8.0, Metric.SLEEP.defaultGoal(), 0.001);
        assertEquals(2000.0, Metric.CALORIES.defaultGoal(), 0.001);
        assertEquals(8.0, Metric.WATER.defaultGoal(), 0.001);
    }


    /**
     * Checks the daily maximums.
     */
    public void testMaxPerDay()
    {
        assertEquals(100000.0, Metric.STEPS.maxPerDay(), 0.001);
        assertEquals(24.0, Metric.SLEEP.maxPerDay(), 0.001);
        assertEquals(20000.0, Metric.CALORIES.maxPerDay(), 0.001);
        assertEquals(40.0, Metric.WATER.maxPerDay(), 0.001);
    }


    /**
     * Checks that only sleep keeps decimals.
     */
    public void testIsWholeUnits()
    {
        assertTrue(Metric.STEPS.isWholeUnits());
        assertFalse(Metric.SLEEP.isWholeUnits());
        assertTrue(Metric.CALORIES.isWholeUnits());
        assertTrue(Metric.WATER.isWholeUnits());
    }


    /**
     * Checks that every metric has its own tip.
     */
    public void testTip()
    {
        assertTrue(Metric.STEPS.tip().startsWith("Take a 15-minute walk"));
        assertTrue(Metric.SLEEP.tip().contains("bedtime"));
        assertTrue(Metric.CALORIES.tip().contains("meals"));
        assertTrue(Metric.WATER.tip().contains("bottle"));
    }


    /**
     * Checks number formatting for the screen.
     */
    public void testFormat()
    {
        assertEquals("6,100", Metric.STEPS.format(6100));
        assertEquals("7,501", Metric.STEPS.format(7500.6));
        assertEquals("7.5", Metric.SLEEP.format(7.5));
        assertEquals("8", Metric.SLEEP.format(8.0));
        assertEquals("0", Metric.WATER.format(0));
    }
}
