package com.example.drivingdatarecord.dto;

import lombok.Data;

@Data
public class DrivingInfoDto {
    private long tableID;
    private double speed;
    private int rpm;
    private double totalDistance;
}
