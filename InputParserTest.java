// Project 1 - Health Bar (CS 2114, Group 26)
// Virginia Tech Honor Code Pledge:
//
// As a Hokie, I will conduct myself with honor and integrity at all times.
// I will not lie, cheat, or steal, nor will I accept the actions of those
// who do.
// -- Nikitha (PID)

import java.time.LocalDate;

/**
 * Tests for InputParser.
 *
 * @author Nikitha (PID)
 * @version 2026.09.22
 */
public class InputParserTest
    extends student.TestCase
{
    private LocalDate today;

    /**
     * Fixes "today" at 09/15/2026 so the date tests never change.
     */
    public void setUp()
    {
        today = LocalDate.of(2026, 9, 15);
    }


    /**
     * Normal amounts: commas, blank and rounding.
     */
    public void testParseAmount()
    {
        assertEquals(4200.0, InputParser.parseAmount("4,200", Metric.STEPS),
            0.001);
        assertEquals(0.0, InputParser.parseAmount("", Metric.STEPS), 0.001);
        assertEquals(0.0, InputParser.parseAmount("   ", Metric.STEPS),
            0.001);
        assertEquals(0.0, InputParser.parseAmount(null, Metric.STEPS),
            0.001);
        assertEquals(7501.0,
            InputParser.parseAmount("7500.6", Metric.STEPS), 0.001);
        assertEquals(7.5, InputParser.parseAmount(" 7.5 ", Metric.SLEEP),
            0.001);
        assertEquals(7.3, InputParser.parseAmount("7.25", Metric.SLEEP),
            0.001);
        assertEquals(24.0, InputParser.parseAmount("24", Metric.SLEEP),
            0.001);
        assertEquals(100000.0,
            InputParser.parseAmount("100 000", Metric.STEPS), 0.001);
        assertEquals(0.0, InputParser.parseAmount("-0", Metric.STEPS),
            0.0);
    }


    /**
     * Bad amounts throw with the right message.
     */
    public void testParseAmountBad()
    {
        assertAmountError("eight thousand", Metric.STEPS, "is not a number");
        assertAmountError("abc", Metric.STEPS, "'abc' is not a number");
        assertAmountError("10k", Metric.STEPS, "is not a number");
        assertAmountError("1e5", Metric.STEPS, "is not a number");
        assertAmountError("NaN", Metric.STEPS, "is not a number");
        assertAmountError("-5", Metric.STEPS, "cannot be negative");
        assertAmountError("30", Metric.SLEEP,
            "above the daily maximum of 24 hours");
        assertAmountError("500,000", Metric.STEPS,
            "above the daily maximum of 100,000 steps");
    }


    /**
     * Goals: blank gives the default, otherwise the typed number.
     */
    public void testParseGoal()
    {
        assertEquals(10000.0, InputParser.parseGoal("", Metric.STEPS),
            0.001);
        assertEquals(8.0, InputParser.parseGoal(null, Metric.WATER), 0.001);
        assertEquals(12000.0, InputParser.parseGoal("12000", Metric.STEPS),
            0.001);
    }


    /**
     * Bad goals throw.
     */
    public void testParseGoalBad()
    {
        Exception thrown = null;
        try
        {
            InputParser.parseGoal("0", Metric.WATER);
        }
        catch (IllegalArgumentException e)
        {
            thrown = e;
        }
        assertNotNull(thrown);
        assertEquals("A goal of 0 is not allowed", thrown.getMessage());

        thrown = null;
        try
        {
            InputParser.parseGoal("abc", Metric.STEPS);
        }
        catch (IllegalArgumentException e)
        {
            thrown = e;
        }
        assertNotNull(thrown);
        assertTrue(thrown.getMessage().contains("is not a number"));
    }


    /**
     * Normal dates and blank (today).
     */
    public void testParseDate()
    {
        assertEquals(LocalDate.of(2026, 9, 9),
            InputParser.parseDate("09/09/2026", today));
        assertEquals(today, InputParser.parseDate("", today));
        assertEquals(today, InputParser.parseDate(null, today));
        assertEquals(today, InputParser.parseDate(" 09/15/2026 ", today));
        assertEquals(LocalDate.of(2024, 2, 29),
            InputParser.parseDate("02/29/2024", today));
    }


    /**
     * Impossible, badly formatted and future dates throw.
     */
    public void testParseDateBad()
    {
        assertDateError("02/31/2026", "is not a valid date");
        assertDateError("02/29/2026", "is not a valid date");
        assertDateError("9/9", "is not a valid date");
        assertDateError("yesterday", "is not a valid date");
        assertDateError("12/25/2026",
            "is in the future - today is 09/15/2026");
    }


    /**
     * Normal menu choices.
     */
    public void testParseMenuChoice()
    {
        assertEquals(3, InputParser.parseMenuChoice("3", 6));
        assertEquals(0, InputParser.parseMenuChoice(" 0 ", 6));
        assertEquals(6, InputParser.parseMenuChoice("6", 6));
    }


    /**
     * Bad menu choices throw.
     */
    public void testParseMenuChoiceBad()
    {
        String[] bad = {"9", "abc", "", "-1", "2.5",
            "99999999999999999999", null};
        for (String raw : bad)
        {
            Exception thrown = null;
            try
            {
                InputParser.parseMenuChoice(raw, 6);
            }
            catch (IllegalArgumentException e)
            {
                thrown = e;
            }
            assertNotNull(thrown);
            assertEquals("That is not one of the options",
                thrown.getMessage());
        }
    }


    /**
     * Yes/no answers: y, yes, n, no, blank (= no), any capitalization.
     */
    public void testParseYesNo()
    {
        assertTrue(InputParser.parseYesNo("y"));
        assertTrue(InputParser.parseYesNo(" Yes "));
        assertFalse(InputParser.parseYesNo("n"));
        assertFalse(InputParser.parseYesNo("NO"));
        assertFalse(InputParser.parseYesNo(""));
        assertFalse(InputParser.parseYesNo(null));
    }


    /**
     * Anything else is refused.
     */
    public void testParseYesNoBad()
    {
        Exception thrown = null;
        try
        {
            InputParser.parseYesNo("maybe");
        }
        catch (IllegalArgumentException e)
        {
            thrown = e;
        }
        assertNotNull(thrown);
        assertEquals("Please answer y or n", thrown.getMessage());
    }


    /**
     * Asserts that parseAmount throws with a message containing text.
     *
     * @param raw
     *            the input
     * @param m
     *            the metric
     * @param text
     *            part of the expected message
     */
    private void assertAmountError(String raw, Metric m, String text)
    {
        Exception thrown = null;
        try
        {
            InputParser.parseAmount(raw, m);
        }
        catch (IllegalArgumentException e)
        {
            thrown = e;
        }
        assertNotNull(thrown);
        assertTrue(thrown.getMessage(), thrown.getMessage().contains(text));
    }


    /**
     * Asserts that parseDate throws with a message containing text.
     *
     * @param raw
     *            the input
     * @param text
     *            part of the expected message
     */
    private void assertDateError(String raw, String text)
    {
        Exception thrown = null;
        try
        {
            InputParser.parseDate(raw, today);
        }
        catch (IllegalArgumentException e)
        {
            thrown = e;
        }
        assertNotNull(thrown);
        assertTrue(thrown.getMessage(), thrown.getMessage().contains(text));
    }
}
