package ru.yandex.practicum.filmorate.storage.dao.mpa;

import ru.yandex.practicum.filmorate.module.Mpa;

import java.util.Collection;

public interface MpaStorage {
    Collection<Mpa> findAll();
    Mpa getById(int id);
}
