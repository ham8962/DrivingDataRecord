package com.example.drivingdatarecord.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data

public class RegisterInformationDto {
    @JsonProperty("ID")
    private String ID;
    @JsonProperty("password")
    private String password;
    @JsonProperty("name")
    private String name;
    @JsonProperty("birth")
    private String birth;
    @JsonProperty("address")
    private String address;
    @JsonProperty("phone_number")
    private String phoneNumber;
    @JsonProperty("driverLicense")
    private String driverLicense;
    @JsonProperty("carNumber")
    private String carNumber;
    @JsonProperty("year")
    private String year;
    @JsonProperty("displacementVolume")
    private String displacementVolume;
    @JsonProperty("option")
    private String option;
    @JsonProperty("vin")
    private String vin;
}
