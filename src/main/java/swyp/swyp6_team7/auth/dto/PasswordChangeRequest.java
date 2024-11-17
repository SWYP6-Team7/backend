package swyp.swyp6_team7.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordChangeRequest {
    private String newPassword;
    private String newPasswordConfirm;
}
