package petCollar.dominio.AtendimentoClinico.nutricao;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class PlanoNutricional {

    private final PlanoNutricionalId id;
    private final String pacienteId;
    private final PesoIdeal pesoIdeal;
    private final NivelAtividade nivelAtividade;
    private final List<Comorbidade> comorbidades;
    private final ResultadoNEM resultadoNEM;
    private final CronogramaTransicao cronogramaTransicao;
    private final AlertaManejoCritico alertaManejoCritico;
    private final LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public PlanoNutricional(PlanoNutricionalId id, String pacienteId, PesoIdeal pesoIdeal,
                            NivelAtividade nivelAtividade, List<Comorbidade> comorbidades) {
        if (id == null)
            throw new IllegalArgumentException("ID do plano nutricional não pode ser nulo.");
        if (pacienteId == null || pacienteId.isBlank())
            throw new IllegalArgumentException("ID do paciente não pode ser vazio.");
        if (pesoIdeal == null)
            throw new IllegalArgumentException("Peso ideal não pode ser nulo.");
        if (nivelAtividade == null)
            throw new IllegalArgumentException("Nível de atividade não pode ser nulo.");

        this.id = id;
        this.pacienteId = pacienteId;
        this.pesoIdeal = pesoIdeal;
        this.nivelAtividade = nivelAtividade;
        this.comorbidades = comorbidades != null ? List.copyOf(comorbidades) : List.of();

        CalculoNEMService calculoNEMService = new CalculoNEMService();
        double kcalDiarias = calculoNEMService.calcularNEMTotal(
            pesoIdeal,
            nivelAtividade,
            this.comorbidades
        );

        double gramasDiarias = calculoNEMService.calcularGramasDiarias(kcalDiarias, 3.5);

        this.resultadoNEM = new ResultadoNEM(
            kcalDiarias,
            gramasDiarias,
            2,
            null
        );

        GeracaoCronogramaTransicaoService cronogramaService = new GeracaoCronogramaTransicaoService();
        this.cronogramaTransicao = cronogramaService.gerarCronograma7Dias();

        AnaliseComparativaPesoService analiseService = new AnaliseComparativaPesoService();
        this.alertaManejoCritico = analiseService.analisarDivergencia(pesoIdeal);

        this.criadoEm = LocalDateTime.now();
    }

    public PlanoNutricional(PlanoNutricionalId id, String pacienteId, PesoIdeal pesoIdeal,
                            NivelAtividade nivelAtividade, List<Comorbidade> comorbidades,
                            ResultadoNEM resultadoNEM, CronogramaTransicao cronogramaTransicao,
                            AlertaManejoCritico alertaManejoCritico,
                            LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.pesoIdeal = pesoIdeal;
        this.nivelAtividade = nivelAtividade;
        this.comorbidades = comorbidades != null ? List.copyOf(comorbidades) : List.of();
        this.resultadoNEM = resultadoNEM;
        this.cronogramaTransicao = cronogramaTransicao;
        this.alertaManejoCritico = alertaManejoCritico;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public PlanoNutricionalId getId() {
        return id;
    }

    public String getPacienteId() {
        return pacienteId;
    }

    public PesoIdeal getPesoIdeal() {
        return pesoIdeal;
    }

    public NivelAtividade getNivelAtividade() {
        return nivelAtividade;
    }

    public List<Comorbidade> getComorbidades() {
        return Collections.unmodifiableList(comorbidades);
    }

    public ResultadoNEM getResultadoNEM() {
        return resultadoNEM;
    }

    public CronogramaTransicao getCronogramaTransicao() {
        return cronogramaTransicao;
    }

    public AlertaManejoCritico getAlertaManejoCritico() {
        return alertaManejoCritico;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }
}
