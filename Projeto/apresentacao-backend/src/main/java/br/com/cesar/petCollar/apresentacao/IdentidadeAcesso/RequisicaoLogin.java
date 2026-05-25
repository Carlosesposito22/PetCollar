package br.com.cesar.petCollar.apresentacao.IdentidadeAcesso;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RequisicaoLogin(
        @NotNull Perfil perfil,
        @NotBlank String identificador,
        @NotBlank String senha
) {}
