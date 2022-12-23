package ru.yandex.practicum.filmorate.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.module.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
@Validated
public class UserController {
    private final UserService userService;

    @GetMapping
    public Collection<User> getUsers() {
        log.info("Получен /GET запрос о выводе пользователей");
        return userService.findAll();
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        log.info("Получен /POST запрос создание пользователя");
        return userService.create(user);
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        log.info("Получен /PUT запрос обновление пользователя");
        return userService.update(user);
    }

    @DeleteMapping("/{id}")
    public User removeUserById(@PathVariable int id) {
        return userService.deleteById(id);
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable int id) {
        return userService.getById(id);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public List<Integer> removeFriend(@PathVariable int id, @PathVariable int friendId) {
        return userService.removeFriend(id, friendId);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public List<Integer> addFriend(@PathVariable int id, @PathVariable int friendId) {
        return userService.addFriend(id, friendId);
    }

    @GetMapping("{id}/friends")
    public List<User> getFriendsList(@PathVariable int id) {
        return userService.getUsersFriendListById(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable int id, @PathVariable int otherId) {
        return userService.getSharedFriendsList(id, otherId);
    }
}

