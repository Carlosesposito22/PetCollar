package br.com.cesar.petCollar.dominio.AgendamentoClinico.porta;

import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

public interface IServicoNotificacao {

    void notificarMedico(MedicoId medicoId, String mensagem);

    void notificarTutor(TutorId tutorId, String mensagem);
}
