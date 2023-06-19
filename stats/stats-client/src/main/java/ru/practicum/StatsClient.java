package ru.practicum;

import ru.practicum.dto.ViewStatsDto;

import java.util.List;

public interface StatsClient {

    void saveEndpoint(String app, String uri, String ip);

    List<ViewStatsDto> getStats(String start, String end, List<String> uris, Boolean unique);
}
