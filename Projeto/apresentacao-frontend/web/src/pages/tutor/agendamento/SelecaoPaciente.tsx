import { useEffect, useState } from "react";
import { useAuth } from "../../../auth/AuthContext";
import type { Paciente } from "../TutorInicio";

function emoji(especie: string) {
  const e = especie.toLowerCase();
  if (e.includes("gato")) return "🐱";
  if (e.includes("cão") || e.includes("cao")) return "🐶";
  if (e.includes("ave")) return "🐦";
  if (e.includes("roedor")) return "🐹";
  if (e.includes("réptil") || e.includes("reptil")) return "🦎";
  return "🐾";
}

/**
 * Lista os pacientes do tutor como cards selecionáveis. A validação de prontuário
 * ativo (RN 1) é feita pelo backend no momento do agendamento — aqui apenas
 * informamos o tutor; o card não decide a regra.
 */
export function SelecaoPaciente({
  pacienteSelecionadoId,
  onSelecionar,
}: {
  pacienteSelecionadoId: string | null;
  onSelecionar: (paciente: Paciente) => void;
}) {
  const { apiFetch } = useAuth();
  const [pacientes, setPacientes] = useState<Paciente[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    let ativo = true;
    setCarregando(true);
    apiFetch("/api/tutor/pacientes")
      .then(r => {
        if (!r.ok) throw new Error(`Falha ao carregar pacientes (HTTP ${r.status}).`);
        return r.json() as Promise<Paciente[]>;
      })
      .then(ps => { if (ativo) setPacientes(ps); })
      .catch(e => { if (ativo) setErro((e as Error).message); })
      .finally(() => { if (ativo) setCarregando(false); });
    return () => { ativo = false; };
  }, [apiFetch]);

  if (carregando) {
    return (
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {[0, 1, 2].map(i => <div key={i} className="card h-32 animate-pulse bg-white/60" />)}
      </div>
    );
  }

  if (erro) {
    return (
      <div role="alert" className="rounded-xl border border-paw-200 bg-paw-50 p-3 text-sm text-paw-700">
        {erro}
      </div>
    );
  }

  if (pacientes.length === 0) {
    return (
      <div className="card flex flex-col items-center justify-center px-6 py-12 text-center">
        <div className="mb-2 text-4xl">🐾</div>
        <p className="text-ink-700">Você ainda não cadastrou nenhum paciente.</p>
        <p className="mt-1 text-sm text-ink-500">Cadastre um pet em “Início” antes de agendar.</p>
      </div>
    );
  }

  return (
    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      {pacientes.map(p => {
        const selecionado = p.id === pacienteSelecionadoId;
        return (
          <button
            key={p.id}
            type="button"
            onClick={() => onSelecionar(p)}
            aria-pressed={selecionado}
            className={
              "card flex h-full flex-col p-5 text-left transition " +
              (selecionado
                ? "ring-2 ring-brand-500"
                : "ring-1 ring-black/5 hover:ring-brand-200")
            }
          >
            <div className="flex items-start justify-between">
              <h3 className="text-lg font-bold text-ink-900">{p.nome}</h3>
              <span className="text-2xl" aria-hidden>{emoji(p.especie)}</span>
            </div>
            <dl className="mt-2 space-y-1 text-sm text-ink-700">
              <div className="flex justify-between gap-2">
                <dt className="text-ink-500">Espécie:</dt>
                <dd className="font-medium text-ink-800">{p.especie}</dd>
              </div>
              <div className="flex justify-between gap-2">
                <dt className="text-ink-500">Raça:</dt>
                <dd className="font-medium text-ink-800">{p.raca}</dd>
              </div>
            </dl>
            {selecionado && (
              <span className="mt-3 inline-flex w-fit items-center gap-1 rounded-full bg-brand-50 px-2.5 py-0.5 text-xs font-semibold text-brand-700 ring-1 ring-brand-100">
                Selecionado ✓
              </span>
            )}
          </button>
        );
      })}
    </div>
  );
}
