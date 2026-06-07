package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.etapa;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.configuracao.ConfiguracaoProtocolo;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.configuracao.IConfiguracaoProtocoloRepositorio;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.CanalContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.NivelCriticidade;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.NivelEscalonamento;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ConteudoNotificacao;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IServicoNotificacao;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ResultadoContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.IProtocoloInacessibilidadeRepositorio;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloInacessibilidade;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.StatusProtocolo;

import java.util.List;
import java.util.Set;

/**
 * Etapa 3 do protocolo — <b>escalonamento</b> progressivo pelos níveis
 * administrativos e clínicos (RN 6) quando ninguém respondeu. Subclasse concreta
 * do {@link EtapaProtocoloService Template Method}.
 *
 * <p>Diferente das etapas de contato, o escalonamento não aciona destinatários por
 * canal: {@link #selecionarDestinatarios} devolve lista vazia (o laço de contato do
 * esqueleto não executa) e o trabalho concentra-se em {@link #avaliarConclusaoDaEtapa},
 * que a cada execução avança um nível registrando o evento auditável (RN 7) e
 * notificando o tutor com a criticidade do nível (RN 13). Esgotados os níveis
 * configurados, o protocolo é encerrado por esgotamento.
 *
 * <p>A precedência da RN 5 (só escalonar após acionar todos os responsáveis
 * secundários) é garantida em dobro: por {@link #prepararEntrada} e pela própria
 * transição {@code escalonar(...)} do agregado.
 */
public class EtapaEscalonamentoService extends EtapaProtocoloService {

    private final IConfiguracaoProtocoloRepositorio configuracaoRepositorio;

    public EtapaEscalonamentoService(IProtocoloInacessibilidadeRepositorio protocoloRepositorio,
                                     IServicoNotificacao servicoNotificacao,
                                     IConfiguracaoProtocoloRepositorio configuracaoRepositorio) {
        super(protocoloRepositorio, servicoNotificacao);
        if (configuracaoRepositorio == null)
            throw new IllegalArgumentException("Repositório de configuração não pode ser nulo.");
        this.configuracaoRepositorio = configuracaoRepositorio;
    }

    @Override
    protected String nomeDaEtapa() { return "Escalonamento"; }

    @Override
    protected Set<StatusProtocolo> estadosDeEntradaPermitidos() {
        return Set.of(StatusProtocolo.EM_TENTATIVA_SECUNDARIOS, StatusProtocolo.EM_ESCALONAMENTO);
    }

    @Override
    protected NivelCriticidade criticidadeDestaEtapa() {
        // A criticidade efetiva é a do nível atingido (vide avaliarConclusaoDaEtapa);
        // este valor representativo não é usado no laço (não há contato por canal).
        return NivelCriticidade.ALTA;
    }

    @Override
    protected ConteudoNotificacao conteudoDaEtapa() {
        return ConteudoNotificacao.escalonamento(criticidadeDestaEtapa());
    }

    @Override
    protected void prepararEntrada(ProtocoloInacessibilidade protocolo) {
        // RN 5 — não se escalona antes de acionar todos os responsáveis secundários.
        if (protocolo.getStatus() == StatusProtocolo.EM_TENTATIVA_SECUNDARIOS
                && !protocolo.todosResponsaveisSecundariosAcionados())
            throw new IllegalStateException(
                "Não é possível escalonar antes de acionar todos os responsáveis secundários (RN 5).");
    }

    @Override
    protected List<DestinatarioEtapa> selecionarDestinatarios(ProtocoloInacessibilidade protocolo) {
        // O escalonamento não contata destinatários por canal — todo o trabalho
        // ocorre na conclusão (avanço de nível + notificação ao tutor).
        return List.of();
    }

    @Override
    protected List<CanalContato> resolverCanaisDeContato(DestinatarioEtapa destinatario,
                                                         ProtocoloInacessibilidade protocolo) {
        return List.of();   // nunca invocado: não há destinatários
    }

    @Override
    protected ResultadoContato executarTentativaNoCanal(ProtocoloInacessibilidade protocolo,
                                                        DestinatarioEtapa destinatario,
                                                        CanalContato canal) {
        throw new UnsupportedOperationException("A etapa de escalonamento não realiza contato por canal.");
    }

    @Override
    protected void avaliarConclusaoDaEtapa(ProtocoloInacessibilidade protocolo, ResultadoEtapa resultado) {
        ConfiguracaoProtocolo config = configuracaoRepositorio.buscarVigente()
            .orElseThrow(() -> new IllegalStateException("Não há configuração de protocolo vigente."));

        List<NivelEscalonamento> niveis = config.getNiveisEscalonamento();
        NivelEscalonamento atual = protocolo.getNivelEscalonamentoAtual();
        int proximoIndice = atual == null ? 0 : niveis.indexOf(atual) + 1;

        if (proximoIndice >= niveis.size()) {
            protocolo.encerrarPorEsgotamento();
            return;
        }

        NivelEscalonamento proximo = niveis.get(proximoIndice);
        protocolo.escalonar(proximo, "Sem resposta do tutor e dos responsáveis secundários.", null);   // RN 6/7

        // RN 13/9 — notifica o tutor a cada mudança de nível, com a criticidade do nível.
        notificarTutorPrincipal(protocolo,
            ConteudoNotificacao.escalonamento(proximo.criticidade()), proximo.criticidade());
    }
}
