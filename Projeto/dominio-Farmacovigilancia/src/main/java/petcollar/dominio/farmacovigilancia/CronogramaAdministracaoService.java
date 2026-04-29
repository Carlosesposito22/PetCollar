package petcollar.dominio.farmacovigilancia;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * RN-143, RN-144, RN-145
 * Calcula a data de fim do tratamento e gera os slots horários de administração.
 */
public class CronogramaAdministracaoService {

    private final IMedicamentoRepositorio medicamentoRepositorio;

    public CronogramaAdministracaoService(IMedicamentoRepositorio medicamentoRepositorio) {
        this.medicamentoRepositorio = medicamentoRepositorio;
    }

    public LocalDate calcularDataFimTratamento(Prescricao prescricao) {
        if (prescricao == null)
            throw new IllegalArgumentException("prescricao não pode ser nula.");
        if (prescricao.getItens().isEmpty())
            throw new IllegalArgumentException("Prescrição não possui itens.");

        return prescricao.getItens().stream()
                .map(item -> item.getDataInicioUso().toLocalDate().plusDays(item.getDuracaoDias()))
                .max(LocalDate::compareTo)
                .orElseThrow(() -> new IllegalArgumentException("Não foi possível calcular a data de fim."));
    }

    public CronogramaAdministracao gerarCronograma(Prescricao prescricao,
                                                    List<String> horariosAdministracao) {
        if (prescricao == null)
            throw new IllegalArgumentException("prescricao não pode ser nula.");
        if (horariosAdministracao == null || horariosAdministracao.isEmpty())
            throw new IllegalArgumentException("horariosAdministracao não pode ser vazio.");

        List<SlotHorario> slots = new ArrayList<>();

        for (ItemPrescricao item : prescricao.getItens()) {
            Medicamento medicamento = medicamentoRepositorio.findById(item.getMedicamentoId());
            if (medicamento == null)
                throw new IllegalArgumentException(
                    "Medicamento não encontrado para o item: " + item.getId());

            double volumeMl = item.getDosePrescritaMg() / medicamento.getConcentracaoMgPorMl();
            String notaCuidado = medicamento.getRestricaoManejo() != RestricaoManejo.NENHUMA
                    ? medicamento.getRestricaoManejo().name()
                    : null;

            for (String horario : horariosAdministracao) {
                slots.add(new SlotHorario(horario, item.getId(), volumeMl, notaCuidado));
            }
        }

        return new CronogramaAdministracao(slots);
    }
}
