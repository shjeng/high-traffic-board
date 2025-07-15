package high_traffic_board.article.repository;

import high_traffic_board.article.entity.Article;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class ArticleRepositoryTest {
    @Autowired
    ArticleRepository articleRepository;

    @Test
    void findAllTest() {
        List<Article> articles = articleRepository.findAll(1L, 1499970L, 30L);
        log.info("articles.size = {}", articles.size());
        for (Article article : articles) {
            log.info("article = {}", article);
        }
    }

    @Test
    void createTest() {
        Long count = articleRepository.count(1L, 10000L);
        log.info("count = {}", count);
    }

    @Test
    void querydslTest() {
        Long boardId = 1L;
        Long offset = 100000L;
        Long limit = 30L;

        StopWatch stopWatch = new StopWatch();

        stopWatch.start("QueryDSL");
        List<Article> querydsl = articleRepository.customFindAll(boardId, offset, limit);
        stopWatch.stop();

        System.out.println(stopWatch.prettyPrint());
    }


    @Test
    void slowTest() {
        Long boardId = 1L;
        Long offset = 100000L;
        Long limit = 30L;

        StopWatch stopWatch = new StopWatch();

        stopWatch.start("Slow");
        List<Article> slow = articleRepository.slowFindAll(boardId, offset, limit);
        stopWatch.stop();

        System.out.println(stopWatch.prettyPrint());
    }

    @Test
    void nativeQueryTest() {
        Long boardId = 1L;
        Long offset = 100000L;
        Long limit = 30L;

        StopWatch stopWatch = new StopWatch();

        stopWatch.start("Native Query");
        List<Article> nativeQuery = articleRepository.findAll(boardId, offset, limit);
        stopWatch.stop();

        System.out.println(stopWatch.prettyPrint());
    }


    @Test
    void findInfiniteScrollTest() {
        List<Article> articles = articleRepository.findAllIInfiniteScroll(1L, 30L);
        for (Article article : articles) {
            log.info("articleId = {}", article.getArticleId());
        }

        Long lastArticleId = articles.getLast().getArticleId();
        List<Article> articles2 = articleRepository.findAllIInfiniteScroll(1L, 30L, lastArticleId);
        for (Article article : articles2) {
            log.info("articleId = {}", article.getArticleId());
        }

    }

}