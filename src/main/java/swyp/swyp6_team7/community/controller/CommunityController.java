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
            @RequireUserNumber Integer userNumber
    ) {
        // TODO: Service layer 로 로직 이동
        PageRequest pageRequest = PageRequest.of(page, size);

        Integer categoryNumber = categoryRepository.findByCategoryName(categoryName)
                .map(Category::getCategoryNumber)
                .orElse(null);

        // 전체 요청이면 CategoryNumber 로 조회 X
        if (categoryName.equals("전체")) {
            categoryNumber = null;
        }

        CommunitySearchSortingType sortingType = CommunitySearchSortingType.of(sortingTypeName);

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

    // 커뮤니티 게시글 삭제
    @DeleteMapping("/posts/{postNumber}")
    public ApiResponse<String> delete(
            @PathVariable(name = "postNumber") int postNumber,
            @RequireUserNumber Integer userNumber
    ) {
        communityService.delete(postNumber, userNumber);
        return ApiResponse.success("커뮤니티 게시글이 삭제되었습니다.");
    }
}
