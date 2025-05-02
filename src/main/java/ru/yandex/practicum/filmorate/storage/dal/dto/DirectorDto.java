package ru.yandex.practicum.filmorate.storage.dal.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.Director;

@Data
public class DirectorDto {
    private int id;
    @NotBlank(message = "Название не может быть пустым")
    private String name;

    // Преобразование из модели Director в DTO
    public static DirectorDto fromModel(Director director) {
        DirectorDto dto = new DirectorDto();
        dto.setId(director.getDirectorId());
        dto.setName(director.getName());
        return dto;
    }

    // Преобразование из DTO в модель Director
    public static Director toModel(DirectorDto dto) {
        Director director = new Director();
        director.setDirectorId(dto.getId());
        director.setName(dto.getName());
        return director;
    }
}