import { ROTULO_CANAL, ROTULO_NIVEL, type CanalContato, type NivelEscalonamento } from "../../tipos";

type Props = {
  tempoLimiteEsperaMinutos: number;
  canaisHabilitados: CanalContato[];
  niveisEscalonamento: NivelEscalonamento[];
  intervaloEntreTentativasMinutos: number;
  quantidadeMaximaTentativasPorCanal: number;
};

/** Resumo visual, em linguagem humana, da configuração (em edição ou histórica). */
export function PreviewConfiguracao({
  tempoLimiteEsperaMinutos,
  canaisHabilitados,
  niveisEscalonamento,
  intervaloEntreTentativasMinutos,
  quantidadeMaximaTentativasPorCanal,
}: Props) {
  return (
    <div className="space-y-3 text-sm text-ink-700">
      <p>
        Em caso de <strong>tutor inacessível</strong>, após{" "}
        <strong>{tempoLimiteEsperaMinutos} minutos</strong> sem resposta o protocolo é ativado.
      </p>
      <div>
        <p className="font-medium text-ink-800">Tentativas com o tutor (nesta ordem):</p>
        {canaisHabilitados.length === 0 ? (
          <p className="text-paw-600">Nenhum canal habilitado.</p>
        ) : (
          <ol className="ml-5 list-decimal">
            {canaisHabilitados.map((c) => (
              <li key={c}>{ROTULO_CANAL[c]}</li>
            ))}
          </ol>
        )}
        <p className="mt-1 text-xs text-ink-500">
          Até {quantidadeMaximaTentativasPorCanal} tentativa(s) por canal, a cada{" "}
          {intervaloEntreTentativasMinutos} minuto(s).
        </p>
      </div>
      <p>
        Esgotado o contato, aciona os <strong>responsáveis secundários</strong> e, em seguida,
        escalona por:
      </p>
      {niveisEscalonamento.length === 0 ? (
        <p className="text-paw-600">Nenhum nível de escalonamento habilitado.</p>
      ) : (
        <p className="text-ink-800">
          {niveisEscalonamento.map((n) => ROTULO_NIVEL[n].replace(/^Nível \d+ — /, "")).join(" → ")}
        </p>
      )}
    </div>
  );
}
