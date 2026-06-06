package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo;

import java.util.Objects;

/**
 * Value Object que descreve por que um {@link ProtocoloInacessibilidade} foi
 * encerrado: o {@link TipoEncerramento} (sucesso com tutor, sucesso com
 * secundário, esgotamento ou intervenção manual) e um detalhe textual livre.
 */
public final class MotivoEncerramento {

    private final TipoEncerramento tipo;
    private final String detalhes;

    public MotivoEncerramento(TipoEncerramento tipo, String detalhes) {
        if (tipo == null)
            throw new IllegalArgumentException("Tipo de encerramento não pode ser nulo.");
        this.tipo = tipo;
        this.detalhes = detalhes == null ? "" : detalhes;
    }

    public static MotivoEncerramento sucessoComTutor(String detalhes) {
        return new MotivoEncerramento(TipoEncerramento.SUCESSO_TUTOR, detalhes);
    }

    public static MotivoEncerramento sucessoComSecundario(String detalhes) {
        return new MotivoEncerramento(TipoEncerramento.SUCESSO_SECUNDARIO, detalhes);
    }

    public static MotivoEncerramento esgotamento(String detalhes) {
        return new MotivoEncerramento(TipoEncerramento.ESGOTAMENTO, detalhes);
    }

    public static MotivoEncerramento intervencaoManual(String detalhes) {
        return new MotivoEncerramento(TipoEncerramento.INTERVENCAO_MANUAL, detalhes);
    }

    public TipoEncerramento getTipo() { return tipo; }
    public String getDetalhes()       { return detalhes; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MotivoEncerramento)) return false;
        MotivoEncerramento outro = (MotivoEncerramento) o;
        return tipo == outro.tipo && Objects.equals(detalhes, outro.detalhes);
    }

    @Override
    public int hashCode() { return Objects.hash(tipo, detalhes); }

    @Override
    public String toString() { return tipo + (detalhes.isBlank() ? "" : " — " + detalhes); }
}
