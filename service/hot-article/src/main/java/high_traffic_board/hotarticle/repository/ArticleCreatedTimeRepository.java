package high_traffic_board.hotarticle.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Repository
@RequiredArgsConstructor
public class ArticleCreatedTimeRepository {
    private final StringRedisTemplate redisTemplate;

    // hot-article::article::{articleId}::create-time
    private static final String KEY_FORMAT = "hot-article::article::%s::create-time";

    /**
     * Redis에 게시글 생성 시간을 저장합니다.
     *
     * @param articleId 게시글 ID
     * @param createAt  게시글 생성 시간
     * @param ttl       Redis 키의 만료 시간
     */
    public void create(Long articleId, LocalDateTime createAt, Duration ttl) {
        redisTemplate.opsForValue().set(
                generateKey(articleId),
                String.valueOf(createAt.toInstant(ZoneOffset.UTC).toEpochMilli()),
                ttl);

        // .toEpochMilli()
        //위에서 생성된 Instant 객체를 '유닉스 에포크(Unix Epoch)' 시간(1970년 1월 1일 00:00:00 UTC)으로부터
        // 몇 밀리초(millisecond)가 지났는지를 나타내는 long 타입의 숫자로 변환.
    }

    // 게시글 생성 시간을 위와 같이 저장하고 있으면, 게시글 서비스를 사용하지 않더라도 당일 게시글인지 바로 확인이 가능함.

    public void delete(Long articleId) {
        redisTemplate.delete(generateKey(articleId));
    }

    public LocalDateTime read(Long articleId) {
        String result = redisTemplate.opsForValue().get(generateKey(articleId));
        if (result == null) {
            return null;
        }
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(Long.parseLong(result)), ZoneOffset.UTC);
    }

    private String generateKey(Long articleId) {
        return String.format(KEY_FORMAT, articleId);
    }
}
