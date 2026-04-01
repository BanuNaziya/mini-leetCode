package com.minileetcode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Mini LeetCode Application Entry Point
 *
 * <p>This is a portfolio project demonstrating a full-stack Spring Boot REST API
 * application inspired by LeetCode. It features:
 * <ul>
 *   <li>User management (register, login, CRUD)</li>
 *   <li>Problem management (CRUD, filtering by difficulty/tag)</li>
 *   <li>Submission tracking with statistics</li>
 *   <li>HTML/CSS/JS frontend served as static files</li>
 *   <li>MySQL persistence via Spring Data JPA</li>
 * </ul>
 *
 * @author Mini LeetCode Portfolio Project
 * @version 1.0.0
 */
@SpringBootApplication
public class MiniLeetCodeApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiniLeetCodeApplication.class, args);
    }
}
