package com.example.drivingdatarecord.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LoginInformationDto {
    @JsonProperty("ID")
    private String userId;
    @JsonProperty("password")
    private String password;
}
