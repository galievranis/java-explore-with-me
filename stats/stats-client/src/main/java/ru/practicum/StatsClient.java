package ru.practicum;

import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;

public interface StatsClient {

    void saveEndpoint(String app, String uri, String ip, LocalDateTime localDateTime);

    ViewStatsDto getStats();
}
