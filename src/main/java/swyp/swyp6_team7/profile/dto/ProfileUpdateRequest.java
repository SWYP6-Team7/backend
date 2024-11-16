package swyp.swyp6_team7.profile.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileUpdateRequest {
    private String name;
    private String[] preferredTags;
    private String ageGroup;

}
