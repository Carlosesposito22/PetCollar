package br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao;

import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Serviço de domínio que orquestra o Programa de Indicação com Recompensas (F-04).
 *
 * <p>Cobre as 12 regras de negócio:
 * <ul>
 *   <li>RN-1  — Acesso restrito a Tutores com conta ativa</li>
 *   <li>RN-2  — Unicidade e permanência do link de indicação</li>
 *   <li>RN-3  — Desconto de boas-vindas (30%) aplicado na primeira mensalidade</li>
 *   <li>RN-4  — Conversão válida apenas após confirmação de pagamento via webhook</li>
 *   <li>RN-5  — Desconto de 15% na próxima fatura em aberto do indicador</li>
 *   <li>RN-6  — Conquista Lendária concedida via motor de gamificação</li>
 *   <li>RN-7  — Prevenção de fraude por autoindicação</li>
 *   <li>RN-8  — Prevenção de fraude por método de pagamento coincidente</li>
 *   <li>RN-9  — Não cumulatividade com outros cupons (regra mais vantajosa)</li>
 *   <li>RN-10 — Unicidade do indicado por CPF (apenas uma conversão válida)</li>
 *   <li>RN-11 — Atribuição por Último Clique em caso de múltiplos links</li>
 *   <li>RN-12 — Persistência auditável de todas as etapas</li>
 * </ul>
 */
public class ProgramaIndicacaoService {

    static final BigDecimal PERCENTUAL_DESCONTO_INDICADO = new BigDecimal("0.30");

    private final ILinkIndicacaoRepositorio linkRepositorio;
    private final IRegistroCliqueRepositorio registroCliqueRepositorio;
    private final IIndicacaoRepositorio indicacaoRepositorio;
    private final IEventoAuditoriaRepositorio auditoriaRepositorio;
    private final IMotorGamificacaoPort motorGamificacao;
    private final IDescontoFaturaPort descontoFatura;

    public ProgramaIndicacaoService(ILinkIndicacaoRepositorio linkRepositorio,
                                    IRegistroCliqueRepositorio registroCliqueRepositorio,
                                    IIndicacaoRepositorio indicacaoRepositorio,
                                    IEventoAuditoriaRepositorio auditoriaRepositorio,
                                    IMotorGamificacaoPort motorGamificacao,
                                    IDescontoFaturaPort descontoFatura) {
        if (linkRepositorio == null)           throw new IllegalArgumentException("Repositório de links não pode ser nulo.");
        if (registroCliqueRepositorio == null) throw new IllegalArgumentException("Repositório de cliques não pode ser nulo.");
        if (indicacaoRepositorio == null)      throw new IllegalArgumentException("Repositório de indicações não pode ser nulo.");
        if (auditoriaRepositorio == null)      throw new IllegalArgumentException("Repositório de auditoria não pode ser nulo.");
        if (motorGamificacao == null)          throw new IllegalArgumentException("Motor de gamificação não pode ser nulo.");
        if (descontoFatura == null)            throw new IllegalArgumentException("Port de desconto de fatura não pode ser nulo.");
        this.linkRepositorio = linkRepositorio;
        this.registroCliqueRepositorio = registroCliqueRepositorio;
        this.indicacaoRepositorio = indicacaoRepositorio;
        this.auditoriaRepositorio = auditoriaRepositorio;
        this.motorGamificacao = motorGamificacao;
        this.descontoFatura = descontoFatura;
    }

    // ── RN-1 / RN-2 ─────────────────────────────────────────────────────────

