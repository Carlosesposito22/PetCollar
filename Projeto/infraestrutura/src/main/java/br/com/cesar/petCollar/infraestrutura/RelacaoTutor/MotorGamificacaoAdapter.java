package br.com.cesar.petCollar.infraestrutura.RelacaoTutor;

import br.com.cesar.petCollar.dominio.Gamificacao.conquista.ConcessaoBadgeService;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.IMotorGamificacaoPort;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MotorGamificacaoAdapter implements IMotorGamificacaoPort {

    private static final Logger log = LoggerFactory.getLogger(MotorGamificacaoAdapter.class);
    private static final String CHAVE_INDICACAO = "indicacao_aceita";

    private final ConcessaoBadgeService concessaoBadgeService;

    public MotorGamificacaoAdapter(ConcessaoBadgeService concessaoBadgeService) {
        this.concessaoBadgeService = concessaoBadgeService;
    }

    @Override
    public void concederConquistaLendaria(TutorId tutorId) {
        var novas = concessaoBadgeService.avaliarBadgesParaEvento(
                tutorId.getValor(), CHAVE_INDICACAO);

        if (novas.isEmpty()) {
            log.info("[GAMIFICACAO] Progresso de indicação incrementado para o Tutor {} (badge ainda não atingido).",
                    tutorId.getValor());
        } else {
            novas.forEach(c -> log.info("[GAMIFICACAO] Badge '{}' concedido ao Tutor {} (F-04 RN-6).",
                    c.getBadgeId(), tutorId.getValor()));
        }
    }
}
