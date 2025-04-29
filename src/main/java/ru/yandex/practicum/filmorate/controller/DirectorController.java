package ru.yandex.practicum.filmorate.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/directors")
public class DirectorController {

    private final FilmService filmService;

    public DirectorController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public List<Director> getAllDirectors() {
        return filmService.getAllDirectors();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDirectorById(@PathVariable int id) {
        Optional<Director> directorOptional = filmService.getDirectorById(id);

        if (directorOptional.isPresent()) {
            return ResponseEntity.ok(directorOptional.get());
        } else {
            Map<String, String> errorResponse = Map.of("error", "Режиссер с ID " + id + " не найден");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @PostMapping
    public Director createDirector(@RequestBody Director director) {
        return filmService.createDirector(director);
    }

    @PutMapping
    public Director updateDirector(@RequestBody Director director) {
        return filmService.updateDirector(director);
    }

    @DeleteMapping("/{id}")
    public void deleteDirector(@PathVariable int id) {
        filmService.deleteDirector(id);
    }
}