package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.etapa;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.configuracao.ConfiguracaoProtocolo;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.configuracao.IConfiguracaoProtocoloRepositorio;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.CanalContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.NivelCriticidade;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ConteudoNotificacao;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IServicoCanalContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IServicoNotificacao;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ResultadoContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.IProtocoloInacessibilidadeRepositorio;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.MotivoEncerramento;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloInacessibilidade;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.StatusProtocolo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EtapaContatoTutorService extends EtapaProtocoloService {

    private final IConfiguracaoProtocoloRepositorio configuracaoRepositorio;
    private final IServicoCanalContato servicoCanalContato;

    public EtapaContatoTutorService(IProtocoloInacessibilidadeRepositorio protocoloRepositorio,
                                    IServicoNotificacao servicoNotificacao,
                                    IConfiguracaoProtocoloRepositorio configuracaoRepositorio,
                                    IServicoCanalContato servicoCanalContato) {
        super(protocoloRepositorio, servicoNotificacao);
        if (configuracaoRepositorio == null)
            throw new IllegalArgumentException("Repositório de configuração não pode ser nulo.");
        if (servicoCanalContato == null)
            throw new IllegalArgumentException("Serviço de canal de contato não pode ser nulo.");
        this.configuracaoRepositorio = configuracaoRepositorio;
        this.servicoCanalContato = servicoCanalContato;
    }

    @Override
    protected String nomeDaEtapa() { return "Contato com Tutor Principal"; }

    @Override
    protected Set<StatusProtocolo> estadosDeEntradaPermitidos() {
        return Set.of(StatusProtocolo.ATIVADO, StatusProtocolo.EM_TENTATIVA_TUTOR);
    }

    @Override
    protected NivelCriticidade criticidadeDestaEtapa() { return NivelCriticidade.BAIXA; }

    @Override
    protected ConteudoNotificacao conteudoDaEtapa() {
        return ConteudoNotificacao.tentativaDeContato(criticidadeDestaEtapa());
    }

    @Override
    protected void prepararEntrada(ProtocoloInacessibilidade protocolo) {
        if (protocolo.getStatus() == StatusProtocolo.ATIVADO)
            protocolo.iniciarTentativasTutor();
    }

    @Override
    protected List<DestinatarioEtapa> selecionarDestinatarios(ProtocoloInacessibilidade protocolo) {
        return List.of(DestinatarioEtapa.deTutor(protocolo.getTutorPrincipalId()));
    }

    @Override
    protected List<CanalContato> resolverCanaisDeContato(DestinatarioEtapa destinatario,
                                                         ProtocoloInacessibilidade protocolo) {

        ConfiguracaoProtocolo config = configuracaoRepositorio.buscarVigente()
            .orElseThrow(() -> new IllegalStateException("Não há configuração de protocolo vigente."));
        List<CanalContato> ordem = new ArrayList<>();
        for (CanalContato canal : config.getCanaisHabilitados())
            for (int i = 0; i < config.getQuantidadeMaximaTentativasPorCanal(); i++)
                ordem.add(canal);
        return ordem;
    }

    @Override
    protected ResultadoContato executarTentativaNoCanal(ProtocoloInacessibilidade protocolo,
                                                        DestinatarioEtapa destinatario,
                                                        CanalContato canal) {
        return servicoCanalContato.contatar(canal, destinatario.getId(), conteudoDaEtapa());
    }

    @Override
    protected void avaliarConclusaoDaEtapa(ProtocoloInacessibilidade protocolo, ResultadoEtapa resultado) {
        if (resultado.houveSucesso()) {
            protocolo.encerrarComSucesso(MotivoEncerramento.sucessoComTutor("Tutor respondeu ao contato."));
            return;
        }

        protocolo.iniciarAcionamentoSecundarios();
    }
}
