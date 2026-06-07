package br.com.cesar.petCollar.apresentacao.AgendamentoClinico;

import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.IServicoNotificacao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * Stand-in da porta {@link IServicoNotificacao} que apenas registra as mensagens em
 * log (RN 6, 13, 14). Substituível pela integração real com o contexto Notificacao.
 */
@Component
@Service("servicoNotificacaoLogAgendamento")
public class ServicoNotificacaoLog implements IServicoNotificacao {

    private static final Logger log = LoggerFactory.getLogger(ServicoNotificacaoLog.class);

    @Override
    public void notificarMedico(MedicoId medicoId, String mensagem) {
        log.info("[NOTIFICAÇÃO MÉDICO {}] {}", medicoId.getValor(), mensagem);
    }

    @Override
    public void notificarTutor(TutorId tutorId, String mensagem) {
        log.info("[NOTIFICAÇÃO TUTOR {}] {}", tutorId.getValor(), mensagem);
    }
}
