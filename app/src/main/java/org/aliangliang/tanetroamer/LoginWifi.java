package org.aliangliang.tanetroamer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.Map;

class LoginWifi {

    /**
     * @param context The android context
     * @param username The account for login
     * @param password The password for login
     */
    public LoginWifi(Context context, String username, String password, JSONObject apiData) throws JSONException {
        this.context = context;
        this.username = username;
        this.password = password;
        this.apiData = apiData;

        DEFAULT_DATA = new JSONObject("{\n" +
                "    \"user\": \"%u\",\n" +
                "    \"password\": \"%p\",\n" +
                "    \"cmd\": \"authenticate\",\n" +
                "    \"Login\": \"繼續\"\n" +
               "  }");
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

    private class LoginTask extends AsyncTask<Context, Void, String> {
        @Override
        protected String doInBackground(Context... contexts) {
            Log.i(Debug.TAG, "LoginTask: Start");
            try {
                Response response = get204Response();
                if (response.statusCode() == 204) { // Don't need to login
                    Log.i(Debug.TAG, "LoginTask: Online now");
                    return ALREADY_ONLINE;
                }
            } catch (IOException e) {
                Log.w(Debug.TAG, "LoginTask: Can not connect generate204, still login process");
            }
            Log.i(Debug.TAG, "LoginTask: Need login");
            try {
                return doLogin();
            } catch (IOException e) {
                Log.w(Debug.TAG, "LoginTask: Something bad happened...: " + e);
                return LOGIN_FAIL_UNKNOWN_REASON;
            } catch (JSONException e) {
                Log.w(Debug.TAG, "LoginTask:Is JSONObject has something happened?", e);
                return LOGIN_FAIL_UNKNOWN_REASON;
            }
        }

        /**
         * Fetch google generate_204 page
         *
         * @return Response of fetching google generate_204 page
         */
        private Response get204Response() throws IOException {
            return Jsoup
                    .connect("http://clients3.google.com/generate_204")
                    .timeout(5000)
                    .followRedirects(false) // Don't follow, sometime will response 200
                    .execute();
        }

        /**
         * Send login and check result
         *
         * @return Successful login or not
         */
        private String doLogin() throws IOException, JSONException {
            Response loginPage = sendLogin();
            Log.i(Debug.TAG, "LoginTask: Get response");
            for (Map.Entry<String,String> entry : loginPage.headers().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                Log.d(Debug.TAG, "LoginTask: Response header: " + key + " " + value);
            }
            Log.i(Debug.TAG, "LoginTask: Check login");
            String url = loginPage.header("Location");
            Log.i(Debug.TAG, "LoginTask: Response redirect url: " + url);
            if(url.contains("?errmsg=")) {
                String reason = URLDecoder.decode(url.replaceFirst(".*?errmsg=", ""), "UTF-8");
                Log.i(Debug.TAG, "LoginTask: Failed: " +  reason);
                if(reason.equals("Authentication failed")) {
                  return LOGIN_FAIL_AUTH_FAIL;
                } else if(reason.equals("No auth server provisioned")) {
                  return LOGIN_FAIL_DUPLICATE_USER;
                }
                return LOGIN_FAIL_UNKNOWN_REASON;
            }
            return LOGIN_SUCCESS;
        }

        /**
         * Send login info
         *
         * @return Response of login page
         */
        private Response sendLogin() throws IOException, JSONException {
            String url =  (apiData.has("apiUrl")) ? apiData.getString("apiUrl") : DEFAULT_API_URL;
            JSONObject data = (apiData.has("data"))  ? apiData.getJSONObject("data"):DEFAULT_DATA ;
            Connection connection = Jsoup.connect(url);
            Log.i(Debug.TAG, "LoginTask: url: " + url);
            Iterator<String> itr = data.keys();
            while(itr.hasNext()) {
                String key = itr.next();
                String value = (data.getString(key).equals("%u")) ? username : ((data.getString(key).equals("%p")) ? password : data.getString(key));
                Log.i(Debug.TAG, "LoginTask: Data field: " + key + " " + value);
                connection.data(key, value);
            }
            Log.i(Debug.TAG, "LoginTask: Send request");
            return connection
                    .method(Connection.Method.POST)
                    .followRedirects(false)
                    .execute();
        }

        /**
         * Return id of result message
         *
         * @param result Login result
         * @return ID of result message
         */
        private int getNotifyText(String result) {
            switch (result) {
                case LOGIN_SUCCESS:
                    return R.string.wifi_login_success;
                case LOGIN_FAIL_AUTH_FAIL:
                    return R.string.wifi_login_wrong_pwd;
                case LOGIN_FAIL_UNKNOWN_REASON:
                    return R.string.wifi_login_duplicate_user;
                case ALREADY_ONLINE:
                    return R.string.wifi_login_already_online;
                default:
                    return R.string.wifi_login_unknown_reason;
            }
        }

        @Override
        protected void onPostExecute(String loginResult) {
            NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(), 0);
            Resources resources = context.getResources();
            int msgId = getNotifyText(loginResult);
            long[] vibrate_effect = {1000, 500, 1000, 400, 1000, 300, 1000, 200, 1000, 100};
            Notification n = new Notification
                    .Builder(context)
                    .setContentTitle(resources.getString(R.string.app_name))
                    .setContentText(resources.getString(msgId))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setTicker("EFFECT")
                    .setVibrate(vibrate_effect)
                    .setLights(Color.GREEN, 1000, 1000)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                    .setContentIntent(contentIntent)
                    .build();
            nm.notify("TANet_Roamer_Login", 1, n);
        }
    }

    public static final String LOGIN_SUCCESS = "Success";
    public static final String ALREADY_ONLINE = "Online";
    public static final String LOGIN_FAIL_AUTH_FAIL = "Auth fail";
    public static final String LOGIN_FAIL_DUPLICATE_USER = "Duplicate user";
    public static final String LOGIN_FAIL_UNKNOWN_REASON = "Unknown reason";
    private Context context;
    private String username, password;
    private JSONObject apiData;
    private static final String DEFAULT_API_URL = "http://securelogin.arubanetworks.com/auth/index.html/u";
    private static JSONObject DEFAULT_DATA = new JSONObject();
}
