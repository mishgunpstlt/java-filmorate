package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;    

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(of = "id")
public class User {
    private int id;

    @NotBlank(message = "Электронная почта не может быть пустой и должна содержать символ @")
    @Email(message = "Электронная почта должна быть в правильном формате")
    private String email;

    @Pattern(regexp = "^[\\S]*$", message = "Логин не должен содержать пробелы")
    @NotBlank(message = "Логин не может быть пустым и содержать пробелы")
    private String login;

    private String name;

    @NotNull(message = "Дата рождения не должна быть пустой")
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;

    @JsonIgnore
    private Set<Friendship> friends = new HashSet<>();
}
