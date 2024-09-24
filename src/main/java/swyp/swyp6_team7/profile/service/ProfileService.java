package swyp.swyp6_team7.profile.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.swyp6_team7.member.entity.Users;
import swyp.swyp6_team7.member.repository.UserProfileRepository;
import swyp.swyp6_team7.member.entity.UserProfile;
import swyp.swyp6_team7.member.repository.UserRepository;
import swyp.swyp6_team7.profile.dto.ProfileCreateRequest;
import swyp.swyp6_team7.profile.dto.ProfileUpdateRequest;

import java.util.Optional;

@Service
public class ProfileService {
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    public ProfileService(UserProfileRepository userProfileRepository,UserRepository userRepository) {
        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
    }

    public void createProfile(ProfileCreateRequest profileCreateRequest) {
        // 프로필 엔티티 생성
        UserProfile userProfile = new UserProfile();
        userProfile.setUserNumber(profileCreateRequest.getUserNumber());

        // 프로필 저장
        userProfileRepository.save(userProfile);
    }

    @Transactional
    public void updateProfile(ProfileUpdateRequest request) {
        System.out.println("Request received for user number: " + request.getUserNumber());
        Optional<UserProfile> userProfileOpt = userProfileRepository.findByUserNumber(request.getUserNumber());
        if (userProfileOpt.isEmpty()) {
            throw new IllegalArgumentException("프로필이 존재하지 않습니다. 새 프로필을 생성해주세요.");
        }
        UserProfile userProfile = userProfileOpt.get();  // 이미 조회한 값을 가져옵니다.

        // 프로필 정보 수정
        userProfile.setProIntroduce(request.getProIntroduce());

        userProfileRepository.save(userProfile);  // DB에 저장
        // Users 업데이트
        Optional<Users> usersOpt = userRepository.findById(request.getUserNumber());
        if (usersOpt.isPresent()) {
            Users user = usersOpt.get();
            user.setUserName(request.getName());       // name 업데이트
            userRepository.save(user);            // Users 저장
        } else {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
    }

    public Optional<UserProfile> getProfileByUserNumber(Integer userNumber) {
        return userProfileRepository.findByUserNumber(userNumber);
    }

    public Optional<Users> getUserByUserNumber(Integer userNumber) {
        return userRepository.findById(userNumber);
    }

}
