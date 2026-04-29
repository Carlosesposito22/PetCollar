package petcollar.dominio.assinaturafaturamento.indicacao;

import java.util.Objects;

public final class UrlCompartilhamento {

    private final String urlBase;
    private final String urlCompleta;

    public UrlCompartilhamento(String urlBase, CodigoIndicacao codigo) {
        if (urlBase == null || urlBase.isBlank())
            throw new IllegalArgumentException("urlBase não pode ser vazia.");
        if (codigo == null)
            throw new IllegalArgumentException("CodigoIndicacao não pode ser nulo.");
        this.urlBase = urlBase;
        this.urlCompleta = urlBase + "?ref=" + codigo.getValor();
    }

    private UrlCompartilhamento(String urlBase, String urlCompleta) {
        this.urlBase = urlBase;
        this.urlCompleta = urlCompleta;
    }

    public static UrlCompartilhamento reconstruir(String urlBase, String urlCompleta) {
        if (urlBase == null || urlBase.isBlank())
            throw new IllegalArgumentException("urlBase não pode ser vazia.");
        if (urlCompleta == null || urlCompleta.isBlank())
            throw new IllegalArgumentException("urlCompleta não pode ser vazia.");
        return new UrlCompartilhamento(urlBase, urlCompleta);
    }

    public String getUrlBase()      { return urlBase; }
    public String getUrlCompleta()  { return urlCompleta; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UrlCompartilhamento)) return false;
        UrlCompartilhamento other = (UrlCompartilhamento) o;
        return Objects.equals(urlCompleta, other.urlCompleta);
    }

    @Override
    public int hashCode() { return Objects.hash(urlCompleta); }

    @Override
    public String toString() { return urlCompleta; }
}
