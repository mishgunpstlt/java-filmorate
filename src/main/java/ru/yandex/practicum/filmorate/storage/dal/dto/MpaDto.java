package ru.yandex.practicum.filmorate.storage.dal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.enumModels.MPA;

@Data
@AllArgsConstructor
public class MpaDto {
    private int id;
    private String name;

    public static MpaDto fromEnum(MPA mpa) {
        return new MpaDto(mpa.getId(), mpa.getFormattedName());
    }
}