package high_traffic_board.like.api;

import high_traffic_board.like.service.response.ArticleLikeResponse;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LikeApiTest {
    RestClient restClient = RestClient.create("http://localhost:9002");

    @Test
    void likeAndUnLikeTest() {
        Long articleId = 9999L;

        like(articleId, 1L, "pessimistic-lock-1");
        like(articleId, 2L, "pessimistic-lock-2");
        like(articleId, 3L, "optimistic-lock");


        ArticleLikeResponse response1 = read(articleId, 1L);
        ArticleLikeResponse response2 = read(articleId, 2L);
        ArticleLikeResponse response3 = read(articleId, 3L);

        System.out.println("response1 = " + response1);
        System.out.println("response2 = " + response2);
        System.out.println("response3 = " + response3);

        unlike(articleId, 1L);
        unlike(articleId, 2L);
        unlike(articleId, 3L);
    }


    void like(Long articleId, Long userId, String lockType) {
        restClient.post()
                .uri("/v1/article-likes/articles/{articleId}/users/{userId}/" + lockType, articleId, userId)
                .retrieve();
    }

    void unlike(Long articleId, Long userId) {
        restClient.delete()
                .uri("/v1/article-likes/articles/{articleId}/users/{userId}", articleId, userId)
                .retrieve();
    }

    ArticleLikeResponse read(Long articleId, Long userId) {
        return restClient.get()
                .uri("/v1/article-likes/articles/{articleId}/users/{userId}", articleId, userId)
                .retrieve()
                .body(ArticleLikeResponse.class);
    }

    @Test
    void likePerformanceTest() throws InterruptedException {
        // ExecutorService: 멀티 스레드 환경에서 작업을 관리하기 위한 인터페이스
        // Executors.newFixedThreadPool(100): 최대 100개의 스레드를 가진 스레드 풀을 생성
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        likePerformanceTest(executorService, 1111L, "pessimistic-lock-1");
        likePerformanceTest(executorService, 2222L, "pessimistic-lock-2");
        likePerformanceTest(executorService, 3333L, "optimistic-lock");
    }

    /**
     * 수많은 사용자가 아주 짧은 시간 안에 동시에 특정 게시물에 좋아요를 누르는 상황을 만듦
     *
     * @param executorService: 테스트를 위한 스레드 풀, 여러 작업을 동시에 실행하기 위해 사용
     */

    void likePerformanceTest(ExecutorService executorService, Long articleId, String lockType) throws InterruptedException {
        /*
         * CountDownLatch: 일종의 카운터 역할을 하는 동기화 도우미
         * 카운터를 3000으로 초기화
         * */
        CountDownLatch latch = new CountDownLatch(3000);
        System.out.println(lockType + " start");
        like(articleId, 1L, lockType);

        long start = System.nanoTime();
        for (int i = 0; i < 3000; i++) {
            long userId = i + 2;
            executorService.submit(() -> {
                like(articleId, userId, lockType);
                latch.countDown(); // 카운터가 1씩 감소
            });
        }
        latch.await(); // 카운터가 0이 될 때까지 현재 스레드를 대기, 비동기 작업이기 때문에 latch를 사용함
        long end = System.nanoTime();
        System.out.println("lockType = " + lockType + ", time = " + (end - start) / 1_000_000 + " ms");

        Long count = restClient.get()
                .uri("/v1/article-likes/articles/{articleId}/count", articleId)
                .retrieve()
                .body(Long.class);
        System.out.println("count = " + count);
    }
}
