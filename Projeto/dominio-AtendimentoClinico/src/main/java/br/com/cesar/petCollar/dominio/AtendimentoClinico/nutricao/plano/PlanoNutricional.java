package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.parametros.Comorbidade;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.RacaoId;
import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

/**
 * Agregado raiz do Plano Nutricional (F-11). Possui uma máquina de estados de
 * dois passos: {@link StatusPlanoNutricional#RASCUNHO RASCUNHO} (o médico ajusta
 * livremente) → {@link StatusPlanoNutricional#FINALIZADO FINALIZADO} (assinado
 * digitalmente, imutável — RN 8).
 *
 * <p>O cálculo da NEM nunca é persistido como número solto: ele é sempre
 * recalculado pela cadeia de Decorators a partir dos {@link ParametrosPaciente}.
 * Quando finaliza, congelamos o {@link ResultadoNEM} junto com a assinatura
 * para preservar o registro histórico.
 */
public class PlanoNutricional {

    private final PlanoNutricionalId id;
    private final PacienteId pacienteId;
    private final TutorId tutorId;
    private final MedicoId medicoResponsavel;
    private final LocalDateTime criadoEm;

    private ParametrosPaciente parametros;
    private CronogramaTransicao cronograma;
    private final List<ObservacaoNutricional> observacoes;

    private StatusPlanoNutricional status;
    private LocalDateTime atualizadoEm;

    // Ração escolhida do catálogo (opcional) — quando vinculada, a densidade
    // calórica dos {@link ParametrosPaciente} deve refletir a densidade da ração.
    private RacaoId racaoId;

    // Justificativa exigida quando a divergência peso atual ↔ ideal ultrapassa
    // {@link #LIMIAR_DIVERGENCIA_JUSTIFICATIVA}% (RN reforçada).
    private String justificativaDivergencia;

    // Snapshot do cálculo + assinatura — preenchidos só após finalizar.
    private ResultadoNEM resultadoFinalizado;
    private AssinaturaDigital assinatura;

    /** Acima desta divergência, exige justificativa clínica para finalizar. */
    public static final BigDecimal LIMIAR_DIVERGENCIA_JUSTIFICATIVA = new BigDecimal("30");

    // ── Construtor de CRIAÇÃO ────────────────────────────────────────────────

    public PlanoNutricional(PlanoNutricionalId id, PacienteId pacienteId,
                            TutorId tutorId, MedicoId medicoResponsavel,
                            ParametrosPaciente parametros) {
        if (id == null)                throw new IllegalArgumentException("Id é obrigatório.");
        if (pacienteId == null)        throw new IllegalArgumentException("PacienteId é obrigatório.");
        if (tutorId == null)           throw new IllegalArgumentException("TutorId é obrigatório.");
        if (medicoResponsavel == null) throw new IllegalArgumentException("Médico responsável é obrigatório.");
        if (parametros == null)        throw new IllegalArgumentException("Parâmetros são obrigatórios.");

        this.id = id;
        this.pacienteId = pacienteId;
        this.tutorId = tutorId;
        this.medicoResponsavel = medicoResponsavel;
        this.parametros = parametros;
        this.cronograma = CronogramaTransicao.padrao7Dias();
        this.observacoes = new ArrayList<>();
        this.status = StatusPlanoNutricional.RASCUNHO;
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = this.criadoEm;
    }

    // ── Construtor de RECONSTRUÇÃO (usado pelos adapters JPA) ────────────────

