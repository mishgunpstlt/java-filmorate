package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "reviewId")
public class Review {
    private int reviewId;
    private String content;
    @JsonProperty("isPositive")
    private boolean positive; // Переименовано из isPositive
    private int userId;
    private int filmId;
    private int useful; // Рейтинг полезности
}