package high_traffic_board.article.api;

import high_traffic_board.article.service.response.ArticleResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

public class ArticleApiTest {
    RestClient restClient = RestClient.create("http://localhost:9000");

    @Test
    void createTest() {
        ArticleResponse response = create(new ArticleCreateRequest("hi", "my content", 1L, 1L));
        System.out.println("response = " + response);
    }

    ArticleResponse create(ArticleCreateRequest request) {
        try {
            return restClient.post()
                    .uri("/v1/articles")
                    .body(request)
                    .retrieve()
                    .body(ArticleResponse.class);
        } catch (HttpClientErrorException e) {
            System.out.println("Error response: " + e.getResponseBodyAsString());
            throw e;
        }
    }

    @Test
    void readTest() {
        ArticleResponse response = read(200942534513180672L);
        System.out.println("response = " + response);
    }

    @Test
    void updateTest() {
        update(200942534513180672L);
        ArticleResponse response = read(200942534513180672L);
        System.out.println("response = " + response);
    }

    @Test
    void deleteTest() {
        restClient.delete()
                .uri("/v1/articles/{articleId}", 200942534513180672L)
                .retrieve();
    }

    void update(Long articleId) {
        restClient.put()
                .uri("/v1/articles/{articleId}", articleId)
                .body(new ArticleUpdateRequest("h1 2", "my content 22"))
                .retrieve()
                .body(ArticleResponse.class);
    }

    ArticleResponse read(Long articleId) {
        return restClient.get()
                .uri("/v1/articles/{articleId}", articleId)
                .retrieve()
                .body(ArticleResponse.class);
    }

    @Getter
    @AllArgsConstructor
    static class ArticleCreateRequest {
        private String title;
        private String content;
        private Long boardId; // shard key
        private Long writerId;
    }

    @Getter
    @AllArgsConstructor
    static class ArticleUpdateRequest {
        private String title;
        private String content;
    }
}
