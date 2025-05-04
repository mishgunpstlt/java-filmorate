package ru.yandex.practicum.filmorate.storage.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.enumModels.EventType;
import ru.yandex.practicum.filmorate.model.enumModels.Operation;
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
        String sql = "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) VALUES (?, ?, ?, ?, ?)";
        jdbc.update(sql, review.getContent(), review.getPositive(), review.getUserId(), review.getFilmId(), 0);

        String idSql = "SELECT MAX(review_id) FROM reviews";
        int reviewId = jdbc.queryForObject(idSql, Integer.class);

        UserDbStorage.addFeed(jdbc, review.getUserId(), reviewId, EventType.REVIEW, Operation.ADD);
        return findReviewById(reviewId).orElseThrow();
    }

    @Override
    public Review updateReview(Review review) {
        String sql = "UPDATE reviews SET content = ?, is_positive = ? " +
                "WHERE review_id = ?";
        jdbc.update(sql, review.getContent(), review.getPositive(), review.getReviewId());

        Review reviewDB = findReviewById(review.getReviewId()).orElseThrow();
        UserDbStorage.addFeed(jdbc, reviewDB.getUserId(), reviewDB.getReviewId(), EventType.REVIEW, Operation.UPDATE);
        return reviewDB;
    }

    @Override
    public void deleteReview(int reviewId) {
        Optional<Review> review = findReviewById(reviewId);
        if (review.isPresent()) {
            UserDbStorage.addFeed(jdbc, review.get().getUserId(), reviewId, EventType.REVIEW, Operation.REMOVE);
        }

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
        checkUlness(reviewId, userId);
        if (!isReviewLikeExists(reviewId, userId)) {
            String sql = "INSERT INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, true)";
            jdbc.update(sql, reviewId, userId);
        }
        updateUsefulness(reviewId, true);
    }

    @Override
    public void addDislike(int reviewId, int userId) {
        checkUlness(reviewId, userId);
        if (!isReviewLikeExists(reviewId, userId)) {
            String sql = "INSERT INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, false)";
            jdbc.update(sql, reviewId, userId);
        }
        updateUsefulness(reviewId, false);
    }

    public void checkUlness(int reviewId, int userId) {
        String sqlTest = "SELECT is_like FROM review_likes WHERE review_id = ? AND user_id = ?";
        Boolean isLike;

        try {
            isLike = jdbc.queryForObject(sqlTest, Boolean.class, reviewId, userId);
        } catch (EmptyResultDataAccessException e) {
            return;
        }

        if (isLike == false) {
            removeDislike(reviewId, userId);
        } else {
            removeLike(reviewId, userId);
        }
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
        int delta = isLike ? +1 : -1;
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