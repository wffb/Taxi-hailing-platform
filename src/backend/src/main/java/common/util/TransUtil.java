package common.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import model.LoginUser;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;

public class TransUtil {

    public static String getRoleIDFromUserID(String userID){

        String cacheKey = "login:"+ userID;
        LoginUser driver = (LoginUser) CacheUtil.LoginUserCache.get(cacheKey);
        return driver.getRoleID();

    }

    public static JSONObject getJsonFromRequest(HttpServletRequest req) throws IOException {

        BufferedReader reader = req.getReader();
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }

        // transform request body to JSON object
        String requestBody = stringBuilder.toString();
        return JSON.parseObject(requestBody);
    }
}
