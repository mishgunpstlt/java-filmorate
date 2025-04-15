package ru.yandex.practicum.filmorate.storage.dal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.enumModels.Genre;

@Data
@AllArgsConstructor
public class GenreDto {
    private int id;
    private String name;

    public static GenreDto fromEnum(Genre genre) {
        return new GenreDto(genre.getId(), genre.getFormattedName());
    }
}