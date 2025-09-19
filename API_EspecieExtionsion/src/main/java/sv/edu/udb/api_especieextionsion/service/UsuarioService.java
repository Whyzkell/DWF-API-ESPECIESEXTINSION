package sv.edu.udb.api_especieextionsion.service;

import sv.edu.udb.api_especieextionsion.controller.dto.*;

import java.util.List;

public interface UsuarioService {
    UsuarioResponse crear(UsuarioRequest req);
    List<UsuarioResponse> listar();
    UsuarioResponse obtener(Long id);
    UsuarioResponse actualizar(Long id, UsuarioRequest req);
    void eliminar(Long id);
}
