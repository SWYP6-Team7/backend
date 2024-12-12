package swyp.swyp6_team7.category.intializer;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import swyp.swyp6_team7.category.domain.Category;
import swyp.swyp6_team7.category.repository.CategoryRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CategoryInitializer {

    private final CategoryRepository categoryRepository;

    //프로그램이 실행될 때 카테고리 데이터가 들어가도록
    @PostConstruct
    public void init() {
        // 카테고리가 존재하지 않을 때만 데이터 삽입
        if (categoryRepository.count() == 0) {
            categoryRepository.saveAll(List.of(
                    new Category("전체"),
                    new Category("잡담"),
                    new Category("여행팁"),
                    new Category("후기")
            ));
        }
    }
}
