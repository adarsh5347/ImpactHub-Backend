package com.impacthub.backend.service;

import com.impacthub.backend.dto.request.NGOUpdateRequest;
import com.impacthub.backend.dto.response.NGOResponse;
import com.impacthub.backend.dto.response.PaginatedResponse;
import com.impacthub.backend.entity.NGO;
import com.impacthub.backend.entity.Project;
import com.impacthub.backend.entity.User;
import com.impacthub.backend.repository.NGORepository;
import com.impacthub.backend.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NGOService {

    private final NGORepository ngoRepository;
    private final ProjectRepository projectRepository;
    private final CloudinaryUploadService cloudinaryUploadService;

    @Transactional(readOnly = true)
    public boolean isNgoOwnedByEmail(Long ngoId, String email) {
        return ngoRepository.findById(ngoId)
                .map(NGO::getUser)
                .map(User::getEmail)
                .map(ownerEmail -> ownerEmail.equalsIgnoreCase(email))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public NGOResponse getNgoById(Long id) {
        NGO ngo = ngoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "NGO not found"));
        return toNgoResponse(ngo);
    }

    @Transactional
    public NGOResponse updateNgo(Long id, NGOUpdateRequest request) {
        NGO ngo = ngoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "NGO not found"));

        if (request.getNgoName() != null) ngo.setNgoName(request.getNgoName());
        if (request.getYearFounded() != null) ngo.setYearFounded(request.getYearFounded());
        if (request.getNgoType() != null) ngo.setNgoType(request.getNgoType());
        if (request.getCauseFocus() != null) ngo.setCauseFocus(request.getCauseFocus());
        if (request.getMission() != null) ngo.setMission(request.getMission());
        if (request.getVision() != null) ngo.setVision(request.getVision());
        if (request.getWebsiteUrl() != null) ngo.setWebsiteUrl(request.getWebsiteUrl());
        if (request.getPhone() != null) ngo.setPhone(request.getPhone());
        if (request.getEmail() != null) ngo.setEmail(request.getEmail());
        if (request.getAddress() != null) ngo.setAddress(request.getAddress());
        if (request.getCity() != null) ngo.setCity(request.getCity());
        if (request.getState() != null) ngo.setState(request.getState());
        if (request.getPincode() != null) ngo.setPincode(request.getPincode());
        if (request.getPanNumber() != null) ngo.setPanNumber(request.getPanNumber());
        if (request.getTanNumber() != null) ngo.setTanNumber(request.getTanNumber());
        if (request.getGstNumber() != null) ngo.setGstNumber(request.getGstNumber());
        if (request.getIs12aRegistered() != null) ngo.setIs12aRegistered(request.getIs12aRegistered());
        if (request.getIs80gRegistered() != null) ngo.setIs80gRegistered(request.getIs80gRegistered());
        if (request.getFcraRegistered() != null) ngo.setFcraRegistered(request.getFcraRegistered());
        if (request.getBankAccountNumber() != null) ngo.setBankAccountNumber(request.getBankAccountNumber());
        if (request.getBankName() != null) ngo.setBankName(request.getBankName());
        if (request.getBankIfsc() != null) ngo.setBankIfsc(request.getBankIfsc());
        if (request.getBankBranch() != null) ngo.setBankBranch(request.getBankBranch());
        if (request.getPrimaryContactName() != null) ngo.setPrimaryContactName(request.getPrimaryContactName());
        if (request.getPrimaryContactDesignation() != null) ngo.setPrimaryContactDesignation(request.getPrimaryContactDesignation());
        if (request.getPrimaryContactPhone() != null) ngo.setPrimaryContactPhone(request.getPrimaryContactPhone());
        if (request.getPrimaryContactEmail() != null) ngo.setPrimaryContactEmail(request.getPrimaryContactEmail());
        if (request.getLogoUrl() != null) ngo.setLogoUrl(request.getLogoUrl());
        if (request.getLogoPublicId() != null) {
            String previousLogoPublicId = ngo.getLogoPublicId();
            if (previousLogoPublicId != null
                    && !previousLogoPublicId.isBlank()
                    && !previousLogoPublicId.equals(request.getLogoPublicId())) {
                cloudinaryUploadService.deleteByPublicId(previousLogoPublicId);
            }
            ngo.setLogoPublicId(request.getLogoPublicId());
        }
        if (request.getCoverImageUrl() != null) ngo.setCoverImageUrl(request.getCoverImageUrl());
        if (request.getCoverImagePublicId() != null) {
            String previousCoverPublicId = ngo.getCoverImagePublicId();
            if (previousCoverPublicId != null
                    && !previousCoverPublicId.isBlank()
                    && !previousCoverPublicId.equals(request.getCoverImagePublicId())) {
                cloudinaryUploadService.deleteByPublicId(previousCoverPublicId);
            }
            ngo.setCoverImagePublicId(request.getCoverImagePublicId());
        }

        NGO saved = ngoRepository.save(ngo);
        return toNgoResponse(saved);
    }

    @Transactional
    public void deactivateNgo(Long id) {
        NGO ngo = ngoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "NGO not found"));

        if (ngo.getUser() != null) {
            ngo.getUser().setIsActive(false);
        }
    }

    @Transactional(readOnly = true)
    public List<Project> getProjectsByNgo(Long ngoId) {
        if (!ngoRepository.existsById(ngoId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "NGO not found");
        }
        return projectRepository.findByNgoId(ngoId);
    }

    @Transactional(readOnly = true)
    public List<NGOResponse> listNgos(String city, String state, String cause, Boolean verified) {
        List<NGO> ngos;
        if (city != null) {
            ngos = ngoRepository.findByCity(city);
        } else if (state != null) {
            ngos = ngoRepository.findByState(state);
        } else if (cause != null) {
            ngos = ngoRepository.findByCause(cause);
        } else if (verified != null) {
            ngos = ngoRepository.findByIsVerified(verified);
        } else {
            ngos = ngoRepository.findAll();
        }

        return ngos.stream().map(this::toNgoResponse).toList();
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<NGOResponse> listNgosPaginated(
            String city,
            String state,
            String cause,
            Boolean verified,
            String search,
            int page,
            int limit
    ) {
        int safePage = Math.max(page, 1);
        int safeLimit = Math.max(limit, 1);
        Pageable pageable = PageRequest.of(safePage - 1, safeLimit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<NGO> ngoPage = ngoRepository.findPublicNgos(city, state, cause, verified, search, pageable);
        return new PaginatedResponse<>(
                ngoPage.getContent().stream().map(this::toNgoResponse).toList(),
                ngoPage.getTotalElements(),
                safePage,
                safeLimit
        );
    }

    private NGOResponse toNgoResponse(NGO ngo) {
        return new NGOResponse(
                ngo.getId(),
                ngo.getUser() != null ? ngo.getUser().getEmail() : null,
                ngo.getNgoName(),
                ngo.getRegistrationNumber(),
                ngo.getYearFounded(),
                ngo.getNgoType(),
                ngo.getCauseFocus(),
                ngo.getMission(),
                ngo.getVision(),
                ngo.getWebsiteUrl(),
                ngo.getPhone(),
                ngo.getEmail(),
                ngo.getAddress(),
                ngo.getCity(),
                ngo.getState(),
                ngo.getPincode(),
                ngo.getPanNumber(),
                ngo.getTanNumber(),
                ngo.getGstNumber(),
                ngo.getIs12aRegistered(),
                ngo.getIs80gRegistered(),
                ngo.getFcraRegistered(),
                ngo.getBankAccountNumber(),
                ngo.getBankName(),
                ngo.getBankIfsc(),
                ngo.getBankBranch(),
                ngo.getPrimaryContactName(),
                ngo.getPrimaryContactDesignation(),
                ngo.getPrimaryContactPhone(),
                ngo.getPrimaryContactEmail(),
                ngo.getIsVerified(),
                ngo.getVerificationDate(),
                ngo.getActiveProjects(),
                ngo.getCompletedProjects(),
                ngo.getLogoUrl(),
                ngo.getLogoPublicId(),
                ngo.getCoverImageUrl(),
                ngo.getCoverImagePublicId(),
                ngo.getCreatedAt(),
                ngo.getUpdatedAt()
        );
    }
}
