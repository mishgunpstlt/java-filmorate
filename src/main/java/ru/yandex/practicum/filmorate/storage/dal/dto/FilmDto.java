package ru.yandex.practicum.filmorate.storage.dal.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
                        .map(GenreDto::fromModel)
                        .collect(Collectors.toList()) // Используем List вместо Set
        );

        dto.setMpa(MpaDto.fromModel(film.getMpa()));
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
                                return new Genre(genreDto.getId(), genreDto.getName());
                            } catch (IllegalArgumentException e) {
                                throw new NotFoundException("Неизвестный id жанра: " + genreDto.getId());
                            }
                        })
                        .collect(Collectors.toSet())
        );

        try {
            film.setMpa(new Mpa(dto.getMpa().getId(), dto.getMpa().getName()));
        } catch (IllegalArgumentException e) {
            throw new NotFoundException("Неизвестный id MPA: " + dto.getMpa().getId());
        }

        return film;
    }

    @AssertTrue(message = "Дата релиза фильма должна быть не раньше 28 декабря 1895 года")
    public boolean isReleaseDateValid() {
        return !releaseDate.isBefore(LocalDate.of(1895, 12, 28));
    }


}
