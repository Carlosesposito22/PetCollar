package br.com.cesar.petCollar.apresentacao.IdentidadeAcesso;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter JPA de {@link UsuarioRepositorio}. Traduz entre
 * {@link UsuarioAutenticavel} (domínio) e {@link UsuarioJpa} (persistência).
 */
@Repository
public class UsuarioRepositorioJpa implements UsuarioRepositorio {

    // Prefixos fixos de matrícula por perfil (regra de negócio de IdentidadeAcesso)
    private static final Map<Perfil, Long> BASE_MATRICULA = Map.of(
            Perfil.RECEPCIONISTA,      100_000L,
            Perfil.MEDICO_VETERINARIO, 200_000L,
            Perfil.ADMIN_CLINICA,      900_000L
    );

    private final UsuarioJpaRepository jpa;

    public UsuarioRepositorioJpa(UsuarioJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<UsuarioAutenticavel> buscar(Perfil perfil, String identificador) {
        if (identificador == null) return Optional.empty();
        return jpa.findById(identificador)
                .filter(u -> u.getPerfil().equals(perfil.name()))
                .map(UsuarioJpa::toDomain);
    }

    @Override
    public Optional<UsuarioAutenticavel> buscarPorEmail(String email) {
        if (email == null) return Optional.empty();
        return jpa.findByEmail(email).map(UsuarioJpa::toDomain);
    }

    @Override
    public List<UsuarioAutenticavel> listarPorPerfil(Perfil... perfis) {
        List<String> nomes = List.of(perfis).stream()
                .map(Perfil::name)
                .collect(Collectors.toList());
        return jpa.findByPerfilIn(nomes).stream()
                .map(UsuarioJpa::toDomain)
                .sorted((a, b) -> a.identificador().compareTo(b.identificador()))
                .collect(Collectors.toList());
    }

    @Override
    public void salvar(UsuarioAutenticavel usuario) {
        jpa.save(UsuarioJpa.fromDomain(usuario));
    }

    @Override
    public String proximaMatricula(Perfil perfil) {
        Long base = BASE_MATRICULA.get(perfil);
        if (base == null) {
            throw new IllegalArgumentException("Perfil sem matrícula sequencial: " + perfil);
        }
        long max = jpa.listarIdentificadoresPorPerfil(perfil.name()).stream()
                .filter(id -> id.matches("\\d+"))
                .mapToLong(Long::parseLong)
                .max()
                .orElse(base);
        return String.valueOf(max + 1);
    }
}
