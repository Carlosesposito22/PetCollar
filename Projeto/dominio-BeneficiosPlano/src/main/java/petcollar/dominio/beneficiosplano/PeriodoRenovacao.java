package petcollar.dominio.beneficiosplano;

import java.time.LocalDateTime;

public enum PeriodoRenovacao {
    MENSAL,
    TRIMESTRAL,
    ANUAL;

    public LocalDateTime adicionarA(LocalDateTime dataBase) {
        return switch (this) {
            case MENSAL -> dataBase.plusMonths(1);
            case TRIMESTRAL -> dataBase.plusMonths(3);
            case ANUAL -> dataBase.plusYears(1);
        };
    }
}

