package ru.yandex.practicum.filmorate.storage.dal.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class UserDto {
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
    private List<Integer> friends;

    public static UserDto fromModel(ru.yandex.practicum.filmorate.model.User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setLogin(user.getLogin());
        dto.setName(user.getName());
        dto.setBirthday(user.getBirthday());
        dto.setFriends(user.getFriends() == null ? List.of() :
                user.getFriends().stream()
                        .map(f -> f.getRequesterId() == user.getId()
                                ? f.getAddresseeId()
                                : f.getRequesterId())
                        .collect(Collectors.toList()));
        return dto;
    }

    public static User toModel(UserDto dto) {
        User user = new User();
        user.setId(dto.getId());
        user.setEmail(dto.getEmail());
        user.setLogin(dto.getLogin());
        user.setName(dto.getName());
        user.setBirthday(dto.getBirthday());
        return user;
    }


}
