package com.impacthub.backend.service;

import com.impacthub.backend.dto.response.AdminNgoDetailResponse;
import com.impacthub.backend.dto.response.AdminNgoListItemResponse;
import com.impacthub.backend.dto.response.AdminStatsResponse;
import com.impacthub.backend.dto.response.AdminVolunteerListItemResponse;
import com.impacthub.backend.dto.response.PaginatedResponse;
import com.impacthub.backend.entity.ApprovalStatus;
import com.impacthub.backend.entity.NGO;
import com.impacthub.backend.entity.User;
import com.impacthub.backend.entity.Volunteer;
import com.impacthub.backend.repository.NGORepository;
import com.impacthub.backend.repository.ProjectRepository;
import com.impacthub.backend.repository.UserRepository;
import com.impacthub.backend.repository.VolunteerEnrollmentRepository;
import com.impacthub.backend.repository.VolunteerRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final NGORepository ngoRepository;
    private final VolunteerRepository volunteerRepository;
    private final VolunteerEnrollmentRepository volunteerEnrollmentRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public PaginatedResponse<AdminNgoListItemResponse> listNgos(ApprovalStatus status, String search, int page, int limit) {
        int safePage = Math.max(page, 1);
        int safeLimit = Math.max(limit, 1);
        Pageable pageable = PageRequest.of(safePage - 1, safeLimit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<NGO> ngoPage = ngoRepository.findForAdminByStatusAndSearch(status, search, pageable);

        return new PaginatedResponse<>(
                ngoPage.getContent().stream().map(this::toListItem).toList(),
                ngoPage.getTotalElements(),
                safePage,
                safeLimit
        );
    }

    @Transactional(readOnly = true)
    public AdminNgoDetailResponse getNgoDetails(Long ngoId) {
        NGO ngo = ngoRepository.findById(ngoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "NGO not found"));
        return toDetail(ngo);
    }

    @Transactional
    public void deleteNgo(Long ngoId) {
        NGO ngo = ngoRepository.findById(ngoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "NGO not found"));

        String ngoEmail = ngo.getEmail();
        Long linkedUserId = ngo.getUser() != null ? ngo.getUser().getId() : null;

        volunteerEnrollmentRepository.deleteByProject_Ngo_Id(ngoId);
        projectRepository.deleteByNgoId(ngoId);

        ngoRepository.delete(ngo);

        if (linkedUserId != null) {
            userRepository.deleteById(linkedUserId);
        }

        log.info("Admin deleted NGO id={}, email={}", ngoId, ngoEmail);
    }

    @Transactional
    public AdminNgoDetailResponse approveNgo(Long ngoId, String adminEmail) {
        NGO ngo = ngoRepository.findById(ngoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "NGO not found"));

        ngo.setApprovalStatus(ApprovalStatus.APPROVED);
        ngo.setApprovedAt(LocalDateTime.now());
        ngo.setApprovedBy(adminEmail);
        ngo.setReviewedAt(LocalDateTime.now());
        ngo.setReviewedBy(adminEmail);
        ngo.setRejectedAt(null);
        ngo.setRejectedBy(null);
        ngo.setRejectedReason(null);
        ngo.setSuspendedAt(null);
        ngo.setSuspendedBy(null);
        ngo.setSuspendedReason(null);
        ngo.setIsVerified(true);
        ngo.setVerificationDate(LocalDateTime.now());

        NGO saved = ngoRepository.save(ngo);
        try {
            emailService.sendNgoApprovedEmail(saved.getEmail(), saved.getNgoName());
        } catch (MailException ex) {
            log.error("NGO approved but approval email failed for {}", saved.getEmail(), ex);
        }
        return toDetail(saved);
    }

    @Transactional
    public AdminNgoDetailResponse rejectNgo(Long ngoId, String adminEmail, String reason) {
        NGO ngo = ngoRepository.findById(ngoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "NGO not found"));

        ngo.setApprovalStatus(ApprovalStatus.REJECTED);
        ngo.setRejectedAt(LocalDateTime.now());
        ngo.setRejectedBy(adminEmail);
        ngo.setRejectedReason(reason);
        ngo.setReviewedAt(LocalDateTime.now());
        ngo.setReviewedBy(adminEmail);
        ngo.setApprovedAt(null);
        ngo.setApprovedBy(null);
        ngo.setSuspendedAt(null);
        ngo.setSuspendedBy(null);
        ngo.setSuspendedReason(null);
        ngo.setIsVerified(false);
        ngo.setVerificationDate(null);

        NGO saved = ngoRepository.save(ngo);
        try {
            emailService.sendNgoRejectedEmail(saved.getEmail(), saved.getNgoName(), reason);
        } catch (MailException ex) {
            log.error("NGO rejected but rejection email failed for {}", saved.getEmail(), ex);
        }
        return toDetail(saved);
    }

    @Transactional
    public AdminNgoDetailResponse suspendNgo(Long ngoId, String adminEmail, String reason) {
        NGO ngo = ngoRepository.findById(ngoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "NGO not found"));

        ngo.setApprovalStatus(ApprovalStatus.SUSPENDED);
        ngo.setSuspendedAt(LocalDateTime.now());
        ngo.setSuspendedBy(adminEmail);
        ngo.setSuspendedReason(reason);
        ngo.setReviewedAt(LocalDateTime.now());
        ngo.setReviewedBy(adminEmail);
        ngo.setIsVerified(false);

        NGO saved = ngoRepository.save(ngo);
        try {
            emailService.sendNgoSuspendedEmail(saved.getEmail(), saved.getNgoName(), reason);
        } catch (MailException ex) {
            log.error("NGO suspended but suspension email failed for {}", saved.getEmail(), ex);
        }
        return toDetail(saved);
    }

    @Transactional(readOnly = true)
    public AdminStatsResponse getStats() {
        long activeVolunteerCount = volunteerRepository.countActiveByUserType(User.UserType.VOLUNTEER);
        return new AdminStatsResponse(
                ngoRepository.countByApprovalStatus(ApprovalStatus.PENDING),
                ngoRepository.countByApprovalStatus(ApprovalStatus.APPROVED),
                ngoRepository.countByApprovalStatus(ApprovalStatus.REJECTED),
            activeVolunteerCount,
                projectRepository.count()
        );
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<AdminVolunteerListItemResponse> listVolunteers(String search, int page, int limit) {
        int safePage = Math.max(page, 1);
        int safeLimit = Math.max(limit, 1);
        Pageable pageable = PageRequest.of(safePage - 1, safeLimit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Volunteer> volunteerPage = volunteerRepository.findForAdmin(search, User.UserType.VOLUNTEER, pageable);

        return new PaginatedResponse<>(
                volunteerPage.getContent().stream().map(this::toVolunteerListItem).toList(),
                volunteerPage.getTotalElements(),
                safePage,
                safeLimit
        );
    }

    private AdminNgoListItemResponse toListItem(NGO ngo) {
        return new AdminNgoListItemResponse(
                ngo.getId(),
                ngo.getNgoName(),
                ngo.getNgoName(),
                ngo.getEmail(),
                ngo.getPhone(),
                ngo.getCity(),
                ngo.getState(),
            ngo.getLogoUrl(),
                ngo.getCreatedAt(),
                ngo.getApprovalStatus()
        );
    }

    private AdminNgoDetailResponse toDetail(NGO ngo) {
        return new AdminNgoDetailResponse(
                ngo.getId(),
                ngo.getNgoName(),
                ngo.getNgoName(),
                ngo.getNgoName(),
                ngo.getEmail(),
                ngo.getUser() != null ? ngo.getUser().getEmail() : null,
                ngo.getPhone(),
                ngo.getAddress(),
                ngo.getCity(),
                ngo.getState(),
                ngo.getPincode(),
                ngo.getRegistrationNumber(),
                ngo.getYearFounded(),
                ngo.getNgoType() != null ? ngo.getNgoType().name() : null,
                ngo.getCauseFocus(),
                ngo.getMission(),
                ngo.getVision(),
                ngo.getWebsiteUrl(),
                ngo.getPanNumber(),
                ngo.getTanNumber(),
                ngo.getGstNumber(),
                ngo.getIs12aRegistered(),
                ngo.getIs80gRegistered(),
                ngo.getFcraRegistered(),
                ngo.getLogoUrl(),
                ngo.getCoverImageUrl(),
                ngo.getPrimaryContactName(),
                ngo.getPrimaryContactDesignation(),
                ngo.getPrimaryContactPhone(),
                ngo.getPrimaryContactEmail(),
                ngo.getCreatedAt(),
                ngo.getUpdatedAt(),
                ngo.getApprovalStatus(),
                ngo.getApprovedAt(),
                ngo.getApprovedBy(),
                ngo.getRejectedAt(),
                ngo.getRejectedBy(),
                ngo.getRejectedReason(),
                ngo.getSuspendedAt(),
                ngo.getSuspendedBy(),
                ngo.getSuspendedReason(),
                ngo.getReviewedAt(),
                ngo.getReviewedBy()
        );
    }

    private AdminVolunteerListItemResponse toVolunteerListItem(Volunteer volunteer) {
        return new AdminVolunteerListItemResponse(
                volunteer.getId(),
                volunteer.getFullName(),
                volunteer.getUser() != null ? volunteer.getUser().getEmail() : null,
                volunteer.getPhone(),
                volunteer.getCity(),
                volunteer.getState(),
                volunteer.getCreatedAt()
        );
    }
}
