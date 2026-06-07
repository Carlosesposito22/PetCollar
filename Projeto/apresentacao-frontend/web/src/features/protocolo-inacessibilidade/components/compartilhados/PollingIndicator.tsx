import { formatarHora } from "../../../../utils/formato";

type Props = {
  atualizando: boolean;
  ativo: boolean;
  ultimaAtualizacao: number | null;
};

/**
 * Indicador discreto de atualização ao vivo. Quando o polling está parado
 * (estado terminal), some o "ponto pulsante" e mostra apenas o instante da
 * última atualização.
 */
export function PollingIndicator({ atualizando, ativo, ultimaAtualizacao }: Props) {
  return (
    <span
      className="inline-flex items-center gap-1.5 text-xs text-ink-500"
      title={ativo ? "Esta tela atualiza automaticamente." : "Atualização ao vivo encerrada."}
    >
      {ativo && (
        <span className="relative flex h-2 w-2" aria-hidden>
          {atualizando && (
            <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-brand-400 opacity-75" />
          )}
          <span className="relative inline-flex h-2 w-2 rounded-full bg-brand-500" />
        </span>
      )}
      <span>
        {ativo ? "Ao vivo" : "Pausado"}
        {ultimaAtualizacao
          ? ` · atualizado ${formatarHora(new Date(ultimaAtualizacao).toISOString())}`
          : ""}
      </span>
    </span>
  );
}
