package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.enumModels.Genre;
import ru.yandex.practicum.filmorate.model.enumModels.MPA;
import ru.yandex.practicum.filmorate.storage.dal.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.dal.UserDbStorage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class FilmService {

    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;

    public FilmService(FilmDbStorage filmDbStorage, UserDbStorage userDbStorage) {
        this.filmDbStorage = filmDbStorage;
        this.userDbStorage = userDbStorage;
    }

    public Film addFilm(Film film) {
        filmDbStorage.addFilm(film);
        log.info("Добавлен новый объект в коллекцию(films): {}", film);
        return film;
    }

    public Film updateFilm(Film newFilm) {
        getExsitsFilm(newFilm.getId());
        Film updatedFilm = filmDbStorage.updateFilm(newFilm);
        log.info("Изменен объект в коллекции(films), теперь новый объект: {}", updatedFilm);
        return updatedFilm;
    }

    public Collection<Film> getFilms() {
        log.info("Получены объекты коллекции(films): {}", filmDbStorage.getFilms());
        return filmDbStorage.getFilms();
    }

    private Optional<Film> getExsitsFilm(int filmId) {
        Optional<Film> film = filmDbStorage.findFilmById(filmId);
        if (film.isPresent()) {
            log.info("Фильм с id={} найден", filmId);
            return film;
        } else {
            log.error("Фильм с id={} не найден", filmId);
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }
    }

    public Optional<Film> getFilmById(int id) {
        return getExsitsFilm(id);
    }

    public void addLike(int filmId, int userId) {
        userDbStorage.findUserById(userId);
        getExsitsFilm(filmId);
        filmDbStorage.addLike(filmId, userId);
        log.info("Пользователь с id={} поставил лайк фильму с id={}", userId, filmId);
    }

    public void removeLike(int filmId, int userId) {
        userDbStorage.findUserById(userId);
        getExsitsFilm(filmId);
        filmDbStorage.removeLike(filmId, userId);
        log.info("Пользователь с id={} удалил лайк с фильма с id={}", userId, filmId);
    }

    public List<Film> getPopularFilms(int count) {
        List<Film> popularFilms = filmDbStorage.getPopularFilms(count);
        log.info("Получены популярные фильмы: {}", popularFilms);
        return popularFilms;
    }

    public Set<Genre> getGenres() {
        Set<Genre> genres = filmDbStorage.getGenres();
        log.info("Получены жанры: {}", genres);
        return genres;
    }

    public Optional<Genre> getGenreById(int id) {
        Optional<Genre> genre = filmDbStorage.getGenreById(id);
        log.info("Получен жанр по id={}: {}", id, genre);
        return genre;
    }

    public Set<MPA> getMpas() {
        Set<MPA> mpas = filmDbStorage.getMpas();
        log.info("Получены рейтинги: {}", mpas);
        return mpas;
    }

    public Optional<MPA> getMpaById(int id) {
        Optional<MPA> mpa = filmDbStorage.getMpaById(id);
        log.info("Получен рейтинг по id={}: {}", id, mpa);
        return mpa;
    }
}