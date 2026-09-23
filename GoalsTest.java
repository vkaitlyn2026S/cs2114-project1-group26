// Project 1 - Health Bar (CS 2114, Group 26)
// Virginia Tech Honor Code Pledge:
//
// As a Hokie, I will conduct myself with honor and integrity at all times.
// I will not lie, cheat, or steal, nor will I accept the actions of those
// who do.
// -- Kaitlyn (PID)

/**
 * Tests for Goals.
 *
 * @author Kaitlyn (PID)
 * @version 2026.09.22
 */
public class GoalsTest
    extends student.TestCase
{
    private Goals goals;

    /**
     * Creates empty goals before each test.
     */
    public void setUp()
    {
        goals = new Goals();
    }


    /**
     * New goals are not set, and get falls back to the defaults.
     */
    public void testNewGoals()
    {
        assertFalse(goals.isSet());
        assertEquals(10000.0, goals.get(Metric.STEPS), 0.001);
        assertEquals(8.0, goals.get(Metric.SLEEP), 0.001);
        assertEquals(2000.0, goals.get(Metric.CALORIES), 0.001);
        assertEquals(8.0, goals.get(Metric.WATER), 0.001);
    }


    /**
     * set stores a target; isSet only after all four.
     */
    public void testSet()
    {
        goals.set(Metric.WATER, 10);
        assertEquals(10.0, goals.get(Metric.WATER), 0.001);
        assertFalse(goals.isSet());
        goals.set(Metric.STEPS, 12000);
        goals.set(Metric.SLEEP, 7.5);
        assertFalse(goals.isSet());
        goals.set(Metric.CALORIES, 2200);
        assertTrue(goals.isSet());
        assertEquals(12000.0, goals.get(Metric.STEPS), 0.001);
        assertEquals(7.5, goals.get(Metric.SLEEP), 0.001);
        assertEquals(2200.0, goals.get(Metric.CALORIES), 0.001);
    }


    /**
     * Zero, negative and over-maximum targets are refused and nothing
     * changes.
     */
    public void testSetBad()
    {
        assertBadGoal(Metric.WATER, 0);
        assertBadGoal(Metric.WATER, -3);
        assertBadGoal(Metric.SLEEP, 30);
        assertBadGoal(Metric.STEPS, Double.NaN);
        assertEquals(8.0, goals.get(Metric.WATER), 0.001);
        assertEquals(8.0, goals.get(Metric.SLEEP), 0.001);
        assertFalse(goals.isSet());
    }


    /**
     * Asserts that setting this target throws.
     *
     * @param m
     *            the metric
     * @param target
     *            the bad target
     */
    private void assertBadGoal(Metric m, double target)
    {
        Exception thrown = null;
        try
        {
            goals.set(m, target);
        }
        catch (IllegalArgumentException e)
        {
            thrown = e;
        }
        assertNotNull(thrown);
    }
}
