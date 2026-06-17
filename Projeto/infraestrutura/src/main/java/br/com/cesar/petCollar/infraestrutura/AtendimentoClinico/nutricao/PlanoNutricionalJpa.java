package br.com.cesar.petCollar.infraestrutura.AtendimentoClinico.nutricao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.parametros.Comorbidade;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.parametros.NivelAtividade;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.AssinaturaDigital;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.CronogramaTransicao;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.DiaTransicao;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.ObservacaoNutricional;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.ParametrosPaciente;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.PlanoNutricional;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.PlanoNutricionalId;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.ResultadoNEM;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.StatusPlanoNutricional;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano.TipoCronograma;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.RacaoId;
import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "planos_nutricionais")
public class PlanoNutricionalJpa {

    @Id
    private String id;

    @Column(nullable = false) private String pacienteId;
    @Column(nullable = false) private String tutorId;
    @Column(nullable = false) private String medicoResponsavelId;

    @Column(nullable = false, precision = 8, scale = 2) private BigDecimal pesoAtualKg;
    @Column(nullable = false, precision = 8, scale = 2) private BigDecimal pesoIdealKg;
    @Column(nullable = false) private String nivelAtividade;
    @Column(nullable = false) private String comorbidade;
    @Column(nullable = false, precision = 8, scale = 2) private BigDecimal densidadeCaloricaKcalPorKg;

    @Column(nullable = false) private String tipoCronograma;

    @Column(nullable = false, columnDefinition = "TEXT") private String cronogramaDias;

    @Column(nullable = false, columnDefinition = "TEXT") private String observacoesTexto;

    @Column(nullable = false) private String status;
    @Column(nullable = false) private LocalDateTime criadoEm;
    @Column(nullable = false) private LocalDateTime atualizadoEm;

    private BigDecimal pesoMetabolico;
    private BigDecimal nemBase;
    private BigDecimal fatorAtividade;
    private BigDecimal modificadorComorbidade;
    private BigDecimal nemTotal;
    private BigDecimal quantidadeRecomendadaGramasDia;

    private String assinadoPorMedicoId;
    @Column(columnDefinition = "TEXT") private String assinaturaImagemBase64;
    private LocalDateTime assinadoEm;
    private String assinaturaHash;

    private String racaoId;
    @Column(columnDefinition = "TEXT") private String justificativaDivergencia;

    protected PlanoNutricionalJpa() {}

    public static PlanoNutricionalJpa fromDomain(PlanoNutricional p) {
        PlanoNutricionalJpa j = new PlanoNutricionalJpa();
        j.id = p.getId().getValor();
        j.pacienteId = p.getPacienteId().getValor();
        j.tutorId = p.getTutorId().getValor();
        j.medicoResponsavelId = p.getMedicoResponsavel().getValor();

        ParametrosPaciente par = p.getParametros();
        j.pesoAtualKg = par.pesoAtualKg();
        j.pesoIdealKg = par.pesoIdealKg();
        j.nivelAtividade = par.nivelAtividade().name();
        j.comorbidade = par.comorbidade().name();
        j.densidadeCaloricaKcalPorKg = par.densidadeCaloricaKcalPorKg();

        CronogramaTransicao cron = p.getCronograma();
        j.tipoCronograma = cron.tipo().name();
        j.cronogramaDias = serializarDias(cron.dias());
        j.observacoesTexto = serializarObservacoes(p.getObservacoes());

        j.status = p.getStatus().name();
        j.criadoEm = p.getCriadoEm();
        j.atualizadoEm = p.getAtualizadoEm();

        if (p.getResultadoFinalizado() != null) {
            ResultadoNEM r = p.getResultadoFinalizado();
            j.pesoMetabolico = r.pesoMetabolico();
            j.nemBase = r.nemBase();
            j.fatorAtividade = r.fatorAtividade();
            j.modificadorComorbidade = r.modificadorComorbidade();
            j.nemTotal = r.nemTotal();
            j.quantidadeRecomendadaGramasDia = r.quantidadeRecomendadaGramasPorDia();
        }
        if (p.getAssinatura() != null) {
            AssinaturaDigital a = p.getAssinatura();
            j.assinadoPorMedicoId = a.medicoResponsavel().getValor();
            j.assinaturaImagemBase64 = a.imagemBase64();
            j.assinadoEm = a.assinadoEm();
            j.assinaturaHash = a.hashConteudo();
        }
        j.racaoId = p.getRacaoId() == null ? null : p.getRacaoId().getValor();
        j.justificativaDivergencia = p.getJustificativaDivergencia();
        return j;
    }

    public PlanoNutricional toDomain() {
        ParametrosPaciente par = new ParametrosPaciente(
                pesoAtualKg, pesoIdealKg,
                NivelAtividade.valueOf(nivelAtividade),
                Comorbidade.valueOf(comorbidade),
                densidadeCaloricaKcalPorKg);

        CronogramaTransicao cron = new CronogramaTransicao(
                TipoCronograma.valueOf(tipoCronograma), desserializarDias(cronogramaDias));

        ResultadoNEM resultado = nemTotal == null ? null : new ResultadoNEM(
                pesoMetabolico, nemBase, fatorAtividade,
                modificadorComorbidade, nemTotal, quantidadeRecomendadaGramasDia);

        AssinaturaDigital assinatura = assinadoEm == null ? null : new AssinaturaDigital(
                MedicoId.de(assinadoPorMedicoId),
                assinaturaImagemBase64, assinadoEm, assinaturaHash);

        return new PlanoNutricional(
                PlanoNutricionalId.de(id),
                PacienteId.de(pacienteId),
                TutorId.de(tutorId),
                MedicoId.de(medicoResponsavelId),
                par, cron,
                desserializarObservacoes(observacoesTexto),
                StatusPlanoNutricional.valueOf(status),
                criadoEm, atualizadoEm,
                resultado, assinatura,
                racaoId == null ? null : RacaoId.de(racaoId),
                justificativaDivergencia);
    }

    private static String serializarDias(List<DiaTransicao> dias) {
        StringBuilder sb = new StringBuilder();
        for (DiaTransicao d : dias) {
            if (sb.length() > 0) sb.append('|');
            sb.append(d.faixaDias()).append(':')
              .append(d.percentualRacaoAtual()).append(':')
              .append(d.percentualRacaoNova());
        }
        return sb.toString();
    }

    private static List<DiaTransicao> desserializarDias(String texto) {
        if (texto == null || texto.isBlank()) return Collections.emptyList();
        List<DiaTransicao> out = new ArrayList<>();
        for (String parte : texto.split("\\|")) {
            String[] campos = parte.split(":");
            out.add(new DiaTransicao(campos[0], Integer.parseInt(campos[1]), Integer.parseInt(campos[2])));
        }
        return out;
    }

    private static String serializarObservacoes(List<ObservacaoNutricional> observacoes) {
        StringBuilder sb = new StringBuilder();
        for (ObservacaoNutricional o : observacoes) {
            if (sb.length() > 0) sb.append('|');
            sb.append(o.texto().replace("|", "/"));
        }
        return sb.toString();
    }

    private static List<ObservacaoNutricional> desserializarObservacoes(String texto) {
        if (texto == null || texto.isBlank()) return Collections.emptyList();
        List<ObservacaoNutricional> out = new ArrayList<>();
        for (String parte : texto.split("\\|"))
            if (!parte.isBlank()) out.add(new ObservacaoNutricional(parte));
        return out;
    }

    public String getId() { return id; }
    public String getPacienteId() { return pacienteId; }
    public String getTutorId() { return tutorId; }
    public String getStatus() { return status; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
}
