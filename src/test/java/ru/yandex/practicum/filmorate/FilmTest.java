package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.module.Film;
import ru.yandex.practicum.filmorate.module.Mpa;
import ru.yandex.practicum.filmorate.module.User;
import ru.yandex.practicum.filmorate.storage.dao.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.user.UserDbStorage;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmTest {
    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;
    private Film film = Film.builder()
            .name("testFilm")
            .description("desc")
            .releaseDate(LocalDate.of(2020, 1, 1))
            .duration(110)
            .mpa(new Mpa(1, "G"))
            .genres(null)
            .build();
    private User user = User.builder()
            .email("pudge2005@mail.ru")
            .login("login")
            .name("ben")
            .birthday(LocalDate.of(2000, 12, 22))
            .build();

    @Test
    void addFilmTest() {
        filmDbStorage.addFilm(film);
        AssertionsForClassTypes.assertThat(film).extracting("id").isNotNull();
        AssertionsForClassTypes.assertThat(film).extracting("name").isNotNull();
    }

    @Test
    void updateFilmTest() {
        filmDbStorage.addFilm(film);
        film.setName("testUpdateFilm");
        film.setDescription("testUpdateDesc");
        filmDbStorage.updateFilm(film);
        AssertionsForClassTypes.assertThat(filmDbStorage.getFilmById(film.getId()))
                .hasFieldOrPropertyWithValue("name", "testUpdateFilm")
                .hasFieldOrPropertyWithValue("description", "testUpdateDesc");
    }

    @Test
    void getFilmByIdTest() {
        filmDbStorage.addFilm(film);
        filmDbStorage.getFilmById(film.getId());
        AssertionsForClassTypes.assertThat(filmDbStorage.getFilmById(film.getId())).hasFieldOrPropertyWithValue("id", film.getId());
    }

    @Test
    void addLikeAndRemoveTest() {
        userDbStorage.createUser(user);
        filmDbStorage.addFilm(film);
        filmDbStorage.addLike(film.getId(), user.getId());
        assertThat(filmDbStorage.getBestFilms(film.getId()).isEmpty());
        assertThat(filmDbStorage.getBestFilms(film.getId())).isNotNull();
        Assertions.assertThat(filmDbStorage.getBestFilms(film.getId()).size() == 2);
        filmDbStorage.removeLike(film.getId(), user.getId());
        Assertions.assertThat(filmDbStorage.getBestFilms(film.getId()).size() == 1);
    }

    @Test
    void getPopularFilms() {
        User user1 = User.builder()
                .id(1)
                .email("ABOBA@mail.mail")
                .login("login")
                .name("N")
                .birthday(LocalDate.of(2000, 12, 22))
                .build();
        User user11 = User.builder()
                .id(1)
                .email("ABOBA@mail.mail")
                .login("login")
                .name("N")
                .birthday(LocalDate.of(2000, 12, 22))
                .build();
        User user2 = User.builder()
                .id(1)
                .email("ABOBA@mail.mail")
                .login("login")
                .name("N")
                .birthday(LocalDate.of(2000, 12, 22))
                .build();
        Film film1 = Film.builder()
                .name("test")
                .description("desc")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(110)
                .mpa(new Mpa(1, "G"))
                .genres(null)
                .build();
        Film film2 = Film.builder()
                .name("test")
                .description("desc")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(110)
                .mpa(new Mpa(1, "G"))
                .genres(null)
                .build();
        userDbStorage.createUser(user1);
        userDbStorage.createUser(user11);
        userDbStorage.createUser(user2);
        filmDbStorage.addFilm(film);
        filmDbStorage.addFilm(film1);
        filmDbStorage.addFilm(film2);
        filmDbStorage.addLike(film.getId(), user1.getId());
        filmDbStorage.addLike(film1.getId(), user1.getId());
        filmDbStorage.addLike(film2.getId(), user2.getId());
        assertThat(filmDbStorage.getBestFilms(film.getId())).isNotNull();
        Assertions.assertThat(filmDbStorage.getBestFilms(film.getId()).size() == 6);
    }
}