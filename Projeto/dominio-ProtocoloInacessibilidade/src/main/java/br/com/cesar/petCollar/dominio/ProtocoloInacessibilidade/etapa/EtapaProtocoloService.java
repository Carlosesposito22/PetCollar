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

public abstract class EtapaProtocoloService {

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

    public final ResultadoEtapa executar(ProtocoloId protocoloId) {
        if (protocoloId == null)
            throw new IllegalArgumentException("Id do protocolo não pode ser nulo.");

        ProtocoloInacessibilidade protocolo = protocoloRepositorio.buscarPorId(protocoloId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Protocolo não encontrado: " + protocoloId.getValor()));

        validarEstadoCompativel(protocolo);

        prepararEntrada(protocolo);

        List<TentativaContato> tentativas = new ArrayList<>();
        boolean sucesso = false;
        for (DestinatarioEtapa destinatario : selecionarDestinatarios(protocolo)) {
            for (CanalContato canal : resolverCanaisDeContato(destinatario, protocolo)) {

                ResultadoContato resultado = executarTentativaNoCanal(protocolo, destinatario, canal);

                TentativaContato tentativa = new TentativaContato(
                    TentativaId.gerar(), destinatario.getId(), destinatario.getTipo(), canal,
                    resultado.getStatus(), LocalDateTime.now(), resultado.getMensagem());
                protocolo.registrarTentativa(tentativa);
                tentativas.add(tentativa);

                servicoNotificacao.notificar(
                    destinatario.getId(), conteudoDaEtapa(), criticidadeDestaEtapa(),
                    protocoloId.getValor());

                if (tentativa.houveSucesso()) {
                    sucesso = true;
                    break;
                }
            }
            if (sucesso) break;
        }

        ResultadoEtapa resultado = new ResultadoEtapa(nomeDaEtapa(), tentativas, sucesso);

        avaliarConclusaoDaEtapa(protocolo, resultado);

        protocoloRepositorio.salvar(protocolo);
        return resultado;
    }

    private void validarEstadoCompativel(ProtocoloInacessibilidade protocolo) {
        if (!estadosDeEntradaPermitidos().contains(protocolo.getStatus()))
            throw new IllegalStateException(String.format(
                "Protocolo no status %s não é compatível com a etapa \"%s\" (esperava um de %s).",
                protocolo.getStatus(), nomeDaEtapa(), estadosDeEntradaPermitidos()));
    }

    protected final void notificarTutorPrincipal(ProtocoloInacessibilidade protocolo,
                                                 ConteudoNotificacao conteudo,
                                                 NivelCriticidade criticidade) {
        servicoNotificacao.notificar(protocolo.getTutorPrincipalId().getValor(), conteudo, criticidade,
            protocolo.getId().getValor());
    }

    protected abstract String nomeDaEtapa();

    protected abstract Set<StatusProtocolo> estadosDeEntradaPermitidos();

    protected abstract NivelCriticidade criticidadeDestaEtapa();

    protected abstract ConteudoNotificacao conteudoDaEtapa();

    protected abstract void prepararEntrada(ProtocoloInacessibilidade protocolo);

    protected abstract List<DestinatarioEtapa> selecionarDestinatarios(ProtocoloInacessibilidade protocolo);

    protected abstract List<CanalContato> resolverCanaisDeContato(DestinatarioEtapa destinatario,
                                                                  ProtocoloInacessibilidade protocolo);

    protected abstract ResultadoContato executarTentativaNoCanal(ProtocoloInacessibilidade protocolo,
                                                                 DestinatarioEtapa destinatario,
                                                                 CanalContato canal);

    protected abstract void avaliarConclusaoDaEtapa(ProtocoloInacessibilidade protocolo,
                                                    ResultadoEtapa resultado);
}
