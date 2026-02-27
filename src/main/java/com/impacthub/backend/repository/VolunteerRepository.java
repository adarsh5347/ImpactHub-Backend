package com.impacthub.backend.repository;

import com.impacthub.backend.entity.User;
import com.impacthub.backend.entity.Volunteer;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VolunteerRepository extends JpaRepository<Volunteer, Long> {
    Optional<Volunteer> findByUserId(Long userId);
    @EntityGraph(attributePaths = {"preferredCauses", "interests", "skills"})
    Optional<Volunteer> findByUser_Email(String email);
    List<Volunteer> findByCity(String city);
    List<Volunteer> findByState(String state);

    @Query("""
            SELECT v
            FROM Volunteer v
            JOIN v.user u
            WHERE u.userType = :userType
              AND u.isActive = true
              AND (
                :search IS NULL
                OR :search = ''
                OR LOWER(v.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(COALESCE(v.phone, '')) LIKE LOWER(CONCAT('%', :search, '%'))
            )
            """)
        Page<Volunteer> findForAdmin(@Param("search") String search, @Param("userType") User.UserType userType, Pageable pageable);

        @Query("""
            SELECT COUNT(v)
            FROM Volunteer v
            JOIN v.user u
            WHERE u.userType = :userType
              AND u.isActive = true
            """)
        long countActiveByUserType(@Param("userType") User.UserType userType);
}
