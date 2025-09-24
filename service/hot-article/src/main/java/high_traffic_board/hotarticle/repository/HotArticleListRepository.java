package high_traffic_board.hotarticle.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class HotArticleListRepository {
    private final StringRedisTemplate redisTemplate;

    // hot-article::list::{yyyMMdd}
    private static final String KEY_FORMAT = "hot-article::list::%s";

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public void add(Long articleId, LocalDateTime time, Long score, Long limit, Duration ttl) {
        // redisTemplate.executePipelined(...): (성능 최적화) 여러 개의 Redis 명령을 하나로 묶어서 한 번의 네트워크 통신으로 처리합니다.
        // 요청이 많을 때 성능을 크게 향상시킵니다.
        redisTemplate.executePipelined((RedisCallback<?>) action -> {
            // ResdisCallback은 인터페이스
            // 스프링에게 "Redis 연결 객체를 받으면 내가 시키는 이 작업을 실행해 줘"라고 작업 내용을 전달하는 설명서와 같음
            // action은 Redis 서버와 직접 통신할 수 있는 통로라고 생각할 수 있음

            StringRedisConnection conn = (StringRedisConnection) action;
            // conn: action을 StringRedisConnection 타입으로 형변환한 변수임 .
            // action의 기본 타입은 RedisConnection인데, 이 타입은 데이터를 byte[] 배열로 다루기 때문에 사용하기 불편.

            String key = generateKey(time);

            conn.zAdd(key, score, String.valueOf(articleId));
            // zAdd() : Sorted Set에 멤버(데이터)를 추가하거나 점수를 업데이트 하는 역할
            // Sorted Set: 멤버(member)와 점수(score)를 한 쌍으로 저장하는 데이터 구조,  기준으로 항상 오름차순으로 자동 정렬됨.
            // 오늘 날짜로 된 key의 Scorted Set에 articleId를 score와 함께 추가함, 만약 articleId가 이미 존재한다면 score 업데이트됨.
            // key: 키, score: 정렬의 기준이 되는 숫자,

            conn.zRemRange(key, 0, -limit - 1);
            // Sorted Set에서 점수가 가장 낮은 항목부터 삭제하여 목록의 개수를 limit 개로 유지함.
            // 순위를 기준으로 범위를 지정해 멤버를 삭제하는 역할을 함.
            // 순위: Sorted Set에서 점수가 가장 낮은 멤버의 순위는 0, 가장 높은 멤버의 순위는 -1
            // 설명: 가장 낮은 멤버부터 삭제를 시작하겠다는 의미
            //          -limit -1 에서 만약 limit이 100이면 뒤에서 101번째 순위, 즉 100위권 밖의 멤버를 가르킴
            //          즉 위 코드는 점수가 가장 낮은 멤버(0위)부터 뒤에서 limit + 1번째 순위의 멤버까지 모두 삭제하라는 명령임
            // 음수 순위: -1은 점수가 가장 높은 멤버(1위), -2는 2위, -3은 3위
            // -4는 4위니깐 4위부터 다 삭제해라!

            conn.expire(key, ttl.toSeconds());
            return null;
        });
    }

    public void remove(Long articleId, LocalDateTime time) {
        redisTemplate.opsForZSet().remove(generateKey(time), String.valueOf(articleId));
    }

    private String generateKey(LocalDateTime time) {
        return generateKey(TIME_FORMATTER.format(time));
    }

    private String generateKey(String dateStr) {
        return KEY_FORMAT.formatted(dateStr);
    }

    public List<Long> readAll(String dateStr) {

        return redisTemplate.opsForZSet()
                .reverseRangeWithScores(generateKey(dateStr), 0, -1).stream() // 점수가 높은 순(역순)으로 가져옴, 0번부터 -1(마지막) 까지 점수와 함께 가져옴
                .peek(tuple -> log.info("[HotArticleListRepository.readAll] articleId={}, score={}", tuple.getValue(), tuple.getScore())) //
                .map(ZSetOperations.TypedTuple::getValue)
                .map(Long::valueOf)
                .toList();
    }
}
