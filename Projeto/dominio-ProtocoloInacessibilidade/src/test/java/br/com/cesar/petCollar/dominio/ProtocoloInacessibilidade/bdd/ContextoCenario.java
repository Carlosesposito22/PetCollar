package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.bdd;

import org.mockito.Mockito;

import br.com.cesar.petCollar.dominio.compartilhado.AtendimentoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.configuracao.ConfiguracaoProtocolo;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.configuracao.ConfiguracaoProtocoloId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.CanalContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.NivelEscalonamento;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IConsultaAtendimento;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IDiretivaConsentimentoRepositorio;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IResponsavelSecundarioRepositorio;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IServicoCanalContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IServicoNotificacao;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ResultadoContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloInacessibilidade;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.TentativaContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.etapa.EtapaContatoResponsaveisSecundariosService;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.etapa.EtapaContatoTutorService;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.etapa.EtapaEscalonamentoService;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.etapa.OrquestradorEtapasProtocolo;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.servico.AtivacaoProtocoloService;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.servico.ConsultaDiretivaConsentimentoService;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.servico.ConsultaStatusProtocoloService;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.servico.VisaoProtocolo;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

public class ContextoCenario {

    public final ProtocoloRepositorioFake protocoloRepositorio = new ProtocoloRepositorioFake();
    public final ConfiguracaoProtocoloRepositorioFake configuracaoRepositorio =
        new ConfiguracaoProtocoloRepositorioFake();

    public final IConsultaAtendimento atendimentos = Mockito.mock(IConsultaAtendimento.class);
    public final IResponsavelSecundarioRepositorio responsaveis =
        Mockito.mock(IResponsavelSecundarioRepositorio.class);
    public final IDiretivaConsentimentoRepositorio diretivas =
        Mockito.mock(IDiretivaConsentimentoRepositorio.class);
    public final IServicoCanalContato canalContato = Mockito.mock(IServicoCanalContato.class);
    public final IServicoNotificacao notificacao = Mockito.mock(IServicoNotificacao.class);

    public final AtivacaoProtocoloService ativacaoService =
        new AtivacaoProtocoloService(protocoloRepositorio, configuracaoRepositorio, atendimentos, notificacao);

    public final EtapaContatoTutorService etapaTutor =
        new EtapaContatoTutorService(protocoloRepositorio, notificacao, configuracaoRepositorio, canalContato);
    public final EtapaContatoResponsaveisSecundariosService etapaSecundarios =
        new EtapaContatoResponsaveisSecundariosService(protocoloRepositorio, notificacao, responsaveis, canalContato);
    public final EtapaEscalonamentoService etapaEscalonamento =
        new EtapaEscalonamentoService(protocoloRepositorio, notificacao, configuracaoRepositorio);
    public final OrquestradorEtapasProtocolo orquestrador =
        new OrquestradorEtapasProtocolo(etapaTutor, etapaSecundarios, etapaEscalonamento);

    public final ConsultaDiretivaConsentimentoService diretivaService =
        new ConsultaDiretivaConsentimentoService(diretivas);
    public final ConsultaStatusProtocoloService statusService =
        new ConsultaStatusProtocoloService(protocoloRepositorio);

    public final AtendimentoId atendimentoId = AtendimentoId.gerar();
    public final PacienteId pacienteId = PacienteId.gerar();
    public final TutorId tutorId = TutorId.gerar();

    public ConfiguracaoProtocolo configuracao;
    public ProtocoloInacessibilidade protocolo;
    public TentativaContato ultimaTentativa;
    public List<TentativaContato> tentativasResultantes;
    public br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.etapa.ResultadoEtapa resultadoEtapa;
    public VisaoProtocolo visao;
    public Boolean autorizacaoConduta;
    public Exception excecao;

    public ContextoCenario() {

        lenient().when(canalContato.contatar(any(), any(), any()))
            .thenReturn(ResultadoContato.semResposta("Sem resposta."));

        lenient().when(responsaveis.listarPorPaciente(any())).thenReturn(List.of());
    }

    public ConfiguracaoProtocolo configuracaoPadrao() {
        ConfiguracaoProtocolo config = new ConfiguracaoProtocolo(
            ConfiguracaoProtocoloId.gerar(), 15,
            List.of(CanalContato.TELEFONE, CanalContato.SMS, CanalContato.EMAIL),
            5, 2,
            List.of(NivelEscalonamento.NIVEL_1_ADMINISTRATIVO, NivelEscalonamento.NIVEL_2_COORDENACAO,
                    NivelEscalonamento.NIVEL_3_CLINICO, NivelEscalonamento.NIVEL_4_DIRECAO));
        configuracaoRepositorio.salvar(config);
        this.configuracao = config;
        return config;
    }

    public ProtocoloInacessibilidade protocoloAtivado() {
        if (configuracao == null) configuracaoPadrao();
        ProtocoloInacessibilidade p = new ProtocoloInacessibilidade(
            ProtocoloId.gerar(), atendimentoId, pacienteId, tutorId, configuracao.getId());
        p.ativar();
        protocoloRepositorio.salvar(p);
        this.protocolo = p;
        return p;
    }

    public ProtocoloInacessibilidade protocoloProntoParaEscalonar() {
        ProtocoloInacessibilidade p = protocoloAtivado();
        p.iniciarTentativasTutor();
        p.iniciarAcionamentoSecundarios();
        p.marcarTodosSecundariosAcionados();
        protocoloRepositorio.salvar(p);
        return p;
    }
}
