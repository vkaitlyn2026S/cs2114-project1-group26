// Project 1 - Health Bar (CS 2114, Group 26)
// Virginia Tech Honor Code Pledge:
//
// As a Hokie, I will conduct myself with honor and integrity at all times.
// I will not lie, cheat, or steal, nor will I accept the actions of those
// who do.
// -- Kaitlyn (PID)

import java.time.LocalDate;

/**
 * Tests for DayLog.
 *
 * @author Kaitlyn (PID)
 * @version 2026.09.22
 */
public class DayLogTest
    extends student.TestCase
{
    private DayLog day;

    /**
     * Creates an empty day before each test.
     */
    public void setUp()
    {
        day = new DayLog(LocalDate.of(2026, 9, 9));
    }


    /**
     * A new day has every value at 0 and is empty.
     */
    public void testConstructor()
    {
        assertEquals(LocalDate.of(2026, 9, 9), day.getDate());
        for (Metric m : Metric.values())
        {
            assertEquals(0.0, day.getValue(m), 0.001);
        }
        assertTrue(day.isEmpty());
    }


    /**
     * A null date is refused.
     */
    public void testConstructorNull()
    {
        Exception thrown = null;
        try
        {
            new DayLog(null);
        }
        catch (IllegalArgumentException e)
        {
            thrown = e;
        }
        assertNotNull(thrown);
    }


    /**
     * setValue stores each metric separately.
     */
    public void testSetValue()
    {
        day.setValue(Metric.SLEEP, 7.5);
        assertEquals(7.5, day.getValue(Metric.SLEEP), 0.001);
        assertFalse(day.isEmpty());
        day.setValue(Metric.STEPS, 8400);
        day.setValue(Metric.CALORIES, 2100);
        day.setValue(Metric.WATER, 6);
        assertEquals(8400.0, day.getValue(Metric.STEPS), 0.001);
        assertEquals(2100.0, day.getValue(Metric.CALORIES), 0.001);
        assertEquals(6.0, day.getValue(Metric.WATER), 0.001);
        day.setValue(Metric.STEPS, 0);
        day.setValue(Metric.SLEEP, 0);
        day.setValue(Metric.CALORIES, 0);
        day.setValue(Metric.WATER, 0);
        assertTrue(day.isEmpty());
    }


    /**
     * The daily maximum itself is allowed.
     */
    public void testSetValueAtMaximum()
    {
        day.setValue(Metric.SLEEP, 24);
        assertEquals(24.0, day.getValue(Metric.SLEEP), 0.001);
    }


    /**
     * Over the maximum throws and leaves the value unchanged.
     */
    public void testSetValueOverMax()
    {
        Exception thrown = null;
        try
        {
            day.setValue(Metric.SLEEP, 30);
        }
        catch (IllegalArgumentException e)
        {
            thrown = e;
        }
        assertNotNull(thrown);
        assertTrue(thrown.getMessage().contains("24"));
        assertEquals(0.0, day.getValue(Metric.SLEEP), 0.001);
    }


    /**
     * A negative value throws and leaves the value unchanged.
     */
    public void testSetValueNegative()
    {
        day.setValue(Metric.STEPS, 100);
        Exception thrown = null;
        try
        {
            day.setValue(Metric.STEPS, -1);
        }
        catch (IllegalArgumentException e)
        {
            thrown = e;
        }
        assertNotNull(thrown);
        assertTrue(thrown.getMessage().contains("negative"));
        assertEquals(100.0, day.getValue(Metric.STEPS), 0.001);
    }


    /**
     * NaN is refused.
     */
    public void testSetValueNaN()
    {
        Exception thrown = null;
        try
        {
            day.setValue(Metric.WATER, Double.NaN);
        }
        catch (IllegalArgumentException e)
        {
            thrown = e;
        }
        assertNotNull(thrown);
        assertTrue(day.isEmpty());
    }
}
