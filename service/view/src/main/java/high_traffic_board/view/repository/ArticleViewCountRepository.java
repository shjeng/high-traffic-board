package high_traffic_board.view.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ArticleViewCountRepository {
    private final StringRedisTemplate redisTemplate;

    // view:article::{article_id}::view_count
    private static final String KEY_FORMAT = "view::article::%s::view_count";


    public Long read(Long articleId) {
        String result = redisTemplate.opsForValue().get(generateKey(articleId));
        return result == null ? 0 : Long.parseLong(result);
    }

    public Long increase(Long articleId) {
        /* 1이 자동으로 증가함. 데이터를 직접 넣어줄 수도 있는듯 */
        return redisTemplate.opsForValue().increment(generateKey(articleId));
    }

    private String generateKey(Long articleId) {
        return KEY_FORMAT.formatted(articleId);
    }

}
