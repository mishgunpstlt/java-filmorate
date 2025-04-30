package ru.yandex.practicum.filmorate.storage.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.BadRequestException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbc;

    @Override
    public Review addReview(Review review) {
        // Проверяем корректность userId
        if (review.getUserId() <= 0) {
            throw new NotFoundException("Invalid userId: " + review.getUserId());
        }

        // Проверяем корректность filmId
        if (review.getFilmId() <= 0) {
            throw new BadRequestException("Invalid filmId: " + review.getFilmId());
        }

        // Проверяем существование пользователя
        if (!isUserExists(review.getUserId())) {
            throw new BadRequestException("User with ID " + review.getUserId() + " not found");
        }

        // Проверяем существование фильма
        if (!isFilmExists(review.getFilmId())) {
            throw new NotFoundException("Film with ID " + review.getFilmId() + " not found");
        }

//        if ((review.isPositive())) { // Используйте метод getIsPositive(), если он реализован
//            throw new BadRequestException("Field 'isPositive' is required");
//        }

        // Добавляем отзыв
        String sql = "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) VALUES (?, ?, ?, ?, ?)";
        jdbc.update(sql, review.getContent(), review.isPositive(), review.getUserId(), review.getFilmId(), 0);

        // Получаем ID последнего добавленного отзыва
        String idSql = "SELECT MAX(review_id) FROM reviews";
        int reviewId = jdbc.queryForObject(idSql, Integer.class);
        return findReviewById(reviewId).orElseThrow();
    }

    public boolean isUserExists(int userId) {
        String sql = "SELECT COUNT(*) FROM users WHERE user_id = ?";
        return jdbc.queryForObject(sql, Integer.class, userId) > 0;
    }

    private boolean isFilmExists(int filmId) {
        String sql = "SELECT COUNT(*) FROM films WHERE film_id = ?";
        return jdbc.queryForObject(sql, Integer.class, filmId) > 0;
    }


    @Override
    public Review updateReview(Review review) {
        String sql = "UPDATE reviews SET content = ?, is_positive = ?, user_id = ?, film_id = ?, useful = ? " +
                "WHERE review_id = ?";
        jdbc.update(sql, review.getContent(), review.isPositive(), review.getUserId(), review.getFilmId(),
                review.getUseful(), review.getReviewId());
        return findReviewById(review.getReviewId()).orElseThrow();
    }

    @Override
    public void deleteReview(int reviewId) {
        String sql = "DELETE FROM reviews WHERE review_id = ?";
        jdbc.update(sql, reviewId);
    }

    @Override
    public Optional<Review> findReviewById(int reviewId) {
        String sql = "SELECT * FROM reviews WHERE review_id = ?";
        return jdbc.query(sql, this::mapRowToReview, reviewId).stream().findFirst();
    }

    @Override
    public List<Review> findReviewsByFilmId(int filmId, int count) {
        String sql = "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?";
        return jdbc.query(sql, this::mapRowToReview, filmId, count);
    }

    @Override
    public List<Review> findAllReviews(int count) {
        String sql = "SELECT * FROM reviews ORDER BY useful DESC LIMIT ?";
        return jdbc.query(sql, this::mapRowToReview, count);
    }

    @Override
    public void addLike(int reviewId, int userId) {
        if (!isReviewLikeExists(reviewId, userId)) {
            String sql = "INSERT INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, true)";
            jdbc.update(sql, reviewId, userId);
        }
        updateUsefulness(reviewId, true);
    }

    @Override
    public void addDislike(int reviewId, int userId) {
        if (!isReviewLikeExists(reviewId, userId)) {
            String sql = "INSERT INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, false)";
            jdbc.update(sql, reviewId, userId);
        }
        updateUsefulness(reviewId, false);
    }

    private boolean isReviewLikeExists(int reviewId, int userId) {
        String sql = "SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND user_id = ?";
        return jdbc.queryForObject(sql, Integer.class, reviewId, userId) > 0;
    }

    @Override
    public void removeLike(int reviewId, int userId) {
        String sql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_like = true";
        jdbc.update(sql, reviewId, userId);
        updateUsefulness(reviewId, false);
    }

    @Override
    public void removeDislike(int reviewId, int userId) {
        String sql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_like = false";
        jdbc.update(sql, reviewId, userId);
        updateUsefulness(reviewId, true);
    }

    public void updateUsefulness(int reviewId, boolean isLike) {
        String sql = "UPDATE reviews SET useful = useful + ? WHERE review_id = ?";
        int delta = isLike ? 1 : -1; // +1 для лайка, -1 для дизлайка
        jdbc.update(sql, delta, reviewId);
    }

    private Review mapRowToReview(ResultSet rs, int rowNum) throws SQLException {
        Review review = new Review();
        review.setReviewId(rs.getInt("review_id"));
        review.setContent(rs.getString("content"));
        review.setPositive(rs.getBoolean("is_positive"));
        review.setUserId(rs.getInt("user_id"));
        review.setFilmId(rs.getInt("film_id"));
        review.setUseful(rs.getInt("useful"));
        return review;
    }

    private int getLastInsertedReviewId() {
        String sql = "SELECT MAX(review_id) FROM reviews";
        return jdbc.queryForObject(sql, Integer.class);
    }
}