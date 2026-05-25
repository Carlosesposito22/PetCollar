package br.com.cesar.petCollar.apresentacao.IdentidadeAcesso;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepositorio {

    Optional<UsuarioAutenticavel> buscar(Perfil perfil, String identificador);

    Optional<UsuarioAutenticavel> buscarPorEmail(String email);

    List<UsuarioAutenticavel> listarPorPerfil(Perfil... perfis);

    void salvar(UsuarioAutenticavel usuario);

    String proximaMatricula(Perfil perfil);
}
