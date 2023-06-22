package ru.practicum.dto;

import lombok.*;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Jacksonized
public class EndpointHitDto {

    private Long id;

    @NotBlank(message = "App name can't be empty or null")
    private String app;

    @NotBlank(message = "URI can't be empty or null")
    private String uri;

    @NotBlank(message = "IP can't be empty or null")
    private String ip;

    @NotNull(message = "Timestamp can't be empty or null")
    private String timestamp;
}
