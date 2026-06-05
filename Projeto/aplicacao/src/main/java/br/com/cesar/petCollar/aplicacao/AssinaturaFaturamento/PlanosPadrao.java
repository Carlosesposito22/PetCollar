package br.com.cesar.petCollar.aplicacao.AssinaturaFaturamento;

import br.com.cesar.petCollar.dominio.compartilhado.PlanoId;

/**
 * Constantes de identificadores estáveis de planos comerciais — referenciados
 * pelo seed da infraestrutura e pelos pontos de entrada que precisam contratar
 * o plano padrão (ex.: simulação de pagamento da contratação na demo).
 */
public final class PlanosPadrao {

    /** Id estável do "Plano Básico Mensal" — o plano default da demo. */
    public static final PlanoId ID_PLANO_BASICO_MENSAL =
            PlanoId.de("00000000-0000-0000-0000-000000000001");

    public static final String NOME_PLANO_BASICO_MENSAL = "Plano Básico Mensal";

    public static final String VALOR_PLANO_BASICO_MENSAL = "150.00";

    private PlanosPadrao() {}
}
