package com.jaswal.portfoliotracker.entities;

import com.jaswal.portfoliotracker.enums.AssetType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "api_call_logs")
public class ApiCallLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "api_name", nullable = false, length = 50)
    private String apiName;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false)
    private AssetType assetType;

    @Column(name = "symbol", nullable = false, length = 10)
    private String symbol;

    @Column(name = "call_timestamp", nullable = false)
    private LocalDateTime callTimestamp;
}