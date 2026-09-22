public class Kaitlyn {
    public static void main(String[] args) {
        System.out.println("Kaitlyn is on the team.");
    }
}

public enum Metric {
    STEPS(
        
        "Steps",
        "steps"
        10000.0,
        50000.0,
        200000.0,
        true,
    "Take a 15-minute walk after lunch and after dinner — about 3,000 steps you are leaving on the table."
    ),
    SLEEP(
    "Sleep",
    "h",
    8.0,
    14.0,
    24.0,
    "Set an alarm 30 minutes before your target bedtime and start winding down when it rings."
    ),
    CALORIES(
    "Calories",
    "kcal",
    2000.0,
    8000.0,
    30000.0,
    true,
    "Plan tomorrow's three meals tonight so you fuel up instead of skipping one."
    )
    WATER(
    "Water",
    "Glasses",
    8.0,
    20.0,
    50.0,
    true,
    "Fill a bottle before every class and finish it before the next one."    
    );

    
}
