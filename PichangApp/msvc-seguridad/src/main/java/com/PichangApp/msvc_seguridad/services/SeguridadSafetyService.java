package com.PichangApp.msvc_seguridad.services;

import com.PichangApp.msvc_seguridad.dtos.BloqueoRequest;
import com.PichangApp.msvc_seguridad.dtos.BloqueoResponse;
import com.PichangApp.msvc_seguridad.dtos.ReporteRequest;
import com.PichangApp.msvc_seguridad.dtos.ReporteResponse;
import com.PichangApp.msvc_seguridad.models.Bloqueo;
import com.PichangApp.msvc_seguridad.models.Reporte;
import com.PichangApp.msvc_seguridad.repositories.BloqueoRepository;
import com.PichangApp.msvc_seguridad.repositories.ReporteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SeguridadSafetyService {

    private final ReporteRepository reporteRepository;
    private final BloqueoRepository bloqueoRepository;

    public SeguridadSafetyService(ReporteRepository reporteRepository, BloqueoRepository bloqueoRepository) {
        this.reporteRepository = reporteRepository;
        this.bloqueoRepository = bloqueoRepository;
    }

    @Transactional
    public ReporteResponse reportarUsuario(ReporteRequest request) {
        if (request.idUsuarioDenunciante().equals(request.idUsuarioDenunciado())) {
            throw new IllegalArgumentException("Un usuario no puede reportarse a sí mismo.");
        }

        Reporte reporte = Reporte.builder()
                .idUsuarioDenunciante(request.idUsuarioDenunciante())
                .idUsuarioDenunciado(request.idUsuarioDenunciado())
                .motivo(request.motivo())
                .descripcion(request.descripcion())
                .estado("PENDIENTE")
                .build();

        return toReporteResponse(reporteRepository.save(reporte));
    }

    @Transactional
    public BloqueoResponse bloquearUsuario(BloqueoRequest request) {
        if (request.idUsuarioOrigen().equals(request.idUsuarioBloqueado())) {
            throw new IllegalArgumentException("Un usuario no puede bloquearse a sí mismo.");
        }

        bloqueoRepository.findByIdUsuarioOrigenAndIdUsuarioBloqueado(
                request.idUsuarioOrigen(),
                request.idUsuarioBloqueado()
        ).ifPresent(bloqueo -> {
            throw new IllegalStateException("El usuario ya se encuentra bloqueado.");
        });

        Bloqueo bloqueo = Bloqueo.builder()
                .idUsuarioOrigen(request.idUsuarioOrigen())
                .idUsuarioBloqueado(request.idUsuarioBloqueado())
                .build();

        return toBloqueoResponse(bloqueoRepository.save(bloqueo));
    }

    @Transactional(readOnly = true)
    public List<ReporteResponse> listarReportes() {
        return reporteRepository.findAll()
                .stream()
                .map(this::toReporteResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BloqueoResponse> listarBloqueosPorUsuario(Long idUsuario) {
        return bloqueoRepository.findByIdUsuarioOrigen(idUsuario)
                .stream()
                .map(this::toBloqueoResponse)
                .toList();
    }

    private ReporteResponse toReporteResponse(Reporte reporte) {
        return new ReporteResponse(
                reporte.getIdReporte(),
                reporte.getIdUsuarioDenunciante(),
                reporte.getIdUsuarioDenunciado(),
                reporte.getMotivo(),
                reporte.getDescripcion(),
                reporte.getEstado(),
                reporte.getFechaReporte()
        );
    }

    private BloqueoResponse toBloqueoResponse(Bloqueo bloqueo) {
        return new BloqueoResponse(
                bloqueo.getIdBloqueo(),
                bloqueo.getIdUsuarioOrigen(),
                bloqueo.getIdUsuarioBloqueado(),
                bloqueo.getFechaBloqueo()
        );
    }
}