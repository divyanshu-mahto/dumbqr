package com.dumbqr.dumbqr.dto;

import lombok.Data;

@Data
public class VerifyForgotPasswordDto {
    private String email;
    private String code;
    private String newPassword;
}
