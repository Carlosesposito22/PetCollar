package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.etapa;

import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.CanalContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ResponsavelSecundario;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.TipoDestinatario;

import java.util.List;
import java.util.Objects;

public final class DestinatarioEtapa {

    private final String id;
    private final TipoDestinatario tipo;
    private final List<CanalContato> canaisPreferenciais;

    private DestinatarioEtapa(String id, TipoDestinatario tipo, List<CanalContato> canaisPreferenciais) {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("Id do destinatário não pode ser vazio.");
        if (tipo == null)
            throw new IllegalArgumentException("Tipo do destinatário não pode ser nulo.");
        this.id = id;
        this.tipo = tipo;
        this.canaisPreferenciais = canaisPreferenciais == null ? List.of() : List.copyOf(canaisPreferenciais);
    }

    public static DestinatarioEtapa deTutor(TutorId tutorId) {
        return new DestinatarioEtapa(tutorId.getValor(), TipoDestinatario.TUTOR_PRINCIPAL, List.of());
    }

    public static DestinatarioEtapa deResponsavelSecundario(ResponsavelSecundario responsavel) {
        return new DestinatarioEtapa(responsavel.getId().getValor(),
            TipoDestinatario.RESPONSAVEL_SECUNDARIO, responsavel.getCanais());
    }

    public String getId()                              { return id; }
    public TipoDestinatario getTipo()                  { return tipo; }
    public List<CanalContato> getCanaisPreferenciais() { return canaisPreferenciais; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DestinatarioEtapa)) return false;
        DestinatarioEtapa outro = (DestinatarioEtapa) o;
        return Objects.equals(id, outro.id) && tipo == outro.tipo;
    }

    @Override
    public int hashCode() { return Objects.hash(id, tipo); }

    @Override
    public String toString() { return tipo + "(" + id + ")"; }
}
