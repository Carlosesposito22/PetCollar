package petcollar.dominio.gamificacao.conquista;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CalculoProgressoService {

    private final IProgressoBadgeRepositorio progressoRepositorio;
    private final IConquistaTutorRepositorio conquistaRepositorio;

    public CalculoProgressoService(IProgressoBadgeRepositorio progressoRepositorio,
                                   IConquistaTutorRepositorio conquistaRepositorio) {
        this.progressoRepositorio = progressoRepositorio;
        this.conquistaRepositorio = conquistaRepositorio;
    }

    public ProgressoBadge atualizarProgresso(String tutorId, BadgeId badgeId, int incremento) {
        if (tutorId == null || tutorId.isBlank())
            throw new IllegalArgumentException("TutorId não pode ser vazio.");
        if (badgeId == null)
            throw new IllegalArgumentException("BadgeId não pode ser nulo.");
        if (incremento <= 0)
            throw new IllegalArgumentException("Incremento deve ser maior que zero.");

        ProgressoBadge progresso = progressoRepositorio.findByTutorEBadge(tutorId, badgeId);
        if (progresso == null)
            throw new IllegalArgumentException("Progresso não encontrado para o tutor e badge informados.");

        progresso.incrementar(incremento);
        progressoRepositorio.save(progresso);
        return progresso;
    }

    // RN-113: retorna as badges com maior percentual de conclusão que ainda não foram conquistadas
    public List<ProgressoBadge> listarProximasConquistas(String tutorId, int quantidade) {
        if (tutorId == null || tutorId.isBlank())
            throw new IllegalArgumentException("TutorId não pode ser vazio.");
        if (quantidade <= 0)
            throw new IllegalArgumentException("Quantidade deve ser maior que zero.");

        List<ProgressoBadge> progressos = progressoRepositorio.findByTutorId(tutorId);

        return progressos.stream()
                .filter(p -> !conquistaRepositorio.existsByTutorEBadge(tutorId, p.getBadgeId()))
                .sorted(Comparator.comparingDouble(ProgressoBadge::calcularPercentualConclusao).reversed())
                .limit(quantidade)
                .collect(Collectors.toList());
    }
}
