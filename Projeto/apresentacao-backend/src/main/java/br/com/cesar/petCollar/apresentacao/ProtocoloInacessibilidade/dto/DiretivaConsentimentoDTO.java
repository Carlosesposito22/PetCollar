package br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade.dto;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.TipoConduta;

public record DiretivaConsentimentoDTO(String conduta, String rotulo, boolean autorizado) {

    public static DiretivaConsentimentoDTO autorizada(TipoConduta conduta) {
        return new DiretivaConsentimentoDTO(conduta.name(), rotuloDeConduta(conduta), true);
    }

    public static DiretivaConsentimentoDTO bloqueada(TipoConduta conduta) {
        return new DiretivaConsentimentoDTO(conduta.name(), rotuloDeConduta(conduta), false);
    }

    private static String rotuloDeConduta(TipoConduta conduta) {
        return switch (conduta) {
            case PROCEDIMENTO_INVASIVO   -> "Procedimento invasivo";
            case MEDICACAO_CONTROLADA    -> "Medicação controlada";
            case INTERNACAO              -> "Internação";
            case PROCEDIMENTO_ELETIVO    -> "Procedimento eletivo";
            case EUTANASIA               -> "Eutanásia";
        };
    }
}
