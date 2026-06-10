package com.emergency.repository;

import com.emergency.model.DistressCall;
import com.emergency.model.DistressCall.CallStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DistressCallRepository extends JpaRepository<DistressCall, Long> {
    List<DistressCall> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT d FROM DistressCall d LEFT JOIN d.user u WHERE " +
           "(:type IS NULL OR d.disasterType.slug = :type) AND " +
           "(:status IS NULL OR d.status = :status) AND " +
           "(:q IS NULL OR LOWER(d.description) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(d.locationName) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(u.name) LIKE LOWER(CONCAT('%', :q, '%'))) AND " +
           "(:dateFrom IS NULL OR d.createdAt >= :dateFrom) AND " +
           "(:dateTo IS NULL OR d.createdAt <= :dateTo) " +
           "ORDER BY d.urgencyScore DESC, d.createdAt DESC")
    List<DistressCall> findByFilters(@Param("type") String type, @Param("status") CallStatus status,
                                     @Param("q") String q, @Param("dateFrom") java.time.LocalDateTime dateFrom,
                                     @Param("dateTo") java.time.LocalDateTime dateTo);

    long countByStatus(DistressCall.CallStatus status);

    @Query("SELECT d.disasterType.slug, COUNT(d) FROM DistressCall d GROUP BY d.disasterType.slug")
    List<Object[]> countByDisasterType();

    List<DistressCall> findTop10ByOrderByCreatedAtDesc();
}
