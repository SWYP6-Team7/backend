package swyp.swyp6_team7.category.domain;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Table(name = "Categories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Entity
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "community_category_number", updatable = false, nullable = false)
    private Integer categoryNumber;

    @Column(name = "community_category_name", updatable = false, nullable = false)
    private String categoryName;

    //C
    @Builder
    public Category(String categoryName) {
        this.categoryName = categoryName;
    }

}
