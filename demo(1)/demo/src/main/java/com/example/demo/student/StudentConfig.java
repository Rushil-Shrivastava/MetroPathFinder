package com.example.demo.student;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static java.time.Month.*;

@Configuration
public class StudentConfig {
    @Bean
    CommandLineRunner commandLineRunner(StudentRepository repository) {
        return args -> {
            Student rushil = new Student(
                    "Rushil",
                    "rush@gmail.com",
                    LocalDate.of(2000, JANUARY,15)
            );

            Student shri = new Student(
                    "Shri",
                    "shri@gmail.com",
                    LocalDate.of(2000, JANUARY,15)
            );

            repository.saveAll(
                    List.of(rushil, shri)
            );
        };
    }
}
