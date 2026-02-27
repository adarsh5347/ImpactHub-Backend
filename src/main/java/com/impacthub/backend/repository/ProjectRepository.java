package com.impacthub.backend.repository;

import com.impacthub.backend.entity.Project;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByNgoId(Long ngoId);
    java.util.Optional<Project> findByIdAndNgoId(Long id, Long ngoId);
    void deleteByNgoId(Long ngoId);
    List<Project> findByStatus(Project.ProjectStatus status);
    List<Project> findByCause(String cause);
    List<Project> findByLocation(String location);
    long countByStatus(Project.ProjectStatus status);
    boolean existsByNgoId(Long ngoId);
}
