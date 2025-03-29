package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserService userService;

    public FilmService(FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public Film addFilm(Film film) {
        film.setId(getNextId());
        filmStorage.addFilm(film);
        log.info("Добавлен новый объект в коллекцию(films): {}", film);
        return film;
    }

    public Film updateFilm(Film newFilm) {
        Film oldFilm = getExsitsFilm(newFilm.getId());
        filmStorage.updateFilm(newFilm);
        log.info("Изменен объект в коллекции(films), теперь новый объект: {}", newFilm);
        return newFilm;
    }

    public Collection<Film> getFilms() {
        log.info("Получены объекты коллекции(films): {}", filmStorage.getFilms());
        return filmStorage.getFilms();
    }

    private Film getExsitsFilm(int filmId) {
        if (filmStorage.findFilmById(filmId) != null) {
            log.info("Фильм с id={} найден", filmId);
            return filmStorage.findFilmById(filmId);
        } else {
            log.error("Фильм с id={} не найден", filmId);
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }
    }

    public Film getFilmById(int id) {
        return  getExsitsFilm(id);
    }

    public void addLike(int filmId, int userId) {
        userService.getUserById(userId);
        Film film = getFilmById(filmId);
        film.getLikes().add(userId);
        log.info("Пользователь с id={} поставил лайк фильму с id={}", userId, filmId);
    }

    public void removeLike(int filmId, int userId) {
        userService.getUserById(userId);
        Film film = getFilmById(filmId);
        film.getLikes().remove(userId);
        log.info("Пользователь с id={} удалил лайк с фильма с id={}", userId, filmId);
    }

    public List<Film> getPopularFilms(int count) {
        List<Film> popularFilms = filmStorage.getFilms().stream()
                .sorted(Comparator.comparingInt((Film film) -> film.getLikes().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());

        log.info("Получены популярные фильмы: {}", popularFilms);
        return popularFilms;
    }

    private int getNextId() {
        int currentMaxId = filmStorage.getFilms()
                .stream()
                .mapToInt(Film::getId)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}