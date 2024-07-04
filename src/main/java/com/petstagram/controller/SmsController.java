package com.petstagram.controller;

import com.petstagram.service.SmsService;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/sms")
public class SmsController {
    private final SmsService smsService;

    @Autowired
    public SmsController(SmsService smsService) {
        this.smsService = smsService;
    }

    public static class SmsRequest {
        public String to;
    }

    public static class SmsResponse {
        public SingleMessageSentResponse response;
        public String verificationCode;

        public SmsResponse(SingleMessageSentResponse response, String verificationCode) {
            this.response = response;
            this.verificationCode = verificationCode;
        }
    }

    /* 문자 단일 메시지 전송 */
    @PostMapping("/send-one")
    public SmsResponse sendOne(@RequestBody SmsRequest smsRequest) {
        String verificationCode = smsService.generateVerificationCode();
        SingleMessageSentResponse response = smsService.sendOne(smsRequest.to, verificationCode);
        return new SmsResponse(response, verificationCode);
    }
}

