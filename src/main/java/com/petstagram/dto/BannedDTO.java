package com.petstagram.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BannedDTO {
    private Long id;
    private Long reportedUserId;
    private Long reporterUserId;
    private String reason;

    public BannedDTO(Long id, Long reportedUserId, Long reporterUserId, String reason) {
        this.id = id;
        this.reportedUserId = reportedUserId;
        this.reporterUserId = reporterUserId;
        this.reason = reason;
    }
}
