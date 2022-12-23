package ru.yandex.practicum.filmorate.storage.dao.film;

import ru.yandex.practicum.filmorate.module.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {
    Film addFilm(Film film);

    Collection<Film> findAll();

    Film updateFilm(Film film);

    Film getFilmById(int id);

    Film deleteById(int id);


    Film addLike(int filmId, int userId);

    Film removeLike(int filmId, int userId);

    List<Film> getBestFilms(int count);

    List<Film> getPopularFilmByGenre(int idGenre,int count);

    List<Film> getPopularFilmByYear(int year);
}
