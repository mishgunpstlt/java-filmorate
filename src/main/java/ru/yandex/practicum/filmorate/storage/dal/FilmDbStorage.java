package ru.yandex.practicum.filmorate.storage.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.dal.mappers.MpaRowMapper;

import java.sql.Statement;
import java.util.*;

@Component
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbc;
    private final FilmRowMapper mapper;
    private final GenreRowMapper mapperGenre;
    private final MpaRowMapper mapperMpa;

    @Override
    public Film addFilm(Film film) {
        getMpaById(film.getMpa().getId());

        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                getGenreById(genre.getId());
            }
        }

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
        List<Film> films = jdbc.query(sql, mapper);

        enrichFilms(films);

        for (Film film : films) {
            addNameMpa(film);
        }

        return films;
    }

    @Override
    public Optional<Film> findFilmById(int id) {
        String sql = "SELECT * FROM films WHERE film_id = ?";
        try {
            Film result = jdbc.queryForObject(sql, mapper, id);
            result.setLikes(getLikes(id));
            result.setGenres(getGenresOfFilm(id));
            addNameMpa(result);
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
        String sql = """
                    SELECT g.genre_id, g.name
                    FROM film_genre fg
                    JOIN genres g ON fg.genre_id = g.genre_id
                    WHERE fg.film_id = ?
                """;
        return new HashSet<>(jdbc.query(sql, mapperGenre, filmId));
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
        enrichFilms(films);

        for (Film film : films) {
            addNameMpa(film);
        }

        return films;
    }

    private void addNameMpa(Film film) {
        int mpaId = film.getMpa().getId();
        Mpa fullMpa = getMpaById(mpaId).orElseThrow(() ->
                new NotFoundException("Рейтинг с id=" + mpaId + " не найден"));
        film.setMpa(fullMpa);
    }

    private void enrichFilms(List<Film> films) {
        Map<Integer, Set<Genre>> genresMap = getAllGenresGroupedByFilmId();
        Map<Integer, Set<Integer>> likesMap = getAllLikesGroupedByFilmId();

        for (Film film : films) {
            film.setGenres(genresMap.getOrDefault(film.getId(), Set.of()));
            film.setLikes(likesMap.getOrDefault(film.getId(), Set.of()));
        }
    }

    private Map<Integer, Set<Genre>> getAllGenresGroupedByFilmId() {
        String sql = """
                    SELECT fg.film_id, g.genre_id, g.name
                    FROM film_genre fg
                    JOIN genres g ON fg.genre_id = g.genre_id
                """;

        return jdbc.query(sql, rs -> {
            Map<Integer, Set<Genre>> result = new HashMap<>();
            while (rs.next()) {
                int filmId = rs.getInt("film_id");
                Genre genre = new Genre(
                        rs.getInt("genre_id"),
                        rs.getString("name")
                );
                result.computeIfAbsent(filmId, k -> new HashSet<>()).add(genre);
            }
            return result;
        });
    }

    private Map<Integer, Set<Integer>> getAllLikesGroupedByFilmId() {
        String sql = "SELECT film_id, user_id FROM likes";

        return jdbc.query(sql, rs -> {
            Map<Integer, Set<Integer>> result = new HashMap<>();
            while (rs.next()) {
                int filmId = rs.getInt("film_id");
                int userId = rs.getInt("user_id");
                result.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
            }
            return result;
        });
    }

    public Set<Genre> getGenres() {
        String sql = "SELECT * FROM genres";
        return new HashSet<>(jdbc.query(sql, mapperGenre));
    }

    public Optional<Genre> getGenreById(int genreId) {
        String sql = "SELECT * FROM genres WHERE genre_id = ?";
        try {
            Genre genre = jdbc.queryForObject(sql, mapperGenre, genreId);
            return Optional.of(genre);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Жанр с таким id не существует");

        }
    }

    public Set<Mpa> getMpas() {
        String sql = "SELECT * FROM mpas";
        return new HashSet<>(jdbc.query(sql, mapperMpa));
    }

    public Optional<Mpa> getMpaById(int mpaId) {
        String sql = "SELECT * FROM mpas WHERE mpa_id = ?";
        try {
            Mpa mpa = jdbc.queryForObject(sql, mapperMpa, mpaId);
            return Optional.of(mpa);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Рейтинг с таким id не существует");
        }
    }

    public List<Film> getFilmsByIds(Set<Integer> mostSimilarUserLikes) {
        String ids = String.join(",", Collections.nCopies(mostSimilarUserLikes.size(), "?"));

        try {
            String sql = "SELECT * FROM films WHERE film_id IN (" + ids + ")";
            List<Film> films = jdbc.query(sql, mapper, mostSimilarUserLikes.toArray());
            enrichFilms(films);
            for (Film film : films) {
                addNameMpa(film);
            }
            return films;
        } catch (DataAccessException e) {
            throw new RuntimeException("Не удалось получить рекомендации для пользователя", e);
        }

    }

}
