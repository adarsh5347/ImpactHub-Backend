package com.impacthub.backend.service;

import com.impacthub.backend.dto.request.LoginRequest;
import com.impacthub.backend.dto.request.NGORegistrationRequest;
import com.impacthub.backend.dto.request.VolunteerRegistrationRequest;
import com.impacthub.backend.dto.response.AuthMeResponse;
import com.impacthub.backend.dto.response.AuthResponse;
import com.impacthub.backend.entity.ApprovalStatus;
import com.impacthub.backend.entity.NGO;
import com.impacthub.backend.entity.User;
import com.impacthub.backend.entity.Volunteer;
import com.impacthub.backend.exception.NgoLoginBlockedException;
import com.impacthub.backend.repository.NGORepository;
import com.impacthub.backend.repository.UserRepository;
import com.impacthub.backend.repository.VolunteerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final VolunteerRepository volunteerRepository;
    private final NGORepository ngoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password.hash}")
    private String adminPasswordHash;

    @Transactional
    public AuthResponse registerVolunteer(VolunteerRegistrationRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setUserType(User.UserType.VOLUNTEER);
        user.setIsActive(true);

        User savedUser = userRepository.save(user);

        Volunteer volunteer = new Volunteer();
        volunteer.setUser(savedUser);
        volunteer.setFullName(request.getFullName());
        volunteer.setPhone(request.getPhone());
        volunteer.setDateOfBirth(request.getDateOfBirth());
        volunteer.setGender(request.getGender()); // DTO must be Volunteer.Gender
        volunteer.setAddress(request.getAddress());
        volunteer.setCity(request.getCity());
        volunteer.setState(request.getState());
        volunteer.setPincode(request.getPincode());
        volunteer.setSkills(request.getSkills());
        volunteer.setInterests(request.getInterests());
        volunteer.setAvailability(request.getAvailability());
        volunteer.setPreferredCauses(request.getPreferredCauses());
        volunteer.setExperienceLevel(request.getExperienceLevel()); // DTO must be Volunteer.ExperienceLevel
        volunteer.setEducation(request.getEducation());
        volunteer.setOccupation(request.getOccupation());
        volunteer.setLinkedinUrl(request.getLinkedinUrl());
        volunteer.setEmergencyContactName(request.getEmergencyContactName());
        volunteer.setEmergencyContactPhone(request.getEmergencyContactPhone());

        volunteerRepository.save(volunteer);
        try {
            emailService.sendWelcomeEmail(savedUser.getEmail(), volunteer.getFullName());
        } catch (Exception ex) {
            log.error("Volunteer registered but welcome email failed for {}", savedUser.getEmail(), ex);
        }

        String token = jwtService.generateToken(savedUser.getEmail(), savedUser.getUserType().name());

        return new AuthResponse(
                token,
                savedUser.getUserType().name(),
            savedUser.getId(),
            volunteer.getFullName(),
                savedUser.getEmail(),
                "Volunteer registered successfully"
        );
    }

    @Transactional
    public AuthResponse registerNGO(NGORegistrationRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        if (ngoRepository.findByRegistrationNumber(request.getRegistrationNumber()).isPresent()) {
            throw new RuntimeException("Registration number already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setUserType(User.UserType.NGO);
        user.setIsActive(true);

        User savedUser = userRepository.save(user);

        NGO ngo = new NGO();
        ngo.setUser(savedUser);
        ngo.setNgoName(request.getNgoName());
        ngo.setRegistrationNumber(request.getRegistrationNumber());
        ngo.setYearFounded(request.getYearFounded());
        ngo.setNgoType(request.getNgoType()); // DTO must be NGO.NGOType
        ngo.setCauseFocus(request.getCauseFocus());
        ngo.setMission(request.getMission());
        ngo.setVision(request.getVision());
        ngo.setWebsiteUrl(request.getWebsiteUrl());
        ngo.setPhone(request.getPhone());

        // If DTO has ngoEmail, you can use it:
        ngo.setEmail(request.getNgoEmail() != null ? request.getNgoEmail() : request.getEmail());

        ngo.setAddress(request.getAddress());
        ngo.setCity(request.getCity());
        ngo.setState(request.getState());
        ngo.setPincode(request.getPincode());
        ngo.setPanNumber(request.getPanNumber());
        ngo.setTanNumber(request.getTanNumber());
        ngo.setGstNumber(request.getGstNumber());

        ngo.setIs12aRegistered(request.getIs12aRegistered());
        ngo.setIs80gRegistered(request.getIs80gRegistered());
        ngo.setFcraRegistered(request.getFcraRegistered());

        ngo.setBankAccountNumber(request.getBankAccountNumber());
        ngo.setBankName(request.getBankName());
        ngo.setBankIfsc(request.getBankIfsc());
        ngo.setBankBranch(request.getBankBranch());

        ngo.setPrimaryContactName(request.getPrimaryContactName());
        ngo.setPrimaryContactDesignation(request.getPrimaryContactDesignation());
        ngo.setPrimaryContactPhone(request.getPrimaryContactPhone());
        ngo.setPrimaryContactEmail(request.getPrimaryContactEmail());
        ngo.setLogoUrl(request.getLogoUrl());
        ngo.setLogoPublicId(request.getLogoPublicId());
        ngo.setCoverImageUrl(request.getCoverImageUrl());
        ngo.setCoverImagePublicId(request.getCoverImagePublicId());

        ngo.setIsVerified(false);
        ngo.setApprovalStatus(ApprovalStatus.PENDING);

        ngoRepository.save(ngo);

        return new AuthResponse(
                null,
                savedUser.getUserType().name(),
                savedUser.getEmail(),
                "NGO registered successfully. Await admin approval before login."
        );
    }

    public AuthResponse login(LoginRequest request) {
        if (isAdminLogin(request.getEmail(), request.getPassword())) {
            String token = jwtService.generateAdminToken(adminEmail.trim());
            return new AuthResponse(
                    token,
                    User.UserType.ADMIN.name(),
                    null,
                    "Admin",
                    adminEmail.trim(),
                    "Login successful"
            );
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new RuntimeException("Account is deactivated");
        }

        if (user.getUserType() == User.UserType.NGO) {
            NGO ngo = ngoRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("NGO profile not found"));
            ApprovalStatus status = ngo.getApprovalStatus() != null ? ngo.getApprovalStatus() : ApprovalStatus.PENDING;
            if (status != ApprovalStatus.APPROVED) {
                throw blockedNgoException(ngo, status);
            }
        }

        String token = jwtService.generateToken(user.getEmail(), user.getUserType().name());

        String fullName = null;
        if (user.getUserType() == User.UserType.VOLUNTEER) {
            fullName = volunteerRepository.findByUserId(user.getId())
                    .map(Volunteer::getFullName)
                    .orElse(user.getEmail());
        } else if (user.getUserType() == User.UserType.NGO) {
            fullName = ngoRepository.findByUserId(user.getId())
                    .map(NGO::getNgoName)
                    .orElse(user.getEmail());
        }

        return new AuthResponse(
                token,
                user.getUserType().name(),
                user.getId(),
                fullName,
                user.getEmail(),
                "Login successful"
        );
    }

    @Transactional(readOnly = true)
    public AuthMeResponse getCurrentUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String fullName;
        if (user.getUserType() == User.UserType.VOLUNTEER) {
            fullName = volunteerRepository.findByUserId(user.getId())
                    .map(Volunteer::getFullName)
                    .orElse(user.getEmail());
        } else if (user.getUserType() == User.UserType.NGO) {
            fullName = ngoRepository.findByUserId(user.getId())
                    .map(NGO::getNgoName)
                    .orElse(user.getEmail());
        } else {
            fullName = "Admin";
        }

        return new AuthMeResponse(user.getId(), fullName, user.getEmail(), user.getUserType().name());
    }

    private boolean isAdminLogin(String email, String rawPassword) {
        if (email == null || rawPassword == null || adminEmail == null || adminEmail.isBlank()) {
            return false;
        }

        if (!adminEmail.trim().equalsIgnoreCase(email.trim())) {
            return false;
        }

        return adminPasswordHash != null
                && !adminPasswordHash.isBlank()
                && passwordEncoder.matches(rawPassword, adminPasswordHash.trim());
    }

    private NgoLoginBlockedException blockedNgoException(NGO ngo, ApprovalStatus status) {
        return switch (status) {
            case PENDING -> new NgoLoginBlockedException(
                    "NGO_PENDING_APPROVAL",
                    "Your NGO account is pending admin approval.",
                    status,
                    null,
                    null
            );
            case REJECTED -> new NgoLoginBlockedException(
                    "NGO_REJECTED",
                    "Your NGO account has been rejected.",
                    status,
                    ngo.getRejectedReason(),
                    null
            );
            case SUSPENDED -> new NgoLoginBlockedException(
                    "NGO_SUSPENDED",
                    "Your NGO account has been suspended.",
                    status,
                    null,
                    ngo.getSuspendedReason()
            );
            case APPROVED -> new NgoLoginBlockedException(
                    "NGO_ACCESS_ERROR",
                    "Unable to continue NGO login.",
                    status,
                    null,
                    null
            );
        };
    }
}
