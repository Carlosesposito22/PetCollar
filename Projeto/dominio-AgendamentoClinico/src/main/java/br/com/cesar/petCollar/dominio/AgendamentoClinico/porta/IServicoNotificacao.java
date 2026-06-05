package br.com.cesar.petCollar.dominio.AgendamentoClinico.porta;

import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

/**
 * Porta de saída para o contexto Notificacao. Permite que os serviços de
 * agendamento avisem médico (RN 6, RN 13) e tutor (RN 14) sem acoplamento direto.
 */
public interface IServicoNotificacao {

    void notificarMedico(MedicoId medicoId, String mensagem);

    void notificarTutor(TutorId tutorId, String mensagem);
}
