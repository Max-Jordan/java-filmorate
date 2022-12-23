package ru.yandex.practicum.filmorate.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.module.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/films")
@Slf4j
@RequiredArgsConstructor
@Validated
public class FilmController {

    private final FilmService filmService;

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получен /GET запрос о выводе фильмов");
        return filmService.findAll();
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        log.info("Получен /POST запрос добавление фильма c названием " + film.getName());
        return filmService.addFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Получен /PUT запрос обновление фильма");
        return filmService.updateFilm(film);
    }

    @DeleteMapping("/{id}")
    private Film deleteFilm(@PathVariable int id) {
        return filmService.deleteById(id);
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable int id) {
        return filmService.getFilmById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public Film putLike(@PathVariable int id, @PathVariable int userId) {
        return filmService.putLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film deleteLike(@PathVariable int id, @PathVariable int userId) {
        return filmService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10", required = false) Integer count) {
        return filmService.getPopularFilms(count);
    }

    @GetMapping("/popular_by_genre/{idGenre}")
    public List<Film> getPopularFilmByGenre(@PathVariable int idGenre,
                                            @RequestParam(defaultValue = "10", required = false) Integer count) {
        return filmService.getPopularFilmByGenre(idGenre, count);
    }

    @GetMapping("/popular_by_year/{year}")
    public List<Film> getPopularFilmByYear(@PathVariable int year) {
        return filmService.getPopularFilmByYear(year);
    }
}
