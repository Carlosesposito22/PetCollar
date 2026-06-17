package br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo;

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
