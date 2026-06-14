package com.emergency.repository;

import com.emergency.model.Response;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResponseRepository extends JpaRepository<Response, Long> {
    List<Response> findByDistressCallIdOrderByCreatedAtDesc(Long distressCallId);
    Optional<Response> findTopByDistressCallIdOrderByCreatedAtDesc(Long distressCallId);
    List<Response> findByRescueCenterId(Long rescueCenterId);
    void deleteByDistressCallId(Long distressCallId);
}
