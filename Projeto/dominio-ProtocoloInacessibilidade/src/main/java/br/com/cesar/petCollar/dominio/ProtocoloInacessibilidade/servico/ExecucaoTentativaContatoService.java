package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.servico;

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
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloInacessibilidade;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.StatusProtocolo;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.TentativaContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.TentativaId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.TipoDestinatario;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Serviço de domínio que executa as tentativas progressivas de contato com o tutor
 * principal nos canais habilitados, na ordem da configuração vigente (RN 2). Cada
 * tentativa é registrada de forma auditável no agregado (RN 3) e dispara uma
 * notificação ao tutor, independentemente do resultado (RN 11), com criticidade
 * proporcional à etapa (RN 9 — {@code BAIXA} para o tutor). Se o tutor responde, o
 * protocolo é encerrado com sucesso.
 */
public class ExecucaoTentativaContatoService {

    private final IProtocoloInacessibilidadeRepositorio protocoloRepositorio;
    private final IConfiguracaoProtocoloRepositorio configuracaoRepositorio;
    private final IServicoCanalContato servicoCanalContato;
    private final IServicoNotificacao notificacao;

    public ExecucaoTentativaContatoService(IProtocoloInacessibilidadeRepositorio protocoloRepositorio,
                                           IConfiguracaoProtocoloRepositorio configuracaoRepositorio,
                                           IServicoCanalContato servicoCanalContato,
                                           IServicoNotificacao notificacao) {
        if (protocoloRepositorio == null)
            throw new IllegalArgumentException("Repositório de protocolos não pode ser nulo.");
        if (configuracaoRepositorio == null)
            throw new IllegalArgumentException("Repositório de configuração não pode ser nulo.");
        if (servicoCanalContato == null)
            throw new IllegalArgumentException("Serviço de canal de contato não pode ser nulo.");
        if (notificacao == null)
            throw new IllegalArgumentException("Serviço de notificação não pode ser nulo.");
        this.protocoloRepositorio = protocoloRepositorio;
        this.configuracaoRepositorio = configuracaoRepositorio;
        this.servicoCanalContato = servicoCanalContato;
        this.notificacao = notificacao;
    }

    /**
     * Executa a próxima tentativa de contato com o tutor, no próximo canal ainda
     * disponível segundo a ordem e o limite por canal da configuração (RN 2).
     */
    public TentativaContato executarProximoCanal(ProtocoloId protocoloId) {
        if (protocoloId == null)
            throw new IllegalArgumentException("Id do protocolo não pode ser nulo.");

        ProtocoloInacessibilidade protocolo = protocoloRepositorio.buscarPorId(protocoloId)
            .orElseThrow(() -> new IllegalArgumentException("Protocolo não encontrado."));
        ConfiguracaoProtocolo config = configuracaoRepositorio.buscarVigente()
            .orElseThrow(() -> new IllegalStateException("Não há configuração de protocolo vigente."));

        if (protocolo.getStatus() == StatusProtocolo.ATIVADO)
            protocolo.iniciarTentativasTutor();
        if (protocolo.getStatus() != StatusProtocolo.EM_TENTATIVA_TUTOR)
            throw new IllegalStateException("O protocolo não está em fase de tentativas com o tutor.");

        CanalContato canal = proximoCanalDisponivel(protocolo, config)
            .orElseThrow(() -> new IllegalStateException(
                "Todos os canais de contato com o tutor já foram esgotados."));

        ConteudoNotificacao conteudo = ConteudoNotificacao.tentativaDeContato(NivelCriticidade.BAIXA);
        String destinatario = protocolo.getTutorPrincipalId().getValor();

        ResultadoContato resultado = servicoCanalContato.contatar(canal, destinatario, conteudo);

        TentativaContato tentativa = new TentativaContato(
            TentativaId.gerar(), destinatario, TipoDestinatario.TUTOR_PRINCIPAL, canal,
            resultado.getStatus(), LocalDateTime.now(), resultado.getMensagem());
        protocolo.registrarTentativa(tentativa);   // RN 3

        // RN 11/9 — notifica o tutor após cada tentativa, com criticidade BAIXA.
        notificacao.notificar(destinatario, conteudo, NivelCriticidade.BAIXA);

        if (tentativa.houveSucesso())
            protocolo.encerrarComSucesso(
                MotivoEncerramento.sucessoComTutor("Tutor respondeu via " + canal + "."));

        protocoloRepositorio.salvar(protocolo);
        return tentativa;
    }

    /** {@code true} quando não há mais canal/tentativa disponível para o tutor (RN 4). */
    public boolean tentativasTutorEsgotadas(ProtocoloId protocoloId) {
        ProtocoloInacessibilidade protocolo = protocoloRepositorio.buscarPorId(protocoloId)
            .orElseThrow(() -> new IllegalArgumentException("Protocolo não encontrado."));
        ConfiguracaoProtocolo config = configuracaoRepositorio.buscarVigente()
            .orElseThrow(() -> new IllegalStateException("Não há configuração de protocolo vigente."));
        return proximoCanalDisponivel(protocolo, config).isEmpty();
    }

    private Optional<CanalContato> proximoCanalDisponivel(ProtocoloInacessibilidade protocolo,
                                                          ConfiguracaoProtocolo config) {
        return config.getCanaisHabilitados().stream()
            .filter(canal -> protocolo.contarTentativas(TipoDestinatario.TUTOR_PRINCIPAL, canal)
                < config.getQuantidadeMaximaTentativasPorCanal())
            .findFirst();
    }
}
