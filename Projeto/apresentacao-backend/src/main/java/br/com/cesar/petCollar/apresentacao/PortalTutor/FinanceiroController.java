package br.com.cesar.petCollar.apresentacao.PortalTutor;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.cesar.petCollar.aplicacao.AssinaturaFaturamento.ConfirmarPagamentoCobrancaUseCase;
import br.com.cesar.petCollar.aplicacao.AssinaturaFaturamento.ConsultarResumoFinanceiroUseCase;
import br.com.cesar.petCollar.apresentacao.IdentidadeAcesso.Perfil;
import br.com.cesar.petCollar.apresentacao.IdentidadeAcesso.StatusConta;
import br.com.cesar.petCollar.apresentacao.IdentidadeAcesso.UsuarioRepositorio;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.Cobranca;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.CobrancaId;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.StatusCobranca;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.plano.Plano;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.servico.SituacaoConta;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

/**
 * Área Financeira do Tutor (F-07). Adapter HTTP fino — toda a regra vive nos
 * use cases de {@code aplicacao.AssinaturaFaturamento} e nos services/agregados
 * de domínio. Aqui só fazemos:
 *  1) extrair o tutor logado do {@link Principal};
 *  2) chamar o use case;
 *  3) converter o resultado em DTOs (records);
 *  4) propagar a SituacaoConta para o {@code UsuarioAutenticavel} (controla login).
 */
@RestController
@RequestMapping("/api/tutor/financeiro")
public class FinanceiroController {

    private final ConsultarResumoFinanceiroUseCase consultarResumo;
    private final ConfirmarPagamentoCobrancaUseCase confirmarPagamento;
    private final UsuarioRepositorio usuarioRepositorio;

