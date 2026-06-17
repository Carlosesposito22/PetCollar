package br.com.cesar.petCollar.apresentacao.IdentidadeAcesso;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.cesar.petCollar.aplicacao.AssinaturaFaturamento.PlanosPadrao;
import br.com.cesar.petCollar.aplicacao.BeneficiosPlano.ProvisionarBeneficiosDoTutorUseCase;
import br.com.cesar.petCollar.dominio.compartilhado.PlanoId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UsuarioRepositorio repositorio;
    private final PasswordEncoder encoder;
    private final ProvisionarBeneficiosDoTutorUseCase provisionarBeneficios;

    public AdminController(UsuarioRepositorio repositorio,
                           PasswordEncoder encoder,
                           ProvisionarBeneficiosDoTutorUseCase provisionarBeneficios) {
        this.repositorio = repositorio;
        this.encoder = encoder;
        this.provisionarBeneficios = provisionarBeneficios;
    }

    @GetMapping("/funcionarios")
    public List<ResumoUsuario> listarFuncionarios() {
        return repositorio.listarPorPerfil(Perfil.RECEPCIONISTA, Perfil.MEDICO_VETERINARIO)
                .stream()
                .map(ResumoUsuario::de)
                .toList();
    }

    @PostMapping("/funcionarios")
    public ResponseEntity<ResumoUsuario> criarFuncionario(@Valid @RequestBody RequisicaoCriarFuncionario req) {
        if (req.perfil() != Perfil.RECEPCIONISTA && req.perfil() != Perfil.MEDICO_VETERINARIO) {
            throw new PerfilInvalidoException(req.perfil());
        }

        String matricula = repositorio.proximaMatricula(req.perfil());
        UsuarioAutenticavel novo = new UsuarioAutenticavel(
                matricula,
                req.nome(),
                req.perfil(),
                encoder.encode(req.senhaInicial()),
                StatusConta.ATIVA
        );
        repositorio.salvar(novo);

        return ResponseEntity.status(HttpStatus.CREATED).body(ResumoUsuario.de(novo));
    }

    @PostMapping("/funcionarios/{matricula}/suspender")
    public ResumoUsuario suspenderFuncionario(@PathVariable String matricula) {
        return mudarStatusFuncionario(matricula, StatusConta.SUSPENSA);
    }

    @PostMapping("/funcionarios/{matricula}/reativar")
    public ResumoUsuario reativarFuncionario(@PathVariable String matricula) {
        return mudarStatusFuncionario(matricula, StatusConta.ATIVA);
    }

    private ResumoUsuario mudarStatusFuncionario(String matricula, StatusConta novo) {
        UsuarioAutenticavel u = repositorio.buscar(Perfil.RECEPCIONISTA, matricula)
                .or(() -> repositorio.buscar(Perfil.MEDICO_VETERINARIO, matricula))
                .orElseThrow(UsuarioNaoEncontradoException::new);
        u.mudarStatus(novo);
        repositorio.salvar(u);
        return ResumoUsuario.de(u);
    }

    @GetMapping("/tutores")
    public List<ResumoUsuario> listarTutores() {
        return repositorio.listarPorPerfil(Perfil.TUTOR).stream()
                .map(ResumoUsuario::de)
                .toList();
    }

    @PostMapping("/tutores/{identificador}/suspender")
    public ResumoUsuario suspenderTutor(@PathVariable String identificador) {
        return mudarStatusTutor(identificador, StatusConta.SUSPENSA);
    }

    @PostMapping("/tutores/{identificador}/reativar")
    public ResumoUsuario reativarTutor(@PathVariable String identificador) {
        return mudarStatusTutor(identificador, StatusConta.ATIVA);
    }

    @PostMapping("/tutores/{identificador}/confirmar-pagamento")
    public ResumoUsuario confirmarPagamento(@PathVariable String identificador) {
        ResumoUsuario resumo = mudarStatusTutor(identificador, StatusConta.ATIVA);
        UsuarioAutenticavel tutor = repositorio.buscar(Perfil.TUTOR, identificador)
                .orElseThrow(UsuarioNaoEncontradoException::new);
        PlanoId planoId = (tutor.planoId() != null && !tutor.planoId().isBlank())
                ? PlanoId.de(tutor.planoId())
                : PlanosPadrao.ID_PLANO_BASICO_MENSAL;

        provisionarBeneficios.executar(TutorId.de(identificador), planoId);
        return resumo;
    }

    private ResumoUsuario mudarStatusTutor(String identificador, StatusConta novo) {
        UsuarioAutenticavel u = repositorio.buscar(Perfil.TUTOR, identificador)
                .orElseThrow(UsuarioNaoEncontradoException::new);
        u.mudarStatus(novo);
        repositorio.salvar(u);
        return ResumoUsuario.de(u);
    }

    public record RequisicaoCriarFuncionario(
            @NotNull Perfil perfil,
            @NotBlank @Size(min = 3, max = 120) String nome,
            @NotBlank @Size(min = 6, max = 64) String senhaInicial
    ) {}

    public record ResumoUsuario(
            String identificador,
            String nome,
            Perfil perfil,
            StatusConta status,
            String email,
            String telefone
    ) {
        static ResumoUsuario de(UsuarioAutenticavel u) {
            return new ResumoUsuario(
                    u.identificador(), u.nome(), u.perfil(), u.status(),
                    u.email(), u.telefone());
        }
    }

    public static class UsuarioNaoEncontradoException extends RuntimeException {
        public UsuarioNaoEncontradoException() { super("Usuário não encontrado."); }
    }

    public static class PerfilInvalidoException extends RuntimeException {
        public PerfilInvalidoException(Perfil p) {
            super("Perfil inválido para criação por admin: " + p);
        }
    }

    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> naoEncontrado(UsuarioNaoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "status", "USUARIO_NAO_ENCONTRADO",
                "mensagem", e.getMessage()
        ));
    }

    @ExceptionHandler(PerfilInvalidoException.class)
    public ResponseEntity<Map<String, String>> perfilInvalido(PerfilInvalidoException e) {
        return ResponseEntity.badRequest().body(Map.of(
                "status", "PERFIL_INVALIDO",
                "mensagem", e.getMessage()
        ));
    }
}
