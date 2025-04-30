package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewStorage reviewStorage;

    public Review addReview(Review review) {
        return reviewStorage.addReview(review);
    }

    public Review updateReview(Review review) {
        if (!reviewStorage.findReviewById(review.getReviewId()).isPresent()) {
            throw new NotFoundException("Отзыв с id=" + review.getReviewId() + " не найден");
        }
        return reviewStorage.updateReview(review);
    }

    public void deleteReview(int reviewId) {
        if (!reviewStorage.findReviewById(reviewId).isPresent()) {
            throw new NotFoundException("Отзыв с id=" + reviewId + " не найден");
        }
        reviewStorage.deleteReview(reviewId);
    }

    public Review getReviewById(int reviewId) {
        return reviewStorage.findReviewById(reviewId).orElseThrow(() ->
                new NotFoundException("Отзыв с id=" + reviewId + " не найден"));
    }

    public List<Review> getReviewsByFilmId(int filmId, int count) {
        return reviewStorage.findReviewsByFilmId(filmId, count);
    }

    public List<Review> getAllReviews(int count) {
        return reviewStorage.findAllReviews(count);
    }

    public void addLike(int reviewId, int userId) {
        reviewStorage.addLike(reviewId, userId);
    }

    public void addDislike(int reviewId, int userId) {
        reviewStorage.addDislike(reviewId, userId);
    }

    public void removeLike(int reviewId, int userId) {
        reviewStorage.removeLike(reviewId, userId);
    }

    public void removeDislike(int reviewId, int userId) {
        reviewStorage.removeDislike(reviewId, userId);
    }
}