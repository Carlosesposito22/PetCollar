package br.com.cesar.petCollar.dominio.Gamificacao.bdd;

import org.mockito.Mockito;
import br.com.cesar.petCollar.dominio.Gamificacao.conquista.*;

import java.util.List;

public class ContextoCenario {

    public final IBadgeRepositorio badgeRepositorio =
            Mockito.mock(IBadgeRepositorio.class);

    public final IConquistaTutorRepositorio conquistaRepositorio =
            Mockito.mock(IConquistaTutorRepositorio.class);

    public final IProgressoBadgeRepositorio progressoRepositorio =
            Mockito.mock(IProgressoBadgeRepositorio.class);

    public final ConcessaoBadgeService concessaoService =
            new ConcessaoBadgeService(badgeRepositorio, conquistaRepositorio, progressoRepositorio);

    public final CalculoProgressoService calculoProgressoService =
            new CalculoProgressoService(progressoRepositorio, conquistaRepositorio);

    public Badge badge;
    public BadgeId badgeId;
    public String tutorId = "tutor-teste-001";
    public ConquistaTutor conquista;
    public ProgressoBadge progresso;
    public List<ConquistaTutor> conquistasRetornadas;
    public List<ProgressoBadge> progressosRetornados;
    public Exception excecaoCapturada;
}
