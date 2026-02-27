package com.impacthub.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity  // Tells Spring this is a database table
@Table(name = "users")  // Table name in MySQL
@Data  // Lombok: Auto-generates getters, setters, toString, etc.
@NoArgsConstructor  // Lombok: Creates empty constructor
@AllArgsConstructor  // Lombok: Creates constructor with all fields
public class User {

    @Id  // Primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-increment
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)  // Store enum as string in DB
    @Column(name = "user_type", nullable = false)
    private UserType userType;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp  // Auto-set on creation
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp  // Auto-update on modification
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enum for user types
    public enum UserType {
        VOLUNTEER, NGO, ADMIN
    }
}
