package petcollar.dominio.gamificacao.bdd;

import org.mockito.Mockito;
import petcollar.dominio.gamificacao.conquista.*;

import java.util.List;

public class ContextoCenario {

    // ── Mocks de repositório ────────────────────────────────────────────────
    public final IBadgeRepositorio badgeRepositorio =
            Mockito.mock(IBadgeRepositorio.class);

    public final IConquistaTutorRepositorio conquistaRepositorio =
            Mockito.mock(IConquistaTutorRepositorio.class);

    public final IProgressoBadgeRepositorio progressoRepositorio =
            Mockito.mock(IProgressoBadgeRepositorio.class);

    // ── Serviços de domínio ─────────────────────────────────────────────────
    public final ConcessaoBadgeService concessaoService =
            new ConcessaoBadgeService(badgeRepositorio, conquistaRepositorio, progressoRepositorio);

    public final CalculoProgressoService calculoProgressoService =
            new CalculoProgressoService(progressoRepositorio, conquistaRepositorio);

    // ── Estado do cenário ───────────────────────────────────────────────────
    public Badge badge;
    public BadgeId badgeId;
    public String tutorId = "tutor-teste-001";
    public ConquistaTutor conquista;
    public ProgressoBadge progresso;
    public List<ConquistaTutor> conquistasRetornadas;
    public List<ProgressoBadge> progressosRetornados;
    public Exception excecaoCapturada;
}
