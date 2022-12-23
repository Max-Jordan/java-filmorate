package ru.yandex.practicum.filmorate.storage.dao.genre;

import ru.yandex.practicum.filmorate.module.Genre;

import java.util.Collection;

public interface GenreStorage {
    Collection<Genre> findAll();
    Genre getById(int id);
}
