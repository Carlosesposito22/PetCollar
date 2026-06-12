package br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo;

/**
 * Recomendação de relação com a alimentação na hora da administração — usada
 * pelo escalonamento de horários (RN 7) para evitar combinar fármacos com
 * exigências conflitantes (ex.: um em jejum + outro com alimento no mesmo horário).
 */
public enum ManejoAlimentar {
    JEJUM,
    COM_ALIMENTO,
    INDIFERENTE;

    public boolean conflitaCom(ManejoAlimentar outro) {
        if (outro == null) return false;
        return (this == JEJUM && outro == COM_ALIMENTO)
                || (this == COM_ALIMENTO && outro == JEJUM);
    }
}
