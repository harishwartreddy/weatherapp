package com.weatherapp.util;

import java.text.DecimalFormat;
import java.util.Locale;

public class TemperatureFormatter {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#");

    private TemperatureFormatter() {
    }

    public static String formatCelsius(double celsius) {
        return DECIMAL_FORMAT.format(celsius) + "°C";
    }

    public static String formatFahrenheit(double fahrenheit) {
        return DECIMAL_FORMAT.format(fahrenheit) + "°F";
    }

    public static double celsiusToFahrenheit(double celsius) {
        return (celsius * 9.0 / 5.0) + 32.0;
    }

    public static double fahrenheitToCelsius(double fahrenheit) {
        return (fahrenheit - 32.0) * 5.0 / 9.0;
    }

    public static String formatWithLocale(double celsius) {
        Locale locale = Locale.getDefault();
        if (locale.equals(Locale.US)) {
            double fahrenheit = celsiusToFahrenheit(celsius);
            return formatFahrenheit(fahrenheit);
        }
        return formatCelsius(celsius);
    }

    public static String formatRange(double minCelsius, double maxCelsius) {
        return DECIMAL_FORMAT.format(minCelsius) + "°C - " +
               DECIMAL_FORMAT.format(maxCelsius) + "°C";
    }

    public static String getTemperatureDescription(double celsius) {
        if (celsius < 0) {
            return "Freezing";
        } else if (celsius < 10) {
            return "Cold";
        } else if (celsius < 20) {
            return "Cool";
        } else if (celsius < 30) {
            return "Warm";
        } else {
            return "Hot";
        }
    }
}
