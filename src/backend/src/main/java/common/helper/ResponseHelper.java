package common.helper;

import com.alibaba.fastjson2.JSON;
import common.api.CommonResult;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ResponseHelper {

    public static void sendErrorMessage(HttpServletResponse response,String message) {

        CommonResult<String> result = CommonResult.failed(message);
        String res = JSON.toJSONString(result);

        try {
            response.getWriter().write(res);
        } catch (IOException e) {
            System.out.println("[ERROR] Failed to send error message to client.");
        }

    }

    public static <T> void sendErrorMessage(HttpServletResponse response,String message,T data) {

        CommonResult<T> result = CommonResult.failed(data,message);
        String res = JSON.toJSONString(result);

        try {
            response.getWriter().write(res);
        } catch (IOException e) {
            System.out.println("[ERROR] Failed to send error message to client.");
        }

    }

    public static void sendSuccessMessage(HttpServletResponse response,String message) {

        CommonResult<String> result = CommonResult.success(message);
        String res = JSON.toJSONString(result);

        try {
            response.getWriter().write(res);
        } catch (IOException e) {
            System.out.println("[ERROR] Failed to send success message to client:"+ message);
        }

    }

    public static <T> void sendSuccessMessage(HttpServletResponse response,String message,T data) {

        CommonResult<T> result = CommonResult.success(message,data);
        String res = JSON.toJSONString(result);

        try {
            response.getWriter().write(res);
        } catch (IOException e) {
            System.out.println("[ERROR] Failed to send success message to client."+ message);
        }

    }
}
