package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "reviewId")
public class Review {
    private int reviewId;
    private String content;
    @JsonProperty("isPositive")
    @NotNull(message = "Not null")
    private Boolean positive;
    private int userId;
    private int filmId;
    private int useful;
}