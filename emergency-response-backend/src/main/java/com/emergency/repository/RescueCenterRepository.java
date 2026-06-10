package com.emergency.repository;

import com.emergency.model.RescueCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RescueCenterRepository extends JpaRepository<RescueCenter, Long> {
    List<RescueCenter> findAllByOrderByNameAsc();
    List<RescueCenter> findByNameContainingIgnoreCaseOrAddressContainingIgnoreCase(String name, String address);
    List<RescueCenter> findByType(RescueCenter.CenterType type);
    List<RescueCenter> findByNameOrderByIdAsc(String name);

    @Query("SELECT r.name, COUNT(r) FROM RescueCenter r GROUP BY r.name HAVING COUNT(r) > 1")
    List<Object[]> findDuplicateNames();
}
