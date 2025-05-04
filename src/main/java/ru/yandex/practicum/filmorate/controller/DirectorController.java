package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;
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
    public Optional<Director> getDirectorById(@PathVariable int id) {
        return filmService.getDirectorById(id);
    }

    @PostMapping
    public Director createDirector(@Valid  @RequestBody Director director) {
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