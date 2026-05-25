package petCollar.dominio.AtendimentoClinico.relatorio;

import java.util.Objects;

public final class AnexoRelatorio {

    private final String nomeArquivo;
    private final TipoAnexo tipoAnexo;
    private final String url;

    public AnexoRelatorio(String nomeArquivo, TipoAnexo tipoAnexo, String url) {
        if (nomeArquivo == null || nomeArquivo.isBlank())
            throw new IllegalArgumentException("Nome do arquivo do anexo não pode ser vazio.");
        if (tipoAnexo == null)
            throw new IllegalArgumentException("Tipo do anexo não pode ser nulo.");
        if (url == null || url.isBlank())
            throw new IllegalArgumentException("URL do anexo não pode ser vazia.");

        this.nomeArquivo = nomeArquivo;
        this.tipoAnexo = tipoAnexo;
        this.url = url;
    }

    public String getNomeArquivo() {
        return nomeArquivo;
    }

    public TipoAnexo getTipoAnexo() {
        return tipoAnexo;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AnexoRelatorio)) return false;
        AnexoRelatorio other = (AnexoRelatorio) o;
        return Objects.equals(nomeArquivo, other.nomeArquivo)
                && tipoAnexo == other.tipoAnexo
                && Objects.equals(url, other.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nomeArquivo, tipoAnexo, url);
    }
}
