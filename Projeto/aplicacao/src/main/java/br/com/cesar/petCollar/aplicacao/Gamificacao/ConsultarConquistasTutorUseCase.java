package br.com.cesar.petCollar.aplicacao.Gamificacao;

import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.Cobranca;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.ICobrancaRepositorio;
import br.com.cesar.petCollar.dominio.Gamificacao.conquista.Badge;
import br.com.cesar.petCollar.dominio.Gamificacao.conquista.ConquistaTutor;
import br.com.cesar.petCollar.dominio.Gamificacao.conquista.IBadgeRepositorio;
import br.com.cesar.petCollar.dominio.Gamificacao.conquista.IConquistaTutorRepositorio;
import br.com.cesar.petCollar.dominio.Gamificacao.conquista.IProgressoBadgeRepositorio;
import br.com.cesar.petCollar.dominio.Gamificacao.conquista.ProgressoBadge;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

/**
 * Consolida o painel de conquistas do tutor: catálogo de badges, as já
 * conquistadas, o progresso das quantitativas em andamento e o tempo de
 * assinatura — derivado da cobrança mais antiga (RN-108: badges de fidelidade
 * dependem do tempo de assinatura ativo).
 */
public class ConsultarConquistasTutorUseCase {

    private final IBadgeRepositorio badgeRepositorio;
    private final IConquistaTutorRepositorio conquistaRepositorio;
    private final IProgressoBadgeRepositorio progressoRepositorio;
    private final ICobrancaRepositorio cobrancaRepositorio;

    public ConsultarConquistasTutorUseCase(IBadgeRepositorio badgeRepositorio,
                                           IConquistaTutorRepositorio conquistaRepositorio,
                                           IProgressoBadgeRepositorio progressoRepositorio,
                                           ICobrancaRepositorio cobrancaRepositorio) {
        if (badgeRepositorio == null)
            throw new IllegalArgumentException("IBadgeRepositorio é obrigatório.");
        if (conquistaRepositorio == null)
            throw new IllegalArgumentException("IConquistaTutorRepositorio é obrigatório.");
        if (progressoRepositorio == null)
            throw new IllegalArgumentException("IProgressoBadgeRepositorio é obrigatório.");
        if (cobrancaRepositorio == null)
            throw new IllegalArgumentException("ICobrancaRepositorio é obrigatório.");
        this.badgeRepositorio = badgeRepositorio;
        this.conquistaRepositorio = conquistaRepositorio;
        this.progressoRepositorio = progressoRepositorio;
        this.cobrancaRepositorio = cobrancaRepositorio;
    }

    public Resultado executar(TutorId tutorId) {
        if (tutorId == null) throw new IllegalArgumentException("TutorId é obrigatório.");

        String tutorIdValor = tutorId.getValor();
        List<Badge> badges = badgeRepositorio.findAll();
        List<ConquistaTutor> conquistas = conquistaRepositorio.findByTutorId(tutorIdValor);
        List<ProgressoBadge> progressos = progressoRepositorio.findByTutorId(tutorIdValor);

        Map<String, ConquistaTutor> conquistasPorBadge = conquistas.stream()
                .collect(Collectors.toMap(c -> c.getBadgeId().getValor(), c -> c, (a, b) -> a));

        int tempoAssinaturaMeses = calcularTempoAssinaturaMeses(tutorId);

        return new Resultado(tempoAssinaturaMeses, badges, conquistasPorBadge, progressos);
    }

    private int calcularTempoAssinaturaMeses(TutorId tutorId) {
        return cobrancaRepositorio.listarPorTutor(tutorId).stream()
                .map(Cobranca::getCompetencia)
                .map(c -> c.getValor())
                .min(YearMonth::compareTo)
                .map(inicio -> (int) ChronoUnit.MONTHS.between(inicio, YearMonth.now()))
                .orElse(0);
    }

    /** Saída do caso de uso, ainda em termos de tipos de domínio. */
    public record Resultado(int tempoAssinaturaMeses,
                            List<Badge> badges,
                            Map<String, ConquistaTutor> conquistasPorBadge,
                            List<ProgressoBadge> progressos) {}
}
