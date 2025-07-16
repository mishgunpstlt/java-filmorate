package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {
    Review addReview(Review review);

    Review updateReview(Review review);

    void deleteReview(int reviewId);

    Optional<Review> findReviewById(int reviewId);

    List<Review> findReviewsByFilmId(int filmId, int count);

    List<Review> findAllReviews(int count);

    void addLike(int reviewId, int userId);

    void addDislike(int reviewId, int userId);

    void removeLike(int reviewId, int userId);

    void removeDislike(int reviewId, int userId);
}