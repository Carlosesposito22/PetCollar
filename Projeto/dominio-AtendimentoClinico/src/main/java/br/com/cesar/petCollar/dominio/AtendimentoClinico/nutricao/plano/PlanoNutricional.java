package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    // Snapshot do cálculo + assinatura — preenchidos só após finalizar.
    private ResultadoNEM resultadoFinalizado;
    private AssinaturaDigital assinatura;

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
                            ResultadoNEM resultadoFinalizado, AssinaturaDigital assinatura) {
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

    /** Finaliza com a assinatura digital — torna o plano imutável (RN 8). */
    public void finalizar(MedicoId medicoQueAssina, String imagemAssinaturaBase64) {
        verificarRascunho();
        if (medicoQueAssina == null)
            throw new IllegalArgumentException("Médico que assina é obrigatório.");
        if (!medicoQueAssina.equals(medicoResponsavel))
            throw new IllegalStateException(
                    "Apenas o médico responsável pelo plano pode assiná-lo.");
        if (imagemAssinaturaBase64 == null || imagemAssinaturaBase64.isBlank())
            throw new IllegalArgumentException("Assinatura é obrigatória.");

        this.resultadoFinalizado = ResultadoNEM.calcular(parametros);
        String hash = AssinaturaDigital.calcularHash(resumoParaHash());
        this.assinatura = new AssinaturaDigital(
                medicoQueAssina, imagemAssinaturaBase64, LocalDateTime.now(), hash);
        this.status = StatusPlanoNutricional.FINALIZADO;
        this.atualizadoEm = LocalDateTime.now();
    }

    private void verificarRascunho() {
        if (status != StatusPlanoNutricional.RASCUNHO)
            throw new IllegalStateException(
                    "Plano já finalizado é imutável. Crie um novo plano para ajustes.");
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
}
