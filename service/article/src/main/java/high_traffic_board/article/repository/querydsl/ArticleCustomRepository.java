package high_traffic_board.article.repository.querydsl;

import high_traffic_board.article.entity.Article;

import java.util.List;

public interface ArticleCustomRepository {
    List<Article> customFindAll(Long boardId, Long offset, Long limit);
    List<Long> testLongGet(Long boardId, Long offset, Long limit);
}
