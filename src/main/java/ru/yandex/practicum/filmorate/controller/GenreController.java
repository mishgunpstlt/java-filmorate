package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.dal.dto.GenreDto;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/genres")
public class GenreController {

    private final FilmService filmService;

    public GenreController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public List<GenreDto> getGenres() {
        return filmService.getGenres().stream()
                .map(GenreDto::fromModel)
                .sorted(Comparator.comparingInt(GenreDto::getId))
                .toList();
    }

    @GetMapping("/{id}")
    public GenreDto getGenreById(@PathVariable int id) {
        Genre genre = filmService.getGenreById(id);
        return GenreDto.fromModel(genre);
    }
}
