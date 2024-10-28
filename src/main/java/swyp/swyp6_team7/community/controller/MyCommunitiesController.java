package swyp.swyp6_team7.community.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import swyp.swyp6_team7.community.dto.response.CommunityListResponseDto;
import swyp.swyp6_team7.community.dto.response.CommunityMyListResponseDto;
import swyp.swyp6_team7.community.service.CommunityListService;
import swyp.swyp6_team7.member.service.MemberService;

import java.security.Principal;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class MyCommunitiesController {

    private final MemberService memberService;
    private final CommunityListService communityListService;

    @GetMapping("/my-communities")
    public ResponseEntity<Page<CommunityMyListResponseDto>> getMyList(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            @RequestParam(defaultValue = "최신순") String sortingTypeName,
            Principal principal
            ) {
        
        //조회 중인 유저
        int userNumber = memberService.findByEmail(principal.getName()).getUserNumber();

        Page<CommunityMyListResponseDto> result = communityListService.getMyCommunityList(PageRequest.of(page, size), sortingTypeName, userNumber);

        return ResponseEntity.status(HttpStatus.OK)
                .body(result);

    }

}
