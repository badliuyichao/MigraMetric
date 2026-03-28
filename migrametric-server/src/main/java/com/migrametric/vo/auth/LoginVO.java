package com.migrametric.vo.auth;

import lombok.Data;

@Data
public class LoginVO {

    private String token;
    private Long expiresIn;
}