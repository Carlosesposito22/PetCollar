package petcollar.dominio.gamificacao.conquista;

import java.util.ArrayList;
import java.util.List;

public class ConcessaoBadgeService {

    private final IBadgeRepositorio badgeRepositorio;
    private final IConquistaTutorRepositorio conquistaRepositorio;
    private final IProgressoBadgeRepositorio progressoRepositorio;

    public ConcessaoBadgeService(IBadgeRepositorio badgeRepositorio,
                                 IConquistaTutorRepositorio conquistaRepositorio,
                                 IProgressoBadgeRepositorio progressoRepositorio) {
        this.badgeRepositorio = badgeRepositorio;
        this.conquistaRepositorio = conquistaRepositorio;
        this.progressoRepositorio = progressoRepositorio;
    }

    public List<ConquistaTutor> avaliarBadgesParaEvento(String tutorId, String chaveEvento) {
        if (tutorId == null || tutorId.isBlank())
            throw new IllegalArgumentException("TutorId não pode ser vazio.");
        if (chaveEvento == null || chaveEvento.isBlank())
            throw new IllegalArgumentException("Chave de evento não pode ser vazia.");

        List<Badge> badgesDoEvento = badgeRepositorio.findByChaveEvento(chaveEvento);
        List<ConquistaTutor> novasConquistas = new ArrayList<>();

        for (Badge badge : badgesDoEvento) {
            // RN-110: verificar se já conquistou
            if (conquistaRepositorio.existsByTutorEBadge(tutorId, badge.getId())) {
                continue;
            }

            if (badge.isEventoUnico()) {
                // RN-108: badge de evento único — conceder imediatamente
                ConquistaTutor conquista = new ConquistaTutor(
                        ConquistaId.gerar(), tutorId, badge.getId());
                conquistaRepositorio.save(conquista);
                novasConquistas.add(conquista);
            } else {
                // RN-109: badge quantitativa — incrementar progresso
                ProgressoBadge progresso = progressoRepositorio.findByTutorEBadge(tutorId, badge.getId());
                if (progresso == null) {
                    progresso = new ProgressoBadge(
                            ProgressoBadgeId.gerar(), tutorId, badge.getId(), badge.getMetaQuantitativa());
                }
                progresso.incrementar(1);
                progressoRepositorio.save(progresso);

                if (progresso.metaAtingida()) {
                    ConquistaTutor conquista = new ConquistaTutor(
                            ConquistaId.gerar(), tutorId, badge.getId());
                    conquistaRepositorio.save(conquista);
                    novasConquistas.add(conquista);
                }
            }
        }

        return novasConquistas;
    }
}
