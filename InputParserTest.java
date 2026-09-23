import junit.framework.TestCase;
import java.time.LocalDate;

public class InputParserTest extends TestCase {
    
    private LocalDate today;

    protected void setUp() {
        today = LocalDate.of(2026, 9, 21);
    }

    // -------------------------------------------------
    // parseAmount Tests
    // -------------------------------------------------

    public void testParseAmountNormal() {
        // Steps allows fractional parsing but rounds if it's a whole unit metric
        double result = InputParser.parseAmount("4,200", Metric.STEPS);
        assertEquals(4200.0, result, 0.001);
    }

    public void testParseAmountNullOrEmpty() {
        assertEquals(0.0, InputParser.parseAmount(null, Metric.STEPS), 0.001);
        assertEquals(0.0, InputParser.parseAmount("   ", Metric.STEPS), 0.001);
    }

    public void testParseAmountWholeUnitRounding() {
        // Metric.STEPS has isWholeUnit() = true, so 5000.7 should round to 5001.0
        double result = InputParser.parseAmount("5000.7", Metric.STEPS);
        assertEquals(5001.0, result, 0.001);
    }

    public void testParseAmountNotANumberThrows() {
        try {
            InputParser.parseAmount("eight thousand", Metric.STEPS);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("eight thousand is not a number, enter digits like 4300", e.getMessage());
        }
    }

    public void testParseAmountNegativeThrows() {
        try {
            InputParser.parseAmount("-5", Metric.STEPS);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("-5 cannot be negative", e.getMessage());
        }
    }

    public void testParseAmountExceedsDailyMaxThrows() {
        // Metric.WATER default maxPerDay is 20.0
        try {
            InputParser.parseAmount("25", Metric.WATER);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("25 is above the daily maximum of 20 Glasses", e.getMessage());
        }
    }

    // -------------------------------------------------
    // parseGoal Tests
    // -------------------------------------------------

    public void testParseGoalNullOrEmptyReturnsDefault() {
        // Metric.STEPS default goal is 10000.0
        double result = InputParser.parseGoal(null, Metric.STEPS);
        assertEquals(10000.0, result, 0.001);
    }

    public void testParseGoalNormal() {
        double result = InputParser.parseGoal("8000", Metric.STEPS);
        assertEquals(8000.0, result, 0.001);
    }

    public void testParseGoalZeroThrows() {
        try {
            InputParser.parseGoal("0", Metric.STEPS);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("A goal of 0 is not allowed", e.getMessage());
        }
    }

    // -------------------------------------------------
    // parseDate Tests
    // -------------------------------------------------

    public void testParseDateNullOrEmptyReturnsToday() {
        LocalDate result = InputParser.parseDate(null, today);
        assertEquals(today, result);
    }

    public void testParseDateNormal() {
        LocalDate result = InputParser.parseDate("09/15/2026", today);
        assertEquals(LocalDate.of(2026, 9, 15), result);
    }

    public void testParseDateInvalidFormatThrows() {
        try {
            InputParser.parseDate("2026-09-15", today);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("2026-09-15 is not a valid date use MM/DD/YYYY", e.getMessage());
        }
    }

    public void testParseDateFutureDateThrows() {
        try {
            InputParser.parseDate("09/25/2026", today);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("09/25/2026 is in the future - today is 09/21/2026", e.getMessage());
        }
    }

    // -------------------------------------------------
    // parseMenuChoice Tests
    // -------------------------------------------------

    public void testParseMenuChoiceNormal() {
        int result = InputParser.parseMenuChoice("3", 5);
        assertEquals(3, result);
    }

    public void testParseMenuChoiceNullOrEmptyThrows() {
        try {
            InputParser.parseMenuChoice(null, 5);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("That is not one of  the options", e.getMessage());
        }
    }

    public void testParseMenuChoiceNotANumberThrows() {
        try {
            InputParser.parseMenuChoice("abc", 5);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("That is not one of the options", e.getMessage());
        }
    }

    public void testParseMenuChoiceOutOfRangeThrows() {
        try {
            InputParser.parseMenuChoice("6", 5);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("That is not one of the options", e.getMessage());
        }
        
        try {
            InputParser.parseMenuChoice("0", 5);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("That is not one of the options", e.getMessage());
        }
    }
}
