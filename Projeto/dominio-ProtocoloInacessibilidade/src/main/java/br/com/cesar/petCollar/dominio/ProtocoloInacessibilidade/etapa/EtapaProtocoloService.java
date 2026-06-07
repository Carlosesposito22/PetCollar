package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.etapa;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.CanalContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.NivelCriticidade;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ConteudoNotificacao;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IServicoNotificacao;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ResultadoContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.IProtocoloInacessibilidadeRepositorio;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloInacessibilidade;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.StatusProtocolo;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.TentativaContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.TentativaId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * <b>Template Method (GoF)</b> — define o esqueleto, invariável, do algoritmo de
 * execução de uma <i>etapa</i> do Protocolo de Inacessibilidade do Tutor (F-03).
 *
 * <p>O protocolo é executado em três etapas progressivas e mutuamente exclusivas
 * (tutor → responsáveis secundários → escalonamento). Apesar de variarem em
 * <i>quem</i> contatam e <i>como</i> concluem, todas seguem o mesmo algoritmo de
 * alto nível, fixado aqui em {@link #executar(ProtocoloId)} (método {@code final}):
 *
 * <pre>
 *   1. Recuperar o agregado                              (invariante)
 *   2. Validar o estado de entrada                       (invariante — usa {@link #estadosDeEntradaPermitidos()})
 *   3. Preparar a entrada na etapa (transição inicial)   (gancho — {@link #prepararEntrada})
 *   4. Selecionar os destinatários da etapa              (gancho — {@link #selecionarDestinatarios})
 *   5. Para cada destinatário, em cada canal resolvido:
 *        a. executar a tentativa de contato no canal     (gancho — {@link #executarTentativaNoCanal})
 *        b. registrar a tentativa no agregado (RN 3)      (invariante)
 *        c. notificar o destinatário (RN 11/RN 14)        (invariante)
 *        d. interromper os demais canais ao primeiro sucesso (invariante)
 *   6. Avaliar a conclusão e decidir a transição         (gancho — {@link #avaliarConclusaoDaEtapa})
 *   7. Persistir o agregado uma única vez                (invariante)
 * </pre>
 *
 * <p>Os passos invariantes ficam aqui (com visibilidade {@code private}, fora do
 * alcance das subclasses); os passos variantes são delegados às subclasses pelos
 * ganchos {@code protected abstract}. Subclasses <b>não</b> podem sobrescrever o
 * esqueleto.
 *
 * <p><b>Nota sobre a etapa de escalonamento:</b> ela não contata destinatários por
 * canal — devolve uma lista vazia em {@link #selecionarDestinatarios} (o laço do
 * passo 5 não executa) e concentra seu trabalho (avançar nível, registrar evento
 * RN 7 e notificar o tutor RN 13) no gancho {@link #avaliarConclusaoDaEtapa}. É um
 * uso legítimo do padrão: a variação fica inteiramente nos ganchos, com o
 * esqueleto intacto.
 *
 * @see EtapaContatoTutorService
 * @see EtapaContatoResponsaveisSecundariosService
 * @see EtapaEscalonamentoService
 */
public abstract class EtapaProtocoloService {

    // ── Dependências compartilhadas (injetadas por construtor) ────────────────
    private final IProtocoloInacessibilidadeRepositorio protocoloRepositorio;
    private final IServicoNotificacao servicoNotificacao;

    protected EtapaProtocoloService(IProtocoloInacessibilidadeRepositorio protocoloRepositorio,
                                    IServicoNotificacao servicoNotificacao) {
        if (protocoloRepositorio == null)
            throw new IllegalArgumentException("Repositório de protocolos não pode ser nulo.");
        if (servicoNotificacao == null)
            throw new IllegalArgumentException("Serviço de notificação não pode ser nulo.");
        this.protocoloRepositorio = protocoloRepositorio;
        this.servicoNotificacao = servicoNotificacao;
    }

    // ── MÉTODO TEMPLATE (final — o esqueleto não pode ser sobrescrito) ────────

    /**
     * Executa uma etapa do protocolo seguindo a ordem fixa dos passos descrita na
     * classe. Este método define o algoritmo e <b>não</b> pode ser sobrescrito.
     *
     * @param protocoloId id do protocolo em execução
     * @return resultado consolidado da etapa
     */
    public final ResultadoEtapa executar(ProtocoloId protocoloId) {
        if (protocoloId == null)
            throw new IllegalArgumentException("Id do protocolo não pode ser nulo.");

        // Passo 1 — invariante: recupera o agregado uma única vez.
        ProtocoloInacessibilidade protocolo = protocoloRepositorio.buscarPorId(protocoloId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Protocolo não encontrado: " + protocoloId.getValor()));

        // Passo 2 — invariante: valida que o protocolo está em um estado executável.
        validarEstadoCompativel(protocolo);

        // Passo 3 — gancho: transição de entrada na etapa.
        prepararEntrada(protocolo);

        // Passos 4 e 5 — seleção dos destinatários e laço de tentativas por canal.
        List<TentativaContato> tentativas = new ArrayList<>();
        boolean sucesso = false;
        for (DestinatarioEtapa destinatario : selecionarDestinatarios(protocolo)) {
            for (CanalContato canal : resolverCanaisDeContato(destinatario, protocolo)) {
                // Passo 5a — gancho: executa a tentativa concreta no canal.
                ResultadoContato resultado = executarTentativaNoCanal(protocolo, destinatario, canal);

                // Passo 5b — invariante: registra a tentativa no agregado (RN 3).
                TentativaContato tentativa = new TentativaContato(
                    TentativaId.gerar(), destinatario.getId(), destinatario.getTipo(), canal,
                    resultado.getStatus(), LocalDateTime.now(), resultado.getMensagem());
                protocolo.registrarTentativa(tentativa);
                tentativas.add(tentativa);

                // Passo 5c — invariante: notifica o destinatário contatado, com a
                // criticidade da etapa (RN 11 para o tutor, RN 14 para o secundário).
                servicoNotificacao.notificar(
                    destinatario.getId(), conteudoDaEtapa(), criticidadeDestaEtapa());

                // Passo 5d — invariante: sucesso interrompe os demais canais/destinatários.
                if (tentativa.houveSucesso()) {
                    sucesso = true;
                    break;
                }
            }
            if (sucesso) break;
        }

        ResultadoEtapa resultado = new ResultadoEtapa(nomeDaEtapa(), tentativas, sucesso);

        // Passo 6 — gancho: avalia a conclusão e decide a transição de estado.
        avaliarConclusaoDaEtapa(protocolo, resultado);

        // Passo 7 — invariante: persiste o agregado uma única vez.
        protocoloRepositorio.salvar(protocolo);
        return resultado;
    }

    // ── PASSOS INVARIANTES (private — não acessíveis às subclasses) ───────────

    private void validarEstadoCompativel(ProtocoloInacessibilidade protocolo) {
        if (!estadosDeEntradaPermitidos().contains(protocolo.getStatus()))
            throw new IllegalStateException(String.format(
                "Protocolo no status %s não é compatível com a etapa \"%s\" (esperava um de %s).",
                protocolo.getStatus(), nomeDaEtapa(), estadosDeEntradaPermitidos()));
    }

    // ── Auxiliar de notificação reutilizável pelas subclasses ─────────────────

    /**
     * Notifica o tutor principal do protocolo. Usado pela etapa de escalonamento
     * (RN 13), cuja notificação ocorre na conclusão e não no laço de contato.
     */
    protected final void notificarTutorPrincipal(ProtocoloInacessibilidade protocolo,
                                                 ConteudoNotificacao conteudo,
                                                 NivelCriticidade criticidade) {
        servicoNotificacao.notificar(protocolo.getTutorPrincipalId().getValor(), conteudo, criticidade);
    }

    // ── GANCHOS VARIANTES (protected abstract — as subclasses DEVEM implementar) ─

    /** Nome humano da etapa, usado em logs, mensagens de erro e no {@link ResultadoEtapa}. */
    protected abstract String nomeDaEtapa();

    /** Estados em que o protocolo pode estar para que esta etapa seja executável. */
    protected abstract Set<StatusProtocolo> estadosDeEntradaPermitidos();

    /** Nível de criticidade das notificações desta etapa (RN 9). */
    protected abstract NivelCriticidade criticidadeDestaEtapa();

    /** Conteúdo da notificação enviada aos destinatários contatados nesta etapa (RN 11/RN 14). */
    protected abstract ConteudoNotificacao conteudoDaEtapa();

    /** Passo 3 — transição que coloca o protocolo no estado próprio desta etapa. */
    protected abstract void prepararEntrada(ProtocoloInacessibilidade protocolo);

    /** Passo 4 — quem deve ser contatado nesta etapa (vazio quando não há contato por canal). */
    protected abstract List<DestinatarioEtapa> selecionarDestinatarios(ProtocoloInacessibilidade protocolo);

    /** Passo 5 — canais disponíveis para um destinatário, na ordem de uso. */
    protected abstract List<CanalContato> resolverCanaisDeContato(DestinatarioEtapa destinatario,
                                                                  ProtocoloInacessibilidade protocolo);

    /** Passo 5a — executa a tentativa concreta no canal e devolve o resultado do contato. */
    protected abstract ResultadoContato executarTentativaNoCanal(ProtocoloInacessibilidade protocolo,
                                                                 DestinatarioEtapa destinatario,
                                                                 CanalContato canal);

    /** Passo 6 — decide se a etapa concluiu e aplica a transição de estado resultante. */
    protected abstract void avaliarConclusaoDaEtapa(ProtocoloInacessibilidade protocolo,
                                                    ResultadoEtapa resultado);
}
