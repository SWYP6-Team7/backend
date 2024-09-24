package swyp.swyp6_team7.profile.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import swyp.swyp6_team7.member.entity.UserProfile;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserProfileRepository;
import swyp.swyp6_team7.profile.dto.ProfileCreateRequest;
import swyp.swyp6_team7.profile.dto.ProfileUpdateRequest;
import swyp.swyp6_team7.profile.service.ProfileService;

import java.util.Optional;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final ProfileService profileService;
    private final UserProfileRepository userProfileRepository;

    public ProfileController(ProfileService profileService,UserProfileRepository userProfileRepository) {
        this.profileService = profileService;
        this.userProfileRepository = userProfileRepository;
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(@RequestBody ProfileUpdateRequest request) {
        System.out.println("Request Body: " + request);

        profileService.updateProfile(request);
        return ResponseEntity.ok("Profile updated successfully");
    }
    @PostMapping("/create")
    public ResponseEntity<String> createProfile(@RequestBody ProfileCreateRequest request) {
        profileService.createProfile(request);
        return ResponseEntity.ok("프로필이 생성되었습니다.");
    }
    @GetMapping("/me")
    public ResponseEntity<?> viewProfile(@RequestParam("userNumber")  Integer userNumber) {
        Optional<Users> user = profileService.getUserByUserNumber(userNumber);
        Optional<UserProfile> userProfile = profileService.getProfileByUserNumber(userNumber);

        if (user.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        if (userProfile.isEmpty()) {
            return ResponseEntity.status(404).body("User profile not found");
        }

        // 필요한 사용자 정보와 프로필 정보 반환
        return ResponseEntity.ok(new ProfileViewResponse(user.get(), userProfile.get()));
    }

    // 프로필 보기 응답 객체
    public static class ProfileViewResponse {
        private String name;
        private String proIntroduce;

        public ProfileViewResponse(Users user, UserProfile userProfile) {
            this.name = user.getUserName();
            this.proIntroduce = userProfile.getProIntroduce();
        }

        // Getters
        public String getName() {
            return name;
        }

        public String getProIntroduce() {
            return proIntroduce;
        }
    }
}
