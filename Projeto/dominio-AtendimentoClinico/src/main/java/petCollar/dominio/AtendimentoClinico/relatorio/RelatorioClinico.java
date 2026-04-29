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

    private SinaisVitais sinaisVitais;
    private EvolucaoComparativa evolucaoComparativa;
    private String diagnosticoTecnico;
    private String orientacoesManejo;
    private String resumoParaTutor;

    private final List<AnexoRelatorio> anexos;

    private boolean imutavel;
    private final LocalDateTime criadoEm;
    private LocalDateTime assinadoEm;

    // Construtor de CRIAÇÃO
    public RelatorioClinico(RelatorioClinicoId id,
                            AtendimentoId atendimentoId,
                            PacienteId pacienteId,
                            MedicoId medicoId) {
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
        this.anexos = new ArrayList<>();
        this.imutavel = false;
        this.criadoEm = LocalDateTime.now();
    }

    // Construtor de RECONSTRUÇÃO
    public RelatorioClinico(RelatorioClinicoId id,
                            AtendimentoId atendimentoId,
                            PacienteId pacienteId,
                            MedicoId medicoId,
                            SinaisVitais sinaisVitais,
                            EvolucaoComparativa evolucaoComparativa,
                            String diagnosticoTecnico,
                            String orientacoesManejo,
                            String resumoParaTutor,
                            List<AnexoRelatorio> anexos,
                            boolean imutavel,
                            LocalDateTime criadoEm,
                            LocalDateTime assinadoEm) {
        this.id = id;
        this.atendimentoId = atendimentoId;
        this.pacienteId = pacienteId;
        this.medicoId = medicoId;
        this.sinaisVitais = sinaisVitais;
        this.evolucaoComparativa = evolucaoComparativa;
        this.diagnosticoTecnico = diagnosticoTecnico;
        this.orientacoesManejo = orientacoesManejo;
        this.resumoParaTutor = resumoParaTutor;
        this.anexos = anexos != null ? new ArrayList<>(anexos) : new ArrayList<>();
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

    public void adicionarAnexo(AnexoRelatorio anexo) {
        verificarImutabilidade();
        if (anexo == null)
            throw new IllegalArgumentException("Anexo não pode ser nulo.");
        this.anexos.add(anexo);
    }

    public void assinarDigitalmente() {
        verificarImutabilidade();
        if (diagnosticoTecnico == null || diagnosticoTecnico.isBlank())
            throw new IllegalStateException(
                    "O diagnóstico técnico é obrigatório para assinar o relatório.");
        if (orientacoesManejo == null || orientacoesManejo.isBlank())
            throw new IllegalStateException(
                    "As orientações de manejo são obrigatórias para assinar o relatório.");
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
    public SinaisVitais getSinaisVitais()                { return sinaisVitais; }
    public EvolucaoComparativa getEvolucaoComparativa()  { return evolucaoComparativa; }
    public String getDiagnosticoTecnico()                { return diagnosticoTecnico; }
    public String getOrientacoesManejo()                 { return orientacoesManejo; }
    public String getResumoParaTutor()                   { return resumoParaTutor; }
    public List<AnexoRelatorio> getAnexos()              { return Collections.unmodifiableList(anexos); }
    public boolean isImutavel()                          { return imutavel; }
    public LocalDateTime getCriadoEm()                   { return criadoEm; }
    public LocalDateTime getAssinadoEm()                 { return assinadoEm; }
}
