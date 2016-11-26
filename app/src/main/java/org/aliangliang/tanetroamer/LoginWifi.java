package org.aliangliang.tanetroamer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Map;
import java.util.Random;


/**
 * Created by ALiangLiang on 2016/11/26.
 */

public class LoginWifi {

    private Context context;
    private String username;
    private String password;

    private final String LOGIN_PAGE = "https://localhost:8888/";
    private final String WIFI_LOGIN_URL = "https://localhost:8888/login";
    private final String GOOGLE_IP[] = {
            "202.39.143.113",
            "202.39.143.98",
            "202.39.143.123",
            "202.39.143.118",
            "202.39.143.99",
            "202.39.143.93",
            "202.39.143.84",
            "202.39.143.89",
            "202.39.143.114",
            "202.39.143.108",
            "202.39.143.103",
            "202.39.143.109",
            "202.39.143.119",
            "202.39.143.88",
            "202.39.143.94",
            "202.39.143.104"
    };

    LoginWifi(Context context,String username,String password){
        context = context;
    }

    public Boolean login() {
        if (username == null || password == null) {
            Log.wtf("AutoLogin", "Login: Username or password is null?!");
            return false;
        }
        Log.i("AutoLogin", "Login: Start LoginTask");
        new LoginTask().execute(context);
        return true;
    }

    private class LoginTask extends AsyncTask<Context, Void, Boolean> {

        private String ip = GOOGLE_IP[new Random().nextInt(GOOGLE_IP.length)];

        protected Boolean doInBackground(Context... context) {
            Log.i("AutoLogin", "LoginTask: Start");
            Log.d("AutoLogin", "LoginTask: Use ip: ${ip}");
            Response response = get204Response();
            if (response.statusCode() == 204) { // Don't need to login
                return true;
            }
            Log.i("AutoLogin", "LoginTask: Need login");
            String url = response.header("location"); // Get redirect url
            Map cookies = response.cookies();
            Log.d("AutoLogin", "LoginTask: URL: " + url);
            if (url.startsWith("http://140.123.1.53") ? true : false) { // It is ccu wireless login
                return doLogin(cookies);
            } else {
                Log.i("AutoLogin", "LoginTask: Not ccu");
                return false;
            }
        }

        /**
             * Fetch google generate_204 page
             *
             * @return Response of fetching google generate_204 page
             */
        private Response get204Response() {
            try{
                return Jsoup
                        .connect("http://${ip}/generate_204")
                        .followRedirects(false) // Don't follow, sometime will response 200
                        .execute();
            } catch (IOException e) {
                Log.w("AutoLogin", "LoginTask: Something bad happend...", e);
            }
            return null;
        }

        /**
         * Send login and check result
         *
         * @return Successful login or not
         */
        private Boolean doLogin(Map<String, String> cookies) {
            Response loginPage = sendLogin(cookies);
            Log.i("AutoLogin", "LoginTask: Send request");
            Document page = Jsoup.parse(loginPage.body());
            String body = page.body().text();
            Log.i("AutoLogin", "LoginTask: Check login");
            if (body.contains("You can now use all our regular network services")) { // If success, body will contain
                Log.i("AutoLogin", "LoginTask: Response logined");
                return true;
            }
            Log.i("AutoLogin", "LoginTask: Fail to login");
            return false;
        }

        /**
         * Send login info
         *
         * @return Response of login page
         */
        private Response sendLogin(Map<String, String> cookies) {
            try {
                return Jsoup
                        .connect(WIFI_LOGIN_URL)
                        .data("buttonClicked", "4")
                        .data("redirect_url", "http://${ip}/generate_204")
                        .data("err_flag", "0")
                        .data("username", username)
                        .data("password", password)
                        .cookies(cookies)
                        .method(Connection.Method.POST)
                        .followRedirects(true)
                        .execute();
            } catch (IOException e) {
                Log.w("AutoLogin", "LoginTask: Something bad happend...", e);
            }
            return null;
        }

        /**
         * Return id of result message
         *
         * @param result Login result
         * @return ID of result message
         */
        private int getNotifyText(Boolean result) {
            if (result == true) {
                return R.string.wifi_login_success;
            } else {
                return R.string.wifi_login_check_pw;
            }
        }

        protected void onPostExecute(Boolean login_result) {
            NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(), 0);
            Resources resources = context.getResources();
            int msgId = getNotifyText(login_result);
            Notification n = new Notification.Builder(context)
                    .setContentTitle(resources.getString(R.string.app_name))
                    .setContentText(resources.getString(msgId))
                    .setContentIntent(contentIntent)
                    .build();
            nm.notify("CCULife_Wifi_Auto_Login", 1, n);
        }
    }
}
