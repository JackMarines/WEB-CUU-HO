package com.emergency.config;

import com.emergency.model.RescueCenter;
import com.emergency.model.Response;
import com.emergency.repository.RescueCenterRepository;
import com.emergency.repository.ResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.emergency.model.RescueCenter;
@Component
public class DeduplicationRunner implements CommandLineRunner {

    @Autowired
    private RescueCenterRepository rescueCenterRepository;

    @Autowired
    private ResponseRepository responseRepository;

    @Override
    @Transactional
    public void run(String... args) {
        List<Object[]> duplicates = rescueCenterRepository.findDuplicateNames();
        if (duplicates.isEmpty()) {
            return;
        }

        System.out.println("[DeduplicationRunner] Found " + duplicates.size() + " duplicate center names");

        for (Object[] row : duplicates) {
            String name = (String) row[0];
            List<Long> ids = rescueCenterRepository.findByNameOrderByIdAsc(name).stream()
                .map(RescueCenter::getId).toList();
            if (ids.size() < 2) continue;

            Long keepId = ids.get(0);
            List<Long> removeIds = ids.subList(1, ids.size());

            for (Long dupId : removeIds) {
                List<Response> responses = responseRepository.findByRescueCenterId(dupId);
                for (Response r : responses) {
                    r.setRescueCenter(rescueCenterRepository.getReferenceById(keepId));
                    responseRepository.save(r);
                }
                rescueCenterRepository.deleteById(dupId);
            }

            System.out.println("[DeduplicationRunner] Merged " + (ids.size() - 1)
                + " duplicate(s) of \"" + name + "\" into id=" + keepId);
        }
    }
}
