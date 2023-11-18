package com.example.drivingdatarecord.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDrivingInformationDto {
    private long id;
    private long dateId;
    private LocalDateTime date;
    private float speed;
    private int rpm;
    private int odometer;
}
