package com.impacthub.backend.repository;

import com.impacthub.backend.entity.VolunteerEnrollment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VolunteerEnrollmentRepository extends JpaRepository<VolunteerEnrollment, Long> {

    List<VolunteerEnrollment> findByVolunteer_Id(Long volunteerId);

    List<VolunteerEnrollment> findByProject_Id(Long projectId);

    List<VolunteerEnrollment> findByProject_Ngo_Id(Long ngoId);

    void deleteByProject_Id(Long projectId);

    void deleteByProject_Ngo_Id(Long ngoId);

    Optional<VolunteerEnrollment> findByVolunteer_IdAndProject_Id(Long volunteerId, Long projectId);

    boolean existsByProject_IdAndVolunteer_Id(Long projectId, Long volunteerId);

    long countByVolunteer_Id(Long volunteerId);

    long countByVolunteer_IdAndStatus(Long volunteerId, VolunteerEnrollment.EnrollmentStatus status);

    @Query("SELECT COALESCE(SUM(e.hoursContributed), 0) FROM VolunteerEnrollment e WHERE e.volunteer.id = :volunteerId")
    Long sumHoursByVolunteerId(@Param("volunteerId") Long volunteerId);
}
