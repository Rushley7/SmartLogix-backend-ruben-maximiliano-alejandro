package com.smartlogix.serviciousuario.service;

import com.smartlogix.serviciousuario.model.Usuario;
import java.util.List;

public interface UsuarioService {

    Usuario guardar(Usuario usuario);

    List<Usuario> listar();

    void eliminar(Long id); 

    String login(String correo, String contrasena);
}