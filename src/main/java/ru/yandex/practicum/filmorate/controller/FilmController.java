package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.dal.dto.FilmDto;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FilmDto addFilm(@Valid @RequestBody FilmDto filmDto) {
        Film film = FilmDto.fromDto(filmDto);
        Film savedFilm = filmService.addFilm(film);
        return FilmDto.toDto(savedFilm);
    }

    @PutMapping
    public FilmDto updateFilm(@Valid @RequestBody FilmDto filmDto) {
        Film film = FilmDto.fromDto(filmDto);
        Film updatedFilm = filmService.updateFilm(film);
        return FilmDto.toDto(updatedFilm);
    }

    @GetMapping
    public Collection<FilmDto> getFilms() {
        return filmService.getFilms().stream()
                .map(FilmDto::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FilmDto> getFilmById(@PathVariable int id) {
        Optional<Film> film = filmService.getFilmById(id);
        if (film.isPresent()) {
            return ResponseEntity.ok(FilmDto.toDto(film.get()));
        } else {
            return ResponseEntity.notFound().build();  // 404 Not Found
        }
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable int id, @PathVariable int userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable int id, @PathVariable int userId) {
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<FilmDto> getPopularFilms(
            @RequestParam(defaultValue = "10") @Min(0) int count,
            @RequestParam(defaultValue = "0") int genreId,
            @RequestParam(defaultValue = "0") int year
            ) {
        return filmService.getPopularFilms(count, genreId, year).stream()
                .map(FilmDto::toDto)
                .collect(Collectors.toList());
    }
}
