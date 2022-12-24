package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.module.Genre;
import ru.yandex.practicum.filmorate.storage.dao.genre.GenreStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreStorage genreStorage;

    public Collection<Genre> findAll() {
        return genreStorage.findAll();
    }

    public Genre getById(int id) {
        return genreStorage.getById(id);
    }
}
