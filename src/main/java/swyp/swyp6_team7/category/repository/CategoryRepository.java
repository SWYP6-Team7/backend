package swyp.swyp6_team7.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import swyp.swyp6_team7.category.domain.Category;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    // 카테고리 번호로 카테고리 찾기
    Optional<Category>  findByCategoryNumber(Integer categoryNumber);

    //카테고리 명으로 카테고리  찾기
    Optional<Category>  findByCategoryName(String categoryName);
}
