package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.servico;

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
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloInacessibilidade;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.StatusProtocolo;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.TentativaContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.TentativaId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.TipoDestinatario;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Serviço de domínio que aciona, em ordem de prioridade, todos os responsáveis
 * secundários cadastrados para o paciente (RN 4) <b>antes</b> de qualquer
 * escalonamento administrativo/clínico (RN 5). Reutiliza o mesmo conteúdo de
 * notificação enviado ao tutor, mudando apenas o destinatário (RN 14), com
 * criticidade {@code MEDIA} (RN 9). Se algum responsável responde, o protocolo é
 * encerrado com sucesso.
 */
public class AcionamentoResponsavelSecundarioService {

    private final IProtocoloInacessibilidadeRepositorio protocoloRepositorio;
    private final IResponsavelSecundarioRepositorio responsaveis;
    private final IServicoCanalContato servicoCanalContato;
    private final IServicoNotificacao notificacao;

    public AcionamentoResponsavelSecundarioService(
            IProtocoloInacessibilidadeRepositorio protocoloRepositorio,
            IResponsavelSecundarioRepositorio responsaveis,
            IServicoCanalContato servicoCanalContato,
            IServicoNotificacao notificacao) {
        if (protocoloRepositorio == null)
            throw new IllegalArgumentException("Repositório de protocolos não pode ser nulo.");
        if (responsaveis == null)
            throw new IllegalArgumentException("Repositório de responsáveis secundários não pode ser nulo.");
        if (servicoCanalContato == null)
            throw new IllegalArgumentException("Serviço de canal de contato não pode ser nulo.");
        if (notificacao == null)
            throw new IllegalArgumentException("Serviço de notificação não pode ser nulo.");
        this.protocoloRepositorio = protocoloRepositorio;
        this.responsaveis = responsaveis;
        this.servicoCanalContato = servicoCanalContato;
        this.notificacao = notificacao;
    }

    /**
     * Aciona todos os responsáveis secundários em ordem de prioridade (RN 4),
     * registrando cada tentativa (RN 3) e notificando o destinatário (RN 14). Para
     * ao primeiro que responder, encerrando o protocolo com sucesso; caso ninguém
     * responda, marca o acionamento como concluído para liberar o escalonamento
     * (RN 5).
     */
    public List<TentativaContato> acionarTodos(ProtocoloId protocoloId) {
        if (protocoloId == null)
            throw new IllegalArgumentException("Id do protocolo não pode ser nulo.");

        ProtocoloInacessibilidade protocolo = protocoloRepositorio.buscarPorId(protocoloId)
            .orElseThrow(() -> new IllegalArgumentException("Protocolo não encontrado."));

        if (protocolo.getStatus() == StatusProtocolo.ATIVADO)
            protocolo.iniciarTentativasTutor();
        if (protocolo.getStatus() == StatusProtocolo.EM_TENTATIVA_TUTOR)
            protocolo.iniciarAcionamentoSecundarios();
        if (protocolo.getStatus() != StatusProtocolo.EM_TENTATIVA_SECUNDARIOS)
            throw new IllegalStateException(
                "O protocolo não está em fase de acionamento de responsáveis secundários.");

        List<ResponsavelSecundario> lista = new ArrayList<>(
            responsaveis.listarPorPaciente(protocolo.getPacienteId()));
        lista.sort(Comparator.comparingInt(ResponsavelSecundario::getPrioridade));

        ConteudoNotificacao conteudo = ConteudoNotificacao.tentativaDeContato(NivelCriticidade.MEDIA);
        List<TentativaContato> registradas = new ArrayList<>();

        for (ResponsavelSecundario responsavel : lista) {
            for (CanalContato canal : responsavel.getCanais()) {
                if (protocolo.isEncerrado())
                    break;
                String destinatario = responsavel.getId().getValor();
                ResultadoContato resultado = servicoCanalContato.contatar(canal, destinatario, conteudo);

                TentativaContato tentativa = new TentativaContato(
                    TentativaId.gerar(), destinatario, TipoDestinatario.RESPONSAVEL_SECUNDARIO, canal,
                    resultado.getStatus(), LocalDateTime.now(), resultado.getMensagem());
                protocolo.registrarTentativa(tentativa);   // RN 3
                registradas.add(tentativa);

                // RN 14/9 — mesmo conteúdo do tutor, destinatário trocado, criticidade MEDIA.
                notificacao.notificar(destinatario, conteudo, NivelCriticidade.MEDIA);

                if (tentativa.houveSucesso())
                    protocolo.encerrarComSucesso(MotivoEncerramento.sucessoComSecundario(
                        "Responsável secundário " + responsavel.getNome() + " respondeu via " + canal + "."));
            }
            if (protocolo.isEncerrado())
                break;
        }

        // RN 5 — esgotados os secundários sem resposta, libera o escalonamento.
        if (!protocolo.isEncerrado())
            protocolo.marcarTodosSecundariosAcionados();

        protocoloRepositorio.salvar(protocolo);
        return registradas;
    }
}
