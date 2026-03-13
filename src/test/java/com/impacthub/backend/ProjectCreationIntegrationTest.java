package com.impacthub.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.impacthub.backend.entity.NGO;
import com.impacthub.backend.repository.NGORepository;
import com.impacthub.backend.service.EmailService;
import com.impacthub.backend.service.JwtService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProjectCreationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NGORepository ngoRepository;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private EmailService emailService;

    @Test
    void approvedNgoCanCreateProjectWithIsoDatePayload() throws Exception {
        String ngoToken = registerApproveAndLoginNgo();

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + ngoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"Community Garden",
                                  "description":"Build a local food garden",
                                  "fundingGoal":25000,
                                  "status":"ongoing",
                                  "startDate":"2026-03-14",
                                  "endDate":"2026-04-14",
                                  "beneficiaries":"",
                                  "volunteersNeeded":"",
                                  "requiredResources":["tools","seeds"]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Community Garden"))
                .andExpect(jsonPath("$.status").value("ONGOING"))
                .andExpect(jsonPath("$.startDate").value("2026-03-14"))
                .andExpect(jsonPath("$.endDate").value("2026-04-14"))
                .andExpect(jsonPath("$.beneficiaries").value(0))
                .andExpect(jsonPath("$.volunteersNeeded").value(0));
    }

    @Test
    void approvedNgoCanCreateProjectWithDayMonthYearDatePayload() throws Exception {
        String ngoToken = registerApproveAndLoginNgo();

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + ngoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"River Cleanup",
                                  "fundingGoal":18000,
                                  "startDate":"14-03-2026",
                                  "endDate":"28-03-2026"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("River Cleanup"))
                .andExpect(jsonPath("$.startDate").value("2026-03-14"))
                .andExpect(jsonPath("$.endDate").value("2026-03-28"));
    }

    private String registerApproveAndLoginNgo() throws Exception {
        String email = uniqueEmail();
        String password = "Ngo@12345";
        String registrationNumber = uniqueRegistrationNumber();

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
                .andExpect(status().isOk());

        NGO ngo = ngoRepository.findByRegistrationNumber(registrationNumber).orElseThrow();

        mockMvc.perform(post("/api/admin/ngos/{ngoId}/approve", ngo.getId())
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email":"%s",
                                  "password":"%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        return loginResponse.split("\"token\":\"")[1].split("\"")[0];
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
