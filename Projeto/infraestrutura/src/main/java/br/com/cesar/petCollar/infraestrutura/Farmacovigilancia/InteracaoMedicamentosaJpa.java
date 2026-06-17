package br.com.cesar.petCollar.infraestrutura.Farmacovigilancia;

import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.InteracaoMedicamentosa;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.MedicamentoId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "interacoes_medicamentosas")
public class InteracaoMedicamentosaJpa {

    @Id
    private String id;

    @Column(nullable = false) private String medicamentoAId;
    @Column(nullable = false) private String medicamentoBId;
    @Column(nullable = false) private String gravidade;
    @Column(nullable = false, columnDefinition = "TEXT") private String descricao;

    protected InteracaoMedicamentosaJpa() {}

    public static InteracaoMedicamentosaJpa fromDomain(InteracaoMedicamentosa i) {
        String a = i.medicamentoA().getValor();
        String b = i.medicamentoB().getValor();
        String menor = a.compareTo(b) <= 0 ? a : b;
        String maior = a.compareTo(b) <= 0 ? b : a;
        InteracaoMedicamentosaJpa j = new InteracaoMedicamentosaJpa();
        j.id = menor + "|" + maior;
        j.medicamentoAId = menor;
        j.medicamentoBId = maior;
        j.gravidade = i.gravidade().name();
        j.descricao = i.descricao();
        return j;
    }

    public InteracaoMedicamentosa toDomain() {
        return new InteracaoMedicamentosa(
                MedicamentoId.de(medicamentoAId),
                MedicamentoId.de(medicamentoBId),
                InteracaoMedicamentosa.Gravidade.valueOf(gravidade),
                descricao);
    }

    public String getMedicamentoAId() { return medicamentoAId; }
    public String getMedicamentoBId() { return medicamentoBId; }
}
