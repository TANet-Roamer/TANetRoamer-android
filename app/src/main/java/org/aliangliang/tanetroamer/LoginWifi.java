package org.aliangliang.tanetroamer;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

class LoginWifi {

    /**
     * @param context The android context
     * @param accounts The account.
     */
    public LoginWifi(Context context, ArrayList<WifiAccount> accounts) throws JSONException {
        this.context = context;
        this.accounts = accounts;

        DEFAULT_DATA = new JSONObject("{\n" +
                "    \"user\": \"%u\",\n" +
                "    \"password\": \"%p\",\n" +
                "    \"cmd\": \"authenticate\",\n" +
                "    \"Login\": \"繼續\"\n" +
               "  }");
    }

    private void shift() {
        accounts.remove(0);
    }

    public void login(Callback callback) throws Exception {
        Log.d(Debug.TAG, "LogoWifi: accounts length: " + accounts.size());
        if (accounts.size() == 0) {
            Log.i(Debug.TAG, "Login: No account");
            return;
        }
        WifiAccount account = accounts.get(0);
        username = account.getUsername();
        password = account.getPassword();
        apiData = account.getSchoolData();
        shift();
        Log.i(Debug.TAG, "Login: Start LoginTask");
        new LoginTask(callback).execute(context);
    }

    private class LoginTask extends AsyncTask<Context, Void, String> {

        private Callback callback;

        LoginTask(Callback callback) throws Exception {
            this.callback = callback;
        }

        @Override
        protected String doInBackground(Context... contexts) {
            Log.i(Debug.TAG, "LoginTask: Start");
            try {
                Response response = get204Response();
                if (response.statusCode() == 204) { // Don't need to login
                    Log.i(Debug.TAG, "LoginTask: Online now");
                    return GlobalValue.ALREADY_ONLINE;
                }
            } catch (IOException e) {
                Log.w(Debug.TAG, "LoginTask: Can not connect generate204, still login process");
            }
            Log.i(Debug.TAG, "LoginTask: Need login");
            try {
                return doLogin();
            } catch (IOException e) {
                if(e.getClass().getName().equals("java.net.SocketTimeoutException"))
                    return GlobalValue.LOGIN_FAIL_CONNECT_TIMEOUT;
                else if(e.getClass().getName().equals("java.net.UnknownHostException"))
                    return GlobalValue.LOGIN_FAIL_UNKOWN_HOST;
                Log.w(Debug.TAG, "LoginTask: Something bad happened...: " + e);
                return GlobalValue.LOGIN_FAIL_UNKNOWN_REASON;
            } catch (JSONException e) {
                Log.w(Debug.TAG, "LoginTask:Is JSONObject has something happened?", e);
                return GlobalValue.LOGIN_FAIL_UNKNOWN_REASON;
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
            if(loginPage.hasHeader("Location")) {
                String url = loginPage.header("Location");
                Log.i(Debug.TAG, "LoginTask: Response redirect url: " + url);
                if(url.contains("?errmsg=")) {
                    String reason = URLDecoder.decode(url.replaceFirst(".*?errmsg=", ""), "UTF-8");
                    Log.i(Debug.TAG, "LoginTask: Failed: " +  reason);
                    switch (reason) {
                        case GlobalValue.LOGIN_FAIL_AUTH_FAIL:
                        case GlobalValue.LOGIN_FAIL_ONLY_ONE_USER:
                        case GlobalValue.LOGIN_FAIL_NO_INFORMATION:
                            return reason;
                        default:
                            Log.w(Debug.TAG, "LoginTask: Receive an unkown error: " +  reason);
                            return GlobalValue.LOGIN_FAIL_UNKNOWN_REASON;
                    }
                }
            }
            return GlobalValue.LOGIN_SUCCESS;
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
                    .timeout(10 * 1000)
                    .execute();
        }

        @Override
        protected void onPostExecute(String loginResult) {
            try {
                if(loginResult != GlobalValue.LOGIN_SUCCESS || loginResult != GlobalValue.ALREADY_ONLINE) {
                    new LoginWifi(context, accounts).login(callback);
                }
                callback.call(loginResult);
            } catch (Exception e) {
                Log.e(Debug.TAG, "LoginWifi:onPostExecute: ", e);
            }
        }
    }
    private Context context;
    private ArrayList<WifiAccount> accounts;
    private String username, password;
    private JSONObject apiData;
    private static final String DEFAULT_API_URL = "http://securelogin.arubanetworks.com/auth/index.html/u";
    private static JSONObject DEFAULT_DATA = new JSONObject();
}
