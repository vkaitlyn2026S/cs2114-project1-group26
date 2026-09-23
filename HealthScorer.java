import java.time.LocalDate;

public class HealthScorer {
    private final HealthLog log;
    private final Goals goals;
    public static final int STREAK_THRESHOLD = 50;
    
    public HealthScorer(HealthLog log, Goals goals) {
        this.log = log;
        this.goals = goals;
    }
    
    public int dailyScore(DayLog day) {
        if (day == null) {
            return 0;
        }
        
        double total = 0.0;
        
        for (Metric metric : Metric.values()) {
            double value = day.get(metric);
            double goal = goals.get(metric);
            
            // FIXED: Added a zero-check to protect against an ArithmeticException division crash
            double score = (goal > 0) ? (value / goal) * 100.0 : 0.0;
            score = Math.min(100.0, score);
            total += score;  
        }
        
        double average = total / Metric.values().length;
        return (int) Math.round(average);
    }
    
    public int currentStreak(LocalDate today) {
        int streak = 0;
        DayLog todayLog = log.get(today);
        
        // Check today's score
        if (todayLog != null) {
            if (dailyScore(todayLog) >= STREAK_THRESHOLD) {
                streak++;
            } else {
                return 0;
            }
        }
        
        // Walk backward through past history
        LocalDate date = today.minusDays(1);
        while (true) {
            DayLog day = log.get(date);
            
            // FIX: Stop scanning if a day doesn't exist OR if its score falls below the streak threshold
            if (day == null || dailyScore(day) < STREAK_THRESHOLD) {
                break;
            }
            
            streak++;
            date = date.minusDays(1);
        }
        
        return streak;
    }
    
    public double sevenDayAverage(Metric metric, LocalDate today) {
        double total = 0.0;

        for (int i = 0; i < 7; i++) {
            LocalDate date = today.minusDays(i);
            DayLog day = log.get(date);

            if (day != null) {
                total += day.get(metric);
            }
        }

        return total / 7.0;
    }
    
    public Metric weakestMetric(LocalDate today) {
        boolean anyLogged = false;

        for (int i = 0; i < 7; i++) {
            DayLog day = log.get(today.minusDays(i));

            if (day != null) {
                anyLogged = true;
                break;
            }
        }

        if (!anyLogged) {
            return null;
        }

        Metric weakest = null;
        double weakestRatio = Double.MAX_VALUE;

        for (Metric metric : Metric.values()) {
            double average = sevenDayAverage(metric, today);
            double goal = goals.get(metric);
            double ratio = (goal > 0) ? (average / goal) : 0.0;

            if (ratio < weakestRatio) {
                weakestRatio = ratio;
                weakest = metric;
            }
        }

        return weakest;
    }    
}
