package high_traffic_board.like.repository;

import high_traffic_board.like.entity.ArticleLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArticleLikeRepository extends JpaRepository<ArticleLike, Long> {
    Optional<ArticleLike> findByArticleIdAndUserId(Long articleId, Long userId);
}
