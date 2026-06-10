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

import br.com.cesar.petCollar.aplicacao.AssinaturaFaturamento.ContratarPlanoUseCase;
import br.com.cesar.petCollar.aplicacao.AssinaturaFaturamento.PlanosPadrao;
import br.com.cesar.petCollar.aplicacao.BeneficiosPlano.ProvisionarBeneficiosDoTutorUseCase;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.CPF;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.Indicacao;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.LinkIndicacao;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.ProgramaIndicacaoService;
import br.com.cesar.petCollar.dominio.compartilhado.PlanoId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/tutores")
public class TutorController {

    private static final Logger log = LoggerFactory.getLogger(TutorController.class);

    private final UsuarioRepositorio repositorio;
    private final PasswordEncoder encoder;
    private final ContratarPlanoUseCase contratarPlano;
    private final ProvisionarBeneficiosDoTutorUseCase provisionarBeneficios;
    private final ProgramaIndicacaoService programaIndicacaoService;

    public TutorController(UsuarioRepositorio repositorio,
                           PasswordEncoder encoder,
                           ContratarPlanoUseCase contratarPlano,
                           ProvisionarBeneficiosDoTutorUseCase provisionarBeneficios,
                           ProgramaIndicacaoService programaIndicacaoService) {
        this.repositorio = repositorio;
        this.encoder = encoder;
        this.contratarPlano = contratarPlano;
        this.provisionarBeneficios = provisionarBeneficios;
        this.programaIndicacaoService = programaIndicacaoService;
    }

    @PostMapping("/contratar")
    public ResponseEntity<RespostaContratacao> contratar(@Valid @RequestBody RequisicaoContratacao req) {
        if (repositorio.buscarPorEmail(req.email()).isPresent()) {
            throw new EmailJaCadastradoException();
        }

        String planoIdEscolhido = (req.planoId() != null && !req.planoId().isBlank())
                ? req.planoId()
                : PlanosPadrao.ID_PLANO_BASICO_MENSAL.getValor();

        UsuarioAutenticavel novo = new UsuarioAutenticavel(
                req.email(),
                req.nomeCompleto(),
                Perfil.TUTOR,
                encoder.encode(req.senha()),
                StatusConta.PENDENTE,
                req.cpf(),
                req.telefone(),
                req.endereco(),
                req.email(),
                planoIdEscolhido
        );
        repositorio.salvar(novo);

        if (req.codigoIndicacao() != null && !req.codigoIndicacao().isBlank()) {
            try {
                processarIndicacao(req.codigoIndicacao(), req.cpf());
            } catch (Exception ex) {
                log.warn("Falha ao processar indicação '{}': {}", req.codigoIndicacao(), ex.getMessage());
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(new RespostaContratacao(
                novo.identificador(),
                novo.nome(),
                novo.email(),
                novo.status().name(),
                gerarCodigoPix(novo.identificador())
        ));
    }

    private void processarIndicacao(String codigo, String cpfIndicado) {
        programaIndicacaoService.registrarClique(codigo, CPF.de(cpfIndicado), LocalDateTime.now());
        programaIndicacaoService.buscarLinkPorCodigo(codigo).ifPresent(link ->
            repositorio.buscar(Perfil.TUTOR, link.getTutorId().getValor()).ifPresent(indicador ->
                programaIndicacaoService.criarIndicacaoParaInscrito(
                    CPF.de(cpfIndicado), CPF.de(indicador.cpf())
                )
            )
        );
    }

    @PostMapping("/{identificador}/simular-pagamento")
    public ResponseEntity<RespostaContratacao> simularPagamento(@PathVariable String identificador) {
        UsuarioAutenticavel tutor = repositorio.buscar(Perfil.TUTOR, identificador)
                .orElseThrow(TutorNaoEncontradoException::new);

        if (tutor.status() != StatusConta.PENDENTE) {
            return ResponseEntity.ok(new RespostaContratacao(
                    tutor.identificador(), tutor.nome(), tutor.email(),
                    tutor.status().name(), null));
        }

        tutor.mudarStatus(StatusConta.ATIVA);
        repositorio.salvar(tutor);

        PlanoId planoId =
                (tutor.planoId() != null && !tutor.planoId().isBlank())
                        ? PlanoId.de(tutor.planoId())
                        : PlanosPadrao.ID_PLANO_BASICO_MENSAL;
        TutorId tutorId = TutorId.de(tutor.identificador());

        // Verifica se este tutor foi indicado para aplicar 30% na primeira fatura (RN-3)
        Optional<Indicacao> indicacaoPendente = Optional.empty();
        if (tutor.cpf() != null && !tutor.cpf().isBlank()) {
            try {
                indicacaoPendente = programaIndicacaoService
                    .buscarIndicacaoPendenteParaCpfIndicado(CPF.de(tutor.cpf()));
            } catch (Exception ex) {
                log.warn("Não foi possível verificar indicação pendente para o tutor {}: {}",
                         identificador, ex.getMessage());
            }
        }

        BigDecimal descontoIndicacao = indicacaoPendente.isPresent() ? new BigDecimal("0.30") : null;
        contratarPlano.executar(tutorId, planoId, descontoIndicacao);
        provisionarBeneficios.executar(tutorId, planoId);

        // Confirma a conversão: aplica 15% de desconto na próxima fatura do indicador (RN-5)
        indicacaoPendente.ifPresent(ind -> {
            try {
                programaIndicacaoService.confirmarConversaoManual(ind.getId());
            } catch (Exception ex) {
                log.warn("Falha ao confirmar conversão da indicação {}: {}",
                         ind.getId().getValor(), ex.getMessage());
            }
        });

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
            @NotBlank @Size(min = 6, max = 64) String senha,
            String planoId,
            String codigoIndicacao
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