    public PlanoNutricional(PlanoNutricionalId id, PacienteId pacienteId,
                            TutorId tutorId, MedicoId medicoResponsavel,
                            ParametrosPaciente parametros, CronogramaTransicao cronograma,
                            List<ObservacaoNutricional> observacoes,
                            StatusPlanoNutricional status,
                            LocalDateTime criadoEm, LocalDateTime atualizadoEm,
                            ResultadoNEM resultadoFinalizado, AssinaturaDigital assinatura,
                            RacaoId racaoId, String justificativaDivergencia) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.tutorId = tutorId;
        this.medicoResponsavel = medicoResponsavel;
        this.parametros = parametros;
        this.cronograma = cronograma;
        this.observacoes = new ArrayList<>(observacoes == null ? List.of() : observacoes);
        this.status = status;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
        this.resultadoFinalizado = resultadoFinalizado;
        this.assinatura = assinatura;
        this.racaoId = racaoId;
        this.justificativaDivergencia = justificativaDivergencia;
    }

    // ── Operações de negócio ─────────────────────────────────────────────────

    public void alterarParametros(ParametrosPaciente novos) {
        verificarRascunho();
        if (novos == null) throw new IllegalArgumentException("Parâmetros são obrigatórios.");
        this.parametros = novos;
        this.atualizadoEm = LocalDateTime.now();
    }

    public void alterarCronograma(CronogramaTransicao novo) {
        verificarRascunho();
        if (novo == null) throw new IllegalArgumentException("Cronograma é obrigatório.");
        this.cronograma = novo;
        this.atualizadoEm = LocalDateTime.now();
    }

    public void substituirObservacoes(List<ObservacaoNutricional> novas) {
        verificarRascunho();
        this.observacoes.clear();
        if (novas != null) this.observacoes.addAll(novas);
        this.atualizadoEm = LocalDateTime.now();
    }

    /** Vincula uma ração do catálogo ao plano (opcional). */
    public void vincularRacao(RacaoId racaoId) {
        verificarRascunho();
        this.racaoId = racaoId;
        this.atualizadoEm = LocalDateTime.now();
    }

    public void desvincularRacao() {
        verificarRascunho();
        this.racaoId = null;
        this.atualizadoEm = LocalDateTime.now();
    }

    /**
     * Registra a justificativa clínica para divergência de peso superior ao
     * {@link #LIMIAR_DIVERGENCIA_JUSTIFICATIVA limiar}. Texto vazio remove a
     * justificativa anterior.
     */
    public void registrarJustificativaDivergencia(String justificativa) {
        verificarRascunho();
        this.justificativaDivergencia = justificativa == null || justificativa.isBlank()
                ? null
                : justificativa.trim();
        this.atualizadoEm = LocalDateTime.now();
    }

    /** Divergência percentual entre peso atual e ideal, em valor absoluto. */
    public BigDecimal divergenciaPercentual() {
        BigDecimal pesoIdeal = parametros.pesoIdealKg();
        if (pesoIdeal == null || pesoIdeal.signum() == 0) return BigDecimal.ZERO;
        return parametros.pesoAtualKg().subtract(pesoIdeal).abs()
                .multiply(new BigDecimal("100"))
                .divide(pesoIdeal, 2, RoundingMode.HALF_UP);
    }

    /**
     * Finaliza com a assinatura digital — torna o plano imutável (RN 8).
     * Antes de assinar, verifica três invariantes clínicas reforçadas:
     * <ol>
     *   <li>se houver comorbidade, exige ao menos uma observação;</li>
     *   <li>se divergência peso atual ↔ ideal &gt; {@link #LIMIAR_DIVERGENCIA_JUSTIFICATIVA}%,
     *       exige justificativa registrada;</li>
     *   <li>apenas o médico responsável pode assinar.</li>
     * </ol>
     */
    public void finalizar(MedicoId medicoQueAssina, String imagemAssinaturaBase64) {
        verificarRascunho();
        if (medicoQueAssina == null)
            throw new IllegalArgumentException("Médico que assina é obrigatório.");
        if (!medicoQueAssina.equals(medicoResponsavel))
            throw new IllegalStateException(
                    "Apenas o médico responsável pelo plano pode assiná-lo.");
        if (imagemAssinaturaBase64 == null || imagemAssinaturaBase64.isBlank())
            throw new IllegalArgumentException("Assinatura é obrigatória.");

        validarInvariantesClinicas();

        this.resultadoFinalizado = ResultadoNEM.calcular(parametros);
        String hash = AssinaturaDigital.calcularHash(resumoParaHash());
        this.assinatura = new AssinaturaDigital(
                medicoQueAssina, imagemAssinaturaBase64, LocalDateTime.now(), hash);
        this.status = StatusPlanoNutricional.FINALIZADO;
        this.atualizadoEm = LocalDateTime.now();
    }

    private void validarInvariantesClinicas() {
        if (parametros.comorbidade() != Comorbidade.NENHUMA && observacoes.isEmpty())
            throw new IllegalStateException(
                    "Plano com comorbidade " + parametros.comorbidade()
                            + " exige ao menos uma observação nutricional antes de finalizar.");

        BigDecimal divergencia = divergenciaPercentual();
        if (divergencia.compareTo(LIMIAR_DIVERGENCIA_JUSTIFICATIVA) > 0
                && (justificativaDivergencia == null || justificativaDivergencia.isBlank()))
            throw new IllegalStateException(
                    "Divergência de " + divergencia + "% entre peso atual e ideal exige justificativa clínica.");
    }

    private void verificarRascunho() {
        if (status != StatusPlanoNutricional.RASCUNHO)
            throw new IllegalStateException(
                    "Plano já finalizado é imutável. Crie um novo plano para ajustes.");
    }

    /**
     * Marca este plano como SUBSTITUIDO. Só faz sentido em planos
     * FINALIZADOS — disparado quando o médico finaliza um novo plano para o
     * mesmo paciente. Preserva o snapshot histórico (assinatura, parâmetros,
     * resultado), apenas troca o status para indicar que não é mais o vigente.
     */
    public void marcarComoSubstituido() {
        if (status != StatusPlanoNutricional.FINALIZADO)
            throw new IllegalStateException(
                    "Só é possível substituir um plano FINALIZADO. Estado atual: " + status);
        this.status = StatusPlanoNutricional.SUBSTITUIDO;
        this.atualizadoEm = LocalDateTime.now();
    }

    /** Representação textual estável usada para gerar o hash da assinatura. */
    private String resumoParaHash() {
        StringBuilder sb = new StringBuilder()
                .append("plano=").append(id.getValor())
                .append("|paciente=").append(pacienteId.getValor())
                .append("|tutor=").append(tutorId.getValor())
                .append("|medico=").append(medicoResponsavel.getValor())
                .append("|pesoAtual=").append(parametros.pesoAtualKg())
                .append("|pesoIdeal=").append(parametros.pesoIdealKg())
                .append("|nivel=").append(parametros.nivelAtividade())
                .append("|comorbidade=").append(parametros.comorbidade())
                .append("|densidade=").append(parametros.densidadeCaloricaKcalPorKg())
                .append("|cronograma=").append(cronograma.tipo());
        for (ObservacaoNutricional o : observacoes) sb.append("|obs=").append(o.texto());
        if (racaoId != null) sb.append("|racao=").append(racaoId.getValor());
        if (justificativaDivergencia != null) sb.append("|justif=").append(justificativaDivergencia);
        return sb.toString();
    }

    /**
     * Calcula a NEM sob demanda. Se o plano está finalizado, devolve o
     * resultado fixado no momento da assinatura (RN 8 — imutabilidade
     * histórica). Caso contrário, recalcula a partir dos parâmetros atuais.
     */
    public ResultadoNEM resultadoAtual() {
        if (status == StatusPlanoNutricional.FINALIZADO && resultadoFinalizado != null)
            return resultadoFinalizado;
        return ResultadoNEM.calcular(parametros);
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public PlanoNutricionalId getId()                  { return id; }
    public PacienteId getPacienteId()                  { return pacienteId; }
    public TutorId getTutorId()                        { return tutorId; }
    public MedicoId getMedicoResponsavel()             { return medicoResponsavel; }
    public ParametrosPaciente getParametros()          { return parametros; }
    public CronogramaTransicao getCronograma()         { return cronograma; }
    public List<ObservacaoNutricional> getObservacoes(){ return Collections.unmodifiableList(observacoes); }
    public StatusPlanoNutricional getStatus()          { return status; }
    public LocalDateTime getCriadoEm()                 { return criadoEm; }
    public LocalDateTime getAtualizadoEm()             { return atualizadoEm; }
    public AssinaturaDigital getAssinatura()           { return assinatura; }
    public ResultadoNEM getResultadoFinalizado()       { return resultadoFinalizado; }
    public RacaoId getRacaoId()                        { return racaoId; }
    public String getJustificativaDivergencia()        { return justificativaDivergencia; }
}
