package common.api;

import lombok.Getter;

@Getter
public enum ResultCode {
    /**
     * enum
     */
    SUCCESS(200, "operation success"),
    FAILED(500, "operation failed"),
    VALIDATE_FAILED(404, "validation failed"),
    FORBIDDEN(403, "no access permission");

    private Long code;
    private String message;

    /**
     * The constructor of an enumeration should be private to ensure that no additional types appear
     */
    private ResultCode(long code, String message) {
        this.code = code;
        this.message = message;
    }

//    public long getCode() {
//        return code;
//    }
//
//    public String getMessage() {
//        return message;
//    }
}
