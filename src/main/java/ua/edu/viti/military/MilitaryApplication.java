package ua.edu.viti.military;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MilitaryApplication {

    public static void main(String[] args) {
        SpringApplication.run(MilitaryApplication.class, args);
    }
}
