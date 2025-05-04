package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.dal.dto.FilmDto;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.storage.dal.FilmDbStorage.log;

@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping("/director/{directorId}")
    public ResponseEntity<?> getFilmsByDirector(
            @PathVariable int directorId,
            @RequestParam(defaultValue = "year") String sortBy) {
        // Проверяем допустимость параметра sortBy
        if (!sortBy.equals("year") && !sortBy.equals("likes")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Параметр sortBy должен быть 'year' или 'likes'"));
        }

        // Получаем фильмы режиссера
        List<Film> films = filmService.getFilmsByDirectorSorted(directorId, sortBy);

        // Если список фильмов пуст, возвращаем 404
        if (films.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Фильмы режиссера с ID " + directorId + " не найдены"));
        }

        // Возвращаем список фильмов с кодом 200
        return ResponseEntity.ok(films);
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

    @DeleteMapping("/{filmId}")
    public ResponseEntity<Void> deleteFilm(@PathVariable int filmId) {
        try {
            filmService.deleteFilm(filmId);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (NotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // 404 Not Found
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

    @GetMapping("/search")
    public List<FilmDto> searchFilms(@RequestParam String query, @RequestParam String by) {
        return filmService.searchFilms(query, by).stream()
                .map(FilmDto::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/common")
    public ResponseEntity<List<FilmDto>> getCommonFilms(
            @RequestParam int userId,
            @RequestParam int friendId) {

        List<Film> films = filmService.getCommonFilmsSortedByPopularity(userId, friendId);

        List<FilmDto> filmDtos = films.stream()
                .map(FilmDto::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(filmDtos);
    }
}
