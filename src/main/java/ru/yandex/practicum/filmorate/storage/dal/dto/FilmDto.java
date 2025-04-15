package ru.yandex.practicum.filmorate.storage.dal.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.enumModels.Genre;
import ru.yandex.practicum.filmorate.model.enumModels.MPA;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Data
public class FilmDto {
    private int id;
    @NotBlank(message = "Название не может быть пустым")
    private String name;
    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;
    @NotNull(message = "Дата релиза не должна быть пустой")
    private LocalDate releaseDate;
    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private int duration;
    @NotNull(message = "У фильма не может не быть жанра")
    private List<GenreDto> genres = new ArrayList<>();
    @NotNull(message = "У фильма не может не быть МРА")
    private MpaDto mpa;

    @AssertTrue(message = "Дата релиза фильма должна быть не раньше 28 декабря 1895 года")
    public boolean isReleaseDateValid() {
        return !releaseDate.isBefore(LocalDate.of(1895, 12, 28));
    }

    public static FilmDto toDto(Film film) {
        FilmDto dto = new FilmDto();
        dto.setId(film.getId());
        dto.setName(film.getName());
        dto.setDescription(film.getDescription());
        dto.setReleaseDate(film.getReleaseDate());
        dto.setDuration(film.getDuration());

        dto.setGenres(
                film.getGenres().stream()
                        .sorted(Comparator.comparingInt(Genre::getId)) // Сортировка по ID
                        .map(GenreDto::fromEnum)
                        .collect(Collectors.toList()) // Используем List вместо Set
        );

        dto.setMpa(MpaDto.fromEnum(film.getMpa()));
        return dto;
    }

    public static Film fromDto(FilmDto dto) {
        Film film = new Film();
        film.setId(dto.getId());
        film.setName(dto.getName());
        film.setDescription(dto.getDescription());
        film.setReleaseDate(dto.getReleaseDate());
        film.setDuration(dto.getDuration());

        film.setGenres(
                dto.getGenres().stream()
                        .map(genreDto -> {
                            try {
                                return Genre.fromId(genreDto.getId());
                            } catch (IllegalArgumentException e) {
                                throw new NotFoundException("Неизвестный id жанра: " + genreDto.getId());
                            }
                        })
                        .collect(Collectors.toSet())
        );

        try {
            film.setMpa(MPA.fromId(dto.getMpa().getId()));
        } catch (IllegalArgumentException e) {
            throw new NotFoundException("Неизвестный id MPA: " + dto.getMpa().getId());
        }

        return film;
    }
}
