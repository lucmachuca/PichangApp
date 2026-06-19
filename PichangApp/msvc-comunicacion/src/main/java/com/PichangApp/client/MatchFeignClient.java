package com.PichangApp.client;

import com.PichangApp.dto.MatchSocialDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "msvc-match-comunicacion",
        url = "${msvc.match.url:http://localhost:8081}",
        path = "/api/v1/matches"
)
public interface MatchFeignClient {

    @GetMapping("/{matchId}")
    MatchSocialDTO obtenerMatchPorId(@PathVariable("matchId") Long matchId);
}