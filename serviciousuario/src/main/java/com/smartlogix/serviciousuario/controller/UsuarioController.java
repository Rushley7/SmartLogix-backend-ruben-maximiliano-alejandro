package com.smartlogix.serviciousuario.controller;

import com.smartlogix.serviciousuario.model.Usuario;
import com.smartlogix.serviciousuario.service.UsuarioService;
import com.smartlogix.serviciousuario.dto.LoginRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    // 🔥 CREAR
    @PostMapping
    public Usuario crear(@RequestBody Usuario usuario){
        return service.guardar(usuario);
    }

    // 🔥 LISTAR
    @GetMapping
    public List<Usuario> listar(){
        return service.listar();
    }

    // 🔥 ACTUALIZAR
    @PutMapping("/{id}")
    public Usuario actualizar(@PathVariable Long id, @RequestBody Usuario usuario){
        usuario.setId(id);
        return service.guardar(usuario);
    }

    // 🔥 ELIMINAR
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id){
        service.eliminar(id);
    }

    // 🔥 LOGIN
    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request){
        return service.login(request.getCorreo(), request.getContrasena());
    }
}