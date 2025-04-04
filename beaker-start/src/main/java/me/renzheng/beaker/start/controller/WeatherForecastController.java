package me.renzheng.beaker.start.controller;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 天气预报 Controller
 *
 * @author Renzheng Zhang
 */
@RestController
@RequestMapping("/api/weather-forecast")
public class WeatherForecastController {

    private static final String[] summaries = {"Freezing", "Bracing", "Chilly", "Mild", "Warm", "Balmy", "Hot",
            "Sweltering", "Scorching"};

    @GetMapping
    public List<WeatherForecast> get() {
        RandomGenerator generator = RandomGenerator.getDefault();
        return IntStream.range(1, 6).mapToObj(index -> {
            WeatherForecast forecast = new WeatherForecast();
            forecast.setDate(LocalDate.now().plusDays(index));
            forecast.setSummary(summaries[generator.nextInt(summaries.length)]);
            forecast.setTemperatureC(generator.nextInt(-22, 55));
            return forecast;
        }).collect(Collectors.toList());
    }

    @Getter
    @Setter
    public static class WeatherForecast {
        private LocalDate date;
        private String summary;
        private int temperatureC;

        public int getTemperatureF() {
            return 32 + (int) (temperatureC / 0.556);
        }

        public String getDayOfWeek() {
            return date.getDayOfWeek().toString();
        }
    }
}
