package com.impacthub.backend.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectEnrollmentGroupResponse {
    private Long projectId;
    private String projectTitle;
    private String location;
    private String status;
    private List<ProjectEnrollmentVolunteerResponse> enrolledVolunteers;
}
