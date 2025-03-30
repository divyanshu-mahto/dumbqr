package com.dumbqr.dumbqr.dto;

import lombok.Data;

@Data
public class VerifyUserEmailDto {
    private String email;
    private String code;
}
