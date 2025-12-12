package ua.edu.viti.military;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync  // Увімкнення асинхронної обробки подій
public class MilitaryApplication {

    public static void main(String[] args) {
        SpringApplication.run(MilitaryApplication.class, args);
    }
}
