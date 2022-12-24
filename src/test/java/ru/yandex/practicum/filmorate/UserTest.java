package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.module.User;
import ru.yandex.practicum.filmorate.storage.dao.user.UserDbStorage;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserTest {
    private final UserDbStorage userDbStorage;
    User user = User.builder()
            .id(1)
            .email("ABOBA@mail.mail")
            .login("login")
            .name("N")
            .birthday(LocalDate.of(2000, 12, 22))
            .build();

    @Test
    void createUserTest() {
        userDbStorage.createUser(user);
        AssertionsForClassTypes.assertThat(user).extracting("name").isNotNull();
    }

    @Test
    void updateUser() {
        userDbStorage.createUser(user);
        user.setName("drujokpirojok");
        user.setLogin("drujokpirojok");
        user.setEmail("updatedExample@mail.mail");
        userDbStorage.updateUser(user);
        AssertionsForClassTypes.assertThat(userDbStorage.getById(user.getId()))
                .hasFieldOrPropertyWithValue("login", "drujokpirojok")
                .hasFieldOrPropertyWithValue("name", "drujokpirojok")
                .hasFieldOrPropertyWithValue("email", "updatedExample@mail.mail");
    }

    @Test
    void addFriendshipAndRemoveTest() {
        User friend = User.builder()
                .email("drug@mail.mail")
                .login("drug")
                .name("beb")
                .birthday(LocalDate.of(2000, 12, 22))
                .build();
        User drujokpirojok = User.builder()
                .email("drujokpirojok@mail.mail")
                .login("drujokpirojok")
                .name("drujokpirojok")
                .birthday(LocalDate.of(2000, 10, 20))
                .build();
        userDbStorage.createUser(friend);
        userDbStorage.createUser(drujokpirojok);
        assertThat(userDbStorage.getFriendsListById(friend.getId()).isEmpty());
        userDbStorage.addFriendship(friend.getId(), drujokpirojok.getId());
        Assertions.assertThat(userDbStorage.getFriendsListById(friend.getId()).size() == 2);
        userDbStorage.removeFriendship(friend.getId(), drujokpirojok.getId());
        Assertions.assertThat(userDbStorage.getFriendsListById(friend.getId()).size() == 0);
    }
}
