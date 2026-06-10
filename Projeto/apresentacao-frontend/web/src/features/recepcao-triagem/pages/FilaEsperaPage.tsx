import { useEffect, useState, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { useRecepcao, type FilaItemDTO } from "../hooks/useRecepcao";

function CorBadge({ cor }: { cor: "VERMELHO" | "AMARELO" | "VERDE" }) {
  const map = {
    VERMELHO: { cls: "bg-red-100 text-red-700 border-red-300", dot: "bg-red-500", label: "Urgente" },
    AMARELO:  { cls: "bg-yellow-100 text-yellow-700 border-yellow-300", dot: "bg-yellow-500", label: "Moderado" },
    VERDE:    { cls: "bg-green-100 text-green-700 border-green-300", dot: "bg-green-500", label: "Leve" },
  } as const;
  const m = map[cor];
  return (
    <span className={`inline-flex items-center gap-1.5 rounded-full border px-3 py-1 text-xs font-semibold ${m.cls}`}>
      <span className={`h-2 w-2 rounded-full ${m.dot}`} />
      {m.label}
    </span>
  );
}

export function FilaEsperaPage() {
  const navigate = useNavigate();
  const recepcao = useRecepcao();
  const [fila, setFila] = useState<FilaItemDTO[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [iniciando, setIniciando] = useState<string | null>(null);

  const carregar = useCallback(async () => {
    const dados = await recepcao.listarFila();
    setFila(dados);
    setCarregando(false);
  }, []);

  useEffect(() => {
    carregar();
    const intervalo = setInterval(carregar, 15000);
    return () => clearInterval(intervalo);
  }, []);

  async function iniciarAtendimento(item: FilaItemDTO) {
    setIniciando(item.triagemId);
    await recepcao.removerDaFila(item.triagemId);
    await carregar();
    setIniciando(null);
  }

  const contagens = {
    VERMELHO: fila.filter(i => i.corDeRisco === "VERMELHO").length,
    AMARELO:  fila.filter(i => i.corDeRisco === "AMARELO").length,
    VERDE:    fila.filter(i => i.corDeRisco === "VERDE").length,
  };

  return (
    <main className="mx-auto max-w-4xl px-6 py-10">
      <button onClick={() => navigate("/recepcao")}
        className="mb-8 flex items-center gap-2 text-sm text-ink-500 hover:text-ink-900 transition">
        ← Voltar ao Painel
      </button>

      <div className="mb-8 flex items-start justify-between">
        <div>
          <h1 className="text-3xl font-bold text-ink-900">Fila de Espera Dinâmica</h1>
          <p className="mt-1 text-sm text-ink-500">Atualização automática a cada 15 segundos</p>
        </div>
        <button onClick={carregar} className="btn-ghost text-sm">↻ Atualizar</button>
      </div>

      <div className="mb-8 grid grid-cols-3 gap-4">
        {([
          ["VERMELHO", "Urgentes",  "text-red-700 bg-red-50 border-red-200"],
          ["AMARELO",  "Moderados", "text-yellow-700 bg-yellow-50 border-yellow-200"],
          ["VERDE",    "Leves",     "text-green-700 bg-green-50 border-green-200"],
        ] as const).map(([cor, label, cls]) => (
          <div key={cor} className={`rounded-2xl border p-5 ${cls}`}>
            <p className="text-xs font-semibold uppercase tracking-widest opacity-70">{label}</p>
            <p className="mt-1 text-3xl font-bold">{contagens[cor]}</p>
          </div>
        ))}
      </div>

      {carregando ? (
        <div className="rounded-3xl border border-ink-200 bg-white p-12 text-center text-sm text-ink-400">
          Carregando fila...
        </div>
      ) : fila.length === 0 ? (
        <div className="rounded-3xl border border-dashed border-ink-300/70 bg-white/90 p-12 text-center text-sm text-ink-500">
          <p className="mb-3 text-2xl">✅</p>
          Nenhum paciente na fila no momento.
        </div>
      ) : (
        <div className="flex flex-col gap-3">
          {fila.map((item, idx) => (
            <div key={item.triagemId}
              className={`flex items-center gap-5 rounded-2xl border bg-white p-5 shadow-sm ${
                item.corDeRisco === "VERMELHO" ? "border-red-200" :
                item.corDeRisco === "AMARELO"  ? "border-yellow-200" : "border-green-200"
              }`}>
              <div className={`flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-full font-bold text-lg ${
                item.corDeRisco === "VERMELHO" ? "bg-red-100 text-red-700" :
                item.corDeRisco === "AMARELO"  ? "bg-yellow-100 text-yellow-700" :
                "bg-green-100 text-green-700"
              }`}>
                {idx + 1}
              </div>
              <div className="min-w-0 flex-1">
                <div className="mb-0.5 flex items-center gap-2">
                  <span className="truncate font-semibold text-ink-900">
                    {item.nomePaciente || `Paciente ${item.pacienteId.slice(-6)}`}
                  </span>
                  <CorBadge cor={item.corDeRisco} />
                </div>
                <p className="text-xs text-ink-500">
                  Triagem: {item.finalizadaEm
                    ? new Date(item.finalizadaEm).toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" })
                    : "—"}
                </p>
              </div>
              <button onClick={() => iniciarAtendimento(item)}
                disabled={iniciando === item.triagemId}
                className="flex-shrink-0 rounded-xl bg-ink-900 px-5 py-2 text-sm font-semibold text-white hover:bg-ink-700 disabled:opacity-50 transition">
                {iniciando === item.triagemId ? "..." : "Iniciar"}
              </button>
            </div>
          ))}
        </div>
      )}
    </main>
  );
}