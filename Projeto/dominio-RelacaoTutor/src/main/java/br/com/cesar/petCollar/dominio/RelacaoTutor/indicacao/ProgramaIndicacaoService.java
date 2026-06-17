package br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao;

import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ProgramaIndicacaoService {

    public static final BigDecimal PERCENTUAL_DESCONTO_INDICADO = new BigDecimal("0.30");

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

    public LinkIndicacao obterOuGerarLink(TutorId tutorId, boolean contaAtiva) {
        if (tutorId == null)
            throw new IllegalArgumentException("TutorId não pode ser nulo.");

        if (!contaAtiva)
            throw new IllegalStateException(
                "Apenas Tutores com conta ativa podem acessar o painel de indicação.");

        Optional<LinkIndicacao> existente = linkRepositorio.buscarPorTutorId(tutorId);
        if (existente.isPresent()) return existente.get();

        CodigoIndicacao codigo;
        do {
            codigo = CodigoIndicacao.gerar();
        } while (linkRepositorio.existePorCodigo(codigo));

        LinkIndicacao link = new LinkIndicacao(LinkIndicacaoId.gerar(), tutorId, codigo);
        linkRepositorio.salvar(link);

        auditoriaRepositorio.salvar(new EventoAuditoria(
            EventoAuditoriaId.gerar(),
            TipoEventoAuditoria.LINK_GERADO,
            tutorId, null,
            "Link de indicação gerado para o Tutor " + tutorId.getValor()
                + " com código " + codigo.getValor()
        ));
        return link;
    }

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

        RegistroClique novoClique = new RegistroClique(
            RegistroCliqueId.gerar(),
            cpfIndicado,
            link.getId(),
            link.getTutorId(),
            timestampClique
        );
        registroCliqueRepositorio.salvar(novoClique);

        auditoriaRepositorio.salvar(new EventoAuditoria(
            EventoAuditoriaId.gerar(),
            TipoEventoAuditoria.CLIQUE_REGISTRADO,
            link.getTutorId(), null,
            "Clique registrado no link " + codigoLink
                + " pelo CPF " + cpfIndicado.getValor()
                + " em " + timestampClique
        ));
    }

    public Indicacao criarIndicacaoParaInscrito(CPF cpfIndicado, CPF cpfIndicador) {
        if (cpfIndicado == null)  throw new IllegalArgumentException("CPF do indicado não pode ser nulo.");
        if (cpfIndicador == null) throw new IllegalArgumentException("CPF do indicador não pode ser nulo.");

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

        if (indicacaoRepositorio.existeConversaoPorCpf(cpfIndicado)) {
            throw new IllegalStateException(
                "Este CPF já foi contabilizado como indicação válida anteriormente (RN-10).");
        }

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

    public void confirmarConversao(IndicacaoId indicacaoId, String tokenMetodoPagamento) {
        new ProcessamentoWebhookAutomatico(
            indicacaoRepositorio, auditoriaRepositorio, descontoFatura, motorGamificacao
        ).processar(indicacaoId, tokenMetodoPagamento);
    }

    public void confirmarConversaoManual(IndicacaoId indicacaoId) {
        new ProcessamentoWebhookManual(
            indicacaoRepositorio, auditoriaRepositorio, descontoFatura, motorGamificacao
        ).processar(indicacaoId, null);
    }

    public Optional<LinkIndicacao> buscarLinkPorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) return Optional.empty();
        return linkRepositorio.buscarPorCodigo(CodigoIndicacao.de(codigo));
    }

    public Optional<Indicacao> buscarIndicacaoPendenteParaCpfIndicado(CPF cpfIndicado) {
        if (cpfIndicado == null) return Optional.empty();
        return indicacaoRepositorio.buscarPendenteParaCpfIndicado(cpfIndicado);
    }

    public Optional<String> resgatarDescontoIndicador(IndicacaoId indicacaoId, TutorId tutorId) {
        if (indicacaoId == null) throw new IllegalArgumentException("Id da indicação não pode ser nulo.");
        if (tutorId == null)     throw new IllegalArgumentException("TutorId não pode ser nulo.");

        Indicacao ind = indicacaoRepositorio.buscarPorId(indicacaoId)
            .orElseThrow(() -> new IllegalArgumentException("Indicação não encontrada: " + indicacaoId.getValor()));

        if (!ind.getTutorIndicadorId().equals(tutorId))
            throw new IllegalStateException("Esta indicação não pertence a este Tutor.");
        if (ind.getStatus() != StatusIndicacao.CONVERTIDA)
            throw new IllegalStateException("O indicado ainda não confirmou o pagamento (status: " + ind.getStatus() + ").");
        if (ind.getCobrancaIndicadorId() != null)
            throw new IllegalStateException("Desconto já aplicado à fatura " + ind.getCobrancaIndicadorId() + ".");

        Optional<String> cobId = descontoFatura.aplicarDescontoProximaFatura(
            tutorId, new java.math.BigDecimal("0.15"));
        cobId.ifPresent(id -> {
            ind.registrarDescontoIndicador(id);
            indicacaoRepositorio.salvar(ind);
            auditoriaRepositorio.salvar(new EventoAuditoria(
                EventoAuditoriaId.gerar(),
                TipoEventoAuditoria.DESCONTO_INDICADOR_APLICADO,
                tutorId, indicacaoId,
                "Desconto de 15% resgatado manualmente e aplicado na cobrança " + id + " (RN-5)."
            ));
        });
        return cobId;
    }

    public List<Indicacao> consultarHistorico(TutorId tutorId) {
        if (tutorId == null)
            throw new IllegalArgumentException("TutorId não pode ser nulo.");
        return indicacaoRepositorio.listarPorTutorIndicador(tutorId);
    }
}
