package com.jobhub.config;

import com.jobhub.model.User;
import com.jobhub.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner init(UserRepository repo) {
        return args -> {
            if(repo.findByEmail("admin@jobhub.com").isEmpty())
                repo.save(new User("Admin","admin@jobhub.com","admin123","ADMIN"));
            if(repo.findByEmail("employer@jobhub.com").isEmpty())
                repo.save(new User("Demo Employer","employer@jobhub.com","employer123","EMPLOYER"));
            if(repo.findByEmail("seeker@jobhub.com").isEmpty())
                repo.save(new User("Demo Seeker","seeker@jobhub.com","seeker123","JOB_SEEKER"));
        };
    }
}
