package high_traffic_board.comment.api;

import high_traffic_board.comment.service.response.CommentResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

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

    @Getter
    @AllArgsConstructor
    public static class CommentCreateRequestV2 {
        private Long articleId;
        private String content;
        private String parentPath;
        private Long writerId;
    }
}
