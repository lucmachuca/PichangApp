package com.PichangApp.dto;

public record MatchCreatedEvent(
        Long matchId,
        Long usuarioAId,
        Long usuarioBId
) {
}