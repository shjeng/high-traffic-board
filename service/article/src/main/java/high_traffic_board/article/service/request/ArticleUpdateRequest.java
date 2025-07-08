package high_traffic_board.article.service.request;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ArticleUpdateRequest {
    private String title;
    private String content;
    private Long boardId; // shard key
    private Long writerId;
}
