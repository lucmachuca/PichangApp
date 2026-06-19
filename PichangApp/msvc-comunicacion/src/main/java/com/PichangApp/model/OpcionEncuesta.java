package com.PichangApp.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "opciones_encuesta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpcionEncuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encuesta_id", nullable = false)
    private Encuesta encuesta;

    @Column(nullable = false)
    private String texto;

    @OneToMany(mappedBy = "opcion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VotoEncuesta> votos;
}
