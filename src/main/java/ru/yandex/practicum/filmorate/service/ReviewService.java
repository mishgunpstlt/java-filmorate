package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.dal.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.dal.UserDbStorage;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserDbStorage userDbStorage;
    private final FilmDbStorage filmDbStorage;

    public Review addReview(Review review) {
        isUserExists(review.getUserId());
        isFilmExists(review.getFilmId());
        Review addedReview = reviewStorage.addReview(review);
        log.info("Добавлен новый отзыв: {}", addedReview);
        return addedReview;
    }

    public Review updateReview(Review review) {
        getExsitsReview(review.getReviewId());
        Review updatedReview = reviewStorage.updateReview(review);
        log.info("Обновлён отзыв: {}", updatedReview);
        return updatedReview;
    }

    private Optional<Review> getExsitsReview(int reviewId) {
        Optional<Review> review = reviewStorage.findReviewById(reviewId);
        if (review.isPresent()) {
            log.info("Отзыв с id={} найден", reviewId);
            return review;
        } else {
            log.error("Отзыв с id={} не найден", reviewId);
            throw new NotFoundException("Отзыв с id=" + reviewId + " не найден");
        }
    }

    private void isUserExists(int userId) {
        if (userDbStorage.findUserById(userId).isEmpty()) {
            log.error("Пользователь с id={} не найден", userId);
            throw new NotFoundException("User with ID " + userId + " not found");
        }
    }

    private void isFilmExists(int filmId) {
        if (filmDbStorage.findFilmById(filmId).isEmpty()) {
            log.error("Фильм с id={} не найден", filmId);
            throw new NotFoundException("Film with ID " + filmId + " not found");
        }
    }

    public void deleteReview(int reviewId) {
        getExsitsReview(reviewId);
        reviewStorage.deleteReview(reviewId);
        log.info("Удалён отзыв с id={}", reviewId);
    }

    public Optional<Review> getReviewById(int reviewId) {
        Optional<Review> review = getExsitsReview(reviewId);
        log.info("Получен отзыв по id {}: {}", reviewId, review);
        return review;
    }

    public List<Review> getReviewsByFilmId(int filmId, int count) {
        List<Review> reviews = reviewStorage.findReviewsByFilmId(filmId, count);
        log.info("Получены отзывы к фильму с id={}, количество: {}, результат: {}", filmId, count, reviews);
        return reviews;
    }

    public List<Review> getAllReviews(int count) {
        List<Review> reviews = reviewStorage.findAllReviews(count);
        log.info("Получены все отзывы, количество: {}, результат: {}", count, reviews);
        return reviews;
    }

    public void addLike(int reviewId, int userId) {
        isUserExists(userId);
        reviewStorage.addLike(reviewId, userId);
        log.info("Пользователь с id={} поставил лайк отзыву с id={}", userId, reviewId);
    }

    public void addDislike(int reviewId, int userId) {
        isUserExists(userId);
        reviewStorage.addDislike(reviewId, userId);
        log.info("Пользователь с id={} поставил дизлайк отзыву с id={}", userId, reviewId);
    }

    public void removeLike(int reviewId, int userId) {
        isUserExists(userId);
        reviewStorage.removeLike(reviewId, userId);
        log.info("Пользователь с id={} удалил лайк у отзыва с id={}", userId, reviewId);
    }

    public void removeDislike(int reviewId, int userId) {
        isUserExists(userId);
        reviewStorage.removeDislike(reviewId, userId);
        log.info("Пользователь с id={} удалил дизлайк у отзыва с id={}", userId, reviewId);
    }
}
