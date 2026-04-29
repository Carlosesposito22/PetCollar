package petcollar.dominio.farmacovigilancia;

import java.util.List;

public interface IMatrizInteracaoRepositorio {
    void save(MatrizInteracao matrizInteracao);
    MatrizInteracao findById(MatrizInteracaoId id);
    List<MatrizInteracao> findByMedicamento(MedicamentoId medicamentoId);
}
