package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.NivelCriticidade;

import java.util.Objects;

public final class ConteudoNotificacao {

    private final String titulo;
    private final String corpo;
    private final NivelCriticidade criticidade;

    public ConteudoNotificacao(String titulo, String corpo, NivelCriticidade criticidade) {
        if (titulo == null || titulo.isBlank())
            throw new IllegalArgumentException("Título da notificação não pode ser vazio.");
        if (corpo == null || corpo.isBlank())
            throw new IllegalArgumentException("Corpo da notificação não pode ser vazio.");
        if (criticidade == null)
            throw new IllegalArgumentException("Criticidade da notificação não pode ser nula.");
        this.titulo = titulo;
        this.corpo = corpo;
        this.criticidade = criticidade;
    }

    public static ConteudoNotificacao ativacaoProtocolo() {
        return new ConteudoNotificacao(
            "Protocolo de contato ativado",
            "Não obtivemos resposta dentro do tempo previsto e iniciamos o protocolo de contato. "
                + "Por favor, retorne o contato com a clínica o quanto antes.",
            NivelCriticidade.BAIXA);
    }

    public static ConteudoNotificacao tentativaDeContato(NivelCriticidade criticidade) {
        return new ConteudoNotificacao(
            "Tentativa de contato — paciente em atendimento",
            "Estamos tentando contato sobre um paciente em atendimento. "
                + "Por favor, retorne o contato com a clínica o quanto antes.",
            criticidade);
    }

    public static ConteudoNotificacao escalonamento(NivelCriticidade criticidade) {
        return new ConteudoNotificacao(
            "Escalonamento do protocolo de contato",
            "O protocolo de contato avançou para um novo nível por falta de resposta.",
            criticidade);
    }

    public String getTitulo()                { return titulo; }
    public String getCorpo()                 { return corpo; }
    public NivelCriticidade getCriticidade() { return criticidade; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConteudoNotificacao)) return false;
        ConteudoNotificacao outro = (ConteudoNotificacao) o;
        return Objects.equals(titulo, outro.titulo)
            && Objects.equals(corpo, outro.corpo)
            && criticidade == outro.criticidade;
    }

    @Override
    public int hashCode() { return Objects.hash(titulo, corpo, criticidade); }
}
