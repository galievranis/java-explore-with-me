package ru.practicum;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsClientImpl implements StatsClient {

    private final WebClient webClient = WebClient.builder()
            .baseUrl("http://localhost:9090")
            .build();

    @Override
    public void saveEndpoint(String app, String uri, String ip, LocalDateTime timestamp) {
        EndpointHitDto endpointHitDto = EndpointHitDto.builder()
                .app(app)
                .uri(uri)
                .ip(ip)
                .timestamp(timestamp)
                .build();

        webClient.post()
                .uri("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(endpointHitDto))
                .retrieve()
                .bodyToMono(EndpointHitDto.class)
                .block();
    }

    @Override
    public List<ViewStatsDto> getStats(String start, String end, List<String> uris, Boolean unique) {
        StringBuilder uri = new StringBuilder("/stats?start=" + start + "&end=" + end);

        if (uris != null) {
            uri.append("&uris=").append(uris);
        }

        if (unique != null) {
            uri.append("&unique=").append(unique);
        }

        return webClient.get()
                .uri(String.valueOf(uri))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(ViewStatsDto.class)
                .collectList()
                .block();
    }
}
