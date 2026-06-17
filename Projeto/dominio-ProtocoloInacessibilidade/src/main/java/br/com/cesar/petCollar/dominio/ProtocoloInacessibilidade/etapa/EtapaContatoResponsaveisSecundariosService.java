package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.etapa;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.CanalContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.NivelCriticidade;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ConteudoNotificacao;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IResponsavelSecundarioRepositorio;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IServicoCanalContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IServicoNotificacao;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ResponsavelSecundario;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ResultadoContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.IProtocoloInacessibilidadeRepositorio;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.MotivoEncerramento;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloInacessibilidade;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.StatusProtocolo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class EtapaContatoResponsaveisSecundariosService extends EtapaProtocoloService {

    private final IResponsavelSecundarioRepositorio responsavelRepositorio;
    private final IServicoCanalContato servicoCanalContato;

    public EtapaContatoResponsaveisSecundariosService(
            IProtocoloInacessibilidadeRepositorio protocoloRepositorio,
            IServicoNotificacao servicoNotificacao,
            IResponsavelSecundarioRepositorio responsavelRepositorio,
            IServicoCanalContato servicoCanalContato) {
        super(protocoloRepositorio, servicoNotificacao);
        if (responsavelRepositorio == null)
            throw new IllegalArgumentException("Repositório de responsáveis secundários não pode ser nulo.");
        if (servicoCanalContato == null)
            throw new IllegalArgumentException("Serviço de canal de contato não pode ser nulo.");
        this.responsavelRepositorio = responsavelRepositorio;
        this.servicoCanalContato = servicoCanalContato;
    }

    @Override
    protected String nomeDaEtapa() { return "Contato com Responsáveis Secundários"; }

    @Override
    protected Set<StatusProtocolo> estadosDeEntradaPermitidos() {
        return Set.of(StatusProtocolo.ATIVADO, StatusProtocolo.EM_TENTATIVA_TUTOR,
            StatusProtocolo.EM_TENTATIVA_SECUNDARIOS);
    }

    @Override
    protected NivelCriticidade criticidadeDestaEtapa() { return NivelCriticidade.MEDIA; }

    @Override
    protected ConteudoNotificacao conteudoDaEtapa() {
        return ConteudoNotificacao.tentativaDeContato(criticidadeDestaEtapa());
    }

    @Override
    protected void prepararEntrada(ProtocoloInacessibilidade protocolo) {
        if (protocolo.getStatus() == StatusProtocolo.ATIVADO)
            protocolo.iniciarTentativasTutor();
        if (protocolo.getStatus() == StatusProtocolo.EM_TENTATIVA_TUTOR)
            protocolo.iniciarAcionamentoSecundarios();
    }

    @Override
    protected List<DestinatarioEtapa> selecionarDestinatarios(ProtocoloInacessibilidade protocolo) {
        List<ResponsavelSecundario> lista = new ArrayList<>(
            responsavelRepositorio.listarPorPaciente(protocolo.getPacienteId()));
        lista.sort(Comparator.comparingInt(ResponsavelSecundario::getPrioridade));
        return lista.stream().map(DestinatarioEtapa::deResponsavelSecundario).toList();
    }

    @Override
    protected List<CanalContato> resolverCanaisDeContato(DestinatarioEtapa destinatario,
                                                         ProtocoloInacessibilidade protocolo) {
        return destinatario.getCanaisPreferenciais();
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
            protocolo.encerrarComSucesso(
                MotivoEncerramento.sucessoComSecundario("Responsável secundário respondeu ao contato."));
            return;
        }

        protocolo.marcarTodosSecundariosAcionados();
    }
}
