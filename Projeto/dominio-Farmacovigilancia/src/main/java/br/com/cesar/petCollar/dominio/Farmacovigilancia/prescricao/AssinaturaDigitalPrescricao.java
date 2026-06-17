package br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;

public record AssinaturaDigitalPrescricao(
        MedicoId medicoResponsavel,
        String imagemBase64,
        LocalDateTime assinadoEm,
        String hashConteudo
) {

    public AssinaturaDigitalPrescricao {
        if (medicoResponsavel == null)
            throw new IllegalArgumentException("Médico responsável é obrigatório.");
        if (imagemBase64 == null || imagemBase64.isBlank())
            throw new IllegalArgumentException("Imagem da assinatura é obrigatória.");
        if (assinadoEm == null)
            throw new IllegalArgumentException("Data/hora da assinatura é obrigatória.");
        if (hashConteudo == null || hashConteudo.isBlank())
            throw new IllegalArgumentException("Hash do conteúdo é obrigatório.");
    }

    public static String calcularHash(String conteudo) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(conteudo.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível na JVM.", e);
        }
    }
}
