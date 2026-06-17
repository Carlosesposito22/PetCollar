package br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

public final class Medicamento {

    private final MedicamentoId id;
    private final String nome;
    private final BigDecimal doseMaximaMgPorKg;
    private final BigDecimal concentracaoMgPorMl;
    private final Set<ViaAdministracao> viasPermitidas;

    private final Set<String> componentes;

    private final ManejoAlimentar manejoAlimentar;
    private final String notaCuidado;

    public Medicamento(MedicamentoId id, String nome,
                       BigDecimal doseMaximaMgPorKg, BigDecimal concentracaoMgPorMl,
                       Set<ViaAdministracao> viasPermitidas,
                       Set<String> componentes,
                       ManejoAlimentar manejoAlimentar,
                       String notaCuidado) {
        if (id == null) throw new IllegalArgumentException("Id é obrigatório.");
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome do medicamento é obrigatório.");
        if (doseMaximaMgPorKg == null || doseMaximaMgPorKg.signum() <= 0)
            throw new IllegalArgumentException("Dose máxima por kg deve ser positiva.");
        if (concentracaoMgPorMl == null || concentracaoMgPorMl.signum() <= 0)
            throw new IllegalArgumentException("Concentração mg/ml deve ser positiva.");
        if (viasPermitidas == null || viasPermitidas.isEmpty())
            throw new IllegalArgumentException("Pelo menos uma via deve ser permitida.");
        if (manejoAlimentar == null)
            throw new IllegalArgumentException("Manejo alimentar é obrigatório.");

        this.id = id;
        this.nome = nome;
        this.doseMaximaMgPorKg = doseMaximaMgPorKg;
        this.concentracaoMgPorMl = concentracaoMgPorMl;
        this.viasPermitidas = Collections.unmodifiableSet(EnumSet.copyOf(viasPermitidas));
        this.componentes = componentes == null || componentes.isEmpty()
                ? Set.of()
                : Collections.unmodifiableSet(new LinkedHashSet<>(componentes));
        this.manejoAlimentar = manejoAlimentar;
        this.notaCuidado = notaCuidado;
    }

    public boolean viaPermitida(ViaAdministracao via)            { return viasPermitidas.contains(via); }
    public boolean compartilhaComponentesCom(Set<String> outros) {
        if (outros == null || outros.isEmpty() || componentes.isEmpty()) return false;
        for (String comp : componentes) {
            for (String alvo : outros)
                if (comp.equalsIgnoreCase(alvo)) return true;
        }
        return false;
    }

    public MedicamentoId getId()                            { return id; }
    public String getNome()                                 { return nome; }
    public BigDecimal getDoseMaximaMgPorKg()                { return doseMaximaMgPorKg; }
    public BigDecimal getConcentracaoMgPorMl()              { return concentracaoMgPorMl; }
    public Set<ViaAdministracao> getViasPermitidas()        { return viasPermitidas; }
    public Set<String> getComponentes()                     { return componentes; }
    public ManejoAlimentar getManejoAlimentar()             { return manejoAlimentar; }
    public String getNotaCuidado()                          { return notaCuidado; }
}
