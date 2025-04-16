package ru.yandex.practicum.filmorate.storage.dal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.Genre;

@Data
@AllArgsConstructor
public class GenreDto {
    private int id;
    private String name;

    public static GenreDto fromModel(Genre genre) {
        return new GenreDto(genre.getId(), genre.getName());
    }
}