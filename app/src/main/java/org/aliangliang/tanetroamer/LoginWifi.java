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
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

class LoginWifi {

    /**
     * @param context  The android context
     * @param accounts 帳戶清單
     */
    public LoginWifi(Context context, ArrayList<WifiAccount> accounts) throws JSONException {
        this.context = context;
        // 帳戶清單
        this.accounts = accounts;
        // 預設 API 欄位與資訊。
        DEFAULT_DATA = new JSONObject("{\n" +
            "    \"user\": \"%u\",\n" +
            "    \"password\": \"%p\",\n" +
            "    \"cmd\": \"authenticate\",\n" +
            "    \"Login\": \"繼續\"\n" +
            "  }");
    }

    /**
     * 移除帳戶清單的首個元素。
     */
    private void shift() {
        accounts.remove(0);
    }

    /**
     * @param callback 回呼函數
     * @throws Exception
     */
    public void login(Callback callback) throws Exception {
        Log.d(Debug.TAG, "LogoWifi: 帳戶清單長度: " + accounts.size());
        if (accounts.size() == 0) {
            Log.w(Debug.TAG, "Login: 帳戶清單為空，停止登入任務。");
            return;
        }
        // 將清單中首個帳號作為此次登入程序用的帳號。
        WifiAccount account = accounts.get(0);
        username = account.getUsername();
        password = account.getPassword();
        // API 所需要的資訊。
        apiData = account.getSchoolData();
        // 將首個帳戶移出清單
        shift();
        Log.i(Debug.TAG, "Login: 開始 LoginTask");
        new LoginTask(callback).execute(context);
    }

    private class LoginTask extends AsyncTask<Context, Void, String> {

        private Callback callback;

        LoginTask(Callback callback) throws Exception {
            // 儲存回呼函數
            this.callback = callback;
        }

        @Override
        protected String doInBackground(Context... contexts) {
            Log.i(Debug.TAG, "LoginTask: 啟動");
            // 檢查是否連上網際網路
            try {
                Response response = get204Response();
                if (response.statusCode() == 204) {
                    Log.i(Debug.TAG, "LoginTask: 已經成功連上網際網路");
                    return GlobalValue.ALREADY_ONLINE;
                }
            } catch (IOException e) {
                Log.w(Debug.TAG, "LoginTask: 無法連線 generate204，繼續登入程序。");
            }
            Log.i(Debug.TAG, "LoginTask: 未連上網際網路，需要登入。");
            try {
                return doLogin();
            } catch (SocketTimeoutException e) {
                // 連線逾時
                return GlobalValue.LOGIN_FAIL_CONNECT_TIMEOUT;
            } catch (UnknownHostException e) {
                // 無法找到 API 主機，有可能是設定檔中的 API 位址設定錯誤。
                return GlobalValue.LOGIN_FAIL_UNKOWN_HOST;
            } catch (IOException e) {
                Log.w(Debug.TAG, "LoginTask: 發生一些問題...: " + e);
                return GlobalValue.LOGIN_FAIL_UNKNOWN_REASON;
            } catch (JSONException e) {
                Log.w(Debug.TAG, "LoginTask: JSONObject 出了點錯誤...", e);
                return GlobalValue.LOGIN_FAIL_UNKNOWN_REASON;
            }
        }

        /**
         * 取得 google generate_204 頁面
         *
         * @return 取得 google generate_204 頁面的結果
         */
        private Response get204Response() throws IOException {
            return Jsoup
                .connect("http://clients3.google.com/generate_204")
                .timeout(5000)
                .followRedirects(false) // Don't follow, sometime will response 200
                .execute();
        }

        /**
         * 傳送登入資訊並檢查登入
         *
         * @return 登入結果
         */
        private String doLogin() throws IOException, JSONException {
            Response loginPage = sendLogin();
            Log.i(Debug.TAG, "LoginTask: 取得 response");
            // 輸出 response 的 headers
            for (Map.Entry<String, String> entry : loginPage.headers().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                Log.d(Debug.TAG, "LoginTask: Response header: " + key + " " + value);
            }
            Log.i(Debug.TAG, "LoginTask: 檢查登入");
            // 檢查是否有轉址 header，有的話代表登入失敗。
            if (loginPage.hasHeader("Location")) {
                String url = loginPage.header("Location");
                Log.i(Debug.TAG, "LoginTask: Response redirect url: " + url);
                // 檢查是否有錯誤訊息
                if (url.contains("?errmsg=")) {
                    // 格式化錯誤訊息
                    String reason = URLDecoder.decode(url.replaceFirst(".*?errmsg=", ""), "UTF-8");
                    Log.i(Debug.TAG, "LoginTask: 登入失敗: " + reason);
                    // 處理錯誤訊息
                    switch (reason) {
                        case GlobalValue.LOGIN_FAIL_AUTH_FAIL:
                        case GlobalValue.LOGIN_FAIL_ONLY_ONE_USER:
                        case GlobalValue.LOGIN_FAIL_NO_INFORMATION:
                            return reason;
                        default:
                            Log.w(Debug.TAG, "LoginTask: 收到未知的錯誤訊息: " + reason);
                            return GlobalValue.LOGIN_FAIL_UNKNOWN_REASON;
                    }
                }
            }
            // 回傳登入成功的訊息
            return GlobalValue.LOGIN_SUCCESS;
        }

        /**
         * 傳送登入資訊
         *
         * @return 登入頁面的 response
         */
        private Response sendLogin() throws IOException, JSONException {
            // 取得此次登入的 API 位址與資訊，未設定的話使用預設值。
            String url = (apiData.has("apiUrl")) ? apiData.getString("apiUrl") : DEFAULT_API_URL;
            JSONObject data = (apiData.has("data")) ? apiData.getJSONObject("data") : DEFAULT_DATA;
            // 建立連線物件
            Connection connection = Jsoup.connect(url);
            Log.i(Debug.TAG, "LoginTask: url: " + url);
            Iterator<String> itr = data.keys();
            // 格式化 API 資訊
            while (itr.hasNext()) {
                String key = itr.next();
                // 將值為 %u 與 %p 以帳號與密碼取代
                String value = (data.getString(key).equals("%u")) ? username : ((data.getString(key).equals("%p")) ? password : data.getString(key));
                Log.i(Debug.TAG, "LoginTask: Data field: " + key + " " + value);
                // 將資料欄位加進連線中
                connection.data(key, value);
            }
            Log.i(Debug.TAG, "LoginTask: Send request");
            // 執行登入要求
            return connection
                .method(Connection.Method.POST)
                .followRedirects(false)
                .timeout(10 * 1000)
                .execute();
        }

        @Override
        protected void onPostExecute(String loginResult) {
            try {
                // 如果登入失敗(登入失敗或未連線)
                if (!loginResult.equals(GlobalValue.LOGIN_SUCCESS) && !loginResult.equals(GlobalValue.ALREADY_ONLINE)) {
                    // 以新的帳戶清單重新嘗試登入
                    new LoginWifi(context, accounts).login(callback);
                }
                // 執行回呼函數
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
    // 預設 API 位址
    private static final String DEFAULT_API_URL = "http://securelogin.arubanetworks.com/auth/index.html/u";
    private static JSONObject DEFAULT_DATA = new JSONObject();
}
