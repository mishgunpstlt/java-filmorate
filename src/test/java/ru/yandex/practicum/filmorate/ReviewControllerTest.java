package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.dal.dto.ReviewDto;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = {"/schema.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ReviewControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String REVIEWS_URL = "/reviews";

    @BeforeEach
    void setUp() {
        // Очистка таблиц перед каждым тестом (если требуется)
        // jdbcTemplate.update("DELETE FROM review_likes");
        // jdbcTemplate.update("DELETE FROM reviews");
    }

    @Test
    void testAddAndGetReview() {
        // Создаем новый отзыв
        ReviewDto newReview = new ReviewDto();
        newReview.setContent("This film is amazing!");
        newReview.setPositive(true);
        newReview.setUserId(1);
        newReview.setFilmId(1);

        // Добавляем отзыв
        ResponseEntity<ReviewDto> response = restTemplate.postForEntity(REVIEWS_URL, newReview, ReviewDto.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        // Получаем ID созданного отзыва
        ReviewDto createdReview = response.getBody();
        assertNotNull(createdReview);
        int reviewId = createdReview.getReviewId();

        // Получаем отзыв по ID
        ResponseEntity<ReviewDto> getResponse = restTemplate.getForEntity(REVIEWS_URL + "/" + reviewId, ReviewDto.class);
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        ReviewDto fetchedReview = getResponse.getBody();
        assertNotNull(fetchedReview);
        assertEquals("This film is amazing!", fetchedReview.getContent());
        assertTrue(fetchedReview.isPositive());
        assertEquals(0, fetchedReview.getUseful());
    }

    @Test
    void testUpdateReview() {
        // Создаем новый отзыв
        ReviewDto newReview = new ReviewDto();
        newReview.setContent("This film is so-so.");
        newReview.setPositive(false);
        newReview.setUserId(2);
        newReview.setFilmId(1);

        // Добавляем отзыв
        ResponseEntity<ReviewDto> response = restTemplate.postForEntity(REVIEWS_URL, newReview, ReviewDto.class);
        ReviewDto createdReview = response.getBody();
        assertNotNull(createdReview);
        int reviewId = createdReview.getReviewId();

        // Обновляем отзыв
        ReviewDto updatedReview = new ReviewDto();
        updatedReview.setReviewId(reviewId);
        updatedReview.setContent("This film is actually great!");
        updatedReview.setPositive(true);
        updatedReview.setUserId(2);
        updatedReview.setFilmId(1);

        restTemplate.put(REVIEWS_URL, updatedReview);

        // Получаем обновленный отзыв
        ResponseEntity<ReviewDto> getResponse = restTemplate.getForEntity(REVIEWS_URL + "/" + reviewId, ReviewDto.class);
        ReviewDto fetchedReview = getResponse.getBody();
        assertNotNull(fetchedReview);
        assertEquals("This film is actually great!", fetchedReview.getContent());
        assertTrue(fetchedReview.isPositive());
    }

    @Test
    void testDeleteReview() {
        // Создаем новый отзыв
        ReviewDto newReview = new ReviewDto();
        newReview.setContent("This film is terrible.");
        newReview.setPositive(false);
        newReview.setUserId(3);
        newReview.setFilmId(2);

        // Добавляем отзыв
        ResponseEntity<ReviewDto> response = restTemplate.postForEntity(REVIEWS_URL, newReview, ReviewDto.class);
        ReviewDto createdReview = response.getBody();
        assertNotNull(createdReview);
        int reviewId = createdReview.getReviewId();

        // Удаляем отзыв
        restTemplate.delete(REVIEWS_URL + "/" + reviewId);

        // Проверяем, что отзыв удален
        ResponseEntity<ReviewDto> getResponse = restTemplate.getForEntity(REVIEWS_URL + "/" + reviewId, ReviewDto.class);
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }

    @Test
    void testAddAndRemoveLike() {
        // Создаем новый отзыв
        ReviewDto newReview = new ReviewDto();
        newReview.setContent("This film is good.");
        newReview.setPositive(true);
        newReview.setUserId(1);
        newReview.setFilmId(1);

        // Добавляем отзыв
        ResponseEntity<ReviewDto> response = restTemplate.postForEntity(REVIEWS_URL, newReview, ReviewDto.class);
        ReviewDto createdReview = response.getBody();
        assertNotNull(createdReview);
        int reviewId = createdReview.getReviewId();

        // Добавляем лайк
        restTemplate.put(REVIEWS_URL + "/" + reviewId + "/like/2", null);

        // Получаем отзыв
        ResponseEntity<ReviewDto> getResponse = restTemplate.getForEntity(REVIEWS_URL + "/" + reviewId, ReviewDto.class);
        ReviewDto fetchedReview = getResponse.getBody();
        assertNotNull(fetchedReview);
        assertEquals(1, fetchedReview.getUseful());

        // Удаляем лайк
        restTemplate.delete(REVIEWS_URL + "/" + reviewId + "/like/2");

        // Получаем отзыв снова
        getResponse = restTemplate.getForEntity(REVIEWS_URL + "/" + reviewId, ReviewDto.class);
        fetchedReview = getResponse.getBody();
        assertNotNull(fetchedReview);
        assertEquals(0, fetchedReview.getUseful());
    }

    @Test
    void testGetReviewsByFilmId() {
        // Создаем два отзыва для одного фильма
        ReviewDto review1 = new ReviewDto();
        review1.setContent("Great movie!");
        review1.setPositive(true);
        review1.setUserId(1);
        review1.setFilmId(1);

        ReviewDto review2 = new ReviewDto();
        review2.setContent("Not bad.");
        review2.setPositive(false);
        review2.setUserId(2);
        review2.setFilmId(1);

        restTemplate.postForEntity(REVIEWS_URL, review1, ReviewDto.class);
        restTemplate.postForEntity(REVIEWS_URL, review2, ReviewDto.class);

        // Получаем отзывы для фильма
        ResponseEntity<ReviewDto[]> response = restTemplate.getForEntity(REVIEWS_URL + "?filmId=1&count=2", ReviewDto[].class);
        ReviewDto[] reviews = response.getBody();
        assertNotNull(reviews);
        assertEquals(2, reviews.length);
    }
}