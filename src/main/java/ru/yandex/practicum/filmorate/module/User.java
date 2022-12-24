package ru.yandex.practicum.filmorate.module;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;


@Getter
@Setter
@Builder
@AllArgsConstructor
public class User {

    private int id;
    @NonNull
    @NotBlank
    @Email
    private String email;
    @NonNull
    @NotBlank
    @Pattern(regexp = "\\S+", message = "Логин содержит пробелы")
    private String login;
    private String name;
    @NonNull
    @PastOrPresent(message = "Некорректная дата рождения")
    private LocalDate birthday;
}
