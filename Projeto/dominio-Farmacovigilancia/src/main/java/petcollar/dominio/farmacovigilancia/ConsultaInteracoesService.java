package petcollar.dominio.farmacovigilancia;

import java.util.ArrayList;
import java.util.List;

/**
 * RN-141
 * Verifica interações entre medicamentos da prescrição.
 * Se alguma interação bloqueante for detectada, trava todos os itens envolvidos
 * e muda o status da prescrição para TRAVADA_AGUARDANDO_REVISAO.
 */
public class ConsultaInteracoesService {

    private final IMatrizInteracaoRepositorio matrizRepositorio;

    public ConsultaInteracoesService(IMatrizInteracaoRepositorio matrizRepositorio) {
        this.matrizRepositorio = matrizRepositorio;
    }

    public List<RegraInteracao> verificar(Prescricao prescricao) {
        if (prescricao == null)
            throw new IllegalArgumentException("prescricao não pode ser nula.");

        List<ItemPrescricao> itens = prescricao.getItens();
        List<RegraInteracao> interacoesDetectadas = new ArrayList<>();
        boolean possuiBloqueante = false;

        for (int i = 0; i < itens.size(); i++) {
            for (int j = i + 1; j < itens.size(); j++) {
                MedicamentoId idA = itens.get(i).getMedicamentoId();
                MedicamentoId idB = itens.get(j).getMedicamentoId();

                List<MatrizInteracao> entradas = matrizRepositorio.findByMedicamento(idA);
                for (MatrizInteracao entrada : entradas) {
                    RegraInteracao regra = entrada.getRegra();
                    boolean envolveParA = regra.getMedicamentoA().equals(idA) && regra.getMedicamentoB().equals(idB);
                    boolean envolveParB = regra.getMedicamentoA().equals(idB) && regra.getMedicamentoB().equals(idA);
                    if (envolveParA || envolveParB) {
                        interacoesDetectadas.add(regra);
                        if (regra.isBloqueante()) {
                            possuiBloqueante = true;
                        }
                    }
                }
            }
        }

        if (possuiBloqueante) {
            for (ItemPrescricao item : itens) {
                item.travarPorInteracao();
            }
            prescricao.travarPorInteracao();
        }

        return interacoesDetectadas;
    }
}
