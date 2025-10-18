package common.api;

import lombok.Data;

import java.io.Serializable;

@Data
public class CommonResult <T> implements Serializable {

    public interface CommonResultView{};

    //status code
    private  long code;

    //status message
    private  String message;

    //data
    private T data;

    private  CommonResult(long code, String message, T data){
        this.code=code;
        this.message=message;
        this.data=data;
    }

    /**
     * success return result
     * @param message
     * @param data
     */
    public  static <T>  CommonResult<T> success(String message,T data){
        return new CommonResult<>(ResultCode.SUCCESS.getCode(), message,data);
    }

    public  static <T>  CommonResult<T> success(T data){
        return new CommonResult<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(),data);
    }

    public static <T> CommonResult<T> success(String message) {
        return new CommonResult<T>(ResultCode.SUCCESS.getCode(), message, null);
    }
    /**
     * failed return result
     * @param data
     */
    public static <T> CommonResult<T> failed(T data,String msg) {
        return new CommonResult<T>(ResultCode.FAILED.getCode(),msg,data);
    }
    public static <T> CommonResult<T> failed(String message) {
        return new CommonResult<T>(ResultCode.FAILED.getCode(), message, null);
    }
    public static <T> CommonResult<T> failed(ResultCode code) {
        return new CommonResult<T>(code.getCode(), code.getMessage(), null);
    }
    /**
     * 失败返回结果
     */
    public static <T> CommonResult<T> failed() {
        return failed(ResultCode.FAILED);
    }


}
