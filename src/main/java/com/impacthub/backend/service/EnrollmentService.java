package com.impacthub.backend.service;

import com.impacthub.backend.dto.response.ProjectEnrollmentGroupResponse;
import com.impacthub.backend.dto.response.ProjectEnrollmentVolunteerResponse;
import com.impacthub.backend.entity.NGO;
import com.impacthub.backend.entity.Project;
import com.impacthub.backend.entity.User;
import com.impacthub.backend.entity.Volunteer;
import com.impacthub.backend.entity.VolunteerEnrollment;
import com.impacthub.backend.repository.NGORepository;
import com.impacthub.backend.repository.ProjectRepository;
import com.impacthub.backend.repository.UserRepository;
import com.impacthub.backend.repository.VolunteerEnrollmentRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final UserRepository userRepository;
    private final NGORepository ngoRepository;
    private final ProjectRepository projectRepository;
    private final VolunteerEnrollmentRepository volunteerEnrollmentRepository;

    @Transactional(readOnly = true)
    public List<ProjectEnrollmentGroupResponse> listEnrollmentsForNgo(Long ngoId, String currentEmail) {
        validateNgoOwnership(ngoId, currentEmail);

        List<VolunteerEnrollment> enrollments = volunteerEnrollmentRepository.findByProject_Ngo_Id(ngoId);
        Map<Long, List<VolunteerEnrollment>> byProject = enrollments.stream()
                .collect(Collectors.groupingBy(enrollment -> enrollment.getProject().getId()));

        return byProject.values().stream()
                .map(projectEnrollments -> {
                    VolunteerEnrollment first = projectEnrollments.getFirst();
                    Project project = first.getProject();
                    return new ProjectEnrollmentGroupResponse(
                            project.getId(),
                            project.getTitle(),
                            project.getLocation(),
                            project.getStatus() != null ? project.getStatus().name() : null,
                            projectEnrollments.stream().map(this::toVolunteerResponse).toList()
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProjectEnrollmentVolunteerResponse> listEnrolledVolunteersForProject(
            Long ngoId,
            Long projectId,
            String currentEmail
    ) {
        validateNgoOwnership(ngoId, currentEmail);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        if (project.getNgo() == null || !ngoId.equals(project.getNgo().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Project does not belong to your NGO");
        }

        return volunteerEnrollmentRepository.findByProject_Id(projectId).stream()
                .map(this::toVolunteerResponse)
                .toList();
    }

    private ProjectEnrollmentVolunteerResponse toVolunteerResponse(VolunteerEnrollment enrollment) {
        Volunteer volunteer = enrollment.getVolunteer();
        String email = volunteer != null && volunteer.getUser() != null ? volunteer.getUser().getEmail() : null;
        return new ProjectEnrollmentVolunteerResponse(
                volunteer != null ? volunteer.getId() : null,
                volunteer != null ? volunteer.getFullName() : null,
                email,
                volunteer != null ? volunteer.getPhone() : null,
                volunteer != null ? volunteer.getCity() : null,
                enrollment.getEnrollmentDate()
        );
    }

    private void validateNgoOwnership(Long ngoId, String email) {
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        NGO ngo = ngoRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Only NGO accounts can view enrollments"));
        if (!ngo.getId().equals(ngoId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only view enrollments for your own NGO");
        }
    }
}
