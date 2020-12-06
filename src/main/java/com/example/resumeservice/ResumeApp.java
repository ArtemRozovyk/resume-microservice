package com.example.resumeservice;

import com.example.resumeservice.models.Resume;
import com.example.resumeservice.repositories.ResumeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

@SpringBootApplication
public class ResumeApp {

    @Autowired
    private ResumeRepository resumeRepository;

/*
	@Autowired
	private SiteRepository siteRepository;
*/

    public static void main(String[] args) {
        SpringApplication.run(ResumeApp.class, args);
    }

    @Bean
    public CommandLineRunner demo(ResumeRepository repository) {
        return (args) -> {
            Resume r = new Resume();
            r.setName("Digger");
            repository.save(r);

            Resume r2 = new Resume();
            r2.setName("KookerE");

            resumeRepository.save(r2);

        };
    }

}
