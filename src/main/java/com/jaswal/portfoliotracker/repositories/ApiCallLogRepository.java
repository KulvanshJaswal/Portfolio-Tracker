package com.jaswal.portfoliotracker.repositories;

import com.jaswal.portfoliotracker.entities.ApiCallLog;
import com.jaswal.portfoliotracker.enums.AssetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ApiCallLogRepository extends JpaRepository<ApiCallLog, Long> {

    long countByApiNameAndAssetTypeAndCallTimestampAfter(
            String apiName,
            AssetType assetType,
            LocalDateTime callTimestamp
    );
}