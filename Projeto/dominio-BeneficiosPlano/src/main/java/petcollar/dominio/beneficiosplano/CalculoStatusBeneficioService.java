package petcollar.dominio.beneficiosplano;

import java.time.LocalDateTime;

public class CalculoStatusBeneficioService {

    public StatusBeneficio calcularStatus(BeneficioTutor beneficioTutor, LocalDateTime agora) {
        if (beneficioTutor == null) {
            throw new IllegalArgumentException("BeneficioTutor não pode ser nulo.");
        }
        if (agora == null) {
            throw new IllegalArgumentException("A data atual não pode ser nula.");
        }

        beneficioTutor.reiniciarPeriodoSeNecessario(agora);
        return beneficioTutor.recalcularStatus(agora);
    }
}

