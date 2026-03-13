package com.impacthub.backend.dto.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.impacthub.backend.config.JacksonConfig;
import java.time.LocalDate;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.junit.jupiter.api.Test;

class ProjectRequestDateDeserializationTest {
    private final ObjectMapper objectMapper = createObjectMapper();

    @Test
    void createRequestAcceptsIsoDates() throws Exception {
        String payload = """
            {
              "title": "Tree Plantation",
              "fundingGoal": 1000,
              "startDate": "2026-03-14",
              "endDate": "2026-03-20"
            }
            """;

        ProjectCreateRequest request = objectMapper.readValue(payload, ProjectCreateRequest.class);

        assertEquals(LocalDate.of(2026, 3, 14), request.getStartDate());
        assertEquals(LocalDate.of(2026, 3, 20), request.getEndDate());
    }

    @Test
    void createRequestAcceptsDayMonthYearDates() throws Exception {
        String payload = """
            {
              "title": "Tree Plantation",
              "fundingGoal": 1000,
              "startDate": "14-03-2026",
              "endDate": "20-03-2026"
            }
            """;

        ProjectCreateRequest request = objectMapper.readValue(payload, ProjectCreateRequest.class);

        assertEquals(LocalDate.of(2026, 3, 14), request.getStartDate());
        assertEquals(LocalDate.of(2026, 3, 20), request.getEndDate());
    }

    @Test
    void createRequestAcceptsEmptyOptionalFieldsAndCaseInsensitiveStatus() throws Exception {
        String payload = """
            {
              "title": "Tree Plantation",
              "fundingGoal": 1000,
              "status": "ongoing",
              "beneficiaries": "",
              "volunteersNeeded": "",
              "startDate": "",
              "endDate": ""
            }
            """;

        ProjectCreateRequest request = objectMapper.readValue(payload, ProjectCreateRequest.class);

        assertEquals("Tree Plantation", request.getTitle());
        assertEquals(1000, request.getFundingGoal().intValueExact());
        assertEquals(com.impacthub.backend.entity.Project.ProjectStatus.ONGOING, request.getStatus());
        assertNull(request.getBeneficiaries());
        assertNull(request.getVolunteersNeeded());
        assertNull(request.getStartDate());
        assertNull(request.getEndDate());
    }

    private ObjectMapper createObjectMapper() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.modules(new JavaTimeModule());
        new JacksonConfig().jacksonCustomizer().customize(builder);
        return builder.build();
    }
}
