package com.petstagram.dto;

import lombok.Data;

@Data
public class KakaoAccountDto {
    private KakaoPropertiesDto properties;
    private KakaoAccountDetailsDto kakao_account;
}
