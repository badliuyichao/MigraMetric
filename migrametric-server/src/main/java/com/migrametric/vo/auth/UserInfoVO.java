package com.migrametric.vo.auth;

import lombok.Data;

@Data
public class UserInfoVO {

    private Long id;
    private String username;
    private String name;
    private String email;
    private String phone;
    private String avatar;
    private String role;
    private Integer status;
}