    public FinanceiroController(ConsultarResumoFinanceiroUseCase consultarResumo,
                                ConfirmarPagamentoCobrancaUseCase confirmarPagamento,
                                UsuarioRepositorio usuarioRepositorio) {
        this.consultarResumo = consultarResumo;
        this.confirmarPagamento = confirmarPagamento;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @GetMapping
    public ResumoFinanceiroDTO resumo(Principal principal) {
        TutorId tutorId = TutorId.de(principal.getName());
        ConsultarResumoFinanceiroUseCase.Resultado r = consultarResumo.executar(tutorId);
        sincronizarStatusContaDoTutor(tutorId, r.situacaoConta());
        return ResumoFinanceiroDTO.de(r);
    }

    @GetMapping("/cobrancas/{id}")
    public DetalhePagamentoDTO detalhePagamento(@PathVariable String id, Principal principal) {
        TutorId tutorId = TutorId.de(principal.getName());
        Cobranca cobranca = consultarResumo.executar(tutorId).cobrancas().stream()
                .filter(c -> c.getId().getValor().equals(id))
                .findFirst()
                .orElseThrow(CobrancaNaoEncontradaException::new);

        if (cobranca.status() == StatusCobranca.PAGA) {
            throw new CobrancaJaPagaException();
        }
        return DetalhePagamentoDTO.de(cobranca);
    }

    @PostMapping("/cobrancas/{id}/pagar")
    public ResumoFinanceiroDTO pagar(@PathVariable String id, Principal principal) {
        TutorId tutorId = TutorId.de(principal.getName());
        try {
            confirmarPagamento.executar(tutorId, CobrancaId.de(id));
        } catch (IllegalArgumentException e) {
            throw new CobrancaNaoEncontradaException();
        } catch (IllegalStateException e) {
            throw new CobrancaJaPagaException();
        }
        return resumo(principal);
    }

    // ── Sincronização de status de login (PortalTutor ↔ IdentidadeAcesso) ────

    /**
     * Reflete a {@link SituacaoConta} calculada por F-07 no {@code StatusConta}
     * do usuário autenticável — é isso que controla o bloqueio de login (RN 7).
     * PENDENTE da contratação inicial é preservado: não rebaixa.
     */
    private void sincronizarStatusContaDoTutor(TutorId tutorId, SituacaoConta situacao) {
        usuarioRepositorio.buscar(Perfil.TUTOR, tutorId.getValor()).ifPresent(tutor -> {
            if (tutor.status() == StatusConta.PENDENTE) return;
            StatusConta novo = switch (situacao) {
                case ATIVA        -> StatusConta.ATIVA;
                case INADIMPLENTE -> StatusConta.INADIMPLENTE;
                case SUSPENSA     -> StatusConta.SUSPENSA;
                case PENDENTE     -> tutor.status(); // sem mudança
            };
            if (tutor.status() != novo) {
                tutor.mudarStatus(novo);
                usuarioRepositorio.salvar(tutor);
            }
        });
    }

    // ── DTOs ─────────────────────────────────────────────────────────────────

    public record PlanoDTO(String id, String nome, BigDecimal valor) {
        static PlanoDTO de(Plano p) {
            return new PlanoDTO(p.getId().getValor(), p.getNome(), p.getMensalidade().getValor());
        }
    }

    public record CobrancaDTO(
            String id,
            YearMonth competencia,
            BigDecimal valorOriginal,
            BigDecimal descontoIndicacao,
            BigDecimal juros,
            BigDecimal valorAtualizado,
            LocalDate vencimento,
            LocalDate dataPagamento,
            int diasAtraso,
            StatusCobranca status
    ) {
        static CobrancaDTO de(Cobranca c) {
            return new CobrancaDTO(
                    c.getId().getValor(),
                    c.getCompetencia().getValor(),
                    c.getValorOriginal(),
                    c.getDescontoIndicacao(),
                    c.juros(),
                    c.valorAtualizado(),
                    c.getVencimento(),
                    c.getDataPagamento(),
                    c.diasAtraso(),
                    c.status()
            );
        }
    }

    public record ResumoFinanceiroDTO(
            PlanoDTO plano,
            SituacaoConta statusConta,
            LocalDate proximoVencimento,
            List<CobrancaDTO> cobrancas
    ) {
        static ResumoFinanceiroDTO de(ConsultarResumoFinanceiroUseCase.Resultado r) {
            return new ResumoFinanceiroDTO(
                    r.plano() == null ? null : PlanoDTO.de(r.plano()),
                    r.situacaoConta(),
                    r.proximoVencimento(),
                    r.cobrancas().stream().map(CobrancaDTO::de).toList()
            );
        }
    }

    public record DetalhePagamentoDTO(
            String id,
            YearMonth competencia,
            LocalDate vencimento,
            BigDecimal valorOriginal,
            BigDecimal descontoIndicacao,
            BigDecimal juros,
            int diasAtraso,
            BigDecimal taxaDiaria,
            BigDecimal valorAtualizado,
            StatusCobranca status,
            String codigoPix
    ) {
        static DetalhePagamentoDTO de(Cobranca c) {
            return new DetalhePagamentoDTO(
                    c.getId().getValor(),
                    c.getCompetencia().getValor(),
                    c.getVencimento(),
                    c.getValorOriginal(),
                    c.getDescontoIndicacao(),
                    c.juros(),
                    c.diasAtraso(),
                    new BigDecimal("0.00033"),
                    c.valorAtualizado(),
                    c.status(),
                    gerarCodigoPix(c.getId().getValor(), c.valorAtualizado())
            );
        }
    }

    private static String gerarCodigoPix(String cobrancaId, BigDecimal valor) {
        return "00020126580014BR.GOV.BCB.PIX0136"
                + Integer.toHexString(cobrancaId.hashCode())
                + "5204000053039865802BR5913petCollar SA6009Sao Paulo62070503***6304"
                + Integer.toHexString(valor.unscaledValue().intValue()).toUpperCase();
    }

    // ── Exceções ────────────────────────────────────────────────────────────

    public static class CobrancaNaoEncontradaException extends RuntimeException {
        public CobrancaNaoEncontradaException() { super("Cobrança não encontrada."); }
    }

    public static class CobrancaJaPagaException extends RuntimeException {
        public CobrancaJaPagaException() { super("Esta cobrança já está paga."); }
    }

    @ExceptionHandler(CobrancaNaoEncontradaException.class)
    public ResponseEntity<Map<String, String>> naoEncontrada(CobrancaNaoEncontradaException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "status", "COBRANCA_NAO_ENCONTRADA",
                "mensagem", e.getMessage()));
    }

    @ExceptionHandler(CobrancaJaPagaException.class)
    public ResponseEntity<Map<String, String>> jaPaga(CobrancaJaPagaException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "status", "COBRANCA_JA_PAGA",
                "mensagem", e.getMessage()));
    }
}