    /**
     * Retorna o link de indicação existente do Tutor ou gera um novo link permanente e único.
     *
     * @param tutorId    identificador do Tutor autenticado
     * @param contaAtiva status atual da conta — deve ser verdadeiro para acesso (RN-1)
     * @return link de indicação (existente ou recém-criado)
     */
    public LinkIndicacao obterOuGerarLink(TutorId tutorId, boolean contaAtiva) {
        if (tutorId == null)
            throw new IllegalArgumentException("TutorId não pode ser nulo.");
        // RN-1
        if (!contaAtiva)
            throw new IllegalStateException(
                "Apenas Tutores com conta ativa podem acessar o painel de indicação.");

        // RN-2: retorna o link já existente sem criar um novo
        Optional<LinkIndicacao> existente = linkRepositorio.buscarPorTutorId(tutorId);
        if (existente.isPresent()) return existente.get();

        // RN-2: gera código único com garantia de unicidade global
        CodigoIndicacao codigo;
        do {
            codigo = CodigoIndicacao.gerar();
        } while (linkRepositorio.existePorCodigo(codigo));

        LinkIndicacao link = new LinkIndicacao(LinkIndicacaoId.gerar(), tutorId, codigo);
        linkRepositorio.salvar(link);

        // RN-12
        auditoriaRepositorio.salvar(new EventoAuditoria(
            EventoAuditoriaId.gerar(),
            TipoEventoAuditoria.LINK_GERADO,
            tutorId, null,
            "Link de indicação gerado para o Tutor " + tutorId.getValor()
                + " com código " + codigo.getValor()
        ));
        return link;
    }

    // ── RN-11 / RN-12 ───────────────────────────────────────────────────────

    /**
     * Registra o clique de um indicado num link de indicação.
     * Sobrescreve cliques anteriores do mesmo CPF (Último Clique — RN-11).
     *
     * @param codigoLink       código alfanumérico do link acessado
     * @param cpfIndicado      CPF da pessoa que clicou no link
     * @param timestampClique  momento exato do acesso
     */
    public void registrarClique(String codigoLink, CPF cpfIndicado,
                                LocalDateTime timestampClique) {
        if (codigoLink == null || codigoLink.isBlank())
            throw new IllegalArgumentException("Código do link não pode ser vazio.");
        if (cpfIndicado == null)
            throw new IllegalArgumentException("CPF do indicado não pode ser nulo.");
        if (timestampClique == null)
            throw new IllegalArgumentException("Timestamp do clique não pode ser nulo.");

        LinkIndicacao link = linkRepositorio.buscarPorCodigo(CodigoIndicacao.de(codigoLink))
            .orElseThrow(() -> new IllegalArgumentException(
                "Link de indicação não encontrado para o código: " + codigoLink));

        // RN-11: substitui o clique anterior do mesmo CPF pelo mais recente
        RegistroClique novoClique = new RegistroClique(
            RegistroCliqueId.gerar(),
            cpfIndicado,
            link.getId(),
            link.getTutorId(),
            timestampClique
        );
        registroCliqueRepositorio.salvar(novoClique);

        // RN-12
        auditoriaRepositorio.salvar(new EventoAuditoria(
            EventoAuditoriaId.gerar(),
            TipoEventoAuditoria.CLIQUE_REGISTRADO,
            link.getTutorId(), null,
            "Clique registrado no link " + codigoLink
                + " pelo CPF " + cpfIndicado.getValor()
                + " em " + timestampClique
        ));
    }

    // ── RN-7 / RN-10 / RN-11 / RN-12 ───────────────────────────────────────

