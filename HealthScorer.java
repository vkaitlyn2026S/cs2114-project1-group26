// Project 1 - Health Bar (CS 2114, Group 26)
// Virginia Tech Honor Code Pledge:
//
// As a Hokie, I will conduct myself with honor and integrity at all times.
// I will not lie, cheat, or steal, nor will I accept the actions of those
// who do.
// -- Nikitha (906760583)

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * All the math: Daily Health Score, current streak, 7-day averages, the
 * weakest metric for the coaching line, and (stretch goals) badges and goal
 * auto-tuning suggestions. Reads HealthLog and Goals but never changes them
 * and never prints. Nothing is cached, so a score can never
 * go stale after an edit.
 *
 * @author Nikitha (906760583)
 * @version 2026.09.22
 */
public class HealthScorer
{
    /** A day with a score at or above this keeps the streak alive. */
    public static final int STREAK_THRESHOLD = 50;

    /** How many calendar days the averages and coaching look back. */
    private static final int WINDOW = 7;

    /** Ratios closer than this count as a tie. */
    private static final double TIE_TOLERANCE = 1e-9;

    /** Badge: 7 calendar days in a row with a score of 50 or more. */
    public static final String BADGE_STREAK = "7-Day Streak";

    /** Badge: any day with 10,000 steps or more. */
    public static final String BADGE_STEPS = "First 10K Steps Day";

    /** Badge: 7 calendar days in a row meeting the water goal. */
    public static final String BADGE_WATER = "Hydration Week";

    /** Steps needed for the steps badge. */
    private static final double BADGE_STEP_COUNT = 10000;

    /** Days in a row needed for auto-tuning and the streak badges. */
    public static final int TUNE_DAYS = 7;

    /** Auto-tuning raises or lowers a goal by this fraction (10%). */
    public static final double TUNE_STEP = 0.10;

    private final HealthLog log;
    private final Goals goals;

    /**
     * Creates a scorer over a history and a set of goals.
     *
     * @param log
     *            the history (read only)
     * @param goals
     *            the goals (read only)
     */
    public HealthScorer(HealthLog log, Goals goals)
    {
        this.log = log;
        this.goals = goals;
    }


    /**
     * The Daily Health Score: for each metric min(100, value / goal x 100),
     * averaged over the four metrics and rounded to a whole number.
     *
     * @param day
     *            the day to score (null means an unlogged day)
     * @return a score from 0 to 100
     */
    public int dailyScore(DayLog day)
    {
        if (day == null)
        {
            return 0;
        }
        double total = 0.0;
        for (Metric m : Metric.values())
        {
            double goal = goals.get(m);
            if (goal > 0)
            {
                total += Math.min(100.0, day.getValue(m) / goal * 100.0);
            }
        }
        return (int) Math.round(total / Metric.values().length);
    }


    /**
     * The current streak: 1 if today scores at least 50 (else 0), plus one
     * for each consecutive earlier day (yesterday, the day before, ...)
     * that is stored and scores at least 50. Counting stops at the first
     * missing or low day, so yesterday's streak is still alive in the
     * morning before today is logged.
     *
     * @param today
     *            today's date
     * @return the streak length in days
     */
    public int currentStreak(LocalDate today)
    {
        int streak = 0;
        if (dailyScore(log.getDay(today)) >= STREAK_THRESHOLD)
        {
            streak++;
        }
        LocalDate date = today.minusDays(1);
        DayLog day = log.getDay(date);
        while (day != null && dailyScore(day) >= STREAK_THRESHOLD)
        {
            streak++;
            date = date.minusDays(1);
            day = log.getDay(date);
        }
        return streak;
    }


    /**
     * The mean of one metric over today and the six previous calendar days,
     * with unlogged days counting as 0.
     *
     * @param m
     *            the metric
     * @param today
     *            today's date
     * @return the 7-day average
     */
    public double sevenDayAverage(Metric m, LocalDate today)
    {
        double total = 0.0;
        for (int i = 0; i < WINDOW; i++)
        {
            DayLog day = log.getDay(today.minusDays(i));
            if (day != null)
            {
                total += day.getValue(m);
            }
        }
        return total / WINDOW;
    }


    /**
     * The metric with the lowest 7-day average divided by its goal - the
     * habit that cost the most points this week. Ties go to the first
     * metric in enum order.
     *
     * @param today
     *            today's date
     * @return the weakest metric, or null if none of the last seven days
     *         is logged
     */
    public Metric weakestMetric(LocalDate today)
    {
        boolean anyLogged = false;
        for (int i = 0; i < WINDOW && !anyLogged; i++)
        {
            anyLogged = log.getDay(today.minusDays(i)) != null;
        }
        if (!anyLogged)
        {
            return null;
        }
        Metric weakest = null;
        double weakestRatio = Double.MAX_VALUE;
        for (Metric m : Metric.values())
        {
            double goal = goals.get(m);
            double ratio = 0.0;
            if (goal > 0)
            {
                ratio = sevenDayAverage(m, today) / goal;
            }
            // TIE_TOLERANCE keeps floating-point noise (1/7 does not divide
            // evenly) from breaking a real tie; ties stay with the first.
            if (ratio < weakestRatio - TIE_TOLERANCE)
            {
                weakestRatio = ratio;
                weakest = m;
            }
        }
        return weakest;
    }

