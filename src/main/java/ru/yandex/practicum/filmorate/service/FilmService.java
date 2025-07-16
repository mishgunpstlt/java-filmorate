package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.BadRequestException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.dal.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.dal.UserDbStorage;

import java.util.*;
import java.util.stream.Collectors;

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

    public List<Film> getPopularFilms(int count, int genreId, int year) {
        if (year < 1895 && year != 0) {
            throw new BadRequestException("Год выхода фильма не может быть ранее 1895 года");
        }
        if (genreId != 0) {
            getGenreById(genreId);
        }

        List<Film> popularFilms = filmDbStorage.getPopularFilms(count, genreId, year);
        log.info("Получены популярные фильмы: {}", popularFilms);
        return popularFilms;
    }

    public Set<Genre> getGenres() {
        Set<Genre> genres = filmDbStorage.getGenres();
        log.info("Получены жанры: {}", genres);
        return genres;
    }

    public Genre getGenreById(int id) {
        Genre genre = filmDbStorage.getGenreById(id).get();
        log.info("Получен жанр по id={}: {}", id, genre);
        return genre;
    }

    public Set<Mpa> getMpas() {
        Set<Mpa> mpas = filmDbStorage.getMpas();
        log.info("Получены рейтинги: {}", mpas);
        return mpas;
    }

    public Mpa getMpaById(int id) {
        Mpa mpa = filmDbStorage.getMpaById(id).get();
        log.info("Получен рейтинг по id={}: {}", id, mpa);
        return mpa;
    }

    public List<Film> searchFilms(String query, String by) {
        List<Film> result = new ArrayList<>();

        boolean searchByTitle = by.contains("title");
        boolean searchByDirector = by.contains("director");

        if (searchByTitle && searchByDirector) {
            result = filmDbStorage.searchByTitleAndDirector(query);
        } else if (searchByTitle) {
            result = filmDbStorage.searchByTitle(query);
        } else if (searchByDirector) {
            result = filmDbStorage.searchByDirector(query);
        }

        log.info("Получен фильма с подстрокой = '{}', поиск по {}", query, by);

        return result.stream()
                .sorted(Comparator.comparing((Film f) -> f.getLikes().size()).reversed())
                .collect(Collectors.toList());
    }

    public Director createDirector(Director director) {
        Director createdDirector = filmDbStorage.createDirector(director);
        log.info("Создан режиссер: {}", createdDirector);
        return createdDirector;
    }

    public Optional<Director> getDirectorById(int directorId) {
        Optional<Director> director = getExsitsDirector(directorId); // тут зашита проверка
        log.info("Получен режиссер по id {}: {}", directorId, director);
        return director;
    }

    public List<Director> getAllDirectors() {
        List<Director> directors = filmDbStorage.getAllDirectors();
        log.info("Получен список всех режиссеров: {}", directors);
        return directors;
    }

    public Director updateDirector(Director director) {
        getExsitsDirector(director.getDirectorId());
        Director updatedDirector = filmDbStorage.updateDirector(director);
        log.info("Обновлен режиссер: {}", updatedDirector);
        return updatedDirector;
    }

    public void deleteDirector(int directorId) {
        getExsitsDirector(directorId);
        filmDbStorage.deleteDirector(directorId);
        log.info("Удален режиссер с id={}", directorId);
    }

    public List<Film> getFilmsByDirectorSorted(int directorId, String sortBy) {
        getExsitsDirector(directorId);
        List<Film> films = filmDbStorage.getFilmsByDirectorSorted(directorId, sortBy);
        log.info("Получены фильмы режиссера с id={}, отсортированные по '{}': {}", directorId, sortBy, films);
        return films;
    }

    public void deleteFilm(int filmId) {
        filmDbStorage.deleteFilmById(filmId);
    }

    public List<Film> getCommonFilmsSortedByPopularity(int userId, int friendId) {
        if (userDbStorage.findUserById(userId).isPresent() && userDbStorage.findUserById(friendId).isPresent()) {
            return filmDbStorage.getCommonFilmsSortedByPopularity(userId, friendId);
        } else {
            throw new NotFoundException("Данные пользователи не найдены!");
        }
    }

    private Optional<Director> getExsitsDirector(int directorId) {
        Optional<Director> director = filmDbStorage.getDirectorById(directorId);
        if (director.isPresent()) {
            log.info("Режиссер с id={} найден", directorId);
            return director;
        } else {
            log.error("Режиссер с id={} не найден", directorId);
            throw new NotFoundException("Режиссер с id=" + directorId + " не найден");
        }
    }
}