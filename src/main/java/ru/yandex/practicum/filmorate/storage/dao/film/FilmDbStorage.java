package ru.yandex.practicum.filmorate.storage.dao.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.module.Film;
import ru.yandex.practicum.filmorate.module.Genre;
import ru.yandex.practicum.filmorate.module.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Repository
@Slf4j
@Qualifier
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film addFilm(Film film) {
        final String sqlQuery = "INSERT INTO FILMS (name, description, release_date, duration) " +
                "VALUES (?, ?, ?, ?)";
        KeyHolder generatedId = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setLong(4, film.getDuration());
            return stmt;
        }, generatedId);
        film.setId(Objects.requireNonNull(generatedId.getKey()).intValue());
        final String mpaSqlQuery = "INSERT INTO mpa_films (film_id, mpa_id) VALUES (?, ?)";
        jdbcTemplate.update(mpaSqlQuery, film.getId(), film.getMpa().getId());
        final String genresSqlQuery = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
        if (film.getGenres() != null) {
            for (Genre g : film.getGenres()) {
                jdbcTemplate.update(genresSqlQuery, film.getId(), g.getId());
            }
        }
        return film;
    }

    @Override
    public Collection<Film> findAll() {
        final String sqlQuery = "SELECT * FROM films";
        return jdbcTemplate.query(sqlQuery, this::makeFilm);
    }

    @Override
    public Film updateFilm(Film film) {
        final String findQuery = "SELECT * FROM films WHERE id = ?";
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(findQuery, film.getId());
        if (!sqlRowSet.next()) {
            log.info("Фильм с таким ид {} не найден", film.getId());
            throw new ObjectNotFoundException("Фильм не найден");
        }
        final String updateQuery = "UPDATE films SET name = ?, description = ?, release_date = ?, " +
                "duration = ?" +
                "WHERE id = ?";
        if (film.getMpa() != null) {
            final String deleteMpa = "DELETE FROM mpa_films WHERE film_id = ?";
            final String updateMpa = "INSERT INTO mpa_films (film_id, mpa_id) VALUES (?, ?)";
            jdbcTemplate.update(deleteMpa, film.getId());
            jdbcTemplate.update(updateMpa, film.getId(), film.getMpa().getId());
        }
        if (film.getGenres() != null) {
            final String deleteGenresQuery = "DELETE FROM film_genre WHERE film_id = ?";
            final String updateGenresQuery = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
            jdbcTemplate.update(deleteGenresQuery, film.getId());
            for (Genre g : film.getGenres()) {
                String checkDuplicate = "SELECT * FROM film_genre WHERE film_id = ? AND genre_id = ?";
                SqlRowSet checkRows = jdbcTemplate.queryForRowSet(checkDuplicate, film.getId(), g.getId());
                if (!checkRows.next()) {
                    jdbcTemplate.update(updateGenresQuery, film.getId(), g.getId());
                }
            }
        }
        jdbcTemplate.update(updateQuery, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getId());
        film.setMpa(findMpa(film.getId()));
        film.setGenres(findGenres(film.getId()));
        log.info("Фильм с ид {} обновлен ", film.getId());
        return film;
    }

    @Override
    public Film getFilmById(int id) {
        final String findFilm = "SELECT * FROM films WHERE id = ?";
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(findFilm, id);
        if (!sqlRowSet.next()) {
            log.info("Фильм с ид {} не найден ", id);
            throw new ObjectNotFoundException("Фильм не найден");
        }
        final String sqlQuery = "SELECT * FROM films WHERE id = ?";
        return jdbcTemplate.queryForObject(sqlQuery, this::makeFilm, id);
    }

    @Override
    public Film deleteById(int id) {
        final String findFilm = "SELECT * FROM films WHERE id = ?";
        jdbcTemplate.update(findFilm, id);
        return getFilmById(id);
    }

    @Override
    public Film addLike(int filmId, int userId) {
        validate(filmId, userId);
        final String sqlQuery = "INSERT INTO films_likes (film_id, user_id) values (?,?)";
        jdbcTemplate.update(sqlQuery, filmId, userId);
        log.info("Лайк поставлен");
        return getFilmById(filmId);
    }

    @Override
    public Film removeLike(int filmId, int userId) {
        validate(filmId, userId);
        final String sqlQuery = "DELETE FROM films_likes " +
                "WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sqlQuery, filmId, userId);
        log.info("Лайк к фильму удалён");
        return getFilmById(filmId);
    }

    @Override
    public List<Film> getBestFilms(int count) {
        String sqlQuery = "SELECT id, name, description, release_date, duration " +
                "FROM films " +
                "LEFT JOIN films_likes fl ON films.id = fl.film_id " +
                "group by films.id, fl.film_id IN ( " +
                "    SELECT film_id " +
                "    FROM films_likes " +
                ") " +
                "ORDER BY COUNT(fl.film_id) DESC " +
                "LIMIT ?";

        return jdbcTemplate.query(sqlQuery, this::makeFilm, count);
    }

    @Override
    public List<Film> getPopularFilmByGenre(int idGenre, int count) {
//        final String popularByGenreSql = "SELECT id, name, description, release_date, duration " +
//                "FROM films AS f" +
//                "LEFT JOIN film_genre fg ON f.id = fg.film_id";
        return List.of();
    }

    @Override
    public List<Film> getPopularFilmByYear(int year) {
        return List.of();
    }

    private Film makeFilm(ResultSet resultSet, int rowNum) throws SQLException {
        final int id = resultSet.getInt("id");
        final String name = resultSet.getString("name");
        final String description = resultSet.getString("description");
        final LocalDate releaseDate = resultSet.getDate("release_date").toLocalDate();
        int duration = resultSet.getInt("duration");

        return new Film(id, name, description, releaseDate, duration, findMpa(id), findGenres(id));
    }

    private Genre makeGenre(ResultSet rs, int rowNum) throws SQLException {
        final int id = rs.getInt("genre_id");
        final String name = rs.getString("name");
        return new Genre(id, name);
    }

    private Mpa makeMpa(ResultSet rs, int rowNum) throws SQLException {
        final int id = rs.getInt("id");
        final String name = rs.getString("name");
        return new Mpa(id, name);
    }

    private Mpa findMpa(int filmId) {
        final String mpaSqlQuery = "SELECT id, name " +
                "FROM mpa " +
                "LEFT JOIN mpa_films MF ON mpa.id = mf.mpa_id " +
                "WHERE film_id = ?";

        return jdbcTemplate.queryForObject(mpaSqlQuery, this::makeMpa, filmId);
    }

    private List<Genre> findGenres(int filmId) {
        final String genresSqlQuery = "SELECT genre.genre_id, name " +
                "FROM genre " +
                "LEFT JOIN film_genre FG on genre.genre_id = FG.GENRE_ID " +
                "WHERE film_id = ?";

        return jdbcTemplate.query(genresSqlQuery, this::makeGenre, filmId);
    }

    private void validate(int filmId, int userId) {
        final String checkFilmQuery = "SELECT * FROM films WHERE id = ?";
        final String checkUserQuery = "SELECT * FROM users WHERE id = ?";

        SqlRowSet filmRows = jdbcTemplate.queryForRowSet(checkFilmQuery, filmId);
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(checkUserQuery, userId);

        if (!filmRows.next() || !userRows.next()) {
            log.info("Фильм {} или пользователь {} не найден.", filmId, userId);
            throw new ObjectNotFoundException("Фильм или пользователь не найден");
        }
    }
}
