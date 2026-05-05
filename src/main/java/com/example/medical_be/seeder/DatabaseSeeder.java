package com.example.medical_be.seeder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder {
    private final List<ISeeder> seeders;
    private volatile boolean started = false;
    @EventListener(ContextRefreshedEvent.class)
    public void seed(ContextRefreshedEvent e) {
        if (e.getApplicationContext().getParent() != null || started) return;
        started = true;

        log.info("Starting database seeding...");
        try {
            seeders.stream()
                    .sorted(Comparator.comparingInt(ISeeder::getOrder))
                    .filter(s -> s.getOrder() > 0)
                    .forEach(ISeeder::seed);
            log.info("Database seeding completed successfully.");
        } catch (Exception ex) {
            log.error("Error during database seeding: ", ex);
        }
    }
}
