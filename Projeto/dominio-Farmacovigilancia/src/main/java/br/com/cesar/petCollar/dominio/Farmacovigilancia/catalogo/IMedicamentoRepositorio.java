package br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo;

import java.util.List;
import java.util.Optional;

public interface IMedicamentoRepositorio {

    void salvar(Medicamento medicamento);

    Optional<Medicamento> buscarPorId(MedicamentoId id);

    List<Medicamento> listarTodos();

    long contar();

    void registrarInteracao(InteracaoMedicamentosa interacao);

    List<InteracaoMedicamentosa> buscarInteracoesEntre(List<MedicamentoId> medicamentos);
}
