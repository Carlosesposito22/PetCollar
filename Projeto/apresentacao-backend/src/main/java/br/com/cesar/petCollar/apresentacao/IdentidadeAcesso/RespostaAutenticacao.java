package br.com.cesar.petCollar.apresentacao.IdentidadeAcesso;

public record RespostaAutenticacao(
        String token,
        Usuario user,
        long expiraEm
) {
    public record Usuario(String identificador, Perfil perfil, String nome) {}
}
