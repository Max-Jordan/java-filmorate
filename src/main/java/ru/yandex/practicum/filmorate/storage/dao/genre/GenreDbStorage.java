package ru.yandex.practicum.filmorate.storage.dao.genre;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.module.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;


@Slf4j
@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Genre> findAll() {
        final String findAllQuery = "SELECT * FROM genre";
        return jdbcTemplate.query(findAllQuery, this::genreMaker);
    }

    @Override
    public Genre getById(int id) {
        final String getGenreQuery = "SELECT * FROM genre WHERE genre_id = ?";
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(getGenreQuery, id);
        if (!sqlRowSet.next()) {
            log.info("Жанр по ид {} не найден", id);
            throw new ObjectNotFoundException("Жанр не найден");
        }
        final String sqlQuery = "SELECT * FROM genre WHERE genre_id = ?";
        return jdbcTemplate.queryForObject(sqlQuery, this::genreMaker, id);
    }

    private Genre genreMaker(ResultSet rs, int rowNum) throws SQLException {
        final int id = rs.getInt("genre_id");
        final String name = rs.getString("name");
        return new Genre(id, name);
    }
}
