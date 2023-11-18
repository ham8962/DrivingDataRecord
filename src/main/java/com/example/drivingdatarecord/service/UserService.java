package com.example.drivingdatarecord.service;

import com.example.drivingdatarecord.dto.*;
import com.example.drivingdatarecord.repository.DataRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final DataRepository dataRepository;

    public UserService(DataRepository dataRepository){
        this.dataRepository = dataRepository;
    }

    public int login(String userID, String password) {
        int loginResult = dataRepository.login(userID, password);
        return loginResult;
    }

    public void register(RegisterInformationDto registerInformationDto){
        dataRepository.saveAllUserData(registerInformationDto);
    }

    public String getUserIdByCookie(String cookieValue){
        return dataRepository.findUserID(cookieValue);
    }


    public List<String> getUserCarVin(String userID) {
        List<String> userCarVins = dataRepository.getUserCarVin(userID);
        return userCarVins;
    }

    public long getTableID(VinDto vinDto) {
      return dataRepository.getDrivingdayTableID(vinDto);
    }

    public void saveAnyUserDrivingDay(long carID) {
        dataRepository.saveUserDrivingDay(carID);
    }

    public void inputDrivingData(long tableID, DrivingInfoDto drivingInfoDto){
        dataRepository.saveDrivingInformations(tableID,drivingInfoDto);
    }



    public List<UserDrivingInformationDto> getUserDrivingDataByVin(String vin) {
        return dataRepository.getOneUserDrivingInformationByVin(vin);
    }

}

