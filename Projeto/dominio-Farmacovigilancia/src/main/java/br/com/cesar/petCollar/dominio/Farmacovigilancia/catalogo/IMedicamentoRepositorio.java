package br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo;

import java.util.List;
import java.util.Optional;

/**
 * Porta de saída do catálogo de medicamentos + matriz de interação.
 * A implementação concreta vive na infra ({@code MedicamentoRepositorioJpa})
 * e é populada via seed no boot do backend.
 */
public interface IMedicamentoRepositorio {

    void salvar(Medicamento medicamento);

    Optional<Medicamento> buscarPorId(MedicamentoId id);

    List<Medicamento> listarTodos();

    long contar();

    void registrarInteracao(InteracaoMedicamentosa interacao);

    /** Interações graves/moderadas que envolvem PELO MENOS um dos medicamentos da lista. */
    List<InteracaoMedicamentosa> buscarInteracoesEntre(List<MedicamentoId> medicamentos);
}
