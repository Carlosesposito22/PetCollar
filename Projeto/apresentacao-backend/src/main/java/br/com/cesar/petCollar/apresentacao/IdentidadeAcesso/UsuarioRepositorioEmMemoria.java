package br.com.cesar.petCollar.apresentacao.IdentidadeAcesso;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

/**
 * Implementação provisória usada enquanto o domínio IdentidadeAcesso não expõe o
 * repositório real. Substituir por adapter que consulte o agregado quando ele existir.
 */
@Repository
public class UsuarioRepositorioEmMemoria implements UsuarioRepositorio {

    private final ConcurrentMap<String, UsuarioAutenticavel> usuarios = new ConcurrentHashMap<>();
    private final ConcurrentMap<Perfil, AtomicInteger> sequenciadores = new ConcurrentHashMap<>();

    public UsuarioRepositorioEmMemoria(PasswordEncoder encoder) {
        sequenciadores.put(Perfil.RECEPCIONISTA,      new AtomicInteger(100000));
        sequenciadores.put(Perfil.MEDICO_VETERINARIO, new AtomicInteger(200000));
        sequenciadores.put(Perfil.ADMIN_CLINICA,      new AtomicInteger(900000));

        var senhaPadrao = encoder.encode("petcollar123");

        salvar(new UsuarioAutenticavel(
                "admin@petcollar.com", "Administrador Demo",
                Perfil.ADMIN_CLINICA, senhaPadrao, StatusConta.ATIVA));

        salvar(new UsuarioAutenticavel(
                "tutor@petcollar.com", "Tutor Demo",
                Perfil.TUTOR, senhaPadrao, StatusConta.ATIVA,
                "123.456.789-00", "(11) 99999-0001",
                "Rua das Flores, 100 — São Paulo/SP", "tutor@petcollar.com"));

        salvar(new UsuarioAutenticavel(
                "suspenso@petcollar.com", "Tutor Suspenso",
                Perfil.TUTOR, senhaPadrao, StatusConta.SUSPENSA,
                "987.654.321-00", "(11) 99999-0002",
                "Av. Paulista, 1000 — São Paulo/SP", "suspenso@petcollar.com"));

        salvar(new UsuarioAutenticavel(
                proximaMatricula(Perfil.RECEPCIONISTA), "Recepcionista Demo",
                Perfil.RECEPCIONISTA, senhaPadrao, StatusConta.ATIVA));

        salvar(new UsuarioAutenticavel(
                proximaMatricula(Perfil.MEDICO_VETERINARIO), "Médico Demo",
                Perfil.MEDICO_VETERINARIO, senhaPadrao, StatusConta.ATIVA));
    }

    private static String chave(Perfil perfil, String identificador) {
        return perfil.name() + "::" + identificador.toLowerCase();
    }

    @Override
    public Optional<UsuarioAutenticavel> buscar(Perfil perfil, String identificador) {
        if (identificador == null) return Optional.empty();
        return Optional.ofNullable(usuarios.get(chave(perfil, identificador)));
    }

    @Override
    public Optional<UsuarioAutenticavel> buscarPorEmail(String email) {
        if (email == null) return Optional.empty();
        return usuarios.values().stream()
                .filter(u -> email.equalsIgnoreCase(u.email()))
                .findFirst();
    }

    @Override
    public List<UsuarioAutenticavel> listarPorPerfil(Perfil... perfis) {
        Set<Perfil> filtro = Set.of(perfis);
        return usuarios.values().stream()
                .filter(u -> filtro.contains(u.perfil()))
                .sorted((a, b) -> a.identificador().compareTo(b.identificador()))
                .collect(Collectors.toList());
    }

    @Override
    public void salvar(UsuarioAutenticavel usuario) {
        usuarios.put(chave(usuario.perfil(), usuario.identificador()), usuario);
    }

    @Override
    public String proximaMatricula(Perfil perfil) {
        AtomicInteger seq = sequenciadores.get(perfil);
        if (seq == null) {
            throw new IllegalArgumentException("Perfil sem matrícula sequencial: " + perfil);
        }
        return String.valueOf(seq.incrementAndGet());
    }
}
