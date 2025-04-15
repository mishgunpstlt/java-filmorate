package ru.yandex.practicum.filmorate.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.enumModels.MPA;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.dal.dto.MpaDto;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/mpa")
public class MpaControlle {

    private final FilmService filmService;

    public MpaControlle(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public List<MpaDto> getMpas() {
        return filmService.getMpas().stream()
                .map(MpaDto::fromEnum)
                .sorted(Comparator.comparingInt(MpaDto::getId))
                .toList();
    }

    @GetMapping("/{id}")
    public MpaDto getMpaById(@PathVariable int id) {
        MPA mpa = MPA.fromId(id);
        return MpaDto.fromEnum(mpa); // Преобразование в DTO
    }
}
