package kuke.board.common.event;

import kuke.board.common.event.payload.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum EventType {
    ARTICLE_CREATED(ArticleCreatedEventPayload.class, Topic.KUKE_BOARD_ARTICLE),
    ARTICLE_UPDATE(ArticleUpdatedEventPayload.class, Topic.KUKE_BOARD_ARTICLE),
    ARTICLE_DELETED(ArticleDeletedEventPayload.class, Topic.KUKE_BOARD_ARTICLE),
    COMMENT_CREATED(CommentCreatedEventPayload.class, Topic.KUKE_BOARD_COMMENT),
    COMMENT_DELETED(CommentDeletedEventPayload.class, Topic.KUKE_BOARD_COMMENT),
    ARTICLE_LIKED(ArticleLikedEventPayload.class, Topic.KUKE_BOARD_LIKE),
    ARTICLE_UNLIKED(ArticleUnlikedEventPayload.class, Topic.KUKE_BOARD_LIKE),
    ARTICLE_VIEWED(ArticleViewedEventPayload.class, Topic.KUKE_BOARD_VIEW);

    private final Class<? extends EventPayload> payloadClass;
    private final String topic;

    public static EventType from(String type) {
        try {
            return valueOf(type);
        } catch (Exception e) {
            log.error("[EventType.from type={}", type, e);
            return null;
        }
    }

    public static class Topic {
        // 관련 상수들을 묶어서 관리하기 위해 내부 클래스로 만든 것으로 보여짐.

        public static final String KUKE_BOARD_ARTICLE = "kuke-board-article";
        public static final String KUKE_BOARD_COMMENT = "kuke-board-comment";
        public static final String KUKE_BOARD_LIKE = "kuke-board-like";
        public static final String KUKE_BOARD_VIEW = "kuke-board-view";
    }
}
