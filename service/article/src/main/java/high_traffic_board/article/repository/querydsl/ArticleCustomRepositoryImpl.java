package high_traffic_board.article.repository.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import high_traffic_board.article.entity.Article;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static high_traffic_board.article.entity.QArticle.article;

@RequiredArgsConstructor
public class ArticleCustomRepositoryImpl implements ArticleCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Article> customFindAll(Long boardId, Long offset, Long limit) {
        /* 서브쿼리 미사용*/
//        return jpaQueryFactory
//                .selectFrom(article)
//                .where(article.boardId.eq(boardId))
//                .orderBy(article.articleId.desc())
//                .offset(offset)
//                .limit(limit)
//                .fetch();
//        /* 서브쿼리 사용 */
        List<Long> articleIds = jpaQueryFactory
                .select(article.articleId)
                .from(article)
                .where(article.boardId.eq(boardId))
                .orderBy(article.articleId.desc())
                .limit(limit)
                .offset(offset)
                .fetch();

        return jpaQueryFactory
                .select(article)
                .from(article)
                .where(article.articleId.in(articleIds))
                .orderBy(article.articleId.desc())
                .fetch();
    }

}
