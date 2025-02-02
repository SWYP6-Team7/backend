package swyp.swyp6_team7.verify.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerifySession {
    private String email;
    private String code;
    @Setter
    private boolean verified;
}
