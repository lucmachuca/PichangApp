package com.PichangApp.controller;

import com.PichangApp.dto.MatchSocialResponse;
import com.PichangApp.dto.InteraccionRequest;
import com.PichangApp.dto.InteraccionResponse;
import com.PichangApp.service.MatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class InteraccionController {

    private final MatchService matchService;

    @PostMapping("/interacciones")
    public ResponseEntity<InteraccionResponse> interaccion(@Valid @RequestBody InteraccionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(matchService.registerInteraccion(request));
    }

    @GetMapping("/matches/user/{userId}")
    public ResponseEntity<List<MatchSocialResponse>> getMatches(@PathVariable Long userId) {
        return ResponseEntity.ok(matchService.obtenerMatchesPorUsuario(userId));
    }

    @GetMapping("/matches/{matchId}")
    public ResponseEntity<MatchSocialResponse> getMatch(@PathVariable Long matchId) {
        return ResponseEntity.ok(matchService.obtenerMatchPorId(matchId));
    }

    @GetMapping("/interacciones/usuarios-interactuados/{usuarioId}")
    public ResponseEntity<List<Long>> obtenerUsuariosInteractuados(
            @PathVariable Long usuarioId
    ) {
        return ResponseEntity.ok(matchService.obtenerUsuariosInteractuados(usuarioId));
    }

}
