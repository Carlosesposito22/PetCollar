package br.com.cesar.petCollar.infraestrutura.Farmacovigilancia;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.ManejoAlimentar;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.Medicamento;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.MedicamentoId;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.ViaAdministracao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "medicamentos")
public class MedicamentoJpa {

    @Id
    private String id;

    @Column(nullable = false) private String nome;
    @Column(nullable = false, precision = 10, scale = 3) private BigDecimal doseMaximaMgPorKg;
    @Column(nullable = false, precision = 10, scale = 3) private BigDecimal concentracaoMgPorMl;
    @Column(nullable = false) private String viasPermitidas;
    @Column(nullable = false) private String componentes;
    @Column(nullable = false) private String manejoAlimentar;
    @Column(columnDefinition = "TEXT") private String notaCuidado;

    protected MedicamentoJpa() {}

    public static MedicamentoJpa fromDomain(Medicamento m) {
        MedicamentoJpa j = new MedicamentoJpa();
        j.id = m.getId().getValor();
        j.nome = m.getNome();
        j.doseMaximaMgPorKg = m.getDoseMaximaMgPorKg();
        j.concentracaoMgPorMl = m.getConcentracaoMgPorMl();
        j.viasPermitidas = m.getViasPermitidas().stream().map(Enum::name)
                .collect(Collectors.joining(","));
        j.componentes = String.join("|", m.getComponentes());
        j.manejoAlimentar = m.getManejoAlimentar().name();
        j.notaCuidado = m.getNotaCuidado();
        return j;
    }

    public Medicamento toDomain() {
        Set<ViaAdministracao> vias = Arrays.stream(viasPermitidas.split(","))
                .map(String::trim).filter(s -> !s.isBlank())
                .map(ViaAdministracao::valueOf)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(ViaAdministracao.class)));
        Set<String> comps = componentes == null || componentes.isBlank()
                ? Set.of()
                : new LinkedHashSet<>(Arrays.asList(componentes.split("\\|")));
        return new Medicamento(
                MedicamentoId.de(id), nome,
                doseMaximaMgPorKg, concentracaoMgPorMl,
                vias, comps,
                ManejoAlimentar.valueOf(manejoAlimentar), notaCuidado);
    }
}
