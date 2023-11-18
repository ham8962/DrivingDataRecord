package com.example.drivingdatarecord.repository;


import com.example.drivingdatarecord.dto.*;

import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DataRepository {
    private static final String url = "jdbc:mariadb://localhost:3306/drivingproject"; //마지막이 DB의 이름이 아닌 DB안의 테이블 이름
    private static final String userName = "root";
    private static final String dbPassWord = "8962";




    public void saveAllUserData(RegisterInformationDto registerInformationDto) {
        PreparedStatement preparedStatement = null;
        Connection connection = null;
        ResultSet resultSet = null;
        try {
            connection = DriverManager.getConnection(url, userName, dbPassWord);
            String sqlLogin = "INSERT INTO user_login(userId,password) VALUES (?,?)";
            preparedStatement = connection.prepareStatement(sqlLogin, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, registerInformationDto.getID());
            preparedStatement.setString(2, registerInformationDto.getPassword());
            preparedStatement.executeUpdate();

            resultSet = preparedStatement.getGeneratedKeys();
            if (resultSet.next()) {
                long userLoginID = resultSet.getLong(1);
                resultSet.close();


                String sqlUser = "INSERT INTO user_information (user_login_id, name, birth, address, phoneNumber, driverLicense) VALUES (?, ?, ?, ?, ?, ?)";
                preparedStatement = connection.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS);
                preparedStatement.setLong(1, userLoginID);
                preparedStatement.setString(2, registerInformationDto.getName());
                preparedStatement.setString(3, registerInformationDto.getBirth());
                preparedStatement.setString(4, registerInformationDto.getAddress());
                preparedStatement.setString(5, registerInformationDto.getPhoneNumber());
                preparedStatement.setString(6, registerInformationDto.getDriverLicense());
                preparedStatement.executeUpdate();

                resultSet = preparedStatement.getGeneratedKeys();
                if (resultSet.next()) {
                    long userInformationId = resultSet.getLong(1);
                    resultSet.close();

                    String sqlCar = "INSERT INTO user_car_information (user_information_id, car_number, model_year, displacement, option, vin) VALUES (?, ?, ?, ?, ?, ?)";
                    preparedStatement = connection.prepareStatement(sqlCar,Statement.RETURN_GENERATED_KEYS);
                    preparedStatement.setLong(1, userInformationId);
                    preparedStatement.setString(2, registerInformationDto.getCarNumber());
                    preparedStatement.setString(3, registerInformationDto.getYear());
                    preparedStatement.setString(4, registerInformationDto.getDisplacementVolume());
                    preparedStatement.setString(5, registerInformationDto.getOption());
                    preparedStatement.setString(6, registerInformationDto.getVin());
                    preparedStatement.executeUpdate();
                }

            }

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //로그인 인증용 (매직넘버 안 좋은 버릇 >> 일단 과제용 이니 작동시키고 고칠 것 >> enum으로 처리 java의 enum은 꼭 숫자를 가지지 않아도 된다, 자바의 enum은 객체)
    public int login(String userID, String passWord) {
        String sql = "SELECT password FROM user_login WHERE userId = ?";
        PreparedStatement preparedStatement = null;
        Connection connection = null;
        ResultSet resultSet = null;
        try {
            connection = DriverManager.getConnection(url, userName, dbPassWord);
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, userID);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                if (resultSet.getString(1).equals(passWord)) { //1이 password 인지 따질 것
                    return 1; // 로그인 성공
                } else return 0; // 비밀번호 불일치
            }
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return -2; // id조차 맞지 않는 상황 >> 회원가입으로 돌려보내자
    }

    //쿠키 인증용으로 사용
    public String findUserID(String cookieValue) {
        String sql = "SELECT userId From user_login WHERE userId = ?";
        PreparedStatement preparedStatement = null;
        Connection connection = null;
        ResultSet resultSet = null;
        try {
            connection = DriverManager.getConnection(url, userName, dbPassWord);
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, cookieValue);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("userid"); //이게 진짜 userID를 반환하는지 체크
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    //로그인 유지한 유저가 자신의 차량을 고르게끔 하는 메서드 HTML페이지를 위해 메서드 작성 >> 그냥 List를 쓰는게 낫다 , 반환값을 넘겨주거나 받을 때는 상속구조로 최상위의 (개념이 제일 구체화가 덜 된) 자료구조로 넘긴다
    public List<String> getUserCarVin(String userID) {
        String sql = """
                SELECT vin FROM user_car_information 
                WHERE user_information_id = 
                (SELECT id FROM user_information WHERE user_login_id = 
                (SELECT id FROM user_login WHERE userId = ?)) 
                """;
        //쿼리 문 검증 필요
        List<String> vins = new ArrayList<>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = DriverManager.getConnection(url, userName, dbPassWord);
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, userID);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                vins.add(resultSet.getString("vin"));
            }
            return vins;

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    //테이블 연결을 위해 차대번호로 carID찾기 >> 멘토님 확인 필요
    // 밑의 메서드 까지 해서 sql이 간단해 보이는데 controller에서 2개 차례로 쓰니 괜찮지 않을까..
    /*
    public int getUserCarID(VinDto vinDto) {
        String sql = """
                SELECT id FROM user_car_information WHERE vin = ?
                """;
        PreparedStatement preparedStatement = null;
        Connection connection = null;
        ResultSet resultSet = null;
        try {
            connection = DriverManager.getConnection(url, userName, dbPassWord);
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, vinDto.getVin());
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int userCarID = resultSet.getInt("id");
                return userCarID;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return -1;
    }*/

    //4번째 테이블 채우기 drivingDate_information
    public void saveUserDrivingDay(long carId) {
        String sql = """
                INSERT INTO drivingdate_information (user_car_information_id, drivingday) VALUES (?,?)
                """;
        PreparedStatement preparedStatement = null;
        Connection connection = null;
        ResultSet resultSet = null;
        try {
            connection = DriverManager.getConnection(url, userName, dbPassWord);
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setLong(1, carId);
            LocalDate localDate = LocalDate.now();
            java.sql.Date today = java.sql.Date.valueOf(localDate);
            preparedStatement.setDate(2, today);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /*4번째 테이블의 id를 클라이언트에게 넘거주기 위해 id 반환하기 >> 차대 번호로 반환 해줘야 한다 >> 밑의 메서드와 끊기면 안 된다, 한 커넥션으로 가져가야 한다
    public int getTableIdToClient(VinDto vinDto) {
        String sql = """
                SELECT drivingdate_information.id
                FROM user_car_information
                JOIN user_car_information ON drivingDate_information.user_car_information_id = user_car_information.id
                WHERE user_car_information.vin = ?
                """;
        PreparedStatement preparedStatement = null;
        Connection connection = null;
        ResultSet resultSet = null;
        try {
            connection = DriverManager.getConnection(url, userName, dbPassWord);
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, vinDto.getVin());
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int tableId = resultSet.getInt(1);
                return tableId;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return -1;
    }*/

    // 주행 데이터 저장을 위한 메서드
    public void saveDrivingInformations(long tableID, DrivingInfoDto drivingInfoDto) {
        String sql = "INSERT INTO driving_information (date_id, drivingtime, speed, rpm, odometer) VALUES (?, ?, ?, ?, ?)";
        try (
                Connection connection = DriverManager.getConnection(url, userName, dbPassWord);
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            preparedStatement.setLong(1, tableID);
            LocalDateTime localDateTime = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
            preparedStatement.setTimestamp(2, Timestamp.valueOf(localDateTime));
            preparedStatement.setDouble(3, drivingInfoDto.getSpeed());
            preparedStatement.setInt(4, drivingInfoDto.getRpm());
            preparedStatement.setDouble(5, drivingInfoDto.getTotalDistance());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public List<UserDrivingInformationDto> getOneUserDrivingInformationByVin(String vin) { //while문에서 무엇을 반환해야 하는지 빠져있다
        String sql = """
                SELECT driving_information.*
                FROM user_car_information
                JOIN drivingdate_information ON user_car_information.id =drivingdate_information.user_car_information_id
                JOIN driving_information ON drivingdate_information.id = driving_information.date_id
                WHERE user_car_information.vin = ?
                """;
        List<UserDrivingInformationDto> drivingInformationList = new ArrayList<>();
        try (
                Connection connection = DriverManager.getConnection(url, userName, dbPassWord);
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            preparedStatement.setString(1, vin);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                UserDrivingInformationDto userDrivingInformationDto = new UserDrivingInformationDto();
                userDrivingInformationDto.setId(resultSet.getLong("id"));
                userDrivingInformationDto.setDateId(resultSet.getLong("date_id"));
                Timestamp timestamp = resultSet.getTimestamp("drivingtime");
                if (timestamp != null) {
                    userDrivingInformationDto.setDate(timestamp.toLocalDateTime());
                }
                userDrivingInformationDto.setSpeed(resultSet.getFloat("speed"));
                userDrivingInformationDto.setRpm(resultSet.getInt("rpm"));
                userDrivingInformationDto.setOdometer(resultSet.getInt("odometer"));
                drivingInformationList.add(userDrivingInformationDto);
                System.out.println(drivingInformationList);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return drivingInformationList;
    }

    //거리 총합 구하기

    public int getUserTotalDrivingDistance(String vin) { //sql문도 잘 편집해서 합계로 가져올 수 있도록 바꿔야지
        String sql = """
                WITH max_odometer_per_date AS (
                    SELECT drivingtime, MAX(odometer) as max_odometer
                    FROM driving_information
                    GROUP BY drivingtime
                ),
                with_lag AS (
                    SELECT max_odometer - COALESCE(LAG(max_odometer) OVER (ORDER BY drivingtime), 0) as odometer_difference
                    FROM max_odometer_per_date
                )
                SELECT SUM(odometer_difference) as total_distance
                FROM with_lag;
                
             """;
        PreparedStatement preparedStatement = null;
        Connection connection = null;
        ResultSet resultSet = null;
        try {
            connection = DriverManager.getConnection(url, userName, dbPassWord);
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, vin);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                System.out.println(resultSet.getInt(1));
                return resultSet.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return -1;
    }

    public long getDrivingdayTableID(VinDto vinDto){
        PreparedStatement preparedStatement = null;
        Connection connection = null;
        ResultSet resultSet = null;
        try {
            connection = DriverManager.getConnection(url, userName, dbPassWord);
            connection.setAutoCommit(false);

            String query1 = "SELECT id FROM user_car_information WHERE vin = ?";
            preparedStatement = connection.prepareStatement(query1);
            preparedStatement.setString(1, vinDto.getVin());
            resultSet = preparedStatement.executeQuery();
            long userID = 0;
            if (resultSet.next()) {
                userID = resultSet.getLong(1);
                System.out.println(userID);
            }
            LocalDate localDate = LocalDate.now();
            java.sql.Date today = java.sql.Date.valueOf(localDate);

            String query2 = "INSERT INTO drivingdate_information (user_car_information_id, drivingday) VALUES (?,?)";
            preparedStatement = connection.prepareStatement(query2,Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setLong(1, userID);
            preparedStatement.setDate(2, today);
            preparedStatement.executeUpdate();
            resultSet = preparedStatement.getGeneratedKeys();
            long dateTableID = 0;
            if (resultSet.next()) {
                dateTableID = resultSet.getLong(1);
                System.out.println(dateTableID);
                connection.commit();
                return dateTableID;
            }

            connection.commit();


        }catch (SQLException e){
            e.printStackTrace();
        }finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return -1; // 잘못된 데이터
    }
}

    /*
    public void saveDrivingData(String receivedTextID, String RPM, String Speed) throws ClassNotFoundException, SQLException {
        String textid = receivedTextID;
        String rpm = RPM;
        String speed = Speed;
        String sql = "INSERT INTO drivingInformation (textid,rpm,speed) VALUES (?,?,?)";
        Connection connection = DriverManager.getConnection(url, userName, dbPassWord);
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, textid);
        preparedStatement.setString(2, rpm);
        preparedStatement.setString(3, speed);
        int result = preparedStatement.executeUpdate();
        System.out.println(result);
        preparedStatement.close();
        connection.close();

    }


    public ArrayList<String> findByTextId(String textid) throws ClassNotFoundException, SQLException {
        String sql = "SELECT * FROM drivingInformation WHERE textid = ? ";
        Connection connection = DriverManager.getConnection(url, userName, dbPassWord);
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1,textid);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            //int id  = resultSet.getInt("id");
            //String receivedtextID = resultSet.getString("textid");
            //String rpm = resultSet.getString("rpm");
            //String speed = resultSet.getString("speed");
            String [] drivingData = {resultSet.getString("textid"), resultSet.getString("rpm"), resultSet.getString("speed")};
            ArrayList<String> receivedData = new ArrayList<>(Arrays.asList(drivingData));
            return receivedData;
        }
        resultSet.close();
        preparedStatement.close();
        connection.close();
        return null;
    }*/

    /*
    public List<TextDto> findAll() throws ClassNotFoundException, SQLException {
        String sql = "SELECT * FROM drivingInformation";
        Connection connection = DriverManager.getConnection(url, userName, passWord);
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        while (resultSet.next()) {
            int id  = resultSet.getInt("id");
            String textid = resultSet.getString("textid");
            String rpm = resultSet.getString("rpm");
            String speed = resultSet.getString("speed");
        }
        resultSet.close();
        statement.close();
        connection.close();
    }*/

    /*
    public void deleteByTextId() throws ClassNotFoundException, SQLException {
        String textid = "";
        String sql = "DELETE drivingInformation WHERE ID=?";
        Connection connection = DriverManager.getConnection(url, userName, passWord);
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString( /);
        String result = preparedStatement.enquoteIdentifier();
    }


    public void deleteAll() throws ClassNotFoundException, SQLException {
        String textid = "";
        String sql = "DELETE drivingInformation WHERE ID=?";
        Connection connection = DriverManager.getConnection(url, userName, passWord);
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString( /);
        String result = preparedStatement.enquoteIdentifier();

    }


    public byte[] findImage() {
        return;
    }

     */



