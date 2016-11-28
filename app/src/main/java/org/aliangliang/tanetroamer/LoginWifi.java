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

class LoginWifi {

    /**
     * @param context The android context
     * @param username The account for login
     * @param password The password for login
     */
    public LoginWifi(Context context, String username, String password) {
        this.context = context;
        this.username = username;
        this.password = password;
    }

    /**
     * @return Successful login or not
     */
    public boolean login() {
        if (username == null || password == null) {
            Log.wtf(Debug.TAG, "Login: Username or password is null?!");
            return false;
        }
        Log.i(Debug.TAG, "Login: Start LoginTask");
        new LoginTask().execute(context);
        return true;
    }

    private class LoginTask extends AsyncTask<Context, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Context... contexts) {
            Log.i(Debug.TAG, "LoginTask: Start");
            try {
                Log.d(Debug.TAG, "LoginTask: Use ip:" + ip);
                Response response = get204Response();
                if (response.statusCode() == 204) { // Don't need to login
                    return true;
                }
                Log.i(Debug.TAG, "LoginTask: Need login");
                String url = response.header("location"); // Get redirect url
                Map<String, String> cookies = response.cookies();
                Log.d(Debug.TAG, "LoginTask: URL: " + url);
                if (url == null) {
                    Log.i(Debug.TAG, "LoginTask: Not ccu");
                    return false;
                }
                if (url.startsWith("http://140.123.1.53")) { // It is ccu wireless login
                    return doLogin(cookies);
                } else {
                    Log.i(Debug.TAG, "LoginTask: Not ccu");
                    return false;
                }
            } catch (IOException e) {
                Log.w(Debug.TAG, "LoginTask: Something bad happend...", e);
            }
            return true;
        }

        /**
         * Fetch google generate_204 page
         *
         * @return Response of fetching google generate_204 page
         */
        private Response get204Response() throws IOException {
            return Jsoup
                    .connect("http://" + ip + "/generate_204")
                    .followRedirects(false) // Don't follow, sometime will response 200
                    .execute();
        }

        /**
         * Send login and check result
         *
         * @return Successful login or not
         */
        private Boolean doLogin(Map<String, String> cookies) throws IOException {
            Response loginPage = sendLogin(cookies);
            Log.i(Debug.TAG, "LoginTask: Send request");
            Document page = Jsoup.parse(loginPage.body());
            String body = page.body().text();
            Log.i(Debug.TAG, "LoginTask: Check login");
            if (body.contains("You can now use all our regular network services")) { // If success, body will contain
                Log.i(Debug.TAG, "LoginTask: Response logined");
                return true;
            }
            Log.i(Debug.TAG, "LoginTask: Fail to login");
            return false;
        }

        /**
         * Send login info
         *
         * @return Response of login page
         */
        private Response sendLogin(Map<String, String> cookies) throws IOException {
            return Jsoup
                    .connect(WIFI_LOGIN_URL)
                    .data("buttonClicked", "4")
                    .data("redirect_url", "http://" + ip + "/generate_204")
                    .data("err_flag", "0")
                    .data("username", username)
                    .data("password", password)
                    .cookies(cookies)
                    .method(Connection.Method.POST)
                    .followRedirects(true)
                    .execute();
        }

        /**
         * Return id of result message
         *
         * @param result Login result
         * @return ID of result message
         */
        private int getNotifyText(Boolean result) {
            if (result) {
                return R.string.wifi_login_success;
            }
            return R.string.wifi_login_check_pw;
        }

        @Override
        protected void onPostExecute(Boolean loginResult) {
            NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(), 0);
            Resources resources = context.getResources();
            int msgId = getNotifyText(loginResult);
            Notification n = new Notification.Builder(context)
                    .setContentTitle(resources.getString(R.string.app_name))
                    .setContentText(resources.getString(msgId))
                    .setContentIntent(contentIntent)
                    .build();
            nm.notify("CCULife_Wifi_Auto_Login", 1, n);
        }

        private String ip = GOOGLE_IP[new Random().nextInt(GOOGLE_IP.length)];
    }

    private Context context;
    private String username, password;
    private static final String LOGIN_PAGE = "http://140.123.1.53";
    private static final String WIFI_LOGIN_URL = "https://wlc.ccu.edu.tw/login.html";
    private static final String[] GOOGLE_IP = {
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
}