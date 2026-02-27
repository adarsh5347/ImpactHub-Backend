package com.impacthub.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "volunteers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Volunteer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    private String phone;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String address;
    private String city;
    private String state;
    private String pincode;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "volunteer_skills", joinColumns = @JoinColumn(name = "volunteer_id"))
    private Set<String> skills = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "volunteer_interests", joinColumns = @JoinColumn(name = "volunteer_id"))
    private Set<String> interests = new HashSet<>();

    private String availability;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "volunteer_preferred_causes", joinColumns = @JoinColumn(name = "volunteer_id"))
    @Column(name = "cause")
    private Set<String> preferredCauses = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "experience_level")
    private ExperienceLevel experienceLevel;

    private String education;
    private String occupation;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Column(name = "emergency_contact_name")
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone")
    private String emergencyContactPhone;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Gender {
        Male, Female, Other, PreferNotToSay
    }

    public enum ExperienceLevel {
        Beginner, Intermediate, Advanced
    }

    public void setSkills(List<String> skills) {
        this.skills = skills == null ? null : new HashSet<>(skills);
    }

    public void setInterests(List<String> interests) {
        this.interests = interests == null ? null : new HashSet<>(interests);
    }

    public void setPreferredCauses(List<String> preferredCauses) {
        this.preferredCauses = preferredCauses == null ? null : new HashSet<>(preferredCauses);
    }
}
