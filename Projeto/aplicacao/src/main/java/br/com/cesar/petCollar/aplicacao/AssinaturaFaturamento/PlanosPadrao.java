package br.com.cesar.petCollar.aplicacao.AssinaturaFaturamento;

import br.com.cesar.petCollar.dominio.compartilhado.PlanoId;

public final class PlanosPadrao {

    public static final PlanoId ID_PLANO_BASICO_MENSAL =
            PlanoId.de("00000000-0000-0000-0000-000000000001");

    public static final String NOME_PLANO_BASICO_MENSAL = "Plano Básico Mensal";

    public static final String VALOR_PLANO_BASICO_MENSAL = "150.00";

    private PlanosPadrao() {}
}
