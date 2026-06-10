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

interface MedicoDTO {
  id: string;
  nome: string;
}

function ModalEscolherMedico({
  item,
  medicos,
  carregandoMedicos,
  contagemPorMedico,
  onConfirmar,
  onClose,
}: {
  item: FilaItemDTO;
  medicos: MedicoDTO[];
  carregandoMedicos: boolean;
  contagemPorMedico: Record<string, number>;
  onConfirmar: (medicoId: string, nomeMedico: string) => Promise<void>;
  onClose: () => void;
}) {
  const [selecionado, setSelecionado] = useState<MedicoDTO | null>(null);
  const [salvando, setSalvando] = useState(false);

  async function handleConfirmar() {
    if (!selecionado) return;
    setSalvando(true);
    await onConfirmar(selecionado.id, selecionado.nome);
    setSalvando(false);
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4"
      onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}
    >
      <div className="flex w-full max-w-md flex-col rounded-3xl bg-white shadow-xl"
           style={{ maxHeight: "calc(100vh - 48px)" }}>

        {/* Cabeçalho fixo */}
        <div className="flex-shrink-0 border-b border-ink-100 px-8 pt-8 pb-5">
          <div className="flex items-start justify-between gap-4">
            <div>
              <h2 className="text-xl font-bold text-ink-900">Encaminhar Paciente</h2>
              <div className="mt-1 flex items-center gap-2">
                <span className="text-sm text-ink-500">
                  {item.nomePaciente || `Paciente ${item.pacienteId.slice(-6)}`}
                </span>
                <CorBadge cor={item.corDeRisco} />
              </div>
            </div>
            <button
              onClick={onClose}
              className="flex-shrink-0 rounded-full p-1 text-ink-400 hover:bg-ink-100 hover:text-ink-700 transition"
            >
              ✕
            </button>
          </div>
          <p className="mt-4 text-sm font-medium text-ink-700">
            Escolha o médico veterinário:
          </p>
        </div>

        {/* Lista com scroll */}
        <div className="flex-1 overflow-y-auto px-8 py-4">
          {carregandoMedicos ? (
            <div className="space-y-2 py-2">
              {[0, 1, 2, 3].map(i => (
                <div key={i} className="h-12 animate-pulse rounded-xl bg-ink-100" />
              ))}
            </div>
          ) : medicos.length === 0 ? (
            <div className="rounded-2xl border border-dashed border-ink-300 p-6 text-center text-sm text-ink-500">
              Nenhum médico disponível no momento.
            </div>
          ) : (
            <div className="flex flex-col gap-2">
              {medicos.map(m => {
                const qtd = contagemPorMedico[m.id] ?? 0;
                const ativo = selecionado?.id === m.id;
                return (
                  <button
                    key={m.id}
                    onClick={() => setSelecionado(m)}
                    className={`flex items-center gap-3 rounded-xl border px-4 py-3 text-left text-sm transition ${
                      ativo
                        ? "border-ink-900 bg-ink-900 text-white"
                        : "border-ink-200 hover:border-ink-400 text-ink-800"
                    }`}
                  >
                    {/* Avatar */}
                    <span className={`flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-full text-xs font-bold ${
                      ativo ? "bg-white/20 text-white" : "bg-ink-100 text-ink-600"
                    }`}>
                      {m.nome.charAt(0).toUpperCase()}
                    </span>

                    {/* Nome */}
                    <span className="flex-1 font-medium truncate">{m.nome}</span>

                    {/* Contador de pacientes */}
                    <span className={`flex-shrink-0 rounded-full px-2 py-0.5 text-xs font-semibold ${
                      ativo
                        ? "bg-white/20 text-white"
                        : qtd === 0
                          ? "bg-ink-100 text-ink-400"
                          : "bg-brand-50 text-brand-700 border border-brand-200"
                    }`}>
                      {qtd} {qtd === 1 ? "paciente" : "pacientes"}
                    </span>

                    {/* Check */}
                    {ativo && <span className="flex-shrink-0 text-white">✓</span>}
                  </button>
                );
              })}
            </div>
          )}
        </div>

        {/* Rodapé fixo */}
        <div className="flex-shrink-0 border-t border-ink-100 px-8 py-5">
          <div className="flex gap-3">
            <button onClick={onClose} className="btn-ghost flex-1">
              Cancelar
            </button>
            <button
              onClick={handleConfirmar}
              disabled={!selecionado || salvando}
              className="flex-1 rounded-xl bg-ink-900 px-6 py-2.5 text-sm font-semibold text-white transition hover:bg-ink-700 disabled:opacity-50"
            >
              {salvando ? "Encaminhando..." : "Confirmar"}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export function FilaEsperaPage() {
  const navigate = useNavigate();
  const recepcao = useRecepcao();

  const [fila, setFila]                           = useState<FilaItemDTO[]>([]);
  const [carregando, setCarregando]               = useState(true);
  const [medicos, setMedicos]                     = useState<MedicoDTO[]>([]);
  const [carregandoMedicos, setCarregandoMedicos] = useState(false);
  const [modalItem, setModalItem]                 = useState<FilaItemDTO | null>(null);
  const [toastMsg, setToastMsg]                   = useState<string | null>(null);

  function toast(msg: string) {
    setToastMsg(msg);
    setTimeout(() => setToastMsg(null), 3000);
  }

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

  async function abrirModalChamar(item: FilaItemDTO) {
    setModalItem(item);
    if (medicos.length === 0) {
      setCarregandoMedicos(true);
      const lista = await recepcao.listarMedicos();
      setMedicos(lista);
      setCarregandoMedicos(false);
    }
  }

  async function confirmarEncaminhar(medicoId: string, nomeMedico: string) {
    if (!modalItem) return;
    const ok = await recepcao.encaminharParaMedico(modalItem.triagemId, medicoId, nomeMedico);
    if (ok) {
      setModalItem(null);
      await carregar();
      toast(`Paciente encaminhado para ${nomeMedico}`);
    }
  }

  // Conta quantos pacientes já estão com cada médico na fila atual
  const contagemPorMedico: Record<string, number> = {};
  for (const item of fila) {
    if (item.medicoId) {
      contagemPorMedico[item.medicoId] = (contagemPorMedico[item.medicoId] ?? 0) + 1;
    }
  }

  const contagens = {
    VERMELHO: fila.filter(i => i.corDeRisco === "VERMELHO").length,
    AMARELO:  fila.filter(i => i.corDeRisco === "AMARELO").length,
    VERDE:    fila.filter(i => i.corDeRisco === "VERDE").length,
  };

  return (
    <main className="mx-auto max-w-4xl px-6 py-10">
      {toastMsg && (
        <div className="fixed bottom-6 right-6 z-50 rounded-2xl bg-ink-900 px-6 py-3 text-sm text-white shadow-lg">
          {toastMsg}
        </div>
      )}

      {modalItem && (
        <ModalEscolherMedico
          item={modalItem}
          medicos={medicos}
          carregandoMedicos={carregandoMedicos}
          contagemPorMedico={contagemPorMedico}
          onConfirmar={confirmarEncaminhar}
          onClose={() => setModalItem(null)}
        />
      )}

      <button
        onClick={() => navigate("/recepcao")}
        className="mb-8 flex items-center gap-2 text-sm text-ink-500 hover:text-ink-900 transition"
      >
        ← Voltar ao Painel
      </button>

      <div className="mb-8 flex items-start justify-between">
        <div>
          <h1 className="text-3xl font-bold text-ink-900">Fila de Espera Dinâmica</h1>
          <p className="mt-1 text-sm text-ink-500">Atualização automática a cada 15 segundos</p>
        </div>
        <button onClick={carregar} className="btn-ghost text-sm">↻ Atualizar</button>
      </div>

      {/* Métricas */}
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
            <div
              key={item.triagemId}
              className={`flex items-center gap-5 rounded-2xl border bg-white p-5 shadow-sm transition ${
                item.corDeRisco === "VERMELHO" ? "border-red-200" :
                item.corDeRisco === "AMARELO"  ? "border-yellow-200" : "border-green-200"
              }`}
            >
              {/* Posição */}
              <div className={`flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-full font-bold text-lg ${
                item.corDeRisco === "VERMELHO" ? "bg-red-100 text-red-700" :
                item.corDeRisco === "AMARELO"  ? "bg-yellow-100 text-yellow-700" :
                "bg-green-100 text-green-700"
              }`}>
                {idx + 1}
              </div>

              {/* Info */}
              <div className="min-w-0 flex-1">
                <div className="mb-0.5 flex flex-wrap items-center gap-2">
                  <span className="font-semibold text-ink-900 truncate">
                    {item.nomePaciente || `Paciente ${item.pacienteId.slice(-6)}`}
                  </span>
                  <CorBadge cor={item.corDeRisco} />
                </div>
                <div className="flex flex-wrap items-center gap-3 text-xs text-ink-500">
                  <span>
                    Triagem:{" "}
                    {item.finalizadaEm
                      ? new Date(item.finalizadaEm).toLocaleTimeString("pt-BR", {
                          hour: "2-digit", minute: "2-digit",
                        })
                      : "—"}
                  </span>
                  {item.medicoId ? (
                    <span className="inline-flex items-center gap-1 rounded-full border border-brand-200 bg-brand-50 px-2 py-0.5 text-xs font-medium text-brand-700">
                      🩺 {item.nomeMedico}
                    </span>
                  ) : (
                    <span className="inline-flex items-center gap-1 rounded-full bg-ink-100 px-2 py-0.5 text-xs text-ink-500">
                      Aguardando encaminhamento
                    </span>
                  )}
                </div>
              </div>

              {/* Ação */}
              <button
                onClick={() => abrirModalChamar(item)}
                className={`flex-shrink-0 rounded-xl px-5 py-2 text-sm font-semibold transition ${
                  item.medicoId
                    ? "border border-ink-300 bg-white text-ink-700 hover:bg-ink-50"
                    : "bg-ink-900 text-white hover:bg-ink-700"
                }`}
              >
                {item.medicoId ? "Rencaminhar" : "Chamar"}
              </button>
            </div>
          ))}
        </div>
      )}
    </main>
  );
}