package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.servico;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.configuracao.ConfiguracaoProtocolo;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.configuracao.IConfiguracaoProtocoloRepositorio;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.NivelEscalonamento;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ConteudoNotificacao;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IServicoNotificacao;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.EventoEscalonamento;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.IProtocoloInacessibilidadeRepositorio;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloInacessibilidade;

import java.util.List;
import java.util.Optional;

/**
 * Serviço de domínio que escalona o protocolo progressivamente pelos níveis
 * administrativos e clínicos (RN 6) quando ninguém respondeu. Só inicia o
 * escalonamento depois que todos os responsáveis secundários foram acionados
 * (RN 5). Cada avanço gera um evento auditável no agregado (RN 7) e notifica o
 * tutor com criticidade proporcional ao nível (RN 9, RN 13). Esgotados os níveis,
 * o protocolo é encerrado por esgotamento.
 */
public class EscalonamentoService {

    private final IProtocoloInacessibilidadeRepositorio protocoloRepositorio;
    private final IConfiguracaoProtocoloRepositorio configuracaoRepositorio;
    private final IServicoNotificacao notificacao;

    public EscalonamentoService(IProtocoloInacessibilidadeRepositorio protocoloRepositorio,
                                IConfiguracaoProtocoloRepositorio configuracaoRepositorio,
                                IServicoNotificacao notificacao) {
        if (protocoloRepositorio == null)
            throw new IllegalArgumentException("Repositório de protocolos não pode ser nulo.");
        if (configuracaoRepositorio == null)
            throw new IllegalArgumentException("Repositório de configuração não pode ser nulo.");
        if (notificacao == null)
            throw new IllegalArgumentException("Serviço de notificação não pode ser nulo.");
        this.protocoloRepositorio = protocoloRepositorio;
        this.configuracaoRepositorio = configuracaoRepositorio;
        this.notificacao = notificacao;
    }

    /**
     * Inicia o escalonamento (primeiro nível), garantindo a precedência da RN 5:
     * todos os responsáveis secundários devem ter sido acionados antes.
     */
    public Optional<EventoEscalonamento> iniciarEscalonamento(ProtocoloId protocoloId) {
        ProtocoloInacessibilidade protocolo = obrigatorio(protocoloId);
        if (!protocolo.todosResponsaveisSecundariosAcionados())
            throw new IllegalStateException(
                "Não é possível escalonar antes de acionar todos os responsáveis secundários (RN 5).");
        return avancarNivel(protocoloId);
    }

    /**
     * Avança para o próximo nível de escalonamento habilitado na configuração
     * vigente (RN 6). Se não houver próximo nível, encerra o protocolo por
     * esgotamento. Retorna o evento gerado, se houve avanço.
     */
    public Optional<EventoEscalonamento> avancarNivel(ProtocoloId protocoloId) {
        ProtocoloInacessibilidade protocolo = obrigatorio(protocoloId);
        ConfiguracaoProtocolo config = configuracaoRepositorio.buscarVigente()
            .orElseThrow(() -> new IllegalStateException("Não há configuração de protocolo vigente."));

        List<NivelEscalonamento> niveis = config.getNiveisEscalonamento();
        NivelEscalonamento atual = protocolo.getNivelEscalonamentoAtual();
        int proximoIndice = atual == null ? 0 : niveis.indexOf(atual) + 1;

        if (proximoIndice >= niveis.size()) {
            protocolo.encerrarPorEsgotamento();
            protocoloRepositorio.salvar(protocolo);
            return Optional.empty();
        }

        NivelEscalonamento proximo = niveis.get(proximoIndice);
        protocolo.escalonar(proximo,
            "Sem resposta do tutor e dos responsáveis secundários.", null);   // RN 6/7

        // RN 13/9 — notifica o tutor a cada mudança de nível, criticidade proporcional.
        notificacao.notificar(protocolo.getTutorPrincipalId().getValor(),
            ConteudoNotificacao.escalonamento(proximo.criticidade()), proximo.criticidade());

        protocoloRepositorio.salvar(protocolo);

        List<EventoEscalonamento> eventos = protocolo.getEventosEscalonamento();
        return Optional.of(eventos.get(eventos.size() - 1));
    }

    private ProtocoloInacessibilidade obrigatorio(ProtocoloId protocoloId) {
        if (protocoloId == null)
            throw new IllegalArgumentException("Id do protocolo não pode ser nulo.");
        return protocoloRepositorio.buscarPorId(protocoloId)
            .orElseThrow(() -> new IllegalArgumentException("Protocolo não encontrado."));
    }
}
