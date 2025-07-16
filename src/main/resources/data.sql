-- Очистка всех таблиц перед вставкой данных
DELETE FROM review_likes;
DELETE FROM reviews;
DELETE FROM likes;
DELETE FROM friends;
DELETE FROM film_genre;
DELETE FROM films;
DELETE FROM users;
DELETE FROM genres;
DELETE FROM mpas;
DELETE FROM review_likes;
DELETE FROM reviews;

-- Наполнение таблицы MPA
INSERT INTO mpas (description) VALUES
('G'),
('PG'),
('PG-13'),
('R'),
('NC-17');

-- Наполнение таблицы жанров
INSERT INTO genres (name) VALUES
('Комедия'),
('Драма'),
('Мультфильм'),
('Триллер'),
('Документальный'),
('Боевик');

-- Наполнение таблицы статусов дружбы
INSERT INTO status (name) VALUES
('CONFIRMED'),
('UNCONFIRMED');

-- Наполнение таблицы пользователей
--INSERT INTO users (email, login, name, birthday) VALUES
--('john.doe@example.com', 'johndoe', 'John Doe', '1990-05-20'),
--('jane.smith@example.com', 'janesmith', 'Jane Smith', '1988-11-15'),
--('alex@example.com', 'alex99', 'Alex Johnson', '2000-03-12');

-- Наполнение таблицы фильмов
--INSERT INTO films (name, description, releaseDate, duration, mpa_id) VALUES
--('The Matrix', 'Futuristic action film', '1999-03-31', 136, 4),
--('Shrek', 'Animated comedy adventure', '2001-05-18', 90, 2),
--('Inception', 'Dream within a dream thriller', '2010-07-16', 148, 4);

-- Наполнение связей фильмов и жанров
--DELETE FROM film_genre;
--INSERT INTO film_genre (film_id, genre_id) VALUES
--(1, 6), -- The Matrix - Action
--(2, 1), -- Shrek - Comedy
--(2, 3), -- Shrek - Animation
--(3, 4); -- Inception - Thriller

-- Наполнение таблицы лайков
--INSERT INTO likes (user_id, film_id) VALUES
--(1, 1),
--(2, 1),
--(2, 3),
--(3, 2);

-- Наполнение таблицы друзей
--INSERT INTO friends (requester_id, addressee_id, status_id) VALUES
--(1, 2, 1),
--(2, 3, 2),
--(3, 1, 1);

--INSERT INTO reviews (content, is_positive, user_id, film_id, useful)
--VALUES
--('This film is amazing!', true, 1, 1, 5),
--('I did not like this movie.', false, 2, 1, -3),
--('Great animation!', true, 3, 2, 10);

--INSERT INTO review_likes (review_id, user_id, is_like)
--VALUES
--(1, 2, true),  -- User 2 likes Review 1
--(1, 3, false), -- User 3 dislikes Review 1
--(2, 1, false); -- User 1 dislikes Review 2
-- Наполнение типов операций
INSERT INTO operation (operation) VALUES
('REMOVE'),
('ADD'),
('UPDATE');

-- Наполнение типов событий
INSERT INTO event_type (type) VALUES
('LIKE'),
('REVIEW'),
('FRIEND');