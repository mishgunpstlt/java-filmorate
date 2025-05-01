package ru.yandex.practicum.filmorate.storage.dal;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.enumModels.EventType;
import ru.yandex.practicum.filmorate.model.enumModels.Operation;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.dal.mappers.MpaRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Component
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    public static final Logger log = LoggerFactory.getLogger(FilmDbStorage.class);
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

            // Сохраняем жанры
            Set<Genre> genres = film.getGenres();
            if (genres != null) {
                for (Genre genre : genres) {
                    addGenre(film.getId(), genre);
                }
            }

            // Сохраняем лайки
            Set<Integer> likes = film.getLikes();
            if (likes != null) {
                for (Integer userId : likes) {
                    addLike(film.getId(), userId);
                }
            }

            // Сохраняем режиссёров
            Set<Director> directors = film.getDirectors();
            if (directors != null) {
                addDirectors(film.getId(), directors);
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

        // Удаляем старые жанры и добавляем новые
        jdbc.update("DELETE FROM film_genre WHERE film_id = ?", film.getId());
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                addGenre(film.getId(), genre);
            }
        }

        // Удаляем старые лайки и добавляем новые
        jdbc.update("DELETE FROM likes WHERE film_id = ?", film.getId());
        if (film.getLikes() != null) {
            for (Integer userId : film.getLikes()) {
                addLike(film.getId(), userId);
            }
        }

        // Удаляем старых режиссёров и добавляем новых
        deleteDirectors(film.getId());
        if (film.getDirectors() != null) {
            addDirectors(film.getId(), film.getDirectors());
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
            film.setDirectors(getDirectorsOfFilm(film.getId())); // Получаем режиссёров
        }

        return films;
    }

    @Override
    public Optional<Film> findFilmById(int id) {
        String sql = "SELECT f.*, d.director_id AS director_id, d.name AS director_name " +
                "FROM films f " +
                "LEFT JOIN film_director fd ON f.film_id = fd.film_id " +
                "LEFT JOIN directors d ON fd.director_id = d.director_id " +
                "WHERE f.film_id = ?";
        try {
            Film result = jdbc.queryForObject(sql, mapper, id);
            result.setLikes(getLikes(id));
            result.setGenres(getGenresOfFilm(id));
            addNameMpa(result);
            result.setDirectors(getDirectorsOfFilm(id));
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    public void deleteFilmById(int filmId) {
        // Удаление зависимых записей из таблицы film_genre
        String deleteGenresSql = "DELETE FROM film_genre WHERE film_id = ?";
        jdbc.update(deleteGenresSql, filmId);

        // Удаление записей из таблицы likes
        String deleteLikesSql = "DELETE FROM likes WHERE film_id = ?";
        jdbc.update(deleteLikesSql, filmId);

        // Удаление фильма из таблицы films
        String deleteFilmSql = "DELETE FROM films WHERE film_id = ?";
        int rowsAffected = jdbc.update(deleteFilmSql, filmId);

        // Если ни одна строка не была затронута, выбрасываем исключение
        if (rowsAffected == 0) {
            throw new NotFoundException("Фильм с id " + filmId + " не найден.");
        }
    }

    public void addLike(int filmId, int userId) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbc.update(sql, filmId, userId);
        UserDbStorage.addFeed(jdbc, userId, filmId, EventType.LIKE, Operation.ADD);
    }

    public void removeLike(int filmId, int userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbc.update(sql, filmId, userId);
        UserDbStorage.addFeed(jdbc, userId, filmId, EventType.LIKE, Operation.REMOVE);
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

    public List<Film> getPopularFilms(int count, int genreId, int year) {
        String sql = getPopularFilmsQuery(genreId, year);

        List<Film> films = jdbc.query(sql, mapper, year, genreId, count);
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
        Map<Integer, Set<Director>> directorsMap = getAllDirectorsGroupedByFilmId();

        for (Film film : films) {
            film.setGenres(genresMap.getOrDefault(film.getId(), Set.of()));
            film.setLikes(likesMap.getOrDefault(film.getId(), Set.of()));
            film.setDirectors(directorsMap.getOrDefault(film.getId(), Set.of()));
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

    private Map<Integer, Set<Director>> getAllDirectorsGroupedByFilmId() {
        String sql = """
                SELECT fd.film_id, d.director_id, d.name
                FROM film_director fd
                JOIN directors d ON fd.director_id = d.director_id
                """;

        return jdbc.query(sql, rs -> {
            Map<Integer, Set<Director>> result = new HashMap<>();
            while (rs.next()) {
                int filmId = rs.getInt("film_id");
                Director director = new Director(
                        rs.getInt("director_id"),
                        rs.getString("name")
                );
                result.computeIfAbsent(filmId, k -> new HashSet<>()).add(director);
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

    public List<Film> searchByTitle(String query) {
        String sql = "SELECT * FROM films WHERE LOWER(name) LIKE ?";
        List<Film> films = jdbc.query(sql, mapper, "%" + query.toLowerCase() + "%");
        enrichFilms(films);
        for (Film film : films) {
            addNameMpa(film);
        }
        return films;
    }

    public List<Film> searchByDirector(String query) {
        String sql = """
                SELECT f.* FROM films f
                JOIN film_director fd ON f.film_id = fd.film_id
                JOIN directors d ON fd.director_id = d.director_id
                WHERE LOWER(d.name) LIKE ?
                """;
        List<Film> films = jdbc.query(sql, mapper, "%" + query.toLowerCase() + "%");
        enrichFilms(films);
        for (Film film : films) {
            addNameMpa(film);
        }
        return films;
    }

    public List<Film> searchByTitleAndDirector(String query) {
        String sql = """
                SELECT DISTINCT f.* FROM films f
                LEFT JOIN film_director fd ON f.film_id = fd.film_id
                LEFT JOIN directors d ON fd.director_id = d.director_id
                WHERE LOWER(f.name) LIKE ?
                   OR LOWER(d.name) LIKE ?
                """;
        List<Film> films = jdbc.query(sql, mapper, "%" + query.toLowerCase() + "%",
                "%" + query.toLowerCase() + "%");
        enrichFilms(films);
        for (Film film : films) {
            addNameMpa(film);
        }
        return films;
    }

    public Director createDirector(Director director) {
        String sql = "INSERT INTO directors (name) VALUES (?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);

        Integer generatedId = keyHolder.getKeyAs(Integer.class);
        if (generatedId != null) {
            director.setDirectorId(generatedId);
            return director;
        } else {
            throw new RuntimeException("Не удалось создать режиссера — id не сгенерирован");
        }
    }

    public Optional<Director> getDirectorById(int directorId) {
        String sql = "SELECT * FROM directors WHERE director_id = ?";
        try {
            Director director = jdbc.queryForObject(sql, (rs, rowNum) -> {
                Director result = new Director();
                result.setDirectorId(rs.getInt("director_id"));
                result.setName(rs.getString("name"));
                return result;
            }, directorId);
            return Optional.ofNullable(director);
        } catch (EmptyResultDataAccessException ignored) {
            throw new NotFoundException("Режиссер с Id " + directorId + " не найден");
        }
    }

    public Director updateDirector(Director director) {
        String sql = "UPDATE directors SET name = ? WHERE director_id = ?";
        int rowsUpdated = jdbc.update(sql, director.getName(), director.getDirectorId());

        if (rowsUpdated == 0) {
            throw new NotFoundException("Режиссер с id=" + director.getDirectorId() + " не найден");
        }

        return director;
    }

    public void deleteDirector(int directorId) {
        String sql = "DELETE FROM directors WHERE director_id = ?";
        int rowsDeleted = jdbc.update(sql, directorId);

        if (rowsDeleted == 0) {
            throw new NotFoundException("Режиссер с id=" + directorId + " не найден");
        }
    }

    public List<Director> getAllDirectors() {
        String sql = "SELECT * FROM directors";
        return jdbc.query(sql, (rs, rowNum) -> {
            Director director = new Director();
            director.setDirectorId(rs.getInt("director_id"));
            director.setName(rs.getString("name"));
            return director;
        });
    }

    public List<Film> getFilmsByDirectorSorted(int directorId, String sortBy) {
        log.info("Request received: directorId = {}, sortBy = {}", directorId, sortBy); //ищу баг
        String sql;

        if ("year".equals(sortBy)) {
            sql = """
                    SELECT f.*,
                           d.director_id AS director_id,
                           d.name AS director_name
                    FROM films f
                    JOIN film_director fd ON f.film_id = fd.film_id
                    JOIN directors d ON fd.director_id = d.director_id
                    WHERE fd.director_id = ?
                    ORDER BY f.releaseDate ASC
                    """;
        } else if ("likes".equals(sortBy)) {
            sql = """
                    SELECT f.*,
                           COUNT(l.user_id) AS like_count,
                           d.director_id AS director_id,
                           d.name AS director_name
                    FROM films f
                    JOIN film_director fd ON f.film_id = fd.film_id
                    LEFT JOIN likes l ON f.film_id = l.film_id
                    JOIN directors d ON fd.director_id = d.director_id
                    WHERE fd.director_id = ?
                    GROUP BY f.film_id, d.director_id, d.name
                    ORDER BY COUNT(l.user_id) DESC
                    """;
        } else {
            throw new IllegalArgumentException("Invalid sortBy parameter");
        }

        return jdbc.query(sql, (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getInt("film_id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("releaseDate").toLocalDate());
            film.setDuration(rs.getInt("duration"));

            log.debug("Mapped film: id = {}, name = {}, releaseDate = {}", //ищу баг
                    film.getId(), film.getName(), film.getReleaseDate());
            // Заполняем MPA
            int mpaId = rs.getInt("mpa_id");
            Mpa mpa = getMpaById(mpaId).orElseThrow(() ->
                    new NotFoundException("MPA with id=" + mpaId + " not found"));
            film.setMpa(mpa);

            // Заполняем жанры
            film.setGenres(getGenresOfFilm(film.getId()));

            // Заполняем лайки
            film.setLikes(getLikes(film.getId()));

            // Заполняем режиссеров
            Director director = new Director();
            director.setDirectorId(rs.getInt("director_id"));
            director.setName(rs.getString("director_name"));
            film.setDirectors(Set.of(director));

            return film;
        }, directorId);
    }

    //метод для получения режиссеров из таблицы
    private Set<Director> getDirectorsOfFilm(int filmId) {
        String sql = "SELECT d.director_id AS id, d.name " +
                "FROM directors d " +
                "JOIN film_director fd ON d.director_id = fd.director_id " +
                "WHERE fd.film_id = ?";
        try {
            // Преобразуем List<Director> в Set<Director>
            return new HashSet<>(jdbc.query(sql, (rs, rowNum) -> {
                Director director = new Director();
                director.setDirectorId(rs.getInt("id")); // Используем псевдоним "id"
                director.setName(rs.getString("name"));
                return director;
            }, filmId));
        } catch (EmptyResultDataAccessException ignored) {
            // Если режиссёров нет, возвращаем пустое множество
            return new HashSet<>();
        } catch (Exception e) {
            System.err.println("Error while fetching directors for film with id: " + filmId);
            e.printStackTrace();
            throw e;
        }
    }

    //метод для сохранения связи между фильмом и режиссёрами
    private void addDirectors(int filmId, Set<Director> directors) {
        String sql = "INSERT INTO film_director (film_id, director_id) VALUES (?, ?)";
        for (Director director : directors) {
            jdbc.update(sql, filmId, director.getDirectorId());
        }
    }

    //метод для удаления старых связей перед обновлением
    private void deleteDirectors(int filmId) {
        String sql = "DELETE FROM film_director WHERE film_id = ?";
        jdbc.update(sql, filmId);
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

    private String getPopularFilmsQuery(int genreId, int year) {
        String variablePart;
        if (genreId == 0 && year == 0) {
            variablePart = """
                        WHERE EXTRACT(YEAR FROM releaseDate) <> ?
                        AND genre_id <> ?
                    """;
        } else if (genreId == 0) {
            variablePart = """
                        WHERE EXTRACT(YEAR FROM releaseDate) = ?
                        AND genre_id <> ?
                    """;
        } else if (year == 0) {
            variablePart = """
                        WHERE EXTRACT(YEAR FROM releaseDate) <> ?
                        AND genre_id = ?
                    """;
        } else {
            variablePart = """
                        WHERE EXTRACT(YEAR FROM releaseDate) = ?
                        AND genre_id = ?
                    """;
        }
        return """
                    SELECT f.*, COUNT(DISTINCT l.user_id) AS like_count
                    FROM films f
                    LEFT JOIN likes l ON f.film_id = l.film_id
                    LEFT JOIN film_genre fg ON f.film_id = fg.film_id
                """ + variablePart + """
                    GROUP BY f.film_id
                    ORDER BY like_count DESC
                    LIMIT ?
                """;
    }

    public List<Film> getCommonFilmsSortedByPopularity(int userId, int friendId) {
        String sql = """
        SELECT f.film_id AS film_id,
               COUNT(l.user_id) AS like_count
        FROM likes l
        JOIN films f ON l.film_id = f.film_id
        WHERE l.user_id IN (?, ?)
          AND EXISTS (
              SELECT 1 FROM likes l1
              WHERE l1.user_id = ? AND l1.film_id = l.film_id
          )
          AND EXISTS (
              SELECT 1 FROM likes l2
              WHERE l2.user_id = ? AND l2.film_id = l.film_id
          )
        GROUP BY f.film_id
        ORDER BY like_count DESC;
    """;

        return jdbc.query(sql, (rs, rowNum) -> {
            int filmId = rs.getInt("film_id");
            return findFilmById(filmId).orElseThrow();
        }, userId, friendId, userId, friendId);
    }
}