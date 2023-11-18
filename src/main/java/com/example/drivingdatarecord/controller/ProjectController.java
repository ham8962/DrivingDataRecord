package com.example.drivingdatarecord.controller;
import com.example.drivingdatarecord.dto.*;
import com.example.drivingdatarecord.service.SumService;
import com.example.drivingdatarecord.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(originPatterns = "*")
@Controller
@RequiredArgsConstructor
public class ProjectController {
    private final UserService userService;
    private final SumService sumService;
    private final String cookieName = "userID";


    @GetMapping("/") //홈페이지
    public String showLogin() {
        return "Login";
    }

    @GetMapping("/signUp") //login페이지에서 링크를 타고 회원가입 페이지로 이동
    public String showRegisterScreen() {
        return "Signup";
    }

    @PostMapping("/register") //Signup.html페이지에서 보낸 post메서드 처리, RegisterInformationDto참고
    public String memberRegister(@RequestBody RegisterInformationDto registerInformationDto) {
        System.out.println(registerInformationDto.toString());
        userService.register(registerInformationDto);
        return "redirect:/";
    }

    @PostMapping("/login") //Login.html페이지 참고, LoginInformationDto참고
    public ResponseEntity<String> LoginChecking(@RequestBody LoginInformationDto loginInformationDto, HttpServletResponse httpServletResponse) {
        //로그인 처리 로직 필요
        System.out.println(loginInformationDto.toString());
        int loginResult = userService.login(loginInformationDto.getUserId(), loginInformationDto.getPassword());
        if (loginResult == 1) {
            Cookie loginCookie = new Cookie(cookieName, loginInformationDto.getUserId());
            httpServletResponse.addCookie(loginCookie);
            return ResponseEntity.ok().body("Login Success");
        } else if (loginResult == 0) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login Failed: Incorrect password");  // 비밀번호 불일치
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Login Failed: ID not found");  // 아이디 불일치
        }
    }

    @GetMapping("/selectingService") //Login.html에서 위 LoginChecking메서드가 제대로 작동한다면 "/selectingService" get메서드
    public String selectService(HttpServletRequest request) {
        //html 페이지 이동이 가능하다면 메서드 없애도 상관없다 아니면 반환자 바꿔주고, 쿠키 검증해야 하나? 단지 페이지 반환인데?
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    String userID = userService.getUserIdByCookie(cookie.getValue());
                    if (userID != null) {
                        return "SelectService";
                    }
                }
            }
        }
        return "redirect:/login";
    }

    /*
    @GetMapping("/selectingService") //Login.html에서 위 LoginChecking메서드가 제대로 작동한다면 "/selectingService" get메서드
    public String selectService2(HttpServletRequest request) {
        //html 페이지 이동이 가능하다면 메서드 없애도 상관없다 아니면 반환자 바꿔주고, 쿠키 검증해야 하나? 단지 페이지 반환인데?
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            Cookie cookie = Arrays.stream(cookies).filter(c -> c.getName().equals("userID")).findFirst().get();
        }
        return "redirect:/login";
    }*/


    //인증한 유저가 차대 번호로 자기 차 고르게 하기 ,아래 세 메서드는 차례대로 묶인다 차대번호 선택 > sendSelectVin(post) >
    @GetMapping("/drivingData")
    public String inputDrivingData(HttpServletRequest request, Model model) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    String userID = userService.getUserIdByCookie(cookie.getValue());
                    if (userID != null) {
                        List<String> userCarVinList = userService.getUserCarVin(userID);
                        model.addAttribute("userCarVinList", userCarVinList);
                        return "SendDrivingData2"; //주행 데이터 입력 페이지 보여주기
                    }
                }
            }
        }
        return "redirect:/login";
    }

    //차대번호 선택 > sendSelectVin(post) > 클라이언트에게 tableID 주기
    @PostMapping("/sendSelectedVin")
    public ResponseEntity<String> receiveVin(@RequestBody VinDto vinDto, HttpServletRequest request) {
        //주행시간도 받아야 하나(dto,html조정함), 저장 후에 다시 REDIRECT로 서비스 선택 페이지로 돌려 보내자, 쿠키랑 연동 어캐 하지? (페이지 돌려 보내는 거 아직 미완성)
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    String userID = userService.getUserIdByCookie(cookie.getValue()); // 쿠키 인증
                    if (userID != null) {
                        //long carID = userService.getCarID(vinDto); // 3번째 테이블에서 쿠키 인증한 유저가 고른 차대번호에 해당하는 테이블 id 받기 >> 서비스로
                        //userService.saveAnyUserDrivingDay(carID); //4번째 테이블에 외래키(user_car_information_id) 컬럼에 값 넣어주기 >> 서비스로 (트랙젝션이 깨짐 이건 내 프로젝트에 해당 아님)
                        //long tableID = userService.getTableID(vinDto); // client에게 보낼 4번째 테이블의 id 가져옴 >> 서비스로
                        long drivingDatetableID = userService.getTableID(vinDto);
                        return ResponseEntity.ok().body(String.valueOf(drivingDatetableID));
                    }
                }
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not Verified");
    }

    // 받은 tableID로 데이터 저장하기
    @PostMapping("/sendDrivingData")
    public ResponseEntity<String> receiveDrivingData(@RequestBody DrivingInfoDto drivingInfoDto, HttpServletRequest request) {
        //주행시간도 받아야 하나(dto,html조정함), 저장 후에 다시 REDIRECT로 서비스 선택 페이지로 돌려 보내자, 쿠키랑 연동 어캐 하지? (페이지 돌려 보내는 거 아직 미완성)
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    String userID = userService.getUserIdByCookie(cookie.getValue()); // 쿠키 인증
                    if (userID != null) {
                        long tableID = drivingInfoDto.getTableID();
                        userService.inputDrivingData(tableID, drivingInfoDto); // 넘어온 tableID로 주행데이터 저장
                        return ResponseEntity.ok().body("{\"message\": \"Data saved\"}");
                    }
                }

            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not Verified");
    }

    //아래 메서드까지 2개 묶어서 볼 것  > >한 페이지에서 차대번호 선택 후 데이터 보여주기니깐
    @GetMapping("/userDrivingData")
    public String showUserDrivingData(HttpServletRequest request, Model model) {
        //주행 데이터 조회(GET)
        //html자체에서 페이지 이동이 가능하다면 메서드 없애도 상관없다 쿠키 검증 >> 얘는 확실히 쿠키 이용해서 userID로 불러오면 되는거고
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    String userID = userService.getUserIdByCookie(cookie.getValue());
                    if (userID != null) {
                        //그냥 페이지 반환이 아니라 db에서 회원 조회해서 나온 데이터 출력해야 한다 외래키 사용 해야 한다. 아직 userService쪽 코드 미완성, html페이지 미완
                        List<String> userCarVinList = userService.getUserCarVin(userID);
                        model.addAttribute("userCarVinList", userCarVinList);
                        //userService.getUserDrivingData(userID);
                        return "OneDrivingInfo";
                    }
                }
            }

        }
        return "redirect:/login";
    }

   //이미 로그인 인증을 마치고 이 메서드로 들어오니 쿠키 인증 빼겠습니다.
    @GetMapping("/userDrivingData/{vin}")
    public ResponseEntity<List<UserDrivingInformationDto>> showUserDrivingDataByVin(@PathVariable String vin){
        //주행 데이터 조회(GET)
        //html자체에서 페이지 이동이 가능하다면 메서드 없애도 상관없다 쿠키 검증 >> 얘는 확실히 쿠키 이용해서 userID로 불러오면 되는거고
        List<UserDrivingInformationDto> drivingData = userService.getUserDrivingDataByVin(vin);
        return ResponseEntity.ok().body(drivingData);
    }

    //아래 두 메서드 묶입니다!
    @GetMapping("/InsuranceFee")
    public String showInsuranceFee(HttpServletRequest request,Model model) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    String userID = userService.getUserIdByCookie(cookie.getValue());
                    if (userID != null) {
                        List<String> userCarVinList = userService.getUserCarVin(userID);
                        model.addAttribute("userCarVinList", userCarVinList);
                        return "InsuranceFee";
                    }
                }
            }
        }
        return "redirect:/login";
    }

    //html 페이지에서 연산하는 거 수정완료
    @GetMapping("/InsuranceFee/{vin}")
    public ResponseEntity<String> showInsuranceFeeByVin(@PathVariable String vin){
        String result = sumService.calculateSum(vin);
        return ResponseEntity.ok().body(result);
    }

    private String cookieVerification(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    String userID = userService.getUserIdByCookie(cookie.getValue());
                    return userID;
                }
            }
        }
        return null;
    }







    /*
    @PostMapping("/text/{textid}")
    public InformationDto saveBody(@PathVariable String textid, @RequestBody InformationDto informationDto) {
        return informationDto;
    }*/

    /*
    @PostMapping("/text")
    public void saveAllBody(@RequestBody List<TextDto> textDtoList){
        for(TextDto textDto : textDtoList) {
            String receivedTextID = textDto.getTextID();
            String RPM = textDto.getRPM();
            String Speed = textDto.getSpeed();
            textService.saveAllText(receivedTextID, RPM, Speed);
            textService.dataChecking(receivedTextID);
        }
    }*/

    /*
    @PostMapping("/text")
    public void saveAllBody(@RequestBody InformationDto informationDto) {

    }

    @GetMapping("/text/{textid}")
    @ResponseBody
    public SendInformationDto getBodyString(@PathVariable String textid) {
        SendInformationDto sendInformationDto = new SendInformationDto();
        sendInformationDto.setFullData(textService.getDrivingInformation(textid));
        return sendInformationDto;
    }
    */

    /*
    @GetMapping("/text")
    @ResponseBody
    public SendTextDto getALLBodyString() {
        SendTextDto sendTextDto = new SendTextDto();
        sendTextDto.setAllDrivingInfo(textService.getAllDrivingInformation());
        return sendTextDto;
    }


    @DeleteMapping("/text/{textid}")
    public SendTextDto deleteBodyString(@PathVariable String textid) {
        SendTextDto sendTextDto = new SendTextDto();
        textService.removeData(textid);
        sendTextDto.setAllDrivingInfo(textService.getAllDrivingInformation());
        return sendTextDto;
    }


    @DeleteMapping("/text")
    public SendTextDto deleteAllBodyString() {
        SendTextDto sendTextDto = new SendTextDto();
        textService.removeAllData();
        sendTextDto.setAllDrivingInfo(textService.getAllDrivingInformation());
        return sendTextDto;
    }

    @GetMapping(value = "/image", produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    public byte[] getImage() throws IOException {
        FileInputStream fileInputStream;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        String fileDir = "C:/Users/dydwls/Pictures/img.jpg";

        fileInputStream = new FileInputStream(fileDir);

        byte[] buffer = new byte[1024];
        int readCount;

        while ((readCount = fileInputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, readCount);
        }

        byte[] fileArray = byteArrayOutputStream.toByteArray();

        fileInputStream.close();
        byteArrayOutputStream.close();

        return fileArray;

    }*/
}


