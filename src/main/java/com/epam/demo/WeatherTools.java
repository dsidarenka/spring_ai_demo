package com.epam.demo;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
class WeatherTools {

  private static final Map<String, String> WEATHER_DATA = Map.ofEntries(
      Map.entry("london", "Overcast, 11°C, humidity 80%, wind 18 km/h SW"),
      Map.entry("paris", "Sunny, 17°C, humidity 52%, wind 9 km/h SE"),
      Map.entry("warsaw", "Partly cloudy, 14°C, humidity 65%, wind 22 km/h NE"),
      Map.entry("tokyo", "Light rain, 9°C, humidity 88%, wind 26 km/h E"),
      Map.entry("sydney", "Clear, 27°C, humidity 44%, wind 14 km/h S"),
      Map.entry("łódź", "Foggy, 7°C, humidity 90%, wind 5 km/h NE"),
      Map.entry("dubai", "Sunny, 35°C, humidity 28%, wind 15 km/h NW"),
      Map.entry("berlin", "Snow, -4°C, humidity 92%, wind 30 km/h N")
  );

  private static final String[] CONDITIONS = {"Sunny", "Cloudy", "Rainy", "Windy", "Clear"};

  @Tool(description = "Get the current weather conditions and temperature for a specified city")
  public String getCurrentWeather(
      @ToolParam(description = "The name of the city") String city) {
    String data = WEATHER_DATA.getOrDefault(
        city.toLowerCase(), "Partly cloudy, 18°C, humidity 60%, wind 12 km/h N");
    return String.format("Current weather in %s: %s", city, data);
  }

  @Tool(description = "Get a multi-day weather forecast for a specified city")
  public String getWeatherForecast(
      @ToolParam(description = "The name of the city") String city,
      @ToolParam(description = "Number of forecast days (1–5)") int days) {
    int n = Math.max(1, Math.min(days, 5));
    int baseTemp = city.equalsIgnoreCase("dubai") ? 35 : city.equalsIgnoreCase("moscow") ? -4 : 15;
    StringBuilder sb = new StringBuilder(n + "-day forecast for " + city + ": ");
    for (int i = 1; i <= n; i++) {
      sb.append(String.format("Day %d: %s %d°C", i, CONDITIONS[i % CONDITIONS.length], baseTemp + (i % 3) - 1));
      if (i < n) sb.append(", ");
    }
    return sb.toString();
  }
}
