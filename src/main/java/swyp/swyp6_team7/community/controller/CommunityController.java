package swyp.swyp6_team7.community.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import swyp.swyp6_team7.category.domain.Category;
import swyp.swyp6_team7.category.repository.CategoryRepository;
import swyp.swyp6_team7.community.dto.request.CommunityCreateRequestDto;
import swyp.swyp6_team7.community.dto.request.CommunityUpdateRequestDto;
import swyp.swyp6_team7.community.dto.response.CommunityDetailResponseDto;
import swyp.swyp6_team7.community.dto.response.CommunityListResponseDto;
import swyp.swyp6_team7.community.dto.response.CommunitySearchCondition;
import swyp.swyp6_team7.community.service.CommunityListService;
import swyp.swyp6_team7.community.service.CommunityService;
import swyp.swyp6_team7.community.util.CommunitySearchSortingType;
import swyp.swyp6_team7.global.exception.MoingApplicationException;
import swyp.swyp6_team7.global.utils.api.ApiResponse;
import swyp.swyp6_team7.global.utils.auth.RequireUserNumber;

import java.security.Principal;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/community")
public class CommunityController {
    private final CommunityService communityService;
    private final CategoryRepository categoryRepository;
    private final CommunityListService communityListService;


    //C
    @PostMapping("/posts")
    public ApiResponse<CommunityDetailResponseDto> create(
            @RequestBody CommunityCreateRequestDto request,
            @RequireUserNumber Integer userNumber
    ) {
        log.info("UserNumber: {}", userNumber);

        // 게시물 등록 동작 후 상세 정보 가져오기
        CommunityDetailResponseDto detailResponse = communityService.create(request, userNumber);
        log.info("Response from Service: {}", detailResponse);

        return ApiResponse.success(detailResponse);
    }

    //게시물 목록
    @GetMapping("/posts")
    public ApiResponse<Page<CommunityListResponseDto>> getList(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "categoryName", defaultValue = "전체") String categoryName,
            @RequestParam(name = "sortingTypeName", defaultValue = "최신순") String sortingTypeName,
            Principal principal,
            @RequireUserNumber Integer userNumber
    ) {
        // TODO: Service layer 로 로직 이동

        if (principal == null) {
            log.info("비회원 커뮤니티 목록 조회 요청");
        } else {
            log.info("회원 커뮤니티 목록 조회 요청 - userNumber: {}", userNumber);
        }
        PageRequest pageRequest = PageRequest.of(page, size);

        Integer categoryNumber = null;
        if (categoryName != null && !categoryName.equals("전체")) {
            categoryNumber = categoryRepository.findByCategoryName(categoryName)
                    .map(Category::getCategoryNumber)
                    .orElse(null);
            if (categoryNumber == null) {
                log.warn("유효하지 않은 카테고리 이름: {}", categoryName);
                return ApiResponse.success(Page.empty()); //빈 페이지 반환
            }
            log.info("커뮤니티 목록 조회 - 카테고리 이름: {}, 카테고리 번호: {}", categoryName, categoryNumber);
        }

        CommunitySearchSortingType sortingType;
        try {
            sortingType = CommunitySearchSortingType.of(sortingTypeName);
            if (sortingTypeName == null || sortingTypeName.isEmpty()) {
                log.error("정렬 기준이 null 또는 빈 문자열입니다.");
                return ApiResponse.success(Page.empty());  // 빈 페이지 반환
            }
            log.info("커뮤니티 목록 조회 시 정렬 기준: {}", sortingType.getDescription());
        } catch (IllegalArgumentException e) {
            log.error("커뮤니티 목록 조회 시 잘못된 정렬 기준: {}", sortingTypeName, e);
            return ApiResponse.success(Page.empty());  // 빈 페이지 반환
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
            if (result == null) {
                log.error("커뮤니티 목록 조회 결과가 null입니다.");
                throw new MoingApplicationException("조회 결과를 불러올 수 없습니다. 관리자에게 문의하세요.");
            }
            log.info("조회 성공: 총 데이터 수 = {}", result.getTotalElements());
            return ApiResponse.success(result);
        } catch (DataAccessException e) {
            log.error("데이터베이스 접근 오류: {}", e.getMessage(), e);
            throw new MoingApplicationException("조회 결과를 불러올 수 없습니다. 관리자에게 문의하세요.");
        } catch (Exception e) {
            log.error("예상치 못한 오류: {}", e.getMessage(), e);
            throw new MoingApplicationException("조회 결과를 불러올 수 없습니다. 관리자에게 문의하세요.");
        }
    }


    //R
    @GetMapping("/posts/{postNumber}")
    public ApiResponse<CommunityDetailResponseDto> getDetail(
            @PathVariable(name = "postNumber") int postNumber,
            @RequireUserNumber Integer userNumber
    ) {
        //게시물 상세보기 데이터 가져오기
        CommunityDetailResponseDto detailResponse = communityService.increaseView(postNumber, userNumber);

        return ApiResponse.success(detailResponse);
    }

    //U
    @PutMapping("/posts/{postNumber}")
    public ApiResponse<CommunityDetailResponseDto> update(
            @RequestBody CommunityUpdateRequestDto request,
            @PathVariable(name = "postNumber") int postNumber,
            @RequireUserNumber Integer userNumber
    ) {
        // 게시물 수정 동작 후 상세 정보 가져오기
        CommunityDetailResponseDto detailResponse = communityService.update(request, postNumber, userNumber);

        return ApiResponse.success(detailResponse);
    }

    @DeleteMapping("/posts/{postNumber}")
    public ApiResponse<Void> delete(
            @PathVariable(name = "postNumber") int postNumber,
            @RequireUserNumber Integer userNumber
    ) {

        try {
            communityService.delete(postNumber, userNumber);
            // 성공 시 204
            return ApiResponse.success(null);
        } catch (Exception e) {
            log.error("커뮤니티 게시물 삭제 중 오류 발생: {}", e.getMessage(), e);
            throw new MoingApplicationException("커뮤니티 게시물 삭제 중 오류 발생." + e.getMessage());
        }
    }
}
