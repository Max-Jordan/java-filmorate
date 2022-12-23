package ru.yandex.practicum.filmorate.storage.dao.mpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.module.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;


@Slf4j
@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Mpa> findAll() {
        final String findAllQuery = "SELECT * FROM mpa";
        return jdbcTemplate.query(findAllQuery, this::mpaMaker);
    }

    @Override
    public Mpa getById(int id) {
        final String getMpaQuery = "SELECT * FROM mpa WHERE id= ?";
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(getMpaQuery, id);
        if (!sqlRowSet.next()) {
            log.info("Рейтинг по ид {} не найден", id);
            throw new ObjectNotFoundException("Рейтинг не найден");
        }
        return jdbcTemplate.queryForObject(getMpaQuery, this::mpaMaker, id);
    }

    private Mpa mpaMaker(ResultSet rs, int rowNum) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        return new Mpa(id, name);
    }
}
