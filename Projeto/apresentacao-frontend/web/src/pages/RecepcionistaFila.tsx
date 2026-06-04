import { useEffect, useState } from "react";
import { useAuth } from "../auth/AuthContext";

interface PosicaoFila {
  pacienteId: string;
  triagemId: string;
  corDeRisco: "VERMELHO" | "AMARELO" | "VERDE";
  finalizadaEm: string;
}

const COR_LABEL: Record<PosicaoFila["corDeRisco"], string> = {
  VERMELHO: "Urgente",
  AMARELO: "Moderado",
  VERDE: "Leve",
};

const COR_CLASSE: Record<PosicaoFila["corDeRisco"], string> = {
  VERMELHO: "bg-red-100 text-red-700 border-red-200",
  AMARELO: "bg-yellow-100 text-yellow-700 border-yellow-200",
  VERDE: "bg-green-100 text-green-700 border-green-200",
};

export function RecepcionistaFila() {
  const { session } = useAuth();
  const [fila, setFila] = useState<PosicaoFila[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);

  async function carregarFila() {
    try {
      const res = await fetch("/api/recepcao/fila", {
        headers: { Authorization: `Bearer ${session?.token}` },
      });
      if (!res.ok) throw new Error("Erro ao carregar fila.");
      setFila(await res.json());
      setErro(null);
    } catch (e: any) {
      setErro(e.message);
    } finally {
      setCarregando(false);
    }
  }

  async function iniciarAtendimento(triagemId: string) {
    await fetch(`/api/recepcao/fila/${triagemId}`, {
      method: "DELETE",
      headers: { Authorization: `Bearer ${session?.token}` },
    });
    carregarFila();
  }

  useEffect(() => {
    carregarFila();
    const intervalo = setInterval(carregarFila, 15000);
    return () => clearInterval(intervalo);
  }, []);

  if (carregando) {
    return <p className="text-sm text-ink-500">Carregando fila...</p>;
  }

  if (erro) {
    return <p className="text-sm text-red-600">{erro}</p>;
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-semibold text-ink-900">Fila de Atendimento</h2>
        <button onClick={carregarFila} className="btn-ghost text-sm">
          Atualizar
        </button>
      </div>

      {fila.length === 0 ? (
        <div className="rounded-3xl border border-dashed border-ink-300/70 bg-white/90 p-8 text-center text-sm text-ink-500">
          Nenhum paciente na fila no momento.
        </div>
      ) : (
        <ul className="flex flex-col gap-3">
          {fila.map((item, index) => (
            <li
              key={item.triagemId}
              className="flex items-center justify-between rounded-2xl border border-ink-200/80 bg-white p-5 shadow-sm"
            >
              <div className="flex items-center gap-4">
                <span className="text-2xl font-bold text-ink-300">
                  {String(index + 1).padStart(2, "0")}
                </span>
                <div className="flex flex-col gap-1">
                  <span className="text-sm font-medium text-ink-900">
                    Paciente {item.pacienteId.slice(0, 8)}…
                  </span>
                  <span className="text-xs text-ink-500">
                    Triagem finalizada às{" "}
                    {new Date(item.finalizadaEm).toLocaleTimeString("pt-BR", {
                      hour: "2-digit",
                      minute: "2-digit",
                    })}
                  </span>
                </div>
              </div>

              <div className="flex items-center gap-3">
                <span className={`rounded-full border px-3 py-1 text-xs font-semibold ${COR_CLASSE[item.corDeRisco]}`}>
                  {COR_LABEL[item.corDeRisco]}
                </span>
                <button
                  onClick={() => iniciarAtendimento(item.triagemId)}
                  className="btn-ghost text-sm text-brand-700"
                >
                  Iniciar →
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}