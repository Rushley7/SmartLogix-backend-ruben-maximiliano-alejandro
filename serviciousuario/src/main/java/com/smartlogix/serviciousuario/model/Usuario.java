package com.smartlogix.serviciousuario.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String correo;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String contrasena;

    // 🔥 IMPORTANTE: rol del sistema
    @Column(nullable = false)
    private String rol; // ADMIN / OPERADOR / LOGISTICA
}