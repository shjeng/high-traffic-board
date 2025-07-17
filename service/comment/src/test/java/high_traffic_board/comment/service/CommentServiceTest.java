package high_traffic_board.comment.service;

import high_traffic_board.comment.entity.Comment;
import high_traffic_board.comment.repository.CommentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @InjectMocks
    CommentService commentService;
    @Mock
    CommentRepository commentRepository;

    @Test
    @DisplayName("삭제할 댓글이 자식 있으면, 삭제 표시만 한다.")
    void deleteShouldMarkDeletedIfHAshChildren() {
        // given
        Long articleId = 1L;
        Long commentId = 2L;
        Comment comment = createComment(articleId, commentId);

        // stub 설정(=given)
        given(commentRepository.findById(commentId))
                .willReturn(Optional.of(comment));
        given(commentRepository.countBy(articleId, commentId, 2L)).willReturn(2L);

        // when
        // 실제 서비스 호출
        commentService.delete(commentId);

        // ↓ 위 stub 덕분에 서비스 내부에서
        // 1) findById → Optional.of(comment)
        // 2) countBy   → 2L
        // 이 반환값을 받아서 branch(자식댓글 유무) 로직이 동작합니다.

        // then
        // 예상 결과: 1번 실행이 되어야됨. (논리적으로만 삭제)
        verify(comment).delete();
    }

    @Test
    @DisplayName("하위 댓글이 삭제되고, 삭제되지 않은 부모면 하위 댓글만 삭제한다.")
    void deleteShouldDeleteChildOnlyIfNotDeleteParent() {
        // given
        Long articleId = 1L;
        Long commentId = 2L;
        Long parentCommentId = 1L;
        Comment comment = createComment(articleId, commentId, parentCommentId);
        given(comment.isRoot()).willReturn(false);

        Comment parentComment = mock(Comment.class);
        given(parentComment.getDeleted()).willReturn(false);

        given(commentRepository.findById(commentId))
                .willReturn(Optional.of(comment));
        given(commentRepository.countBy(articleId, commentId, 2L)).willReturn(1L);

        given(commentRepository.findById(parentCommentId))
                .willReturn(Optional.of(parentComment));

        // when
        commentService.delete(commentId);

        // then
        verify(commentRepository).delete(comment);
        verify(commentRepository, never()).delete(parentComment);

    }

    @Test
    @DisplayName("하위 댓글이 삭제되고, 삭제된 부모면, 재귀적으로 모두 삭제")
    void deleteShouldDeleteAllRecursivelyIfDeleteParent() {
        // given
        Long articleId = 1L;
        Long commentId = 2L;
        Long parentCommentId = 1L;
        Comment comment = createComment(articleId, commentId, parentCommentId);
        given(comment.isRoot()).willReturn(false);

        Comment parentComment = createComment(articleId, parentCommentId);
        given(parentComment.isRoot()).willReturn(true);
        given(parentComment.getDeleted()).willReturn(true);

        given(commentRepository.findById(commentId))
                .willReturn(Optional.of(comment));
        given(commentRepository.countBy(articleId, commentId, 2L)).willReturn(1L);

        given(commentRepository.findById(parentCommentId))
                .willReturn(Optional.of(parentComment));
        given(commentRepository.countBy(articleId, parentCommentId, 2L)).willReturn(1L);

        // when
        commentService.delete(commentId);

        // then
        verify(commentRepository).delete(comment);
        verify(commentRepository).delete(parentComment);

    }

    private Comment createComment(Long articleId, Long commentId) {
        Comment comment = mock(Comment.class);
        given(comment.getArticleId()).willReturn(articleId);
        given(comment.getCommentId()).willReturn(commentId);
        return comment;
    }

    private Comment createComment(Long articleId, Long commentId, Long parentCommentId) {
        Comment comment = createComment(articleId, commentId);
        given(comment.getParentCommentId()).willReturn(parentCommentId);
        return comment;
    }
}