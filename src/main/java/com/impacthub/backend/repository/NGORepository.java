package com.impacthub.backend.repository;

import com.impacthub.backend.entity.ApprovalStatus;
import com.impacthub.backend.entity.NGO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface NGORepository extends JpaRepository<NGO, Long> {
    Optional<NGO> findByUserId(Long userId);
    Optional<NGO> findByRegistrationNumber(String registrationNumber);
    List<NGO> findByIsVerified(Boolean isVerified);
    List<NGO> findByCity(String city);
    List<NGO> findByState(String state);

    @Query("SELECT n FROM NGO n WHERE :cause MEMBER OF n.causeFocus")
    List<NGO> findByCause(String cause);

    @Query("""
            SELECT n FROM NGO n
            WHERE n.approvalStatus = :status
              AND (
                   :search IS NULL OR :search = ''
                   OR LOWER(n.ngoName) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(n.email) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(n.user.email) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """)
    Page<NGO> findForAdminByStatusAndSearch(
            @Param("status") ApprovalStatus status,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("""
            SELECT DISTINCT n FROM NGO n
            WHERE (:city IS NULL OR LOWER(n.city) = LOWER(:city))
              AND (:state IS NULL OR LOWER(n.state) = LOWER(:state))
              AND (:verified IS NULL OR n.isVerified = :verified)
              AND (:cause IS NULL OR :cause MEMBER OF n.causeFocus)
              AND (
                    :search IS NULL OR :search = ''
                    OR LOWER(n.ngoName) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(n.email) LIKE LOWER(CONCAT('%', :search, '%'))
                 )
            """)
    Page<NGO> findPublicNgos(
            @Param("city") String city,
            @Param("state") String state,
            @Param("cause") String cause,
            @Param("verified") Boolean verified,
            @Param("search") String search,
            Pageable pageable
    );

    long countByApprovalStatus(ApprovalStatus approvalStatus);
}
