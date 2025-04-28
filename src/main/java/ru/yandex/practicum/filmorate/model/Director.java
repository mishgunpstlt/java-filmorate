package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Director {
    @JsonProperty("id") // Аннотация для Jackson (чтобы тест принимал id)
    private int directorId;
    private String name;
}