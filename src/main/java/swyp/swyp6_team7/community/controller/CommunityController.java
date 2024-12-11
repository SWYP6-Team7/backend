package swyp.swyp6_team7.community.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import swyp.swyp6_team7.auth.jwt.JwtProvider;
import swyp.swyp6_team7.category.repository.CategoryRepository;
import swyp.swyp6_team7.community.dto.request.CommunityCreateRequestDto;
import swyp.swyp6_team7.community.dto.request.CommunityUpdateRequestDto;
import swyp.swyp6_team7.community.dto.response.CommunityDetailResponseDto;
import swyp.swyp6_team7.community.dto.response.CommunityListResponseDto;
import swyp.swyp6_team7.community.dto.response.CommunitySearchCondition;
import swyp.swyp6_team7.community.repository.CommunityCustomRepository;
import swyp.swyp6_team7.category.domain.Category;

import swyp.swyp6_team7.community.service.CommunityListService;
import swyp.swyp6_team7.community.service.CommunityService;
import swyp.swyp6_team7.community.util.CommunitySearchSortingType;
import swyp.swyp6_team7.member.service.MemberService;
import swyp.swyp6_team7.member.util.MemberAuthorizeUtil;

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
    private final JwtProvider jwtProvider;


    //C
    @PostMapping("/posts")
    public ResponseEntity<CommunityDetailResponseDto> create(
            @RequestBody CommunityCreateRequestDto request, Principal principal) {

        //user number 가져오기
        Integer userNumber = MemberAuthorizeUtil.getLoginUserNumber();

        // 게시물 등록 동작 후 상세 정보 가져오기
        CommunityDetailResponseDto detailResponse = communityService.create(request, userNumber);

        return ResponseEntity.ok(detailResponse);
    }

    //게시물 목록
    @GetMapping("/posts")
    public ResponseEntity<?> getList(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            @RequestParam(name = "keyword",required = false) String keyword,
            @RequestParam(name = "categoryName", required = false) String categoryName,
            @RequestParam(name = "sortingTypeName", defaultValue = "최신순") String sortingTypeName,
            Principal principal) {

        Integer userNumber = (principal != null) ? MemberAuthorizeUtil.getLoginUserNumber() : null;
        if (principal == null) {
            log.info("비회원 커뮤니티 목록 조회 요청");
        } else {
            log.info("회원 커뮤니티 목록 조회 요청 - userNumber: {}", userNumber);
        }
        PageRequest pageRequest = PageRequest.of(page, size);

        Integer categoryNumber = null;
        // categoryName이 null이 아닐 경우에만 카테고리 번호를 조회
        if (categoryName != null) {
            categoryNumber = categoryRepository.findByCategoryName(categoryName)
                    .map(Category::getCategoryNumber)
                    .orElse(null); // 카테고리가 없을 경우 null
            if (categoryNumber == null) {
                log.warn("유효하지 않은 카테고리 이름: {}", categoryName);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Page.empty(pageRequest)); // 빈 페이지 반환
            }
            log.info("커뮤니티 목록 조회 - 카테고리 이름: {}, 카테고리 번호: {}", categoryName, categoryNumber);
        }

        CommunitySearchSortingType sortingType;
        try {
            sortingType = CommunitySearchSortingType.of(sortingTypeName);
            if (sortingTypeName == null || sortingTypeName.isEmpty()) {
                log.error("정렬 기준이 null 또는 빈 문자열입니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Page.empty(pageRequest)); // 빈 페이지 반환
            }
            log.info("커뮤니티 목록 조회 시 정렬 기준: {}", sortingType.getDescription());
        } catch (IllegalArgumentException e) {
            log.error("커뮤니티 목록 조회 시 잘못된 정렬 기준: {}", sortingTypeName, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Page.empty(pageRequest)); // 빈 페이지 반환
        }

        // 검색 조건 설정
        CommunitySearchCondition condition = CommunitySearchCondition.builder()
                .pageRequest(PageRequest.of(page, size))
                .keyword(keyword)
                .categoryNumber(categoryNumber)
                .sortingType(sortingType.toString())
                .build();

        try {
            Page<CommunityListResponseDto> result = communityListService.getCommunityList(pageRequest, condition, userNumber);
            log.info("조회 성공: 총 데이터 수 = {}", result.getTotalElements());
            return ResponseEntity.ok(result);
        } catch (DataAccessException e) {
            log.error("데이터베이스 접근 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("데이터를 불러오는 중 문제가 발생했습니다. 관리자에게 문의해주세요.");
        } catch (Exception e) {
            log.error("예상치 못한 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("알 수 없는 오류가 발생했습니다. 다시 시도해주세요.");
        }
    }


    //R
    @GetMapping("/posts/{postNumber}")
    public ResponseEntity<CommunityDetailResponseDto> getDetail( @PathVariable(name = "postNumber") int postNumber, Principal principal
    ) {

        //user number 가져오기
        Integer userNumber = (principal != null) ? MemberAuthorizeUtil.getLoginUserNumber() : null;

        //게시물 상세보기 데이터 가져오기
        CommunityDetailResponseDto detailResponse = communityService.increaseView(postNumber, userNumber);

        return ResponseEntity.ok(detailResponse);
    }

    //U
    @PutMapping("/posts/{postNumber}")
    public ResponseEntity<CommunityDetailResponseDto> update(
            @RequestBody CommunityUpdateRequestDto request, Principal principal, @PathVariable(name = "postNumber")  int postNumber) {

        //user number 가져오기
        Integer userNumber = MemberAuthorizeUtil.getLoginUserNumber();


        // 게시물 수정 동작 후 상세 정보 가져오기
        CommunityDetailResponseDto detailResponse = communityService.update(request, postNumber, userNumber);

        return ResponseEntity.ok(detailResponse);
    }

    @DeleteMapping("/posts/{postNumber}")
    public ResponseEntity<Void> delete(@PathVariable(name = "postNumber") int postNumber, Principal principal){

        //user number 가져오기
        Integer userNumber = MemberAuthorizeUtil.getLoginUserNumber();

        try {
            communityService.delete(postNumber, userNumber);
            // 성공 시 204
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("커뮤니티 게시물 삭제 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
