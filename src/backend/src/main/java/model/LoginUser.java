package model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;


@Data
@AllArgsConstructor
public class LoginUser {

    private String id;
    private String roleID;
    private Date lastLogin;
    private String role;

}
