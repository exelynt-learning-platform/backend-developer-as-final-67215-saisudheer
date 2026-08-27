package com.resourcebooking.config;

import com.resourcebooking.entity.Resource;
import com.resourcebooking.entity.User;
import com.resourcebooking.enums.Role;
import com.resourcebooking.repository.ResourceRepository;
import com.resourcebooking.repository.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initializeData(
            UserRepository userRepository,
            ResourceRepository resourceRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            // ==========================================
            // ADMIN USER
            // ==========================================

            if (!userRepository.existsByEmail(
                    "admin@example.com")) {

                User admin = new User();

                admin.setEmail("admin@example.com");

                admin.setPassword(
                        passwordEncoder.encode("Admin@123")
                );

                admin.setRole(Role.ADMIN);

                userRepository.save(admin);
            }

            // ==========================================
            // NORMAL USER
            // ==========================================

            if (!userRepository.existsByEmail(
                    "user@example.com")) {

                User user = new User();

                user.setEmail("user@example.com");

                user.setPassword(
                        passwordEncoder.encode("User@123")
                );

                user.setRole(Role.USER);

                userRepository.save(user);
            }

            // ==========================================
            // SAMPLE RESOURCE 1
            // ==========================================

            if (resourceRepository.count() == 0) {

                Resource conferenceRoom = new Resource();

                conferenceRoom.setName(
                        "Conference Room A"
                );

                conferenceRoom.setDescription(
                        "Large conference room with projector and Wi-Fi"
                );

                conferenceRoom.setType(
                        "ROOM"
                );

                conferenceRoom.setAvailable(
                        true
                );

                resourceRepository.save(
                        conferenceRoom
                );

                // ======================================
                // SAMPLE RESOURCE 2
                // ======================================

                Resource vehicle = new Resource();

                vehicle.setName(
                        "Toyota Innova"
                );

                vehicle.setDescription(
                        "Seven-seater vehicle available for booking"
                );

                vehicle.setType(
                        "VEHICLE"
                );

                vehicle.setAvailable(
                        true
                );

                resourceRepository.save(
                        vehicle
                );

                // ======================================
                // SAMPLE RESOURCE 3
                // ======================================

                Resource equipment = new Resource();

                equipment.setName(
                        "Projector"
                );

                equipment.setDescription(
                        "Full HD projector for presentations"
                );

                equipment.setType(
                        "EQUIPMENT"
                );

                equipment.setAvailable(
                        true
                );

                resourceRepository.save(
                        equipment
                );
            }
        };
    }
}