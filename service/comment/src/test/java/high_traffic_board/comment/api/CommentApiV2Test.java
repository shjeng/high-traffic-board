package high_traffic_board.comment.api;

import high_traffic_board.comment.service.response.CommentPageResponse;
import high_traffic_board.comment.service.response.CommentResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;

public class CommentApiV2Test {
    RestClient restClient = RestClient.create("http://localhost:9001");

    @Test
    void create() {
        CommentResponse response1 = create(new CommentCreateRequestV2(1L, "my comment1", null, 1L));
        CommentResponse response2 = create(new CommentCreateRequestV2(1L, "my comment2", response1.getPath(), 1L));
        CommentResponse response3 = create(new CommentCreateRequestV2(1L, "my comment3", response2.getPath(), 1L));

        System.out.println("response1.getCommentId() = " + response1.getCommentId());
        System.out.println("response2.getCommentId() = " + response2.getCommentId());
        System.out.println("response3.getCommentId() = " + response3.getCommentId());

        System.out.println("response1.getPath() = " + response1.getPath());
        System.out.println("response2.getPath() = " + response2.getPath());
        System.out.println("response3.getPath() = " + response3.getPath());

        System.out.println("response1.getCommentId() = " + response1.getCommentId());
        System.out.println("response2.getCommentId() = " + response2.getCommentId());
        System.out.println("response3.getCommentId() = " + response3.getCommentId());
    }

    CommentResponse create(CommentCreateRequestV2 request) {
        return restClient.post()
                .uri("/v2/comments")
                .body(request)
                .retrieve()
                .body(CommentResponse.class);
    }

    /*
     *
     * response1.getCommentId() = 207837705887768576
     * response2.getCommentId() = 207837706231701504
     * response3.getCommentId() = 207837706336559104
     * response1.getPath() = 00002
     * response2.getPath() = 0000200000
     * response3.getPath() = 000020000000000
     * response1.getCommentId() = 207837705887768576
     * response2.getCommentId() = 207837706231701504
     * response3.getCommentId() = 207837706336559104
     * */
    @Test
    void read() {
        CommentResponse response = restClient.get()
                .uri("/v2/comments/{commentId}", 207837705887768576L)
                .retrieve()
                .body(CommentResponse.class);
        System.out.println("response = " + response);
    }

    @Test
    void delete() {
        restClient.delete()
                .uri("/v2/comments/{commentId}", 207837705887768576L)
                .retrieve();
    }

    @Test
    void readAll() {
        CommentPageResponse response = restClient.get()
                .uri("/v2/comments?articleId=1&pageSize=10&page=1")
                .retrieve()
                .body(CommentPageResponse.class);
        System.out.println("response.getCommentCount() = " + response.getCommentCount());
        for (CommentResponse comment : response.getComments()) {
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }
    }

    /*readAll
    * comment.getCommentId() = 207836180813668352
comment.getCommentId() = 207836181954519040
comment.getCommentId() = 207836182050988032
comment.getCommentId() = 207837229276422144
comment.getCommentId() = 207837229708435456
comment.getCommentId() = 207837229834264576
comment.getCommentId() = 207837705887768576
comment.getCommentId() = 207837706231701504
comment.getCommentId() = 207837706336559104
comment.getCommentId() = 208198787667828741
    *
    * */


    @Test
    void readAllInfiniteScroll() {
        System.out.println("firstPage");
        List<CommentResponse> response = restClient.get()
                .uri("/v2/comments/infinite-scroll?articleId=1&pageSize=5")
                .retrieve()
                .body(new ParameterizedTypeReference<List<CommentResponse>>() {
                });
        for (CommentResponse commentResponse : response) {
            System.out.println("commentResponse.getCommentId() = " + commentResponse.getCommentId());
        }

        System.out.println("secondPage");
        String lastPath = response.getLast().getPath();
        List<CommentResponse> response2 = restClient.get()
                .uri("/v2/comments/infinite-scroll?articleId=1&pageSize=5&lastPath=%s".formatted(lastPath))
                .retrieve()
                .body(new ParameterizedTypeReference<List<CommentResponse>>() {
                });
        for (CommentResponse commentResponse : response2) {
            System.out.println("commentResponse.getCommentId() = " + commentResponse.getCommentId());
        }

    }

    @Test
    void countTest() {
        CommentResponse commentResponse = create(new CommentCreateRequestV2(2L, "my comment1", null, 1L));
        Long count1 = restClient.get()
                .uri("/v2/comments/articles/{articleId}/count", 2L)
                .retrieve()
                .body(Long.class);
        System.out.println("count1 = " + count1);

        restClient.delete()
                .uri("/v2/comments/{commentId}", commentResponse.getCommentId())
                .retrieve();

        Long count2 = restClient.get()
                .uri("/v2/comments/articles/{articleId}/count", 2L)
                .retrieve()
                .body(Long.class);
        System.out.println("count2 = " + count2);
    }

    @Getter
    @AllArgsConstructor
    public static class CommentCreateRequestV2 {
        private Long articleId;
        private String content;
        private String parentPath;
        private Long writerId;
    }
}
