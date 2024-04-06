package system.pos.spring.utility;

public class CapitalizeFirstLetter {
    public static String capitalizeFirstLetter(String input) {
        return (input == null || input.isEmpty()) ? input : input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }
}
