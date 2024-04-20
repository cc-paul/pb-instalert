package com.pegp.arwithmapping;

import android.app.Application;

public class Links extends Application {
    String mainLink = "https://apps.project4teen.online/ar/";
    String registrationAPI = mainLink + "php/register.php";
    String loginAPI = mainLink + "php/login.php";
    String OTPAPI = mainLink + "php/otp.php";
    String changePassAPI = mainLink + "php/forgotpass_change.php";
    String getHospitalsAPI = mainLink + "php/hospitals.php";
    String checkFCMApi = mainLink + "php/check_fcm.php";
    String emergencyListApi = mainLink + "php/emergency_list.php";
    String acceptRequestApi = mainLink + "php/accept_request.php";
    String checkRespondentApi = mainLink + "php/check_respondents.php";
    String emergencyLatLongAPI = mainLink + "php/emergency_latlong.php";
}
