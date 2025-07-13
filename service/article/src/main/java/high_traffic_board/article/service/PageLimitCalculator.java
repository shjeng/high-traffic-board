package high_traffic_board.article.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PageLimitCalculator {
    public static Long calculatePageLimit(Long page, Long pageSIze, Long movablePageCount) {
        return (((page - 1) / movablePageCount) + 1) * pageSIze * movablePageCount + 1;
    }
}
