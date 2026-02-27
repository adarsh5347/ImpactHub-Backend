package com.impacthub.backend.service;

import com.impacthub.backend.dto.request.ProjectCreateRequest;
import com.impacthub.backend.dto.request.ProjectUpdateRequest;
import com.impacthub.backend.dto.response.EnrollmentResponse;
import com.impacthub.backend.dto.response.ProjectResponse;
import com.impacthub.backend.entity.ApprovalStatus;
import com.impacthub.backend.entity.NGO;
import com.impacthub.backend.entity.Project;
import com.impacthub.backend.entity.User;
import com.impacthub.backend.entity.Volunteer;
import com.impacthub.backend.entity.VolunteerEnrollment;
import com.impacthub.backend.repository.NGORepository;
import com.impacthub.backend.repository.ProjectRepository;
import com.impacthub.backend.repository.UserRepository;
import com.impacthub.backend.repository.VolunteerEnrollmentRepository;
import com.impacthub.backend.repository.VolunteerRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final NGORepository ngoRepository;
    private final VolunteerRepository volunteerRepository;
    private final VolunteerEnrollmentRepository volunteerEnrollmentRepository;

    @Transactional
    public ProjectResponse createProject(ProjectCreateRequest request, String currentEmail) {
        NGO ownerNgo = resolveNgoByEmail(currentEmail);

        Project project = new Project();
        project.setNgo(ownerNgo);
        project.setTitle(request.getTitle());
        project.setDescription(request.getDescription());
        project.setObjectives(request.getObjectives());
        project.setCause(request.getCause());
        project.setLocation(request.getLocation());
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }
        project.setFundingGoal(request.getFundingGoal());
        project.setFundsRaised(BigDecimal.ZERO);
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setBeneficiaries(request.getBeneficiaries() != null ? request.getBeneficiaries() : 0);
        project.setImageUrl(request.getImageUrl());
        project.setRequiredResources(request.getRequiredResources());
        project.setVolunteersNeeded(request.getVolunteersNeeded() != null ? request.getVolunteersNeeded() : 0);
        project.setVolunteersEnrolled(0);

        Project saved = projectRepository.save(project);
        return toProjectResponse(saved);
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        return toProjectResponse(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> listProjects(
            Project.ProjectStatus status,
            String cause,
            String location,
            Long ngoId
    ) {
        List<Project> projects;
        if (ngoId != null) {
            projects = projectRepository.findByNgoId(ngoId);
        } else if (status != null) {
            projects = projectRepository.findByStatus(status);
        } else if (cause != null) {
            projects = projectRepository.findByCause(cause);
        } else if (location != null) {
            projects = projectRepository.findByLocation(location);
        } else {
            projects = projectRepository.findAll();
        }

        return projects.stream().map(this::toProjectResponse).toList();
    }

    @Transactional
    public ProjectResponse updateProject(Long id, ProjectUpdateRequest request, String currentEmail) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        validateOwnership(project, currentEmail);

        applyProjectUpdates(project, request);

        Project saved = projectRepository.save(project);
        return toProjectResponse(saved);
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectForNgo(Long ngoId, Long projectId, String currentEmail) {
        NGO ngo = resolveNgoByEmail(currentEmail);
        if (!ngo.getId().equals(ngoId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only view projects in your own NGO");
        }

        Project project = projectRepository.findByIdAndNgoId(projectId, ngoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found for this NGO"));
        return toProjectResponse(project);
    }

    @Transactional
    public ProjectResponse updateProjectForNgo(Long ngoId, Long projectId, ProjectUpdateRequest request, String currentEmail) {
        NGO ngo = resolveNgoByEmail(currentEmail);
        if (!ngo.getId().equals(ngoId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update projects in your own NGO");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        if (project.getNgo() == null || !ngoId.equals(project.getNgo().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Project does not belong to your NGO");
        }

        applyProjectUpdates(project, request);

        Project saved = projectRepository.save(project);
        return toProjectResponse(saved);
    }

    private void applyProjectUpdates(Project project, ProjectUpdateRequest request) {
        if (request.getTitle() != null) project.setTitle(request.getTitle());
        if (request.getDescription() != null) project.setDescription(request.getDescription());
        if (request.getObjectives() != null) project.setObjectives(request.getObjectives());
        if (request.getCause() != null) project.setCause(request.getCause());
        if (request.getLocation() != null) project.setLocation(request.getLocation());
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
            if (request.getStatus() == Project.ProjectStatus.COMPLETED && project.getCompletedAt() == null) {
                project.setCompletedAt(LocalDateTime.now());
            } else if (request.getStatus() != Project.ProjectStatus.COMPLETED) {
                project.setCompletedAt(null);
            }
        }
        if (request.getFundingGoal() != null) project.setFundingGoal(request.getFundingGoal());
        if (request.getFundsRaised() != null) project.setFundsRaised(request.getFundsRaised());
        if (request.getStartDate() != null) project.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) project.setEndDate(request.getEndDate());
        if (request.getBeneficiaries() != null) project.setBeneficiaries(request.getBeneficiaries());
        if (request.getImageUrl() != null) project.setImageUrl(request.getImageUrl());
        if (request.getRequiredResources() != null) project.setRequiredResources(request.getRequiredResources());
        if (request.getVolunteersNeeded() != null) project.setVolunteersNeeded(request.getVolunteersNeeded());
        if (request.getVolunteersEnrolled() != null) project.setVolunteersEnrolled(request.getVolunteersEnrolled());
    }

    @Transactional
    public void deleteProject(Long id, String currentEmail) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        validateOwnership(project, currentEmail);
        projectRepository.delete(project);
    }

    @Transactional
    public EnrollmentResponse enrollVolunteer(Long projectId, String volunteerEmail) {
        // Get the project
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        // Get the volunteer
        User user = userRepository.findByEmail(volunteerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        
        Volunteer volunteer = volunteerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Only volunteer accounts can enroll in projects"));

        // Check if already enrolled
        Optional<VolunteerEnrollment> existing = volunteerEnrollmentRepository
                .findByVolunteer_IdAndProject_Id(volunteer.getId(), projectId);
        
        if (existing.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You are already enrolled in this project");
        }

        // Create enrollment
        VolunteerEnrollment enrollment = new VolunteerEnrollment();
        enrollment.setVolunteer(volunteer);
        enrollment.setProject(project);
        enrollment.setStatus(VolunteerEnrollment.EnrollmentStatus.ACTIVE);
        enrollment.setHoursContributed(0);
        
        // Save enrollment
        VolunteerEnrollment savedEnrollment = volunteerEnrollmentRepository.save(enrollment);

        // Increment project volunteer count
        project.setVolunteersEnrolled((project.getVolunteersEnrolled() != null ? project.getVolunteersEnrolled() : 0) + 1);
        projectRepository.save(project);

        return new EnrollmentResponse(
            "Successfully enrolled in the project",
            savedEnrollment.getId(),
            project.getId(),
            volunteer.getId(),
            savedEnrollment.getStatus() != null ? savedEnrollment.getStatus().name() : null,
            savedEnrollment.getEnrollmentDate()
        );
        }

        @Transactional(readOnly = true)
        public boolean isVolunteerEnrolled(Long projectId, String volunteerEmail) {
        User user = userRepository.findByEmail(volunteerEmail)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Volunteer volunteer = volunteerRepository.findByUserId(user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Only volunteer accounts can check enrollment"));
        return volunteerEnrollmentRepository.existsByProject_IdAndVolunteer_Id(projectId, volunteer.getId());
    }

    @Transactional
    public ProjectResponse markProjectCompleted(Long ngoId, Long projectId, String currentEmail) {
        NGO ngo = resolveNgoByEmail(currentEmail);
        if (!ngo.getId().equals(ngoId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only complete projects in your own NGO");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        if (project.getNgo() == null || !ngoId.equals(project.getNgo().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Project does not belong to your NGO");
        }

        if (project.getStatus() != Project.ProjectStatus.COMPLETED) {
            project.setStatus(Project.ProjectStatus.COMPLETED);
        }
        if (project.getCompletedAt() == null) {
            project.setCompletedAt(LocalDateTime.now());
        }

        Project saved = projectRepository.save(project);
        return toProjectResponse(saved);
    }

    private NGO resolveNgoByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        NGO ngo = ngoRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Only NGO accounts can create projects"));
        if (ngo.getApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "NGO must be approved to create projects");
        }
        return ngo;
    }

    private void validateOwnership(Project project, String currentEmail) {
        String ownerEmail = project.getNgo() != null && project.getNgo().getUser() != null
                ? project.getNgo().getUser().getEmail()
                : null;
        if (ownerEmail == null || !ownerEmail.equalsIgnoreCase(currentEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only modify your own NGO projects");
        }
    }

    private ProjectResponse toProjectResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getNgo() != null ? project.getNgo().getId() : null,
                project.getNgo() != null ? project.getNgo().getNgoName() : null,
                project.getTitle(),
                project.getDescription(),
                project.getObjectives(),
                project.getCause(),
                project.getLocation(),
                project.getStatus(),
                project.getFundingGoal(),
                project.getFundsRaised(),
                project.getStartDate(),
                project.getEndDate(),
                project.getBeneficiaries(),
                project.getImageUrl(),
                project.getRequiredResources(),
                project.getVolunteersNeeded(),
                project.getVolunteersEnrolled(),
                project.getCompletedAt(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
