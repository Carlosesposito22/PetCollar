package petcollar.dominio.recepcaotriagem.paciente;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class QueixaComportamento {

    private final String motivoConsulta;
    private final List<AlertaComportamental> alertas;

    public QueixaComportamento(String motivoConsulta, List<AlertaComportamental> alertas) {
        if (motivoConsulta == null || motivoConsulta.isBlank())
            throw new IllegalArgumentException("Motivo da consulta não pode ser vazio.");
        if (alertas == null)
            throw new IllegalArgumentException("Lista de alertas não pode ser nula.");
        this.motivoConsulta = motivoConsulta;
        this.alertas = new ArrayList<>(alertas);
    }

    public String getMotivoConsulta()                { return motivoConsulta; }
    public List<AlertaComportamental> getAlertas()   { return Collections.unmodifiableList(alertas); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QueixaComportamento)) return false;
        QueixaComportamento other = (QueixaComportamento) o;
        return Objects.equals(motivoConsulta, other.motivoConsulta)
            && Objects.equals(alertas, other.alertas);
    }

    @Override
    public int hashCode() { return Objects.hash(motivoConsulta, alertas); }
}
