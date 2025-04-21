package ru.yandex.practicum.filmorate.storage.dal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.Mpa;

@Data
@AllArgsConstructor
public class MpaDto {
    private int id;
    private String name;

    public static MpaDto fromModel(Mpa mpa) {
        return new MpaDto(mpa.getId(), mpa.getName());
    }
}