package swyp.swyp6_team7.community.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import swyp.swyp6_team7.category.repository.CategoryRepository;
import swyp.swyp6_team7.community.dto.request.CommunityCreateRequestDto;
import swyp.swyp6_team7.community.dto.request.CommunityUpdateRequestDto;
import swyp.swyp6_team7.community.dto.response.CommunityDetailResponseDto;
import swyp.swyp6_team7.community.dto.response.CommunityListResponseDto;
import swyp.swyp6_team7.community.dto.response.CommunitySearchCondition;
import swyp.swyp6_team7.community.repository.CommunityCustomRepository;

import swyp.swyp6_team7.community.service.CommunityListService;
import swyp.swyp6_team7.community.service.CommunityService;
import swyp.swyp6_team7.community.util.CommunitySearchSortingType;
import swyp.swyp6_team7.member.service.MemberService;

import java.security.Principal;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/community")
public class CommunityController {
    private final CommunityService communityService;
    private final MemberService memberService;
    private final CategoryRepository categoryRepository;
    private final CommunityListService communityListService;


    //C
    @PostMapping("/posts")
    public ResponseEntity<CommunityDetailResponseDto> create(
            @RequestBody CommunityCreateRequestDto request, Principal principal) {

        //user number 가져오기
        int userNumber = memberService.findByEmail(principal.getName()).getUserNumber();

        // 게시물 등록 동작 후 상세 정보 가져오기
        CommunityDetailResponseDto detailResponse = communityService.create(request, userNumber);

        return ResponseEntity.ok(detailResponse);
    }

    //게시물 목록
    @GetMapping("/posts")
    public ResponseEntity<Page<CommunityListResponseDto>> getList(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categoryName,
            @RequestParam(defaultValue = "최신순") String sortingTypeName,
            Principal principal) {

        int userNumber = memberService.findByEmail(principal.getName()).getUserNumber();

        Integer categoryNumber = null;
        // categoryName이 null이 아닐 경우에만 카테고리 번호를 조회
        if (categoryName != null) {
            // 카테고리 이름에 대한 조회 시 예외 처리
            try {
                categoryNumber = categoryRepository.findByCategoryName(categoryName).getCategoryNumber();
            } catch (Exception e) {

            }
        }

        CommunitySearchSortingType sortingType = CommunitySearchSortingType.of(sortingTypeName);

        // 검색 조건 설정
        CommunitySearchCondition condition = CommunitySearchCondition.builder()
                .pageRequest(PageRequest.of(page, size))
                .keyword(keyword)
                .categoryNumber(categoryNumber)
                .sortingType(String.valueOf(sortingType))
                .build();

        Page<CommunityListResponseDto> result = communityListService.getCommunityList(PageRequest.of(page, size), condition, userNumber);


        return ResponseEntity.status(HttpStatus.OK)
                .body(result);    }


    //R
    @GetMapping("/posts/{postNumber}")
    public ResponseEntity<CommunityDetailResponseDto> getDetail( @PathVariable int postNumber, Principal principal
    ) {

        //user number 가져오기
        int userNumber = memberService.findByEmail(principal.getName()).getUserNumber();

        //게시물 상세보기 데이터 가져오기
        CommunityDetailResponseDto detailResponse = communityService.increaseView(postNumber, userNumber);

        return ResponseEntity.ok(detailResponse);
    }

    //U
    @PutMapping("/posts/{postNumber}")
    public ResponseEntity<CommunityDetailResponseDto> update(
            @RequestBody CommunityUpdateRequestDto request, Principal principal, @PathVariable int postNumber) {

        //user number 가져오기
        int userNumber = memberService.findByEmail(principal.getName()).getUserNumber();


        // 게시물 수정 동작 후 상세 정보 가져오기
        CommunityDetailResponseDto detailResponse = communityService.update(request, postNumber, userNumber);

        return ResponseEntity.ok(detailResponse);
    }

    @DeleteMapping("/posts/{postNumber}")
    public ResponseEntity<Void> delete(@PathVariable int postNumber, Principal principal){

        //user number 가져오기
        int userNumber = memberService.findByEmail(principal.getName()).getUserNumber();

        try {
            communityService.delete(postNumber, userNumber);
            // 성공 시 204
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            // 기타 오류 발생 시 500 Internal Server Error 반환
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
