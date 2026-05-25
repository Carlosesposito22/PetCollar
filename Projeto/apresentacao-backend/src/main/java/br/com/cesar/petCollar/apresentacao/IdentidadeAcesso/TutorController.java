package br.com.cesar.petCollar.apresentacao.IdentidadeAcesso;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/tutores")
public class TutorController {

    private final UsuarioRepositorio repositorio;
    private final PasswordEncoder encoder;

    public TutorController(UsuarioRepositorio repositorio, PasswordEncoder encoder) {
        this.repositorio = repositorio;
        this.encoder = encoder;
    }

    @PostMapping("/contratar")
    public ResponseEntity<RespostaContratacao> contratar(@Valid @RequestBody RequisicaoContratacao req) {
        if (repositorio.buscarPorEmail(req.email()).isPresent()) {
            throw new EmailJaCadastradoException();
        }

        UsuarioAutenticavel novo = new UsuarioAutenticavel(
                req.email(),
                req.nomeCompleto(),
                Perfil.TUTOR,
                encoder.encode(req.senha()),
                StatusConta.PENDENTE_PAGAMENTO,
                req.cpf(),
                req.telefone(),
                req.endereco(),
                req.email()
        );
        repositorio.salvar(novo);

        return ResponseEntity.status(HttpStatus.CREATED).body(new RespostaContratacao(
                novo.identificador(),
                novo.nome(),
                novo.email(),
                novo.status().name(),
                gerarCodigoPix(novo.identificador())
        ));
    }

    @PostMapping("/{identificador}/simular-pagamento")
    public ResponseEntity<RespostaContratacao> simularPagamento(@PathVariable String identificador) {
        UsuarioAutenticavel tutor = repositorio.buscar(Perfil.TUTOR, identificador)
                .orElseThrow(TutorNaoEncontradoException::new);

        if (tutor.status() != StatusConta.PENDENTE_PAGAMENTO) {
            return ResponseEntity.ok(new RespostaContratacao(
                    tutor.identificador(), tutor.nome(), tutor.email(),
                    tutor.status().name(), null));
        }

        tutor.mudarStatus(StatusConta.ATIVA);
        repositorio.salvar(tutor);

        return ResponseEntity.ok(new RespostaContratacao(
                tutor.identificador(), tutor.nome(), tutor.email(),
                tutor.status().name(), null));
    }

    private static String gerarCodigoPix(String identificador) {
        // Mock: em produção seria o payload BR Code retornado pelo PSP.
        return "00020126580014BR.GOV.BCB.PIX0136" +
                Integer.toHexString(identificador.hashCode()) +
                "5204000053039865802BR5913petCollar SA6009Sao Paulo62070503***6304ABCD";
    }

    public record RequisicaoContratacao(
            @NotBlank @Size(min = 11, max = 18) String cpf,
            @NotBlank @Size(min = 3, max = 120) String nomeCompleto,
            @NotBlank String telefone,
            @NotBlank @Email String email,
            @NotBlank String endereco,
            @NotBlank @Size(min = 6, max = 64) String senha
    ) {}

    public record RespostaContratacao(
            String identificador,
            String nome,
            String email,
            String status,
            String codigoPix
    ) {}

    public static class EmailJaCadastradoException extends RuntimeException {
        public EmailJaCadastradoException() { super("Já existe uma conta com esse e-mail."); }
    }

    public static class TutorNaoEncontradoException extends RuntimeException {
        public TutorNaoEncontradoException() { super("Tutor não encontrado."); }
    }

    @ExceptionHandler(EmailJaCadastradoException.class)
    public ResponseEntity<Map<String, String>> tratarEmailDuplicado(EmailJaCadastradoException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "status", "EMAIL_DUPLICADO",
                "mensagem", e.getMessage()
        ));
    }

    @ExceptionHandler(TutorNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> tratarTutorNaoEncontrado(TutorNaoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "status", "TUTOR_NAO_ENCONTRADO",
                "mensagem", e.getMessage()
        ));
    }
}
