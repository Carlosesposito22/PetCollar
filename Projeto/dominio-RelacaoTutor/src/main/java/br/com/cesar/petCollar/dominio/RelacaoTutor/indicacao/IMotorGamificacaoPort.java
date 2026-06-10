package br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao;

import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

/**
 * Port de saída para o motor de gamificação (RN-6).
 * Desacopla o domínio de indicação do contexto Gamificação.
 */
public interface IMotorGamificacaoPort {

    /**
     * Dispara o evento de concessão da Conquista Lendária ao Tutor indicador (RN-6).
     * Idempotente: o motor de gamificação é responsável por evitar duplicidade.
     */
    void concederConquistaLendaria(TutorId tutorId);
}
