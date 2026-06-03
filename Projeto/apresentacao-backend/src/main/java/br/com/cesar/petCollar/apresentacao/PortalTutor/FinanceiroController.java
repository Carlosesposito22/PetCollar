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

import br.com.cesar.petCollar.apresentacao.IdentidadeAcesso.Perfil;
import br.com.cesar.petCollar.apresentacao.IdentidadeAcesso.StatusConta;
import br.com.cesar.petCollar.apresentacao.IdentidadeAcesso.UsuarioAutenticavel;
import br.com.cesar.petCollar.apresentacao.IdentidadeAcesso.UsuarioRepositorio;

/**
 * Área Financeira do Tutor (F-07).
 * Centraliza plano contratado, status da conta, mensalidades e ação de pagamento.
 * Status da Conta é recomputado a cada leitura:
 *   - 0 mensalidades em atraso → ATIVA
 *   - 1 ou 2 em atraso          → INADIMPLENTE
 *   - 3+ em atraso              → SUSPENSA (bloqueia o login)
 */
@RestController
@RequestMapping("/api/tutor/financeiro")
public class FinanceiroController {

    private final PortalTutorRepositorio repositorio;
    private final UsuarioRepositorio usuarioRepositorio;

    public FinanceiroController(PortalTutorRepositorio repositorio,
                                UsuarioRepositorio usuarioRepositorio) {
        this.repositorio = repositorio;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @GetMapping
    public ResumoFinanceiroDTO resumo(Principal principal) {
        String tutorId = principal.getName();
        List<Mensalidade> mensalidades = repositorio.listarMensalidadesDoTutor(tutorId);
        Plano plano = repositorio.planoDoTutor(tutorId);

        StatusConta statusAtualizado = recomputarStatusConta(tutorId, mensalidades);
        // "Próximo vencimento":
        //  - se existe uma PENDENTE, é o vencimento dela (calendário real);
        //  - caso contrário (tutor recém-contratado, sem fatura aberta), projeta a partir
        //    da última paga (+ 1 mês). Em atraso não conta como "próximo" pois já venceu.
        LocalDate proximoVencimento = mensalidades.stream()
                .filter(m -> m.status() == StatusMensalidade.PENDENTE)
                .map(Mensalidade::vencimento)
                .sorted()
                .findFirst()
                .orElseGet(() -> mensalidades.stream()
                        .filter(m -> m.status() == StatusMensalidade.PAGO)
                        .map(Mensalidade::vencimento)
                        .max(java.util.Comparator.naturalOrder())
                        .map(d -> d.plusMonths(1))
                        .orElse(null));

        List<MensalidadeDTO> dto = mensalidades.stream().map(MensalidadeDTO::de).toList();
        return new ResumoFinanceiroDTO(
                new PlanoDTO(plano.nome(), plano.valor()),
                statusAtualizado,
                proximoVencimento,
                dto
        );
    }

    @GetMapping("/mensalidades/{id}")
    public DetalhePagamentoDTO detalhePagamento(@PathVariable String id, Principal principal) {
        Mensalidade m = obterDoTutor(id, principal);
        if (m.status() == StatusMensalidade.PAGO) {
            throw new MensalidadeJaPagaException();
        }
        return new DetalhePagamentoDTO(
                m.id(),
                m.competencia(),
                m.vencimento(),
                m.valorOriginal(),
                m.descontoIndicacao(),
                m.juros(),
                m.diasAtraso(),
                Mensalidade.TAXA_JUROS_DIARIA,
                m.valorAtualizado(),
                m.status(),
                gerarCodigoPix(m.id(), m.valorAtualizado())
        );
    }

    @PostMapping("/mensalidades/{id}/pagar")
    public ResumoFinanceiroDTO pagar(@PathVariable String id, Principal principal) {
        Mensalidade m = obterDoTutor(id, principal);
        if (m.status() == StatusMensalidade.PAGO) {
            throw new MensalidadeJaPagaException();
        }
        m.marcarPaga();
        repositorio.salvarMensalidade(m);
        return resumo(principal);
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private Mensalidade obterDoTutor(String id, Principal principal) {
        Mensalidade m = repositorio.buscarMensalidade(id)
                .orElseThrow(MensalidadeNaoEncontradaException::new);
        if (!m.tutorId().equalsIgnoreCase(principal.getName())) {
            throw new MensalidadeNaoEncontradaException();
        }
        return m;
    }

    private StatusConta recomputarStatusConta(String tutorId, List<Mensalidade> mensalidades) {
        long emAtraso = mensalidades.stream()
                .filter(m -> m.status() == StatusMensalidade.EM_ATRASO)
                .count();

        StatusConta calculado;
        if (emAtraso >= 3) calculado = StatusConta.SUSPENSA;
        else if (emAtraso >= 1) calculado = StatusConta.INADIMPLENTE;
        else calculado = StatusConta.ATIVA;

        usuarioRepositorio.buscar(Perfil.TUTOR, tutorId).ifPresent(tutor -> {
            // Preservar PENDENTE (contratação ainda não confirmada) — não regredir.
            if (tutor.status() == StatusConta.PENDENTE) return;
            if (tutor.status() != calculado) {
                tutor.mudarStatus(calculado);
                usuarioRepositorio.salvar(tutor);
            }
        });

        return calculado;
    }

    private static String gerarCodigoPix(String mensalidadeId, BigDecimal valor) {
        // Mock: em produção seria o payload BR Code retornado pelo PSP.
        return "00020126580014BR.GOV.BCB.PIX0136"
                + Integer.toHexString(mensalidadeId.hashCode())
                + "5204000053039865802BR5913petCollar SA6009Sao Paulo62070503***6304"
                + Integer.toHexString(valor.unscaledValue().intValue()).toUpperCase();
    }

    // ── DTOs ────────────────────────────────────────────────────────────────

    public record PlanoDTO(String nome, BigDecimal valor) {}

    public record MensalidadeDTO(
            String id,
            YearMonth competencia,
            BigDecimal valorOriginal,
            BigDecimal descontoIndicacao,
            BigDecimal juros,
            BigDecimal valorAtualizado,
            LocalDate vencimento,
            LocalDate dataPagamento,
            int diasAtraso,
            StatusMensalidade status
    ) {
        static MensalidadeDTO de(Mensalidade m) {
            return new MensalidadeDTO(
                    m.id(), m.competencia(),
                    m.valorOriginal(), m.descontoIndicacao(), m.juros(),
                    m.valorAtualizado(), m.vencimento(), m.dataPagamento(),
                    m.diasAtraso(), m.status());
        }
    }

    public record ResumoFinanceiroDTO(
            PlanoDTO plano,
            StatusConta statusConta,
            LocalDate proximoVencimento,
            List<MensalidadeDTO> mensalidades
    ) {}

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
            StatusMensalidade status,
            String codigoPix
    ) {}

    // ── Exceções ────────────────────────────────────────────────────────────

    public static class MensalidadeNaoEncontradaException extends RuntimeException {
        public MensalidadeNaoEncontradaException() { super("Mensalidade não encontrada."); }
    }

    public static class MensalidadeJaPagaException extends RuntimeException {
        public MensalidadeJaPagaException() { super("Esta mensalidade já está paga."); }
    }

    @ExceptionHandler(MensalidadeNaoEncontradaException.class)
    public ResponseEntity<Map<String, String>> naoEncontrada(MensalidadeNaoEncontradaException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "status", "MENSALIDADE_NAO_ENCONTRADA",
                "mensagem", e.getMessage()));
    }

    @ExceptionHandler(MensalidadeJaPagaException.class)
    public ResponseEntity<Map<String, String>> jaPaga(MensalidadeJaPagaException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "status", "MENSALIDADE_JA_PAGA",
                "mensagem", e.getMessage()));
    }
}
