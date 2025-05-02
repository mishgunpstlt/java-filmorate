package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Director {
    @JsonProperty("id") // Аннотация для Jackson (чтобы тест принимал id)
    private int directorId;
    @NotBlank(message = "Название не может быть пустым")
    private String name;
}