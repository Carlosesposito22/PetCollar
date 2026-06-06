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
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.servico.AcionamentoResponsavelSecundarioService;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.servico.AtivacaoProtocoloService;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.servico.ConsultaDiretivaConsentimentoService;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.servico.ConsultaStatusProtocoloService;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.servico.EscalonamentoService;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.servico.ExecucaoTentativaContatoService;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.servico.VisaoProtocolo;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Estado compartilhado entre os passos BDD (injetado via picocontainer). Reúne
 * repositórios em memória (estado persiste entre chamadas), portas externas
 * mockadas com Mockito, os serviços de domínio reais e os objetos manipulados pelos
 * cenários.
 */
public class ContextoCenario {

    // ── Repositórios em memória (estado real entre passos) ────────────────────
    public final ProtocoloRepositorioFake protocoloRepositorio = new ProtocoloRepositorioFake();
    public final ConfiguracaoProtocoloRepositorioFake configuracaoRepositorio =
        new ConfiguracaoProtocoloRepositorioFake();

    // ── Portas externas (mockadas) ────────────────────────────────────────────
    public final IConsultaAtendimento atendimentos = Mockito.mock(IConsultaAtendimento.class);
    public final IResponsavelSecundarioRepositorio responsaveis =
        Mockito.mock(IResponsavelSecundarioRepositorio.class);
    public final IDiretivaConsentimentoRepositorio diretivas =
        Mockito.mock(IDiretivaConsentimentoRepositorio.class);
    public final IServicoCanalContato canalContato = Mockito.mock(IServicoCanalContato.class);
    public final IServicoNotificacao notificacao = Mockito.mock(IServicoNotificacao.class);

    // ── Serviços de domínio (reais) ───────────────────────────────────────────
    public final AtivacaoProtocoloService ativacaoService =
        new AtivacaoProtocoloService(protocoloRepositorio, configuracaoRepositorio, atendimentos, notificacao);
    public final ExecucaoTentativaContatoService execucaoService =
        new ExecucaoTentativaContatoService(protocoloRepositorio, configuracaoRepositorio, canalContato, notificacao);
    public final AcionamentoResponsavelSecundarioService acionamentoService =
        new AcionamentoResponsavelSecundarioService(protocoloRepositorio, responsaveis, canalContato, notificacao);
    public final EscalonamentoService escalonamentoService =
        new EscalonamentoService(protocoloRepositorio, configuracaoRepositorio, notificacao);
    public final ConsultaDiretivaConsentimentoService diretivaService =
        new ConsultaDiretivaConsentimentoService(diretivas);
    public final ConsultaStatusProtocoloService statusService =
        new ConsultaStatusProtocoloService(protocoloRepositorio);

    // ── Identidades fixas do cenário ──────────────────────────────────────────
    public final AtendimentoId atendimentoId = AtendimentoId.gerar();
    public final PacienteId pacienteId = PacienteId.gerar();
    public final TutorId tutorId = TutorId.gerar();

    // ── Estado mutável ────────────────────────────────────────────────────────
    public ConfiguracaoProtocolo configuracao;
    public ProtocoloInacessibilidade protocolo;
    public TentativaContato ultimaTentativa;
    public List<TentativaContato> tentativasResultantes;
    public VisaoProtocolo visao;
    public Boolean autorizacaoConduta;
    public Exception excecao;

    public ContextoCenario() {
        // Por padrão, todo canal não responde — os cenários sobrescrevem para sucesso.
        lenient().when(canalContato.contatar(any(), any(), any()))
            .thenReturn(ResultadoContato.semResposta("Sem resposta."));
        // Por padrão, não há responsáveis secundários cadastrados.
        lenient().when(responsaveis.listarPorPaciente(any())).thenReturn(List.of());
    }

    // ── Auxiliares de domínio ─────────────────────────────────────────────────

    /** Configuração padrão usada na maioria dos cenários: tempo 15min, TELEFONE→SMS→EMAIL, 2 por canal, 4 níveis. */
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

    /** Cria, ativa e persiste um protocolo (status ATIVADO). */
    public ProtocoloInacessibilidade protocoloAtivado() {
        if (configuracao == null) configuracaoPadrao();
        ProtocoloInacessibilidade p = new ProtocoloInacessibilidade(
            ProtocoloId.gerar(), atendimentoId, pacienteId, tutorId, configuracao.getId());
        p.ativar();
        protocoloRepositorio.salvar(p);
        this.protocolo = p;
        return p;
    }

    /** Protocolo já pronto para escalonar: tutor e secundários acionados (RN 5 satisfeita). */
    public ProtocoloInacessibilidade protocoloProntoParaEscalonar() {
        ProtocoloInacessibilidade p = protocoloAtivado();
        p.iniciarTentativasTutor();
        p.iniciarAcionamentoSecundarios();
        p.marcarTodosSecundariosAcionados();
        protocoloRepositorio.salvar(p);
        return p;
    }
}
