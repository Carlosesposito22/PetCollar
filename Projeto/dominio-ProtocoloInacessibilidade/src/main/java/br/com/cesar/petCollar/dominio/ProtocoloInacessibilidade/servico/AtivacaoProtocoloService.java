package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.servico;

import br.com.cesar.petCollar.dominio.compartilhado.AtendimentoId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.configuracao.ConfiguracaoProtocolo;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.configuracao.IConfiguracaoProtocoloRepositorio;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.NivelCriticidade;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ConteudoNotificacao;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IConsultaAtendimento;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IServicoNotificacao;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ResumoAtendimento;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.IProtocoloInacessibilidadeRepositorio;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloInacessibilidade;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

public class AtivacaoProtocoloService {

    private final IProtocoloInacessibilidadeRepositorio protocoloRepositorio;
    private final IConfiguracaoProtocoloRepositorio configuracaoRepositorio;
    private final IConsultaAtendimento atendimentos;
    private final IServicoNotificacao notificacao;

    public AtivacaoProtocoloService(IProtocoloInacessibilidadeRepositorio protocoloRepositorio,
                                    IConfiguracaoProtocoloRepositorio configuracaoRepositorio,
                                    IConsultaAtendimento atendimentos,
                                    IServicoNotificacao notificacao) {
        if (protocoloRepositorio == null)
            throw new IllegalArgumentException("Repositório de protocolos não pode ser nulo.");
        if (configuracaoRepositorio == null)
            throw new IllegalArgumentException("Repositório de configuração não pode ser nulo.");
        if (atendimentos == null)
            throw new IllegalArgumentException("Porta de consulta de atendimentos não pode ser nula.");
        if (notificacao == null)
            throw new IllegalArgumentException("Serviço de notificação não pode ser nulo.");
        this.protocoloRepositorio = protocoloRepositorio;
        this.configuracaoRepositorio = configuracaoRepositorio;
        this.atendimentos = atendimentos;
        this.notificacao = notificacao;
    }

    public Optional<ProtocoloInacessibilidade> verificarEAtivar(AtendimentoId atendimentoId) {
        if (atendimentoId == null)
            throw new IllegalArgumentException("Id do atendimento não pode ser nulo.");

        Optional<ProtocoloInacessibilidade> jaAtivo =
            protocoloRepositorio.buscarAtivoPorAtendimento(atendimentoId);
        if (jaAtivo.isPresent())
            return jaAtivo;

        ResumoAtendimento resumo = atendimentos.buscarResumo(atendimentoId)
            .orElseThrow(() -> new IllegalArgumentException("Atendimento não encontrado."));

        if (!resumo.isEmAndamento())
            return Optional.empty();

        ConfiguracaoProtocolo config = configuracaoRepositorio.buscarVigente()
            .orElseThrow(() -> new IllegalStateException("Não há configuração de protocolo vigente."));

        long minutosSemResposta = Duration.between(
            resumo.getUltimaInteracaoTutorEm(), LocalDateTime.now()).toMinutes();
        if (minutosSemResposta < config.getTempoLimiteEsperaMinutos())
            return Optional.empty();

        return Optional.of(criarEAtivar(atendimentoId, resumo, config));
    }

    public ProtocoloInacessibilidade ativarManualmente(AtendimentoId atendimentoId) {
        if (atendimentoId == null)
            throw new IllegalArgumentException("Id do atendimento não pode ser nulo.");

        Optional<ProtocoloInacessibilidade> jaAtivo =
            protocoloRepositorio.buscarAtivoPorAtendimento(atendimentoId);
        if (jaAtivo.isPresent())
            return jaAtivo.get();

        ResumoAtendimento resumo = atendimentos.buscarResumo(atendimentoId)
            .orElseThrow(() -> new IllegalArgumentException("Atendimento não encontrado."));
        ConfiguracaoProtocolo config = configuracaoRepositorio.buscarVigente()
            .orElseThrow(() -> new IllegalStateException("Não há configuração de protocolo vigente."));

        return criarEAtivar(atendimentoId, resumo, config);
    }

    private ProtocoloInacessibilidade criarEAtivar(AtendimentoId atendimentoId,
                                                   ResumoAtendimento resumo, ConfiguracaoProtocolo config) {
        ProtocoloInacessibilidade protocolo = new ProtocoloInacessibilidade(
            ProtocoloId.gerar(), atendimentoId, resumo.getPacienteId(),
            resumo.getTutorPrincipalId(), config.getId());
        protocolo.ativar();
        protocoloRepositorio.salvar(protocolo);

        notificacao.notificar(resumo.getTutorPrincipalId().getValor(),
            ConteudoNotificacao.ativacaoProtocolo(), NivelCriticidade.BAIXA,
            protocolo.getId().getValor());

        return protocolo;
    }

    public int verificarTodosAtendimentosAtivos() {
        int ativos = 0;
        for (ResumoAtendimento resumo : atendimentos.listarEmAndamento()) {
            Optional<ProtocoloInacessibilidade> resultado = verificarEAtivar(resumo.getAtendimentoId());
            if (resultado.map(ProtocoloInacessibilidade::isAtivo).orElse(false))
                ativos++;
        }
        return ativos;
    }
}
