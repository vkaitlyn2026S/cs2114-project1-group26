// Project 1 - Health Bar (CS 2114, Group 26)
// Virginia Tech Honor Code Pledge:
//
// As a Hokie, I will conduct myself with honor and integrity at all times.
// I will not lie, cheat, or steal, nor will I accept the actions of those
// who do.
// -- Nikitha (906760583)

import java.time.LocalDate;
import java.util.List;

/**
 * Tests for HealthScorer. Default goals are used throughout: 10,000
 * steps, 8 hours, 2,000 calories, 8 glasses.
 *
 * @author Nikitha (906760583)
 * @version 2026.09.22
 */
public class HealthScorerTest
    extends student.TestCase
{
    private HealthLog log;
    private Goals goals;
    private HealthScorer scorer;
    private LocalDate today;

    /**
     * Creates an empty log and default goals before each test.
     */
    public void setUp()
    {
        log = new HealthLog();
        goals = new Goals();
        scorer = new HealthScorer(log, goals);
        today = LocalDate.of(2026, 9, 15);
    }


    /**
     * 61%, 93.75%, 100% and 75% average to 82.
     */
    public void testDailyScore()
    {
        DayLog day = new DayLog(today);
        day.setValue(Metric.STEPS, 6100);
        day.setValue(Metric.SLEEP, 7.5);
        day.setValue(Metric.CALORIES, 2000);
        day.setValue(Metric.WATER, 6);
        assertEquals(82, scorer.dailyScore(day));
    }


    /**
     * Each metric is capped at 100%.
     */
    public void testDailyScoreCapped()
    {
        DayLog day = new DayLog(today);
        day.setValue(Metric.STEPS, 12000);
        assertEquals(25, scorer.dailyScore(day));
        day.setValue(Metric.SLEEP, 24);
        day.setValue(Metric.CALORIES, 20000);
        day.setValue(Metric.WATER, 40);
        assertEquals(100, scorer.dailyScore(day));
    }


    /**
     * An unlogged day and an all-zero day both score 0.
     */
    public void testDailyScoreZero()
    {
        assertEquals(0, scorer.dailyScore(null));
        assertEquals(0, scorer.dailyScore(new DayLog(today)));
    }


    /**
     * Three good days in a row, ending today, is a streak of 3.
     */
    public void testStreakThroughToday()
    {
        logScore(today.minusDays(2), 80);
        logScore(today.minusDays(1), 80);
        logScore(today, 80);
        assertEquals(3, scorer.currentStreak(today));
    }


    /**
     * The streak is still alive in the morning before today is logged.
     */
    public void testStreakTodayNotLogged()
    {
        logScore(today.minusDays(2), 80);
        logScore(today.minusDays(1), 80);
        assertEquals(2, scorer.currentStreak(today));
    }


    /**
     * A missing day ends the streak.
     */
    public void testStreakGap()
    {
        logScore(today, 80);
        logScore(today.minusDays(1), 80);
        logScore(today.minusDays(3), 80);
        assertEquals(2, scorer.currentStreak(today));
    }


    /**
     * A low day yesterday ends the streak.
     */
    public void testStreakLowYesterday()
    {
        logScore(today.minusDays(2), 80);
        logScore(today.minusDays(1), 40);
        assertEquals(0, scorer.currentStreak(today));
        logScore(today, 80);
        assertEquals(1, scorer.currentStreak(today));
    }


    /**
     * Exactly 50 counts; 49 does not.
     */
    public void testStreakThreshold()
    {
        logScore(today, 50);
        assertEquals(1, scorer.currentStreak(today));
        log.setValue(today, Metric.STEPS, 4600);
        assertEquals(49, scorer.dailyScore(log.getDay(today)));
        assertEquals(0, scorer.currentStreak(today));
        assertEquals(0, new HealthScorer(new HealthLog(), goals)
            .currentStreak(today));
    }


    /**
     * 7,000 steps every day averages 7,000.
     */
    public void testSevenDayAverage()
    {
        for (int i = 0; i < 7; i++)
        {
            log.addValue(today.minusDays(i), Metric.STEPS, 7000);
        }
        log.addValue(today.minusDays(7), Metric.STEPS, 100000);
        assertEquals(7000.0, scorer.sevenDayAverage(Metric.STEPS, today),
            0.001);
    }


    /**
     * Unlogged days count as 0.
     */
    public void testSevenDayAverageMissingDays()
    {
        log.addValue(today.minusDays(3), Metric.STEPS, 7000);
        assertEquals(1000.0, scorer.sevenDayAverage(Metric.STEPS, today),
            0.001);
        assertEquals(0.0, scorer.sevenDayAverage(Metric.WATER, today),
            0.001);
    }


    /**
     * Water at 25% all week while the rest are at 80%+ is the weakest.
     */
    public void testWeakestMetric()
    {
        for (int i = 0; i < 7; i++)
        {
            LocalDate d = today.minusDays(i);
            log.addValue(d, Metric.STEPS, 8000);
            log.addValue(d, Metric.SLEEP, 7);
            log.addValue(d, Metric.CALORIES, 1900);
            log.addValue(d, Metric.WATER, 2);
        }
        assertEquals(Metric.WATER, scorer.weakestMetric(today));
    }


    /**
     * No logged days in the window gives null.
     */
    public void testWeakestMetricEmpty()
    {
        assertNull(scorer.weakestMetric(today));
        assertEquals(0.0, scorer.sevenDayAverage(Metric.SLEEP, today),
            0.001);
        log.addValue(today.minusDays(7), Metric.STEPS, 5000);
        assertNull(scorer.weakestMetric(today));
    }


    /**
     * A four-way tie goes to STEPS, even with 1/7 rounding noise.
     */
    public void testWeakestMetricTie()
    {
        log.addValue(today, Metric.STEPS, 5000);
        log.addValue(today, Metric.SLEEP, 4);
        log.addValue(today, Metric.CALORIES, 1000);
        log.addValue(today, Metric.WATER, 4);
        assertEquals(Metric.STEPS, scorer.weakestMetric(today));
        log.setValue(today, Metric.STEPS, 10000);
        log.setValue(today, Metric.SLEEP, 8);
        log.setValue(today, Metric.CALORIES, 2000);
        log.setValue(today, Metric.WATER, 8);
        assertEquals(Metric.STEPS, scorer.weakestMetric(today));
    }


    /**
     * No history means no badges.
     */
    public void testBadgesNone()
    {
        assertTrue(scorer.badges().isEmpty());
        logScore(today, 80);
        assertTrue(scorer.badges().isEmpty());
    }


    /**
     * Seven good days in a row earn the streak badge; six do not.
     */
    public void testBadgeStreak()
    {
        for (int i = 1; i <= 6; i++)
        {
            logScore(today.minusDays(i), 60);
        }
        assertFalse(scorer.badges().contains(HealthScorer.BADGE_STREAK));
        logScore(today, 60);
        assertTrue(scorer.badges().contains(HealthScorer.BADGE_STREAK));
    }


    /**
     * A gap or a low day breaks the run for the streak badge.
     */
    public void testBadgeStreakBroken()
    {
        for (int i = 0; i < 8; i++)
        {
            logScore(today.minusDays(i), 60);
        }
        log.setValue(today.minusDays(4), Metric.STEPS, 0);
        log.setValue(today.minusDays(4), Metric.SLEEP, 0);
        log.setValue(today.minusDays(4), Metric.CALORIES, 0);
        log.setValue(today.minusDays(4), Metric.WATER, 0);
        assertFalse(scorer.badges().contains(HealthScorer.BADGE_STREAK));
        logScore(today.minusDays(4), 40);
        assertFalse(scorer.badges().contains(HealthScorer.BADGE_STREAK));
    }


    /**
     * Any 10,000-step day earns the steps badge.
     */
    public void testBadgeSteps()
    {
        log.addValue(today.minusDays(20), Metric.STEPS, 9999);
        assertFalse(scorer.badges().contains(HealthScorer.BADGE_STEPS));
        log.addValue(today.minusDays(30), Metric.STEPS, 10000);
        assertTrue(scorer.badges().contains(HealthScorer.BADGE_STEPS));
    }


    /**
     * Seven days in a row at the water goal earn the water badge, and the
     * badges come back in a fixed order.
     */
    public void testBadgeWaterAndOrder()
    {
        for (int i = 0; i < 7; i++)
        {
            log.addValue(today.minusDays(i), Metric.WATER, 8);
        }
        log.addValue(today, Metric.STEPS, 10000);
        List<String> earned = scorer.badges();
        assertEquals(2, earned.size());
        assertEquals(HealthScorer.BADGE_STEPS, earned.get(0));
        assertEquals(HealthScorer.BADGE_WATER, earned.get(1));
    }


    /**
     * Beating the goal on all 7 days before today suggests 10% more.
     */
    public void testSuggestedGoalRaise()
    {
        for (int i = 1; i <= 7; i++)
        {
            log.addValue(today.minusDays(i), Metric.STEPS, 10000);
            log.addValue(today.minusDays(i), Metric.SLEEP, 8);
            log.addValue(today.minusDays(i), Metric.WATER, 9);
        }
        assertEquals(11000.0, scorer.suggestedGoal(Metric.STEPS, today),
            0.001);
        assertEquals(8.8, scorer.suggestedGoal(Metric.SLEEP, today), 0.001);
        assertEquals(9.0, scorer.suggestedGoal(Metric.WATER, today), 0.001);
    }


    /**
     * Missing the goal on all 7 days suggests 10% less.
     */
    public void testSuggestedGoalLower()
    {
        for (int i = 1; i <= 7; i++)
        {
            log.addValue(today.minusDays(i), Metric.STEPS, 3000);
        }
        assertEquals(9000.0, scorer.suggestedGoal(Metric.STEPS, today),
            0.001);
        assertEquals(1800.0, scorer.suggestedGoal(Metric.CALORIES, today),
            0.001);
        assertEquals(7.0, scorer.suggestedGoal(Metric.WATER, today), 0.001);
    }


    /**
     * No suggestion unless all 7 days are logged and all match; today
     * itself is not counted.
     */
    public void testSuggestedGoalNoChange()
    {
        assertEquals(10000.0, scorer.suggestedGoal(Metric.STEPS, today),
            0.001);
        for (int i = 1; i <= 6; i++)
        {
            log.addValue(today.minusDays(i), Metric.STEPS, 12000);
        }
        log.addValue(today, Metric.STEPS, 12000);
        assertEquals(10000.0, scorer.suggestedGoal(Metric.STEPS, today),
            0.001);
        log.addValue(today.minusDays(7), Metric.STEPS, 500);
        assertEquals(10000.0, scorer.suggestedGoal(Metric.STEPS, today),
            0.001);
    }


    /**
     * A raise never goes above the daily maximum, and a lower that would
     * round to the same goal is no change.
     */
    public void testSuggestedGoalLimits()
    {
        goals.set(Metric.WATER, 40);
        goals.set(Metric.SLEEP, 0.5);
        for (int i = 1; i <= 7; i++)
        {
            log.addValue(today.minusDays(i), Metric.WATER, 40);
        }
        assertEquals(40.0, scorer.suggestedGoal(Metric.WATER, today), 0.001);
        goals.set(Metric.WATER, 1);
        for (int i = 1; i <= 7; i++)
        {
            log.setValue(today.minusDays(i), Metric.WATER, 0);
            log.setValue(today.minusDays(i), Metric.STEPS, 1);
        }
        assertEquals(1.0, scorer.suggestedGoal(Metric.WATER, today), 0.001);
        assertEquals(0.5, scorer.suggestedGoal(Metric.SLEEP, today), 0.001);
    }


    /**
     * Logs a day whose score is exactly the given percent, by putting
     * every metric at that percent of its default goal.
     *
     * @param d
     *            the date
     * @param percent
     *            the score wanted
     */
    private void logScore(LocalDate d, int percent)
    {
        for (Metric m : Metric.values())
        {
            log.setValue(d, m, m.defaultGoal() * percent / 100.0);
        }
    }
}
