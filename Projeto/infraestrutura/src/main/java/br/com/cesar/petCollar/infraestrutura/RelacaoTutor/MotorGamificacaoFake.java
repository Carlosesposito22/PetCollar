package br.com.cesar.petCollar.infraestrutura.RelacaoTutor;

import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.IMotorGamificacaoPort;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MotorGamificacaoFake implements IMotorGamificacaoPort {

    private static final Logger log = LoggerFactory.getLogger(MotorGamificacaoFake.class);

    @Override
    public void concederConquistaLendaria(TutorId tutorId) {
        log.info("[GAMIFICACAO] Conquista Lendária concedida ao Tutor {} (F-04 RN-6).",
                 tutorId.getValor());
    }
}
