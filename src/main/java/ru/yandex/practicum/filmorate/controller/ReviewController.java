package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;
import ru.yandex.practicum.filmorate.storage.dal.dto.ReviewDto;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewDto addReview(@RequestBody @Valid ReviewDto reviewDto) {
        Review review = ReviewDto.toModel(reviewDto);
        Review savedReview = reviewService.addReview(review);
        return ReviewDto.fromModel(savedReview);
    }

    @PutMapping
    public ReviewDto updateReview(@RequestBody @Valid ReviewDto reviewDto) {
        Review review = ReviewDto.toModel(reviewDto);
        Review updatedReview = reviewService.updateReview(review);
        return ReviewDto.fromModel(updatedReview);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable int id) {
        reviewService.deleteReview(id);
    }

    @GetMapping("/{id}")
    public ReviewDto getReviewById(@PathVariable int id) {
        Review review = reviewService.getReviewById(id);
        return ReviewDto.fromModel(review);
    }

    @GetMapping
    public List<ReviewDto> getReviews(@RequestParam(required = false) Integer filmId,
                                      @RequestParam(defaultValue = "10") int count) {
        List<Review> reviews = (filmId == null)
                ? reviewService.getAllReviews(count)
                : reviewService.getReviewsByFilmId(filmId, count);
        return reviews.stream()
                .map(ReviewDto::fromModel)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable int id, @PathVariable int userId) {
        reviewService.addLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable int id, @PathVariable int userId) {
        reviewService.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable int id, @PathVariable int userId) {
        reviewService.removeLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDislike(@PathVariable int id, @PathVariable int userId) {
        reviewService.removeDislike(id, userId);
    }
}