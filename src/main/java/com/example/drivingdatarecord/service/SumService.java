package com.example.drivingdatarecord.service;

import com.example.drivingdatarecord.repository.DataRepository;
import org.springframework.stereotype.Service;


@Service
public class SumService {
    private int standardOdometer = 12000;

    private final DataRepository dataRepository;
    public SumService(DataRepository dataRepository){
        this.dataRepository = dataRepository;
    }
    public String calculateSum(String vin){
        int totalDrivingDistance = dataRepository.getUserTotalDrivingDistance(vin);
        if(totalDrivingDistance <= 12000){
            return "연 주행 합계" + totalDrivingDistance + "으로 연 주행 12000km 미만 보험료 감면 대상자입니다";
        }else{
            return "연 주행 합계" + totalDrivingDistance + "으로 보험료 감면 미대상자 입니다";
        }
    }
}
