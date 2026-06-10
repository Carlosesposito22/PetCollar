package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;

/**
 * Assinatura digital do plano nutricional (F-11 RN 8). É gerada no momento da
 * finalização e contém:
 *  - imagem da assinatura desenhada pelo médico (PNG base64 vindo do {@code SignaturePad});
 *  - identificação do médico responsável;
 *  - timestamp da finalização;
 *  - hash SHA-256 do conteúdo do plano (para verificação de integridade futura).
 *
 * <p>O hash é calculado sobre uma representação textual estável do plano,
 * permitindo verificar mais tarde se o conteúdo persistido bate com o que foi
 * assinado.
 */
public record AssinaturaDigital(
        MedicoId medicoResponsavel,
        String imagemBase64,
        LocalDateTime assinadoEm,
        String hashConteudo
) {
    public AssinaturaDigital {
        if (medicoResponsavel == null)
            throw new IllegalArgumentException("Médico responsável é obrigatório.");
        if (imagemBase64 == null || imagemBase64.isBlank())
            throw new IllegalArgumentException("Imagem da assinatura é obrigatória.");
        if (assinadoEm == null)
            throw new IllegalArgumentException("Data/hora da assinatura é obrigatória.");
        if (hashConteudo == null || hashConteudo.isBlank())
            throw new IllegalArgumentException("Hash do conteúdo é obrigatório.");
    }

    /** Gera o hash SHA-256 de uma string e devolve em hexadecimal. */
    public static String calcularHash(String conteudo) {
        if (conteudo == null) throw new IllegalArgumentException("Conteúdo não pode ser nulo.");
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(conteudo.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 sempre existe nas JVMs suportadas pelo projeto.
            throw new IllegalStateException("SHA-256 indisponível na JVM.", e);
        }
    }
}
