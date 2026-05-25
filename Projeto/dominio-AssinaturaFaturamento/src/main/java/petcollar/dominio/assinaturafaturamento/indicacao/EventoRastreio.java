package petcollar.dominio.assinaturafaturamento.indicacao;

import java.time.LocalDateTime;

public final class EventoRastreio {

    private final TipoEventoRastreio tipo;
    private final LocalDateTime ocorridoEm;
    private final String metadados;

    public EventoRastreio(TipoEventoRastreio tipo, String metadados) {
        if (tipo == null)
            throw new IllegalArgumentException("Tipo do evento de rastreio não pode ser nulo.");
        this.tipo = tipo;
        this.ocorridoEm = LocalDateTime.now();
        this.metadados = metadados != null ? metadados : "{}";
    }

    public EventoRastreio(TipoEventoRastreio tipo, LocalDateTime ocorridoEm, String metadados) {
        if (tipo == null)
            throw new IllegalArgumentException("Tipo do evento de rastreio não pode ser nulo.");
        if (ocorridoEm == null)
            throw new IllegalArgumentException("ocorridoEm não pode ser nulo.");
        this.tipo = tipo;
        this.ocorridoEm = ocorridoEm;
        this.metadados = metadados != null ? metadados : "{}";
    }

    public TipoEventoRastreio getTipo()     { return tipo; }
    public LocalDateTime getOcorridoEm()   { return ocorridoEm; }
    public String getMetadados()           { return metadados; }
}
