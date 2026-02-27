package com.impacthub.backend.dto.request;

import com.impacthub.backend.entity.Volunteer;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class VolunteerUpdateRequest {
    private String fullName;
    private String phone;
    private LocalDate dateOfBirth;
    private Volunteer.Gender gender;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private List<String> skills;
    private List<String> interests;
    private String availability;
    private List<String> preferredCauses;
    private Volunteer.ExperienceLevel experienceLevel;
    private String education;
    private String occupation;
    private String linkedinUrl;
    private String emergencyContactName;
    private String emergencyContactPhone;
}
