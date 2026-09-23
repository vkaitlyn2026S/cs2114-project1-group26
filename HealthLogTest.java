// Project 1 - Health Bar (CS 2114, Group 26)
// Virginia Tech Honor Code Pledge:
//
// As a Hokie, I will conduct myself with honor and integrity at all times.
// I will not lie, cheat, or steal, nor will I accept the actions of those
// who do.
// -- Kaitlyn (906810716)

import java.time.LocalDate;
import java.util.List;

/**
 * Tests for HealthLog.
 *
 * @author Kaitlyn (906810716)
 * @version 2026.09.22
 */
public class HealthLogTest
    extends student.TestCase
{
    private HealthLog log;
    private LocalDate sep9;
    private LocalDate sep10;

    /**
     * Creates an empty log before each test.
     */
    public void setUp()
    {
        log = new HealthLog();
        sep9 = LocalDate.of(2026, 9, 9);
        sep10 = LocalDate.of(2026, 9, 10);
    }


    /**
     * Adding twice to the same day adds to the total; one day is stored.
     */
    public void testAddValueTwice()
    {
        assertEquals(4200.0, log.addValue(sep9, Metric.STEPS, 4200), 0.001);
        assertEquals(8400.0, log.addValue(sep9, Metric.STEPS, 4200), 0.001);
        assertEquals(8400.0, log.getDay(sep9).getValue(Metric.STEPS),
            0.001);
        assertEquals(1, log.allDays().size());
    }


    /**
     * A day never logged is null; adding 0 to it creates nothing.
     */
    public void testGetDayAndAddZero()
    {
        assertNull(log.getDay(sep10));
        assertEquals(0.0, log.addValue(sep10, Metric.STEPS, 0), 0.001);
        assertNull(log.getDay(sep10));
        assertEquals(0, log.allDays().size());
    }


    /**
     * A negative amount throws and the total is unchanged.
     */
    public void testAddValueNegative()
    {
        log.addValue(sep9, Metric.STEPS, 8400);
        Exception thrown = null;
        try
        {
            log.addValue(sep9, Metric.STEPS, -1);
        }
        catch (IllegalArgumentException e)
        {
            thrown = e;
        }
        assertNotNull(thrown);
        assertEquals(8400.0, log.getDay(sep9).getValue(Metric.STEPS),
            0.001);
    }


    /**
     * A running total over the maximum throws and is unchanged.
     */
    public void testAddValueOverMax()
    {
        log.addValue(sep9, Metric.STEPS, 60000);
        Exception thrown = null;
        try
        {
            log.addValue(sep9, Metric.STEPS, 50000);
        }
        catch (IllegalArgumentException e)
        {
            thrown = e;
        }
        assertNotNull(thrown);
        assertEquals(60000.0, log.getDay(sep9).getValue(Metric.STEPS),
            0.001);
    }


    /**
     * A too-large first amount does not create a day.
     */
    public void testAddValueOverMaxNewDay()
    {
        Exception thrown = null;
        try
        {
            log.addValue(sep9, Metric.SLEEP, 30);
        }
        catch (IllegalArgumentException e)
        {
            thrown = e;
        }
        assertNotNull(thrown);
        assertNull(log.getDay(sep9));
    }


    /**
     * Sleep totals do not pick up floating-point noise.
     */
    public void testAddValueSleepDecimals()
    {
        log.addValue(sep9, Metric.SLEEP, 7.1);
        assertEquals(7.3, log.addValue(sep9, Metric.SLEEP, 0.2), 0.0);
    }


    /**
     * setValue replaces a value.
     */
    public void testSetValue()
    {
        log.setValue(sep9, Metric.SLEEP, 7.5);
        assertEquals(7.5, log.getDay(sep9).getValue(Metric.SLEEP), 0.001);
        log.setValue(sep9, Metric.SLEEP, 6);
        assertEquals(6.0, log.getDay(sep9).getValue(Metric.SLEEP), 0.001);
    }


    /**
     * Setting the only non-zero value to 0 removes the day.
     */
    public void testSetValueRemovesEmptyDay()
    {
        log.addValue(sep9, Metric.STEPS, 4200);
        log.setValue(sep9, Metric.STEPS, 0);
        assertNull(log.getDay(sep9));
        log.setValue(sep10, Metric.WATER, 0);
        assertNull(log.getDay(sep10));
    }


    /**
     * setValue over the maximum throws.
     */
    public void testSetValueOverMax()
    {
        Exception thrown = null;
        try
        {
            log.setValue(sep9, Metric.SLEEP, 30);
        }
        catch (IllegalArgumentException e)
        {
            thrown = e;
        }
        assertNotNull(thrown);
        assertNull(log.getDay(sep9));
    }


    /**
     * Days stay in date order however they are inserted, and
     * lastLoggedDays is newest first.
     */
    public void testOrderAndLastLoggedDays()
    {
        LocalDate sep12 = LocalDate.of(2026, 9, 12);
        LocalDate sep13 = LocalDate.of(2026, 9, 13);
        LocalDate sep14 = LocalDate.of(2026, 9, 14);
        log.setValue(sep14, Metric.STEPS, 1);
        log.setValue(sep12, Metric.STEPS, 1);
        log.setValue(sep13, Metric.STEPS, 1);

        List<DayLog> all = log.allDays();
        assertEquals(3, all.size());
        assertEquals(sep12, all.get(0).getDate());
        assertEquals(sep13, all.get(1).getDate());
        assertEquals(sep14, all.get(2).getDate());

        List<DayLog> last = log.lastLoggedDays(7);
        assertEquals(3, last.size());
        assertEquals(sep14, last.get(0).getDate());
        assertEquals(sep12, last.get(2).getDate());

        assertEquals(2, log.lastLoggedDays(2).size());
        assertEquals(sep14, log.lastLoggedDays(2).get(0).getDate());
    }


    /**
     * An empty log gives empty lists.
     */
    public void testEmptyLists()
    {
        assertTrue(log.allDays().isEmpty());
        assertTrue(log.lastLoggedDays(7).isEmpty());
        assertTrue(log.lastLoggedDays(0).isEmpty());
    }


    /**
     * allDays is a copy, so changing it cannot break the log.
     */
    public void testAllDaysIsCopy()
    {
        log.setValue(sep9, Metric.STEPS, 1);
        log.allDays().clear();
        assertEquals(1, log.allDays().size());
    }
}
