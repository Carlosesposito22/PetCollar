package br.com.cesar.petCollar.apresentacao.PortalTutor;

import java.time.LocalDate;
import java.time.YearMonth;

import org.springframework.stereotype.Service;

/**
 * Inicializa o estado financeiro do Portal do Tutor quando o pagamento da contratação é
 * confirmado: cria a 1ª mensalidade já paga (representando o boleto inicial).
 *
 * A próxima fatura NÃO é pré-criada — ela só vira PENDENTE perto do vencimento. Até lá,
 * o tutor enxerga apenas o histórico de pagas e o "Próximo Vencimento" projetado pelo
 * FinanceiroController.
 *
 * Idempotente: se o tutor já tiver mensalidades registradas, não faz nada.
 */
@Service
public class PortalTutorBootstrap {

    private final PortalTutorRepositorio repositorio;

    public PortalTutorBootstrap(PortalTutorRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public void inicializarFinanceiroDoTutor(String tutorId) {
        if (!repositorio.listarMensalidadesDoTutor(tutorId).isEmpty()) return;

        LocalDate hoje = LocalDate.now();
        Plano plano = repositorio.planoDoTutor(tutorId);

        // 1ª mensalidade — paga hoje (representa o boleto inicial da contratação).
        repositorio.salvarMensalidade(new Mensalidade(
                repositorio.novoId(), tutorId,
                YearMonth.from(hoje),
                plano.valor(), null,
                hoje, hoje
        ));
    }
}
