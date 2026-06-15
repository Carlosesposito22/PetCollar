package br.com.cesar.petCollar.apresentacao.RelacaoTutor;

import br.com.cesar.petCollar.aplicacao.RelacaoTutor.ConfirmarConversaoIndicacaoUseCase;
import br.com.cesar.petCollar.aplicacao.RelacaoTutor.ObterOuGerarLinkIndicacaoUseCase;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.CPF;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.Indicacao;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.IndicacaoId;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.LinkIndicacao;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.ProgramaIndicacaoService;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.StatusIndicacao;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import br.com.cesar.petCollar.apresentacao.IdentidadeAcesso.StatusConta;
import br.com.cesar.petCollar.apresentacao.IdentidadeAcesso.UsuarioRepositorio;
import br.com.cesar.petCollar.apresentacao.IdentidadeAcesso.Perfil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Adapter HTTP fino para o Programa de Indicação com Recompensas (F-04).
 * Toda a lógica de negócio vive em {@link ProgramaIndicacaoService} e no domínio.
 */
@RestController
@RequestMapping("/api/tutor/indicacao")
public class IndicacaoController {

    private final String baseUrl;
    private final ObterOuGerarLinkIndicacaoUseCase obterOuGerarLinkUseCase;
    private final ConfirmarConversaoIndicacaoUseCase confirmarConversaoUseCase;
    private final ProgramaIndicacaoService programaIndicacao;
    private final UsuarioRepositorio usuarioRepositorio;

    public IndicacaoController(
            @Value("${petcollar.indicacao.base-url:http://localhost:3000}") String baseUrl,
            ObterOuGerarLinkIndicacaoUseCase obterOuGerarLinkUseCase,
            ConfirmarConversaoIndicacaoUseCase confirmarConversaoUseCase,
            ProgramaIndicacaoService programaIndicacao,
            UsuarioRepositorio usuarioRepositorio) {
        this.baseUrl = baseUrl;
        this.obterOuGerarLinkUseCase = obterOuGerarLinkUseCase;
        this.confirmarConversaoUseCase = confirmarConversaoUseCase;
        this.programaIndicacao = programaIndicacao;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    // ── GET /api/tutor/indicacao/link ─────────────────────────────────────────

    @GetMapping("/link")
    public LinkIndicacaoDTO obterLink(Principal principal) {
        TutorId tutorId = TutorId.de(principal.getName());
        boolean ativa = contaAtiva(tutorId);
        LinkIndicacao link = obterOuGerarLinkUseCase.executar(tutorId, ativa);
        return LinkIndicacaoDTO.de(link, baseUrl);
    }

    // ── POST /api/tutor/indicacao/clique/{codigo} — público (sem auth) ────────

    @PostMapping("/clique/{codigo}")
    public ResponseEntity<Void> registrarClique(@PathVariable String codigo,
                                                @RequestBody RequisicaoCliqueDTO req) {
        programaIndicacao.registrarClique(codigo, CPF.de(req.cpfIndicado()),
                                          LocalDateTime.now());
        return ResponseEntity.ok().build();
    }

    // ── POST /api/tutor/indicacao/inscricao — chamado no fluxo de contratação ─

    @PostMapping("/inscricao")
    public IndicacaoDTO registrarInscricao(@RequestBody RequisicaoInscricaoDTO req) {
        Indicacao indicacao = programaIndicacao.criarIndicacaoParaInscrito(
            CPF.de(req.cpfIndicado()), CPF.de(req.cpfIndicador()));
        return IndicacaoDTO.de(indicacao);
    }

    // ── GET /api/tutor/indicacao/historico ────────────────────────────────────

    @GetMapping("/historico")
    public List<IndicacaoDTO> historico(Principal principal) {
        TutorId tutorId = TutorId.de(principal.getName());
        return programaIndicacao.consultarHistorico(tutorId).stream()
                                .map(IndicacaoDTO::de)
                                .toList();
    }

    // ── POST /api/tutor/indicacao/webhook/pagamento — chamado pelo gateway ────

    @PostMapping("/webhook/pagamento")
    public ResponseEntity<Void> webhookPagamento(@RequestBody RequisicaoWebhookDTO req) {
        confirmarConversaoUseCase.executar(
            IndicacaoId.de(req.indicacaoId()), req.tokenMetodoPagamento());
        return ResponseEntity.ok().build();
    }

    // ── POST /api/tutor/indicacao/{id}/resgatar-desconto — tutor resgata 15% ──

    @PostMapping("/{indicacaoId}/resgatar-desconto")
    public ResponseEntity<Map<String, Object>> resgatarDesconto(@PathVariable String indicacaoId,
                                                                Principal principal) {
        TutorId tutorId = TutorId.de(principal.getName());
        java.util.Optional<String> cobId = programaIndicacao.resgatarDescontoIndicador(
            IndicacaoId.de(indicacaoId), tutorId);

        if (cobId.isPresent()) {
            return ResponseEntity.ok(Map.of(
                "cobrancaId", cobId.get(),
                "mensagem", "Desconto de 15% aplicado com sucesso."
            ));
        }
        return ResponseEntity.ok(Map.of(
            "cobrancaId", "",
            "mensagem", "Nenhuma fatura em aberto no momento. Tente novamente quando houver uma fatura pendente."
        ));
    }

    // ── POST /api/tutor/indicacao/confirmacao-manual/{id} ────────────────────

    @PostMapping("/confirmacao-manual/{indicacaoId}")
    public ResponseEntity<Void> confirmacaoManual(@PathVariable String indicacaoId,
                                                  Principal principal) {
        programaIndicacao.confirmarConversaoManual(IndicacaoId.de(indicacaoId));
        return ResponseEntity.ok().build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean contaAtiva(TutorId tutorId) {
        return usuarioRepositorio.buscar(Perfil.TUTOR, tutorId.getValor())
            .map(u -> u.status() == StatusConta.ATIVA)
            .orElse(false);
    }

    // ── DTOs ──────────────────────────────────────────────────────────────────

    public record LinkIndicacaoDTO(String id, String codigo, String url) {
        static LinkIndicacaoDTO de(LinkIndicacao link, String baseUrl) {
            return new LinkIndicacaoDTO(
                link.getId().getValor(),
                link.getCodigo().getValor(),
                link.getUrl(baseUrl)
            );
        }
    }

    public record IndicacaoDTO(
            String id,
            String tutorIndicadorId,
            String cpfIndicado,
            StatusIndicacao status,
            String cobrancaIndicadorId,
            String motivoInvalidacao,
            LocalDateTime dataClique,
            LocalDateTime convertidaEm
    ) {
        static IndicacaoDTO de(Indicacao ind) {
            return new IndicacaoDTO(
                ind.getId().getValor(),
                ind.getTutorIndicadorId().getValor(),
                ind.getCpfIndicado().getValor(),
                ind.getStatus(),
                ind.getCobrancaIndicadorId(),
                ind.getMotivoInvalidacao(),
                ind.getTimestampClique(),
                ind.getConvertidaEm()
            );
        }
    }

    public record RequisicaoCliqueDTO(String cpfIndicado) {}
    public record RequisicaoInscricaoDTO(String cpfIndicado, String cpfIndicador) {}
    public record RequisicaoWebhookDTO(String indicacaoId, String tokenMetodoPagamento) {}

}
