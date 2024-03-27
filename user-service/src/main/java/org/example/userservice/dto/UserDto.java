package org.example.userservice.dto;

import lombok.Data;
import org.example.userservice.vo.ResponseOrder;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Data
public class UserDto {
    private String email;
    private String name;
    private String pwd;
    private String userId;
    private LocalDate createdAt;

    private String encryptedPwd;

    private List<ResponseOrder> orders;

}
