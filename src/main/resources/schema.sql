CREATE TABLE IF NOT EXISTS films (
  film_id integer GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  name varchar,
  description varchar(200),
  releaseDate date,
  duration integer,
  mpa_id integer
);

CREATE TABLE IF NOT EXISTS film_genre (
  film_id integer,
  genre_id integer,
  PRIMARY KEY (film_id, genre_id)
);

CREATE TABLE IF NOT EXISTS users (
  user_id integer GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  email varchar,
  login varchar,
  name varchar,
  birthday date
);

CREATE TABLE IF NOT EXISTS genres (
  genre_id integer GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  name varchar
);

CREATE TABLE IF NOT EXISTS mpas (
  mpa_id integer GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  description varchar
);

CREATE TABLE IF NOT EXISTS likes (
  user_id integer,
  film_id integer,
  PRIMARY KEY (user_id, film_id)
);

CREATE TABLE IF NOT EXISTS friends (
  requester_id integer,
  addressee_id integer,
  status_id integer,
  PRIMARY KEY (requester_id, addressee_id)
);

CREATE TABLE IF NOT EXISTS status (
  status_id integer GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  name varchar
);

ALTER TABLE films ADD FOREIGN KEY (mpa_id) REFERENCES mpas(mpa_id);

ALTER TABLE film_genre ADD FOREIGN KEY (film_id) REFERENCES films (film_id);

ALTER TABLE film_genre ADD FOREIGN KEY (genre_id) REFERENCES genres (genre_id);

ALTER TABLE likes ADD FOREIGN KEY (user_id) REFERENCES users (user_id);

ALTER TABLE likes ADD FOREIGN KEY (film_id) REFERENCES films (film_id);

ALTER TABLE friends ADD FOREIGN KEY (requester_id) REFERENCES users (user_id);

ALTER TABLE friends ADD FOREIGN KEY (addressee_id) REFERENCES users (user_id);

ALTER TABLE friends ADD FOREIGN KEY (status_id) REFERENCES status (status_id);
