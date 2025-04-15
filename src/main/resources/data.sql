-- Наполнение таблицы mpas
INSERT INTO mpas (description) VALUES
('G - General Audiences'),
('PG - Parental Guidance Suggested'),
('PG-13 - Parents Strongly Cautioned'),
('R - Restricted'),
('NC-17 - Adults Only');

-- Наполнение таблицы genres
INSERT INTO genres (name) VALUES
('Comedy'),
('Drama'),
('Animation'),
('Thriller'),
('Documentary'),
('Action');

-- Наполнение таблицы status
INSERT INTO status (name) VALUES
('CONFIRMED'),
('UNCONFIRMED');

-- Наполнение таблицы users
INSERT INTO users (email, login, name, birthday) VALUES
('john.doe@example.com', 'johndoe', 'John Doe', '1990-05-20'),
('jane.smith@example.com', 'janesmith', 'Jane Smith', '1988-11-15'),
('alex@example.com', 'alex99', 'Alex Johnson', '2000-03-12');

-- Наполнение таблицы films
INSERT INTO films (name, description, releaseDate, duration, mpa_id) VALUES
('The Matrix', 'Futuristic action film', '1999-03-31', 136, 4),
('Shrek', 'Animated comedy adventure', '2001-05-18', 90, 2),
('Inception', 'Dream within a dream thriller', '2010-07-16', 148, 4);

-- Наполнение связей film_genre (предполагаем, что фильмы и жанры уже в базе с нужными ID)
INSERT INTO film_genre (film_id, genre_id) VALUES
(1, 6), -- The Matrix - Action
(2, 1), -- Shrek - Comedy
(2, 3), -- Shrek - Animation
(3, 4); -- Inception - Thriller

-- Наполнение лайков
INSERT INTO likes (user_id, film_id) VALUES
(1, 1),
(2, 1),
(2, 3),
(3, 2);

-- Наполнение друзей
INSERT INTO friends (requester_id, addressee_id, status_id) VALUES
(1, 2, 1),
(2, 3, 2),
(3, 1, 1);
