package ru.yandex.practicum.filmorate.storage.dal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.Review;

@Data
public class ReviewDto {
    private int reviewId;
    private String content;

    @JsonProperty("isPositive")
    @NotNull(message = "Not null")
    private Boolean positive;

    private int userId;
    private int filmId;
    private int useful;

    public static ReviewDto fromModel(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setReviewId(review.getReviewId());
        dto.setContent(review.getContent());
        dto.setPositive(review.getPositive());
        dto.setUserId(review.getUserId());
        dto.setFilmId(review.getFilmId());
        dto.setUseful(review.getUseful());
        return dto;
    }

    public static Review toModel(ReviewDto dto) {
        Review review = new Review();
        review.setReviewId(dto.getReviewId());
        review.setContent(dto.getContent());
        review.setPositive(dto.getPositive());
        review.setUserId(dto.getUserId());
        review.setFilmId(dto.getFilmId());
        review.setUseful(dto.getUseful());
        return review;
    }
}