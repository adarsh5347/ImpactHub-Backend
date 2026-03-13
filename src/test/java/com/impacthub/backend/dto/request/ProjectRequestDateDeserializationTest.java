package com.impacthub.backend.dto.request;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ProjectRequestDateDeserializationTest {
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

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
}
