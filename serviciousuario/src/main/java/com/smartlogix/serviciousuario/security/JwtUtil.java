package com.smartlogix.serviciousuario.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final String SECRET = "clave_super_secreta_123456789_clave_larga_segura_2026";
    private final long EXPIRATION = 1000 * 60 * 60; // 1 hora

    private Key getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    // 🔥 GENERAR TOKEN CON ROL
    public String generarToken(String correo, String rol) {
        return Jwts.builder()
                .setSubject(correo)
                .claim("rol", rol) // 🔥 AQUÍ GUARDAMOS EL ROL
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 🔥 EXTRAER CORREO
    public String extraerCorreo(String token) {
        return getClaims(token).getSubject();
    }

    // 🔥 EXTRAER ROL
    public String extraerRol(String token) {
        return getClaims(token).get("rol", String.class);
    }

    // 🔥 VALIDAR TOKEN
    public boolean esTokenValido(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 🔥 MÉTODO INTERNO
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}