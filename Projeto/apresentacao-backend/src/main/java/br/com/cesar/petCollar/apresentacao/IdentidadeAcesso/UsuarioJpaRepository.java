package br.com.cesar.petCollar.apresentacao.IdentidadeAcesso;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioJpa, String> {

    Optional<UsuarioJpa> findByEmail(String email);

    List<UsuarioJpa> findByPerfilIn(List<String> perfis);

    @Query("SELECT u.identificador FROM UsuarioJpa u WHERE u.perfil = :perfil")
    List<String> listarIdentificadoresPorPerfil(String perfil);
}
