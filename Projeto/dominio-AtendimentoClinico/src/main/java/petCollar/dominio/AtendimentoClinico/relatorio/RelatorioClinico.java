package petCollar.dominio.AtendimentoClinico.relatorio;

import br.com.cesar.petCollar.dominio.compartilhado.AtendimentoId;
import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RelatorioClinico {

    private final RelatorioClinicoId id;
    private final AtendimentoId atendimentoId;
    private final PacienteId pacienteId;
    private final MedicoId medicoId;
    private final TipoRelatorio tipoRelatorio;

    private SinaisVitais sinaisVitais;
    private EvolucaoComparativa evolucaoComparativa;
    private String diagnosticoTecnico;
    private String orientacoesManejo;
    private String resumoParaTutor;
    private String cuidadosPosOperatorios;
    private String tempoRecuperacaoEstimado;

    private final List<AnexoRelatorio> anexos;
    private final List<String> medicamentosPrescritos;

    private boolean imutavel;
    private final LocalDateTime criadoEm;
    private LocalDateTime assinadoEm;

    public RelatorioClinico(RelatorioClinicoId id, AtendimentoId atendimentoId,
                            PacienteId pacienteId, MedicoId medicoId, TipoRelatorio tipoRelatorio) {
        if (id == null)
            throw new IllegalArgumentException("Id do relatório não pode ser nulo.");
        if (atendimentoId == null)
            throw new IllegalArgumentException("AtendimentoId não pode ser nulo.");
        if (pacienteId == null)
            throw new IllegalArgumentException("PacienteId não pode ser nulo.");
        if (medicoId == null)
            throw new IllegalArgumentException("MedicoId não pode ser nulo.");
        this.id = id;
        this.atendimentoId = atendimentoId;
        this.pacienteId = pacienteId;
        this.medicoId = medicoId;
        this.tipoRelatorio = tipoRelatorio != null ? tipoRelatorio : TipoRelatorio.ROTINEIRO;
        this.anexos = new ArrayList<>();
        this.medicamentosPrescritos = new ArrayList<>();
        this.imutavel = false;
        this.criadoEm = LocalDateTime.now();
    }

    // Construtor de RECONSTRUÇÃO
    public RelatorioClinico(RelatorioClinicoId id, AtendimentoId atendimentoId,
                            PacienteId pacienteId, MedicoId medicoId, TipoRelatorio tipoRelatorio,
                            SinaisVitais sinaisVitais, EvolucaoComparativa evolucaoComparativa,
                            String diagnosticoTecnico, String orientacoesManejo,
                            String resumoParaTutor, String cuidadosPosOperatorios,
                            String tempoRecuperacaoEstimado, List<AnexoRelatorio> anexos,
                            List<String> medicamentosPrescritos, boolean imutavel,
                            LocalDateTime criadoEm, LocalDateTime assinadoEm) {
        this.id = id;
        this.atendimentoId = atendimentoId;
        this.pacienteId = pacienteId;
        this.medicoId = medicoId;
        this.tipoRelatorio = tipoRelatorio != null ? tipoRelatorio : TipoRelatorio.ROTINEIRO;
        this.sinaisVitais = sinaisVitais;
        this.evolucaoComparativa = evolucaoComparativa;
        this.diagnosticoTecnico = diagnosticoTecnico;
        this.orientacoesManejo = orientacoesManejo;
        this.resumoParaTutor = resumoParaTutor;
        this.cuidadosPosOperatorios = cuidadosPosOperatorios;
        this.tempoRecuperacaoEstimado = tempoRecuperacaoEstimado;
        this.anexos = anexos != null ? new ArrayList<>(anexos) : new ArrayList<>();
        this.medicamentosPrescritos = medicamentosPrescritos != null
            ? new ArrayList<>(medicamentosPrescritos) : new ArrayList<>();
        this.imutavel = imutavel;
        this.criadoEm = criadoEm;
        this.assinadoEm = assinadoEm;
    }

    // ── Métodos de negócio ────────────────────────────────────────────────────

    public void registrarSinaisVitais(SinaisVitais sinaisVitais) {
        verificarImutabilidade();
        if (sinaisVitais == null)
            throw new IllegalArgumentException("Sinais vitais não podem ser nulos.");
        this.sinaisVitais = sinaisVitais;
    }

    public void registrarEvolucaoComparativa(EvolucaoComparativa evolucaoComparativa) {
        verificarImutabilidade();
        if (evolucaoComparativa == null)
            throw new IllegalArgumentException("Evolução comparativa não pode ser nula.");
        this.evolucaoComparativa = evolucaoComparativa;
    }

    public void preencherDiagnosticoTecnico(String diagnosticoTecnico) {
        verificarImutabilidade();
        if (diagnosticoTecnico == null || diagnosticoTecnico.isBlank())
            throw new IllegalArgumentException("Diagnóstico técnico não pode ser vazio.");
        this.diagnosticoTecnico = diagnosticoTecnico;
    }

    public void preencherOrientacoesManejo(String orientacoesManejo) {
        verificarImutabilidade();
        if (orientacoesManejo == null || orientacoesManejo.isBlank())
            throw new IllegalArgumentException("Orientações de manejo não podem ser vazias.");
        this.orientacoesManejo = orientacoesManejo;
    }

    public void preencherResumoParaTutor(String resumoParaTutor) {
        verificarImutabilidade();
        if (resumoParaTutor == null || resumoParaTutor.isBlank())
            throw new IllegalArgumentException("Resumo para o tutor não pode ser vazio.");
        this.resumoParaTutor = resumoParaTutor;
    }

    public void preencherCuidadosPosOperatorios(String cuidados) {
        verificarImutabilidade();
        if (cuidados == null || cuidados.isBlank())
            throw new IllegalArgumentException("Cuidados pós-operatórios não podem ser vazios.");
        this.cuidadosPosOperatorios = cuidados;
    }

    public void preencherTempoRecuperacaoEstimado(String tempo) {
        verificarImutabilidade();
        if (tempo == null || tempo.isBlank())
            throw new IllegalArgumentException("Tempo de recuperação estimado não pode ser vazio.");
        this.tempoRecuperacaoEstimado = tempo;
    }

    public void adicionarAnexo(AnexoRelatorio anexo) {
        verificarImutabilidade();
        if (anexo == null)
            throw new IllegalArgumentException("Anexo não pode ser nulo.");
        if (anexos.size() >= 4)
            throw new IllegalStateException("O relatório já atingiu o limite de 4 anexos (RN-119).");
        this.anexos.add(anexo);
    }

    public void adicionarMedicamentoPrescrito(String medicamento) {
        verificarImutabilidade();
        if (medicamento == null || medicamento.isBlank())
            throw new IllegalArgumentException("Nome do medicamento não pode ser vazio.");
        this.medicamentosPrescritos.add(medicamento);
    }

    public void assinarDigitalmente() {
        verificarImutabilidade();
        this.imutavel = true;
        this.assinadoEm = LocalDateTime.now();
    }

    // ── Verificação interna de imutabilidade ─────────────────────────────────

    private void verificarImutabilidade() {
        if (this.imutavel)
            throw new IllegalStateException(
                "O relatório já foi assinado digitalmente e não pode ser modificado.");
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public RelatorioClinicoId getId()                    { return id; }
    public AtendimentoId getAtendimentoId()              { return atendimentoId; }
    public PacienteId getPacienteId()                    { return pacienteId; }
    public MedicoId getMedicoId()                        { return medicoId; }
    public TipoRelatorio getTipoRelatorio()              { return tipoRelatorio; }
    public SinaisVitais getSinaisVitais()                { return sinaisVitais; }
    public EvolucaoComparativa getEvolucaoComparativa()  { return evolucaoComparativa; }
    public String getDiagnosticoTecnico()                { return diagnosticoTecnico; }
    public String getOrientacoesManejo()                 { return orientacoesManejo; }
    public String getResumoParaTutor()                   { return resumoParaTutor; }
    public String getCuidadosPosOperatorios()            { return cuidadosPosOperatorios; }
    public String getTempoRecuperacaoEstimado()          { return tempoRecuperacaoEstimado; }
    public List<AnexoRelatorio> getAnexos()              { return Collections.unmodifiableList(anexos); }
    public List<String> getMedicamentosPrescritos()      { return Collections.unmodifiableList(medicamentosPrescritos); }
    public boolean isImutavel()                          { return imutavel; }
    public LocalDateTime getCriadoEm()                   { return criadoEm; }
    public LocalDateTime getAssinadoEm()                 { return assinadoEm; }
}
