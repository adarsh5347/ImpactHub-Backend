package com.impacthub.backend;

import com.impacthub.backend.entity.NGO;
import com.impacthub.backend.repository.NGORepository;
import com.impacthub.backend.service.EmailService;
import com.impacthub.backend.service.JwtService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminNgoWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NGORepository ngoRepository;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private EmailService emailService;

    @Test
    void ngoCannotLoginBeforeApproval() throws Exception {
        String email = uniqueEmail();
        String password = "Ngo@12345";
        registerNgo(email, password, uniqueRegistrationNumber());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email":"%s",
                                  "password":"%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NGO_PENDING_APPROVAL"))
                .andExpect(jsonPath("$.ngoStatus").value("PENDING"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void pendingNgoAppearsInAdminList() throws Exception {
        String email = uniqueEmail();
        registerNgo(email, "Ngo@12345", uniqueRegistrationNumber());

        mockMvc.perform(get("/api/admin/ngos")
                        .param("status", "PENDING")
                        .param("search", email)
                        .param("page", "1")
                        .param("limit", "10")
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.data[0].approvalStatus").value("PENDING"))
                .andExpect(jsonPath("$.data[0].email").value(email));
    }

    @Test
    void approvalTriggersEmailAndEnablesLogin() throws Exception {
        String email = uniqueEmail();
        String password = "Ngo@12345";
        String registrationNumber = uniqueRegistrationNumber();
        registerNgo(email, password, registrationNumber);

        NGO ngo = ngoRepository.findByRegistrationNumber(registrationNumber).orElseThrow();

        mockMvc.perform(post("/api/admin/ngos/{ngoId}/approve", ngo.getId())
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvalStatus").value("APPROVED"));

        verify(emailService, times(1))
                .sendNgoApprovedEmail(ArgumentMatchers.eq(email), ArgumentMatchers.anyString());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email":"%s",
                                  "password":"%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.userType").value("NGO"));
    }

    @Test
    void rejectionBlocksLoginAndReturnsReason() throws Exception {
        String email = uniqueEmail();
        String password = "Ngo@12345";
        String reason = "Registration documents incomplete";
        String registrationNumber = uniqueRegistrationNumber();
        registerNgo(email, password, registrationNumber);

        NGO ngo = ngoRepository.findByRegistrationNumber(registrationNumber).orElseThrow();

        mockMvc.perform(post("/api/admin/ngos/{ngoId}/reject", ngo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reason":"%s"
                                }
                                """.formatted(reason))
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvalStatus").value("REJECTED"))
                .andExpect(jsonPath("$.rejectedReason").value(reason));

        verify(emailService, times(1))
                .sendNgoRejectedEmail(ArgumentMatchers.eq(email), ArgumentMatchers.anyString(), ArgumentMatchers.eq(reason));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email":"%s",
                                  "password":"%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NGO_REJECTED"))
                .andExpect(jsonPath("$.ngoStatus").value("REJECTED"))
                .andExpect(jsonPath("$.rejectionReason").value(reason));
    }

    private void registerNgo(String email, String password, String registrationNumber) throws Exception {
        mockMvc.perform(post("/api/auth/register/ngo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email":"%s",
                                  "password":"%s",
                                  "ngoName":"Hope Foundation",
                                  "registrationNumber":"%s"
                                }
                                """.formatted(email, password, registrationNumber)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isEmpty())
                .andExpect(jsonPath("$.userType").value("NGO"));
    }

    private String adminToken() {
        return jwtService.generateAdminToken("admin@impacthub.local");
    }

    private String uniqueEmail() {
        return "ngo-" + UUID.randomUUID() + "@example.org";
    }

    private String uniqueRegistrationNumber() {
        return "REG-" + UUID.randomUUID();
    }
}
