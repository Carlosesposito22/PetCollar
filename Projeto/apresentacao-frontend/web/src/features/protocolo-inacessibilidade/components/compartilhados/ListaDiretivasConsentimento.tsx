import { type DiretivaConsentimentoDTO } from "../../tipos";

/**
 * Exibe as diretivas de consentimento previamente assinadas pelo tutor (RN 10),
 * listando todas as condutas clínicas possíveis com indicação visual de autorização
 * ou bloqueio.
 */
export function ListaDiretivasConsentimento({
  diretivas,
}: {
  diretivas: DiretivaConsentimentoDTO[];
}) {
  if (diretivas.length === 0) {
    return (
      <p className="text-sm text-ink-500">
        Nenhuma diretiva de consentimento registrada para este paciente.
      </p>
    );
  }

  const autorizadas = diretivas.filter((d) => d.autorizado);
  const bloqueadas = diretivas.filter((d) => !d.autorizado);

  return (
    <div className="space-y-5">
      <div className="rounded-xl border border-emerald-200 bg-emerald-50 p-4">
        <h3 className="mb-3 text-sm font-semibold text-emerald-800">
          Condutas autorizadas pelo tutor ({autorizadas.length})
        </h3>
        {autorizadas.length === 0 ? (
          <p className="text-sm text-emerald-700">Nenhuma conduta autorizada.</p>
        ) : (
          <ul className="space-y-2">
            {autorizadas.map((d) => (
              <ItemDiretiva key={d.conduta} diretiva={d} />
            ))}
          </ul>
        )}
      </div>

      <div className="rounded-xl border border-paw-200 bg-paw-50 p-4">
        <h3 className="mb-3 text-sm font-semibold text-paw-800">
          Condutas não autorizadas / sem diretiva ({bloqueadas.length})
        </h3>
        {bloqueadas.length === 0 ? (
          <p className="text-sm text-paw-700">Todas as condutas estão autorizadas.</p>
        ) : (
          <ul className="space-y-2">
            {bloqueadas.map((d) => (
              <ItemDiretiva key={d.conduta} diretiva={d} />
            ))}
          </ul>
        )}
      </div>

      <p className="text-xs text-ink-400">
        As diretivas são verificadas pelo sistema antes de qualquer conduta clínica durante a
        execução do protocolo (RN 10). Alterações requerem novo consentimento presencial do tutor.
      </p>
    </div>
  );
}

function ItemDiretiva({ diretiva: d }: { diretiva: DiretivaConsentimentoDTO }) {
  return (
    <li className="flex items-center gap-3">
      <span
        className={
          "flex h-5 w-5 shrink-0 items-center justify-center rounded-full text-xs font-bold " +
          (d.autorizado
            ? "bg-emerald-200 text-emerald-800"
            : "bg-paw-200 text-paw-800")
        }
        aria-label={d.autorizado ? "Autorizado" : "Não autorizado"}
      >
        {d.autorizado ? "✓" : "✕"}
      </span>
      <span className="text-sm text-ink-800">{d.rotulo}</span>
    </li>
  );
}
