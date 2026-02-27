package com.impacthub.backend.dto.response;

import com.impacthub.backend.entity.Volunteer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VolunteerResponse {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private LocalDate dateOfBirth;
    private Volunteer.Gender gender;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private Set<String> skills;
    private Set<String> interests;
    private String availability;
    private Set<String> preferredCauses;
    private Volunteer.ExperienceLevel experienceLevel;
    private String education;
    private String occupation;
    private String linkedinUrl;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
