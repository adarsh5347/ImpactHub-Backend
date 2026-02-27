package com.impacthub.backend.service;

import com.impacthub.backend.dto.request.VolunteerUpdateRequest;
import com.impacthub.backend.dto.response.ActivityResponse;
import com.impacthub.backend.dto.response.VolunteerResponse;
import com.impacthub.backend.dto.response.VolunteerStatsResponse;
import com.impacthub.backend.entity.User;
import com.impacthub.backend.entity.Volunteer;
import com.impacthub.backend.entity.VolunteerEnrollment;
import com.impacthub.backend.repository.VolunteerEnrollmentRepository;
import com.impacthub.backend.repository.VolunteerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class VolunteerService {

    private final VolunteerRepository volunteerRepository;
    private final VolunteerEnrollmentRepository volunteerEnrollmentRepository;

    @Transactional(readOnly = true)
    public boolean isVolunteerOwnedByEmail(Long volunteerId, String email) {
        return volunteerRepository.findById(volunteerId)
                .map(Volunteer::getUser)
                .map(User::getEmail)
                .map(ownerEmail -> ownerEmail.equalsIgnoreCase(email))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public VolunteerResponse getVolunteerById(Long id) {
        Volunteer volunteer = volunteerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Volunteer not found"));
        return toVolunteerResponse(volunteer);
    }

    @Transactional(readOnly = true)
    public VolunteerResponse getVolunteerByEmail(String email) {
        Volunteer volunteer = volunteerRepository.findByUser_Email(email)
                .orElseThrow(() -> new RuntimeException("Volunteer not found"));
        return toVolunteerResponse(volunteer);
    }

    @Transactional(readOnly = true)
    public List<VolunteerResponse> listVolunteers() {
        return volunteerRepository.findAll().stream()
                .map(this::toVolunteerResponse)
                .toList();
    }

    @Transactional
    public VolunteerResponse updateVolunteer(Long id, VolunteerUpdateRequest request) {
        Volunteer volunteer = volunteerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Volunteer not found"));

        if (request.getFullName() != null) volunteer.setFullName(request.getFullName());
        if (request.getPhone() != null) volunteer.setPhone(request.getPhone());
        if (request.getDateOfBirth() != null) volunteer.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender() != null) volunteer.setGender(request.getGender());
        if (request.getAddress() != null) volunteer.setAddress(request.getAddress());
        if (request.getCity() != null) volunteer.setCity(request.getCity());
        if (request.getState() != null) volunteer.setState(request.getState());
        if (request.getPincode() != null) volunteer.setPincode(request.getPincode());
        if (request.getSkills() != null) volunteer.setSkills(request.getSkills());
        if (request.getInterests() != null) volunteer.setInterests(request.getInterests());
        if (request.getAvailability() != null) volunteer.setAvailability(request.getAvailability());
        if (request.getPreferredCauses() != null) volunteer.setPreferredCauses(request.getPreferredCauses());
        if (request.getExperienceLevel() != null) volunteer.setExperienceLevel(request.getExperienceLevel());
        if (request.getEducation() != null) volunteer.setEducation(request.getEducation());
        if (request.getOccupation() != null) volunteer.setOccupation(request.getOccupation());
        if (request.getLinkedinUrl() != null) volunteer.setLinkedinUrl(request.getLinkedinUrl());
        if (request.getEmergencyContactName() != null) volunteer.setEmergencyContactName(request.getEmergencyContactName());
        if (request.getEmergencyContactPhone() != null) volunteer.setEmergencyContactPhone(request.getEmergencyContactPhone());

        Volunteer saved = volunteerRepository.save(volunteer);
        return toVolunteerResponse(saved);
    }

    @Transactional
    public void deleteVolunteer(Long id) {
        Volunteer volunteer = volunteerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Volunteer not found"));

        volunteerEnrollmentRepository.findByVolunteer_Id(id)
                .forEach(enrollment -> enrollment.setStatus(VolunteerEnrollment.EnrollmentStatus.CANCELLED));

        if (Objects.nonNull(volunteer.getUser())) {
            volunteer.getUser().setIsActive(false);
        }
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> getActivities(Long volunteerId) {
        return volunteerEnrollmentRepository.findByVolunteer_Id(volunteerId).stream()
                .map(this::toActivityResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public VolunteerStatsResponse getVolunteerStats(Long volunteerId) {
        if (!volunteerRepository.existsById(volunteerId)) {
            throw new RuntimeException("Volunteer not found");
        }

        return new VolunteerStatsResponse(
                volunteerId,
                volunteerEnrollmentRepository.countByVolunteer_Id(volunteerId),
                volunteerEnrollmentRepository.countByVolunteer_IdAndStatus(volunteerId, VolunteerEnrollment.EnrollmentStatus.ACTIVE),
                volunteerEnrollmentRepository.countByVolunteer_IdAndStatus(volunteerId, VolunteerEnrollment.EnrollmentStatus.COMPLETED),
                volunteerEnrollmentRepository.countByVolunteer_IdAndStatus(volunteerId, VolunteerEnrollment.EnrollmentStatus.CANCELLED),
                volunteerEnrollmentRepository.sumHoursByVolunteerId(volunteerId)
        );
    }

    private VolunteerResponse toVolunteerResponse(Volunteer volunteer) {
        return new VolunteerResponse(
                volunteer.getId(),
                volunteer.getUser() != null ? volunteer.getUser().getEmail() : null,
                volunteer.getFullName(),
                volunteer.getPhone(),
                volunteer.getDateOfBirth(),
                volunteer.getGender(),
                volunteer.getAddress(),
                volunteer.getCity(),
                volunteer.getState(),
                volunteer.getPincode(),
                volunteer.getSkills(),
                volunteer.getInterests(),
                volunteer.getAvailability(),
                volunteer.getPreferredCauses(),
                volunteer.getExperienceLevel(),
                volunteer.getEducation(),
                volunteer.getOccupation(),
                volunteer.getLinkedinUrl(),
                volunteer.getEmergencyContactName(),
                volunteer.getEmergencyContactPhone(),
                volunteer.getCreatedAt(),
                volunteer.getUpdatedAt()
        );
    }

    private ActivityResponse toActivityResponse(VolunteerEnrollment enrollment) {
        return new ActivityResponse(
                enrollment.getId(),
                enrollment.getProject() != null ? enrollment.getProject().getId() : null,
                enrollment.getProject() != null ? enrollment.getProject().getTitle() : null,
                enrollment.getStatus() != null ? enrollment.getStatus().name() : null,
                enrollment.getHoursContributed(),
                enrollment.getEnrollmentDate()
        );
    }
}
