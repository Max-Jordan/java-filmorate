package ru.yandex.practicum.filmorate.storage.dao.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.module.FriendshipStatus;
import ru.yandex.practicum.filmorate.module.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
@Slf4j
@Qualifier
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User createUser(User user) {
        final String sqlQuery = "INSERT INTO users (EMAIL, LOGIN, NAME, BIRTHDAY) " +
                "VALUES ( ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"id"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setDate(4, Date.valueOf(user.getBirthday()));
            return stmt;
        }, keyHolder);

        log.info("Пользователь с логином {} создан ",user.getLogin());
        user.setId(keyHolder.getKey().intValue());
        return user;
    }

    @Override
    public Collection<User> findAll() {
        final String sqlQuery = "SELECT * FROM users";
        log.info("Список пользователей отправлен");
        return jdbcTemplate.query(sqlQuery, this::makeUser);
    }

    @Override
    public User updateUser(User user) {
        final String findQuery = "SELECT * FROM users WHERE id = ?";
        SqlRowSet usersRows = jdbcTemplate.queryForRowSet(findQuery, user.getId());
        if (!usersRows.next()) {
            log.info("Пользователь с ид {} не найден", user.getId());
            throw new ObjectNotFoundException("Пользователь не найден.");
        }
        final String updateQuery = "UPDATE users SET EMAIL = ?, LOGIN = ?, NAME = ?, BIRTHDAY = ?" + "WHERE id = ?";
        jdbcTemplate.update(updateQuery, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        log.info("Пользователь с ид {} обновлен", user.getId());
        return user;
    }

    @Override
    public User getById(int id) {
        final String findUser = "SELECT * FROM users WHERE id = ?";
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(findUser, id);
        if (!sqlRowSet.next()) {
            log.info("Пользователь с ид {} не найден ", id);
            throw new ObjectNotFoundException("Пользователь не найден");
        }
        final String getUser = "SELECT * FROM users WHERE id = ?";
        log.info("Пользователь с ид {} отправлен", id);
        return jdbcTemplate.queryForObject(getUser, this::makeUser, id);
    }

    @Override
    public User deleteById(int id) {
        final String deleteUser = "DELETE FROM users WHERE id = ?";
        User user = getById(id);
        jdbcTemplate.update(deleteUser, id);
        log.info("Пользователь с ид {} удален", id);
        return user;
    }


    @Override
    public List<Integer> addFriendship(int firstId, int secondId) {
        validate(firstId, secondId);
        final String sqlForWriteQuery = "INSERT INTO mutual_friendship (user_id, friend_id, status) " +
                "VALUES (?, ?, ?)";
        final String sqlForUpdateQuery = "UPDATE mutual_friendship SET status = ? " +
                "WHERE user_id = ? AND friend_id = ?";
        final String checkMutualQuery = "SELECT * FROM mutual_friendship WHERE user_id = ? AND friend_id = ?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(checkMutualQuery, firstId, secondId);

        if (userRows.first()) {
            jdbcTemplate.update(sqlForUpdateQuery, FriendshipStatus.ACCEPTED.toString(), firstId, secondId);
        } else {
            jdbcTemplate.update(sqlForWriteQuery, firstId, secondId, FriendshipStatus.REQUIRED.toString());
        }

        return List.of(firstId, secondId);
    }

    @Override
    public List<Integer> removeFriendship(int firstId, int secondId) {
        final String sqlQuery = "DELETE FROM mutual_friendship WHERE user_id = ? and friend_id = ?";
        jdbcTemplate.update(sqlQuery, firstId, secondId);
        return List.of(firstId, secondId);
    }

    @Override
    public List<User> getFriendsListById(int id) {
        final String checkQuery = "SELECT * FROM users WHERE id = ?";
        SqlRowSet followingRow = jdbcTemplate.queryForRowSet(checkQuery, id);

        if (!followingRow.next()) {
            log.warn("Пользователь с ид {} не найден", id);
            throw new ObjectNotFoundException("Пользователь не найден");
        }

        final String sqlQuery = "SELECT id, email, login, name, birthday " +
                "FROM USERS " +
                "LEFT JOIN mutual_friendship mf on users.id = mf.friend_id " +
                "WHERE user_id = ? AND status LIKE 'REQUIRED'";

        log.info("Список друзей пользователя с ид {} отправлен ",id);
        return jdbcTemplate.query(sqlQuery, this::makeUser, id);
    }

    @Override
    public List<User> getSharedFriendsList(int firstId, int secondId) {
        validate(firstId, secondId);
        final String sqlQuery = "SELECT id, email, login, name, birthday " +
                "FROM mutual_friendship AS mf " +
                "LEFT JOIN users u ON u.id = mf.friend_id " +
                "WHERE mf.user_id = ? AND mf.friend_id IN ( " +
                "SELECT friend_id " +
                "FROM mutual_friendship AS mf " +
                "LEFT JOIN users AS u ON u.id = mf.friend_id " +
                "WHERE mf.user_id = ? )";
        log.info("Список общих друзей {} и {} отправлен", firstId, secondId);
        return jdbcTemplate.query(sqlQuery, this::makeUser, firstId, secondId);
    }

    private User makeUser(ResultSet rs, int rowNum) throws SQLException {
        int id = rs.getInt("id");
        String email = rs.getString("email");
        String login = rs.getString("login");
        String name = rs.getString("name");
        LocalDate birthday = rs.getDate("birthday").toLocalDate();
        return new User(id, email, login, name, birthday);
    }

    private void validate(int firstId, int secondId) {
        final String check = "SELECT * FROM users WHERE id = ?";
        SqlRowSet followingRow = jdbcTemplate.queryForRowSet(check, firstId);
        SqlRowSet followerRow = jdbcTemplate.queryForRowSet(check, secondId);

        if (!followingRow.next() || !followerRow.next()) {
            log.warn("Пользователи с ид {} и {} не найдены", firstId, secondId);
            throw new ObjectNotFoundException("Пользователи не найдены");
        }
    }
}
