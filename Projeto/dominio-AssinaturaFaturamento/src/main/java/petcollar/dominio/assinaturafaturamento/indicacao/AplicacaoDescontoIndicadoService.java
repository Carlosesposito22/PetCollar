package petcollar.dominio.assinaturafaturamento.indicacao;

public class AplicacaoDescontoIndicadoService {

    public double resolver(RecompensaIndicacao recompensa, double percentualCupomExistente) {
        double percentualIndicacao = recompensa.getDescontoIndicado().getPercentualDesconto();
        if (percentualIndicacao >= percentualCupomExistente) {
            recompensa.aplicarDescontoIndicado();
            return percentualIndicacao;
        }
        return percentualCupomExistente;
    }
}
