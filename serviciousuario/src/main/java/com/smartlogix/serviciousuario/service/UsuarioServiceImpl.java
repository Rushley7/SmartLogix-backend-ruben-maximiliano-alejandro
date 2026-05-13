package com.smartlogix.serviciousuario.service;

import com.smartlogix.serviciousuario.model.Usuario;
import com.smartlogix.serviciousuario.repository.UsuarioRepository;
import com.smartlogix.serviciousuario.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository repository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UsuarioServiceImpl(UsuarioRepository repository,
                              BCryptPasswordEncoder passwordEncoder,
                              JwtUtil jwtUtil) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // 🔥 REGEX NOMBRE (SOLO LETRAS Y ESPACIOS)
    private final Pattern nombreRegex =
            Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$");

    // 🔥 REGEX CORREO (.COM Y .CL)
    private final Pattern correoRegex =
            Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.(com|cl)$");

    @Override
    public Usuario guardar(Usuario usuario) {

        // 🔥 VALIDAR NOMBRE
        if (usuario.getNombre() == null ||
                usuario.getNombre().trim().isEmpty()) {

            throw new RuntimeException(
                    "El nombre es obligatorio"
            );
        }

        String nombreLimpio = usuario.getNombre().trim();

        if (nombreLimpio.length() < 3) {

            throw new RuntimeException(
                    "El nombre debe tener mínimo 3 caracteres"
            );
        }

        if (!nombreRegex.matcher(nombreLimpio).matches()) {

            throw new RuntimeException(
                    "El nombre solo puede contener letras"
            );
        }

        // 🔥 VALIDAR CORREO
        if (usuario.getCorreo() == null ||
                !correoRegex.matcher(usuario.getCorreo()).matches()) {

            throw new RuntimeException(
                    "Correo inválido. Debe terminar en .com o .cl"
            );
        }

        // 🔥 VALIDAR ROL
        if (usuario.getRol() == null ||
                usuario.getRol().isEmpty()) {

            throw new RuntimeException(
                    "El rol es obligatorio"
            );
        }

        // 🔥 UPDATE
        if (usuario.getId() != null &&
                repository.existsById(usuario.getId())) {

            Usuario existente = repository.findById(usuario.getId())
                    .orElseThrow(() ->
                            new RuntimeException("Usuario no encontrado"));

            // 🔥 SI VIENE CONTRASEÑA → VALIDAR
            if (usuario.getContrasena() != null &&
                    !usuario.getContrasena().isEmpty()) {

                if (usuario.getContrasena().length() < 8 ||
                        usuario.getContrasena().length() > 24) {

                    throw new RuntimeException(
                            "La contraseña debe tener entre 8 y 24 caracteres"
                    );
                }

                existente.setContrasena(
                        passwordEncoder.encode(usuario.getContrasena())
                );
            }

            // 🔥 ACTUALIZAR DATOS
            existente.setNombre(nombreLimpio);

            existente.setCorreo(
                    usuario.getCorreo().trim().toLowerCase()
            );

            existente.setRol(usuario.getRol());

            return repository.save(existente);

        } else {

            // 🔥 CREATE

            if (usuario.getContrasena() == null ||
                    usuario.getContrasena().isEmpty()) {

                throw new RuntimeException(
                        "La contraseña es obligatoria"
                );
            }

            if (usuario.getContrasena().length() < 8 ||
                    usuario.getContrasena().length() > 24) {

                throw new RuntimeException(
                        "La contraseña debe tener entre 8 y 24 caracteres"
                );
            }

            usuario.setNombre(nombreLimpio);

            usuario.setCorreo(
                    usuario.getCorreo().trim().toLowerCase()
            );

            usuario.setContrasena(
                    passwordEncoder.encode(usuario.getContrasena())
            );

            return repository.save(usuario);
        }
    }

    @Override
    public List<Usuario> listar() {
        return repository.findAll();
    }

    // 🔥 ELIMINAR
    @Override
    public void eliminar(Long id) {

        if (!repository.existsById(id)) {

            throw new RuntimeException(
                    "Usuario no existe"
            );
        }

        repository.deleteById(id);
    }

    @Override
    public String login(String correo, String contrasena) {

        // 🔥 VALIDAR CORREO
        if (correo == null ||
                !correoRegex.matcher(correo).matches()) {

            throw new RuntimeException(
                    "Correo inválido"
            );
        }

        // 🔥 VALIDAR CONTRASEÑA
        if (contrasena == null ||
                contrasena.length() < 8 ||
                contrasena.length() > 24) {

            throw new RuntimeException(
                    "Contraseña inválida"
            );
        }

        Usuario usuario = repository.findByCorreo(
                        correo.trim().toLowerCase()
                )
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(
                contrasena,
                usuario.getContrasena()
        )) {

            throw new RuntimeException(
                    "Credenciales incorrectas"
            );
        }

        // 🔥 TOKEN CON ROL
        return jwtUtil.generarToken(
                usuario.getCorreo(),
                usuario.getRol()
        );
    }
}