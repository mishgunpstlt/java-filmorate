package ru.yandex.practicum.filmorate.storage.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.enumModels.Genre;
import ru.yandex.practicum.filmorate.model.enumModels.MPA;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.Statement;
import java.util.*;

@Component
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbc;
    private final RowMapper<Film> mapper;

    @Override
    public Film addFilm(Film film) {
        String sql = "INSERT INTO films (name, description, releaseDate, duration, mpa_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            var ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        Integer generatedId = keyHolder.getKeyAs(Integer.class);
        if (generatedId != null) {
            film.setId(generatedId);
            Set<Genre> genres = film.getGenres();
            if (genres != null) {
                for (Genre genre : genres) {
                    addGenre(film.getId(), genre);
                }
            }

            Set<Integer> likes = film.getLikes();
            if (likes != null) {
                for (Integer userId : likes) {
                    addLike(film.getId(), userId);
                }
            }

            return film;
        } else {
            throw new RuntimeException("Не удалось сохранить фильм — id не сгенерирован");
        }
    }

    @Override
    public Film updateFilm(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, releaseDate = ?, " +
                "duration = ?, mpa_id = ? WHERE film_id = ?";
        if (findFilmById(film.getId()).isEmpty()) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }

        jdbc.update(sql, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpa().getId(), film.getId());

        jdbc.update("DELETE FROM film_genre WHERE film_id = ?", film.getId());
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                addGenre(film.getId(), genre);
            }
        }

        jdbc.update("DELETE FROM likes WHERE film_id = ?", film.getId());
        if (film.getLikes() != null) {
            for (Integer userId : film.getLikes()) {
                addLike(film.getId(), userId);
            }
        }

        return film;
    }

    @Override
    public Collection<Film> getFilms() {
        String sql = "SELECT * FROM films";
        List<Film> results = jdbc.query(sql, mapper);
        for (Film f : results) {
            f.setLikes(getLikes(f.getId()));
            f.setGenres(getGenresOfFilm(f.getId()));
        }
        return results;
    }

    @Override
    public Optional<Film> findFilmById(int id) {
        String sql = "SELECT * FROM films WHERE film_id = ?";
        try {
            Film result = jdbc.queryForObject(sql, mapper, id);
            result.setLikes(getLikes(id));
            result.setGenres(getGenresOfFilm(id));
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    public void addLike(int filmId, int userId) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbc.update(sql, filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbc.update(sql, filmId, userId);
    }

    public Set<Integer> getLikes(int filmId) {
        String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        return new HashSet<>(jdbc.queryForList(sql, Integer.class, filmId));
    }

    public void addGenre(int filmId, Genre genre) {
        String sql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
        jdbc.update(sql, filmId, genre.getId());
    }

    public void deleteGenre(int filmId, int genreId) {
        String sql = "DELETE FROM film_genre WHERE film_id = ? AND genre_id = ?";
        jdbc.update(sql, filmId, genreId);
    }

    public Set<Genre> getGenresOfFilm(int filmId) {
        String sql = "SELECT genre_id FROM film_genre WHERE film_id = ?";
        return new HashSet<>(jdbc.query(sql, (rs, rowNum) ->
                Genre.fromId(rs.getInt("genre_id")), filmId));
    }

    public List<Film> getPopularFilms(int count) {
        String sql = """
        SELECT f.*, COUNT(l.user_id) AS like_count
        FROM films f
        LEFT JOIN likes l ON f.film_id = l.film_id
        GROUP BY f.film_id
        ORDER BY like_count DESC
        LIMIT ?
    """;

        List<Film> films = jdbc.query(sql, mapper, count);
        for (Film film : films) {
            film.setLikes(getLikes(film.getId()));
            film.setGenres(getGenresOfFilm(film.getId()));
        }

        return films;
    }

    public Set<Genre> getGenres() {
        String sql = "SELECT * FROM genres";
        return new HashSet<>(jdbc.query(sql, (rs, rowNum) ->
                Genre.fromId(rs.getInt("genre_id"))));
    }

    public Optional<Genre> getGenreById(int genreId) {
        String sql = "SELECT * FROM genres WHERE genre_id = ?";
        try {
            Genre genre = jdbc.queryForObject(sql, (rs, rowNum) ->
                    Genre.fromId(rs.getInt("genre_id")), genreId
            );
            return Optional.of(genre);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Set<MPA> getMpas() {
        String sql = "SELECT * FROM mpas";
        return new HashSet<>(jdbc.query(sql, (rs, rowNum) ->
                MPA.fromId(rs.getInt("mpa_id"))));
    }

    public Optional<MPA> getMpaById(int mpaId) {
        String sql = "SELECT * FROM mpas WHERE mpa_id = ?";
        try {
            MPA mpa = jdbc.queryForObject(sql, (rs, rowNum) ->
                    MPA.fromId(rs.getInt("genre_id")), mpaId
            );
            return Optional.of(mpa);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

}
