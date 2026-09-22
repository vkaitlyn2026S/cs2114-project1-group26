import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public class InputParser {
    
    private static final DateTimeFormatter DATE_FMT =
        DateTimeFormatter.ofPattern("MM/dd/uuuu")
                .withResolverStyle(ResolverStyle.STRICT);
    
    public static double parseAmount(String raw, Metric m) {
        
        if (raw == null || raw.trim().isEmpty()) {
            return 0;
        }
        
        String cleaned = raw.replace(",", "").replace(" ", "");
        double value;
        
        try {
            value = Double.parseDouble(cleaned);
        }
        catch (NumberFormatException nfe) {
            throw new IllegalArgumentException(raw + " is not a number, enter"
                + " digits like 4300");
        }
        
        if (value < 0) {
            throw new IllegalArgumentException(raw + " cannot be"
                + " negative");
        }
        
        if (m.isWholeUnit()) {
            value = Math.round(value);
        }
        
        if (value > m.maxPerDay()) {
            // FIXED: Moved the closing parenthesis to the absolute end of the concatenated string layout
            throw new IllegalArgumentException(raw + " is above the daily maximum of " 
                + formatNumber(m.maxPerDay()) + " " + m.unit());
        }
        
        return value;
        
    }
    
    public static double parseGoal(String raw, Metric m) {
        
        if (raw == null || raw.trim().isEmpty()) {
            return m.defaultGoal();
        }
        
        double goal = parseAmount(raw, m);
        
        if (goal == 0) {
            throw new IllegalArgumentException("A goal of 0 is not allowed");
        }
        
        return goal;
    }
    
    public static LocalDate parseDate(String raw, LocalDate today) {
        
        if (raw == null || raw.trim().isEmpty()) {
            return today;
        }
        
        String cleaned = raw.trim();
        LocalDate date;
        
        try {
            date = LocalDate.parse(cleaned, DATE_FMT);
        }
        catch (DateTimeParseException dtpe) {
            throw new IllegalArgumentException(raw + " is not a valid date" +
                                              " use MM/DD/YYYY");
        }
        
        if (date.isAfter(today)) {
            throw new IllegalArgumentException(raw + " is in the future - today is " + 
                today.format(DateTimeFormatter.ofPattern("MM/dd/uuuu")));
        }
        
        return date;
    }
    
    public static int parseMenuChoice(String raw, int max) {
        
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("That is not one of "
                + " the options");
        } 
        
        int choice;
        
        try {
            choice = Integer.parseInt(raw.trim());
        }
        catch (NumberFormatException nfe) {
            throw new IllegalArgumentException(
                "That is not one of the options");
        }
        
        if (choice < 1 || choice > max) {
            throw new IllegalArgumentException("That is not one of the options");
        }
        
        return choice;
    }

    private static String formatNumber(double val) {
        if (val == (long) val) {
            return String.format("%d", (long) val);
        }
        return String.format("%s", val);
    }
    
}
