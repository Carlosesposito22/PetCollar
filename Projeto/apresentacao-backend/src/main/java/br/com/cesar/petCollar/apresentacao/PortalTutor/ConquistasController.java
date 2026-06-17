package br.com.cesar.petCollar.apresentacao.PortalTutor;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.cesar.petCollar.aplicacao.Gamificacao.ConsultarConquistasTutorUseCase;
import br.com.cesar.petCollar.dominio.Gamificacao.conquista.Badge;
import br.com.cesar.petCollar.dominio.Gamificacao.conquista.CategoriaBadge;
import br.com.cesar.petCollar.dominio.Gamificacao.conquista.ConquistaTutor;
import br.com.cesar.petCollar.dominio.Gamificacao.conquista.ProgressoBadge;
import br.com.cesar.petCollar.dominio.Gamificacao.conquista.RaridadeBadge;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

@RestController
@RequestMapping("/api/tutor/conquistas")
public class ConquistasController {

    private final ConsultarConquistasTutorUseCase consultarConquistas;

    public ConquistasController(ConsultarConquistasTutorUseCase consultarConquistas) {
        this.consultarConquistas = consultarConquistas;
    }

    @GetMapping
    public ConquistasResponseDTO listar(Principal principal) {
        TutorId tutorId = TutorId.de(principal.getName());
        return ConquistasResponseDTO.de(consultarConquistas.executar(tutorId));
    }

    public record BadgeDTO(
            String badgeId,
            String nome,
            String descricao,
            CategoriaBadge categoria,
            RaridadeBadge raridade,
            LocalDate conquistadaEm,
            String eventoDisparador
    ) {
        static BadgeDTO de(Badge badge, ConquistaTutor conquista) {
            return new BadgeDTO(
                    badge.getId().getValor(),
                    badge.getNome(),
                    badge.getDescricao(),
                    badge.getCategoria(),
                    badge.getRaridade(),
                    conquista == null ? null : conquista.getConquistadoEm().toLocalDate(),
                    badge.getChaveEvento()
            );
        }
    }

    public record ProgressoBadgeDTO(
            String badgeId,
            String badgeNome,
            int valorAtual,
            int metaTotal,
            int percentualConclusao
    ) {
        static ProgressoBadgeDTO de(ProgressoBadge progresso, String badgeNome) {
            return new ProgressoBadgeDTO(
                    progresso.getBadgeId().getValor(),
                    badgeNome,
                    progresso.getValorAtual(),
                    progresso.getMetaTotal(),
                    (int) Math.round(progresso.calcularPercentualConclusao())
            );
        }
    }

    public record ConquistasResponseDTO(
            int tempoAssinaturaMeses,
            int totalBadges,
            int badgesDesbloqueadas,
            List<BadgeDTO> badges,
            List<ProgressoBadgeDTO> progressos
    ) {
        static ConquistasResponseDTO de(ConsultarConquistasTutorUseCase.Resultado resultado) {
            List<BadgeDTO> badges = resultado.badges().stream()
                    .map(badge -> BadgeDTO.de(badge, resultado.conquistasPorBadge().get(badge.getId().getValor())))
                    .toList();

            List<ProgressoBadgeDTO> progressos = resultado.progressos().stream()
                    .map(progresso -> ProgressoBadgeDTO.de(progresso, nomeDoBadge(resultado, progresso)))
                    .toList();

            long desbloqueadas = badges.stream().filter(b -> b.conquistadaEm() != null).count();
            return new ConquistasResponseDTO(
                    resultado.tempoAssinaturaMeses(),
                    badges.size(),
                    (int) desbloqueadas,
                    badges,
                    progressos
            );
        }

        private static String nomeDoBadge(ConsultarConquistasTutorUseCase.Resultado resultado, ProgressoBadge progresso) {
            return resultado.badges().stream()
                    .filter(b -> b.getId().equals(progresso.getBadgeId()))
                    .map(Badge::getNome)
                    .findFirst()
                    .orElse(null);
        }
    }
}
