package com.PichangApp.msvc_usuario.models.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.Map;

@Entity
@Table(name = "user_profiles")
@Data
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String descripcion;
    private Integer edad;
    private String fotoUrl;

    // El deporte principal elegido (ej: "BOXEO", "BASKET")
    private String deportePrincipal;

    /**
     * Aquí vive la magia: JSONB de PostgreSQL.
     * Si eliges BOXEO, aquí guardas: {"peso": 80, "mano": "zurda"}
     * Si eliges BASKET, guardas: {"altura": 190, "posicion": "Base"}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> atributosDeportivos;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}