# java-filmorate
Template repository for Filmorate project.


## Структура базы данных
![filmorate.png](src%2Fmain%2Fresources%2Ffilmorate.png)
Диаграмма описывает структуру базы данных приложения Filmorate, включая связи между пользователями, фильмами, 
лайками, жанрами, рейтингами (MPA) и дружбой с указанием статуса.

## Примеры SQL-запросов
### Получить все фильмы с их жанрами и рейтингами:
```sql
SELECT f.name, f.description, f.releaseDate, f.duration, m.description AS mpa_rating, g.name AS genre
FROM films f
JOIN mpas m ON f.mpa_id = m.mpa_id
JOIN film_genre fg ON f.film_id = fg.film_id
JOIN genres g ON fg.genre_id = g.genre_id;
```
### Добавить лайк фильму от пользователя:
```sql
INSERT INTO likes (user_id, film_id) VALUES (1, 10);
```
### Добавить друга (отправить запрос):
```sql
INSERT INTO friends (requester_id, addressee_id, status_id)
VALUES (1, 2, 1);  -- где 1 — статус "ожидает подтверждения"
```
### Подтвердить дружбу:
```sql
UPDATE friends SET status_id = 2
WHERE requester_id = 1 AND addressee_id = 2;
-- где 2 — статус "подтверждено"
```
### Найти ТОП-5 фильмов по количеству лайков:
```sql
SELECT f.name, COUNT(l.user_id) AS like_count
FROM films f
LEFT JOIN likes l ON f.film_id = l.film_id
GROUP BY f.film_id
ORDER BY like_count DESC
LIMIT 5;
```