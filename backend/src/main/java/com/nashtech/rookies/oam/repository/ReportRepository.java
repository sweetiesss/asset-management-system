package com.nashtech.rookies.oam.repository;

import com.nashtech.rookies.oam.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Integer> {

    
}
