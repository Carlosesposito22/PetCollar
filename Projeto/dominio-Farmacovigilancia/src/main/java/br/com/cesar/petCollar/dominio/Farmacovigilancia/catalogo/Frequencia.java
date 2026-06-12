package br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo;

import java.util.List;

/**
 * Frequência diária de administração. Cada valor carrega a sugestão padrão de
 * horários (24h) usada pela RN 7 ({@code Escalonamento Horário Inteligente}).
 */
public enum Frequencia {
    UMA_VEZ_DIA(1,      List.of("08:00")),
    DUAS_VEZES_DIA(2,   List.of("08:00", "20:00")),
    TRES_VEZES_DIA(3,   List.of("08:00", "14:00", "20:00")),
    QUATRO_VEZES_DIA(4, List.of("06:00", "12:00", "18:00", "00:00")),
    A_CADA_8H(3,        List.of("08:00", "16:00", "00:00")),
    A_CADA_12H(2,       List.of("08:00", "20:00"));

    private final int dosesPorDia;
    private final List<String> horariosSugeridos;

    Frequencia(int dosesPorDia, List<String> horariosSugeridos) {
        this.dosesPorDia = dosesPorDia;
        this.horariosSugeridos = horariosSugeridos;
    }

    public int dosesPorDia()              { return dosesPorDia; }
    public List<String> horariosSugeridos() { return horariosSugeridos; }
}
