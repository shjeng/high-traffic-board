package high_traffic_board.comment.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Table(name = "comment")
@Getter
@Entity
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

    @Id
    private Long commentId;
    private String content;
    private Long parentCommentId;
    private Long articleId; // shard key
    private Long writerId;
    private Boolean deleted;
    private LocalDateTime createdAt;

    public static Comment create(Long commentId, String content, Long parentCommentId, Long articleId, Long writerId) {
        Comment comment = new Comment();
        comment.commentId = commentId;
        comment.content = content;
        comment.parentCommentId = parentCommentId == null ? commentId : parentCommentId;
        comment.articleId = articleId;
        comment.writerId = writerId;
        comment.deleted = false;
        comment.createdAt = LocalDateTime.now();
        return comment;

        /* **parentCommentId가 null일 때 자기 자신 id를 넣는 이유**
        * 1.  애플리케이션과 DB 모두 null 검사를 직접 안 해도 됨.
        * 2. DB마다 null처리 방식이 다를 수 있음.
        *     예) 인덱스 트리에서 null이 가장 작은 값으로 판단될 수도 있고, 가장 큰 값으로 판단될 수가 있음.  이 경우 값을 명시하는 게 명확하고 유리할 수 있따.
        * 3. 현재 설정된 인덱스는 parentCommentId ASC, commentId ASC임.
        *      null이면 이러한 순서가 명확하지 않을 수 있음.
        * */
    }

    public boolean isRoot() {
        return parentCommentId.longValue() == commentId;
    }

    public void delete() {
        deleted = true;
    }
}
