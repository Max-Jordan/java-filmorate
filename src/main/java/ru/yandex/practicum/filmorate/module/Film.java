package ru.yandex.practicum.filmorate.module;

import lombok.*;
import ru.yandex.practicum.filmorate.validation.BeginOfCinemaEra;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Film {

    private int id;
    @NonNull
    @NotBlank
    private String name;
    @NonNull
    @NotBlank
    @Size(max = 200, message = "слишком длинное описание, больше 200 символов")
    private String description;
    @NonNull
    @BeginOfCinemaEra
    private LocalDate releaseDate;
    @Positive
    private int duration;
    private Mpa mpa;
    private List<Genre> genres;
}