    /**
     * The badges earned so far, looking at the whole history with the
     * current goals: a 7-day streak (7 calendar days in a row scoring 50 or
     * more), a first 10,000-step day, and a hydration week (7 calendar days
     * in a row meeting the water goal).
     *
     * @return the names of the earned badges, in that order (empty if none)
     */
    public List<String> badges()
    {
        List<String> earned = new ArrayList<String>();
        if (longestScoreRun() >= TUNE_DAYS)
        {
            earned.add(BADGE_STREAK);
        }
        if (hasTenThousandStepDay())
        {
            earned.add(BADGE_STEPS);
        }
        if (longestWaterRun() >= TUNE_DAYS)
        {
            earned.add(BADGE_WATER);
        }
        return earned;
    }


    /**
     * Goal auto-tuning. Looks at the 7 complete days before today
     * (yesterday back to 7 days ago). If every one of them is logged and
     * beat the goal, suggests a goal 10% higher; if every one is logged and
     * missed the goal, suggests a goal 10% lower. The suggestion is rounded
     * the way the metric is stored and kept between 0 and m.maxPerDay().
     *
     * @param m
     *            the metric
     * @param today
     *            today's date
     * @return the suggested goal, or the current goal if no change is
     *         suggested
     */
    public double suggestedGoal(Metric m, LocalDate today)
    {
        double goal = goals.get(m);
        double proposed = goal;
        if (goalMetEveryDay(m, today, true))
        {
            proposed = goal * (1 + TUNE_STEP);
        }
        else if (goalMetEveryDay(m, today, false))
        {
            proposed = goal * (1 - TUNE_STEP);
        }
        if (m.isWholeUnits())
        {
            proposed = Math.round(proposed);
        }
        else
        {
            proposed = Math.round(proposed * 10) / 10.0;
        }
        if (proposed > m.maxPerDay())
        {
            proposed = m.maxPerDay();
        }
        if (proposed <= 0)
        {
            proposed = goal;
        }
        return proposed;
    }


    /**
     * Checks the 7 days before today for auto-tuning.
     *
     * @param m
     *            the metric
     * @param today
     *            today's date
     * @param beat
     *            true to check "beat the goal every day", false to check
     *            "missed the goal every day"
     * @return true if all 7 days are logged and all match
     */
    private boolean goalMetEveryDay(Metric m, LocalDate today, boolean beat)
    {
        double goal = goals.get(m);
        for (int i = 1; i <= TUNE_DAYS; i++)
        {
            DayLog day = log.getDay(today.minusDays(i));
            if (day == null)
            {
                return false;
            }
            boolean met = day.getValue(m) >= goal;
            if (met != beat)
            {
                return false;
            }
        }
        return true;
    }


    /**
     * The longest run of consecutive calendar days scoring 50 or more.
     *
     * @return the length of the longest run
     */
    private int longestScoreRun()
    {
        int best = 0;
        int run = 0;
        LocalDate previous = null;
        for (DayLog day : log.allDays())
        {
            boolean good = dailyScore(day) >= STREAK_THRESHOLD;
            boolean nextDay = previous != null
                && previous.plusDays(1).equals(day.getDate());
            if (!good)
            {
                run = 0;
            }
            else if (nextDay)
            {
                run++;
            }
            else
            {
                run = 1;
            }
            best = Math.max(best, run);
            previous = day.getDate();
        }
        return best;
    }


    /**
     * The longest run of consecutive calendar days meeting the water goal.
     *
     * @return the length of the longest run
     */
    private int longestWaterRun()
    {
        int best = 0;
        int run = 0;
        LocalDate previous = null;
        double goal = goals.get(Metric.WATER);
        for (DayLog day : log.allDays())
        {
            boolean good = day.getValue(Metric.WATER) >= goal;
            boolean nextDay = previous != null
                && previous.plusDays(1).equals(day.getDate());
            if (!good)
            {
                run = 0;
            }
            else if (nextDay)
            {
                run++;
            }
            else
            {
                run = 1;
            }
            best = Math.max(best, run);
            previous = day.getDate();
        }
        return best;
    }


    /**
     * Whether any logged day has 10,000 steps or more.
     *
     * @return true if such a day exists
     */
    private boolean hasTenThousandStepDay()
    {
        for (DayLog day : log.allDays())
        {
            if (day.getValue(Metric.STEPS) >= BADGE_STEP_COUNT)
            {
                return true;
            }
        }
        return false;
    }

}
