package br.com.cesar.petCollar.infraestrutura.Farmacovigilancia;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.Frequencia;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.MedicamentoId;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.ViaAdministracao;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao.AssinaturaDigitalPrescricao;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao.HorarioAdministracao;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao.ItemPrescricao;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao.Prescricao;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao.PrescricaoId;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao.StatusPrescricao;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao.TagClinica;
import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "prescricoes_farmacovigilancia")
public class PrescricaoJpa {

    @Id
    private String id;

    @Column(nullable = false) private String pacienteId;
    @Column(nullable = false) private String tutorId;
    @Column(nullable = false) private String medicoResponsavelId;
    @Column(nullable = false, precision = 8, scale = 2) private BigDecimal pesoPacienteKg;

    @Column(nullable = false, columnDefinition = "TEXT") private String itensTexto;

    @Column(nullable = false, columnDefinition = "TEXT") private String nomesItens;

    @Column(nullable = false, columnDefinition = "TEXT") private String instrucoesTexto;

    @Column(nullable = false) private String tagsClinicas;

    @Column(nullable = false, columnDefinition = "TEXT") private String alergiasConsideradas;

    @Column(nullable = false) private String status;
    @Column(nullable = false) private LocalDate dataInicio;
    @Column(nullable = false) private LocalDate dataFim;
    @Column(nullable = false) private LocalDateTime criadoEm;
    @Column(nullable = false) private LocalDateTime atualizadoEm;

    @Column(nullable = false) private String assinadoPorMedicoId;
    @Column(nullable = false, columnDefinition = "TEXT") private String assinaturaImagemBase64;
    @Column(nullable = false) private LocalDateTime assinadoEm;
    @Column(nullable = false) private String assinaturaHash;

    protected PrescricaoJpa() {}

    public static PrescricaoJpa fromDomain(Prescricao p) {
        PrescricaoJpa j = new PrescricaoJpa();
        j.id = p.getId().getValor();
        j.pacienteId = p.getPacienteId().getValor();
        j.tutorId = p.getTutorId().getValor();
        j.medicoResponsavelId = p.getMedicoResponsavel().getValor();
        j.pesoPacienteKg = p.getPesoPacienteKg();
        j.itensTexto = serializarItens(p.getItens());
        j.nomesItens = p.getItens().stream()
                .map(it -> it.nomeMedicamento().replace("|", "/"))
                .collect(Collectors.joining("|"));
        j.instrucoesTexto = String.join("||", p.getInstrucoesGerais());
        j.tagsClinicas = p.getTagsClinicas().stream().map(Enum::name).collect(Collectors.joining(","));
        j.alergiasConsideradas = String.join("|", p.getAlergiasConsideradas());
        j.status = p.getStatus().name();
        j.dataInicio = p.getDataInicio();
        j.dataFim = p.getDataFim();
        j.criadoEm = p.getCriadoEm();
        j.atualizadoEm = p.getAtualizadoEm();
        AssinaturaDigitalPrescricao a = p.getAssinatura();
        j.assinadoPorMedicoId = a.medicoResponsavel().getValor();
        j.assinaturaImagemBase64 = a.imagemBase64();
        j.assinadoEm = a.assinadoEm();
        j.assinaturaHash = a.hashConteudo();
        return j;
    }

    public Prescricao toDomain() {
        List<ItemPrescricao> itens = desserializarItens(itensTexto, nomesItens);
        List<String> instrucoes = instrucoesTexto.isBlank() ? List.of()
                : new ArrayList<>(Arrays.asList(instrucoesTexto.split("\\|\\|")));
        Set<TagClinica> tags = tagsClinicas.isBlank() ? EnumSet.noneOf(TagClinica.class)
                : Arrays.stream(tagsClinicas.split(",")).map(String::trim).filter(s -> !s.isBlank())
                    .map(TagClinica::valueOf)
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(TagClinica.class)));
        List<String> alergias = alergiasConsideradas.isBlank() ? List.of()
                : new ArrayList<>(Arrays.asList(alergiasConsideradas.split("\\|")));
        AssinaturaDigitalPrescricao assinatura = new AssinaturaDigitalPrescricao(
                MedicoId.de(assinadoPorMedicoId), assinaturaImagemBase64, assinadoEm, assinaturaHash);

        return new Prescricao(
                PrescricaoId.de(id),
                PacienteId.de(pacienteId),
                TutorId.de(tutorId),
                MedicoId.de(medicoResponsavelId),
                pesoPacienteKg,
                itens, instrucoes, tags, alergias,
                StatusPrescricao.valueOf(status), assinatura,
                dataInicio, dataFim, criadoEm, atualizadoEm);
    }

    private static String serializarItens(List<ItemPrescricao> itens) {
        StringBuilder sb = new StringBuilder();
        for (ItemPrescricao it : itens) {
            if (sb.length() > 0) sb.append('|');
            String horarios = it.horarios().stream().map(HorarioAdministracao::valor)
                    .collect(Collectors.joining(","));
            String nota = it.notaCuidado() == null ? "" : it.notaCuidado().replace("#", " ").replace("|", "/");

            sb.append(it.medicamentoId().getValor()).append('~')
              .append(it.doseMgPorKg()).append('~')
              .append(it.doseTotalMg()).append('~')
              .append(it.volumeFinalMl()).append('~')
              .append(it.duracaoDias()).append('~')
              .append(it.frequencia().name()).append('~')
              .append(it.via().name()).append('~')
              .append(horarios).append('#')
              .append(nota);
        }
        return sb.toString();
    }

    private static List<ItemPrescricao> desserializarItens(String texto, String nomesTexto) {
        if (texto == null || texto.isBlank()) return List.of();
        String[] partes = texto.split("\\|");
        String[] nomes = nomesTexto == null || nomesTexto.isBlank()
                ? new String[partes.length] : nomesTexto.split("\\|");
        List<ItemPrescricao> out = new ArrayList<>();
        for (int idx = 0; idx < partes.length; idx++) {
            String[] hashSplit = partes[idx].split("#", 2);
            String campos = hashSplit[0];
            String nota = hashSplit.length > 1 ? hashSplit[1] : "";
            String[] c = campos.split("~");
            List<HorarioAdministracao> horarios = c[7].isBlank() ? List.of()
                    : Arrays.stream(c[7].split(",")).map(HorarioAdministracao::new).toList();
            String nome = idx < nomes.length && nomes[idx] != null ? nomes[idx] : "Medicamento";
            out.add(new ItemPrescricao(
                    MedicamentoId.de(c[0]),
                    nome,
                    new BigDecimal(c[1]), new BigDecimal(c[2]), new BigDecimal(c[3]),
                    Integer.parseInt(c[4]),
                    Frequencia.valueOf(c[5]),
                    ViaAdministracao.valueOf(c[6]),
                    horarios,
                    nota.isBlank() ? null : nota));
        }
        return out;
    }

    public String getId()                  { return id; }
    public String getPacienteId()          { return pacienteId; }
    public String getTutorId()             { return tutorId; }
    public String getStatus()              { return status; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
}