    /**
     * Cria a {@link Indicacao} para o indicado no momento da inscrição na plataforma.
     * Aplica a regra de Último Clique (RN-11) para determinar o Tutor indicador,
     * valida autoindicação (RN-7) e unicidade do CPF (RN-10).
     *
     * @param cpfIndicado   CPF do novo usuário que acabou de se inscrever
     * @param cpfIndicador  CPF do Tutor indicador (obtido via link → tutorId)
     * @return indicação criada com status PENDENTE
     */
    public Indicacao criarIndicacaoParaInscrito(CPF cpfIndicado, CPF cpfIndicador) {
        if (cpfIndicado == null)  throw new IllegalArgumentException("CPF do indicado não pode ser nulo.");
        if (cpfIndicador == null) throw new IllegalArgumentException("CPF do indicador não pode ser nulo.");

        // RN-7: bloqueia autoindicação
        if (cpfIndicado.equals(cpfIndicador)) {
            auditoriaRepositorio.salvar(new EventoAuditoria(
                EventoAuditoriaId.gerar(),
                TipoEventoAuditoria.FRAUDE_BLOQUEADA,
                null, null,
                "Tentativa de autoindicação bloqueada para CPF " + cpfIndicado.getValor()
            ));
            throw new IllegalStateException(
                "Autoindicação não é permitida no programa de indicação (RN-7).");
        }

        // RN-10: cada CPF só pode ser indicação válida uma única vez
        if (indicacaoRepositorio.existeConversaoPorCpf(cpfIndicado)) {
            throw new IllegalStateException(
                "Este CPF já foi contabilizado como indicação válida anteriormente (RN-10).");
        }

        // RN-11: Último Clique determina o Tutor indicador
        RegistroClique ultimoClique = registroCliqueRepositorio.buscarUltimoPorCpf(cpfIndicado)
            .orElseThrow(() -> new IllegalStateException(
                "Nenhum link de indicação foi acessado por este usuário antes da inscrição."));

        Indicacao indicacao = new Indicacao(
            IndicacaoId.gerar(),
            ultimoClique.getTutorIndicadorId(),
            ultimoClique.getLinkId(),
            cpfIndicado,
            ultimoClique.getTimestamp()
        );
        indicacaoRepositorio.salvar(indicacao);

        // RN-12
        auditoriaRepositorio.salvar(new EventoAuditoria(
            EventoAuditoriaId.gerar(),
            TipoEventoAuditoria.INDICADO_INSCRITO,
            ultimoClique.getTutorIndicadorId(),
            indicacao.getId(),
            "Indicado CPF " + cpfIndicado.getValor()
                + " inscrito via link do Tutor " + ultimoClique.getTutorIndicadorId().getValor()
        ));
        return indicacao;
    }

    // ── RN-4 / RN-5 / RN-6 / RN-8 / RN-9 / RN-12 ──────────────────────────

    /**
     * Confirma a conversão via webhook do gateway de pagamentos (fluxo automático).
     * Delega ao {@link ProcessamentoWebhookAutomatico}, que executa a verificação
     * de fraude por método de pagamento (RN-8) antes de aplicar os benefícios.
     *
     * @param indicacaoId           id da indicação que está sendo convertida
     * @param tokenMetodoPagamento  token/fingerprint do método de pagamento usado pelo indicado
     */
    public void confirmarConversao(IndicacaoId indicacaoId, String tokenMetodoPagamento) {
        new ProcessamentoWebhookAutomatico(
            indicacaoRepositorio, auditoriaRepositorio, descontoFatura, motorGamificacao
        ).processar(indicacaoId, tokenMetodoPagamento);
    }

    /**
     * Confirma a conversão manualmente por um administrador, dispensando a
     * verificação automática de fraude por método de pagamento.
     * Delega ao {@link ProcessamentoWebhookManual}.
     *
     * @param indicacaoId id da indicação confirmada manualmente
     */
    public void confirmarConversaoManual(IndicacaoId indicacaoId) {
        new ProcessamentoWebhookManual(
            indicacaoRepositorio, auditoriaRepositorio, descontoFatura, motorGamificacao
        ).processar(indicacaoId, null);
    }

    // ── Painel do Tutor ──────────────────────────────────────────────────────

    /**
     * Retorna o histórico de indicações do Tutor com status de conversão (painel F-04).
     */
    public List<Indicacao> consultarHistorico(TutorId tutorId) {
        if (tutorId == null)
            throw new IllegalArgumentException("TutorId não pode ser nulo.");
        return indicacaoRepositorio.listarPorTutorIndicador(tutorId);
    }
}
