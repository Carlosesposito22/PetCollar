package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.etapa;

import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.CanalContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ResponsavelSecundario;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.TipoDestinatario;

import java.util.List;
import java.util.Objects;

/**
 * Value Object que abstrai um destinatário de tentativa de contato dentro da
 * execução do {@link EtapaProtocoloService Template Method}. Permite que o
 * esqueleto na superclasse trate uniformemente o tutor principal e os
 * responsáveis secundários, sem conhecer o tipo concreto: carrega a identidade
 * ({@code id} cross-agregado como {@code String}), o {@link TipoDestinatario} e
 * os canais de contato preferenciais do destinatário (na ordem de uso).
 *
 * <p>Como o agregado mantém referências entre contextos apenas por {@code String}
 * (sem {@code @ManyToOne}), este VO também guarda o id como {@code String}. Os
 * factories estáticos ({@code deTutor}/{@code deResponsavelSecundario}) cobrem os
 * dois tipos de destinatário previstos pela F-03.
 */
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

    /**
     * Tutor principal do protocolo. Os canais são resolvidos pela configuração
     * vigente (não pelo destinatário), então a lista preferencial fica vazia.
     */
    public static DestinatarioEtapa deTutor(TutorId tutorId) {
        return new DestinatarioEtapa(tutorId.getValor(), TipoDestinatario.TUTOR_PRINCIPAL, List.of());
    }

    /** Responsável secundário cadastrado, com os canais próprios na ordem de prioridade (RN 4). */
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
