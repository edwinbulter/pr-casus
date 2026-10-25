package nl.kabulter.threekids;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * The only place that reads the system time (ADR 0006), in the zone Europe/Amsterdam (ADR 0007).
 */
@Configuration
class ClockConfig {

    @Bean
    Clock clock() {
        return Clock.system(ZoneId.of("Europe/Amsterdam"));
    }
}
