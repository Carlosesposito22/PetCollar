import { useCallback, useEffect, useRef, useState, type ReactNode } from "react";
import { useAuth } from "../../auth/AuthContext";

type LinkIndicacao = {
  id: string;
  codigo: string;
  url: string;
};

type StatusIndicacao = "PENDENTE" | "CONVERTIDA" | "INVALIDA";

type Indicacao = {
  id: string;
  cpfIndicado: string;
  status: StatusIndicacao;
  cobrancaIndicadorId: string | null;
  motivoInvalidacao: string | null;
  dataClique: string;
  convertidaEm: string | null;
};

export function TutorIndicacoes() {
  const { apiFetch } = useAuth();
  const [link, setLink] = useState<LinkIndicacao | null>(null);
  const [historico, setHistorico] = useState<Indicacao[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);
  const [copiado, setCopiado] = useState(false);
  const timeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const carregar = useCallback(async () => {
    setErro(null);
    setCarregando(true);
    try {
      const [resLink, resHistorico] = await Promise.all([
        apiFetch("/api/tutor/indicacao/link"),
        apiFetch("/api/tutor/indicacao/historico"),
      ]);
      if (!resLink.ok) {
        const corpo = await resLink.json().catch(() => ({}));
        throw new Error(corpo.mensagem ?? `Erro ao carregar link (HTTP ${resLink.status}).`);
      }
      if (!resHistorico.ok) throw new Error(`Erro ao carregar histórico (HTTP ${resHistorico.status}).`);
      setLink(await resLink.json());
      setHistorico(await resHistorico.json());
    } catch (e) {
      setErro((e as Error).message);
    } finally {
      setCarregando(false);
    }
  }, [apiFetch]);

  useEffect(() => { void carregar(); }, [carregar]);

  function copiarLink() {
    if (!link) return;
    navigator.clipboard.writeText(link.url).then(() => {
      setCopiado(true);
      if (timeoutRef.current) clearTimeout(timeoutRef.current);
      timeoutRef.current = setTimeout(() => setCopiado(false), 2000);
    });
  }

  function compartilharWhatsApp() {
    if (!link) return;
    const texto = encodeURIComponent(
      `Olha que incrível! Assine o PetCollar e ganhe 30% de desconto na primeira mensalidade usando meu link: ${link.url}`
    );
    window.open(`https://wa.me/?text=${texto}`, "_blank");
  }

  const convertidas = historico.filter(i => i.status === "CONVERTIDA").length;
  const pendentes   = historico.filter(i => i.status === "PENDENTE").length;

  return (
    <div>
      <h1 className="mb-2 text-2xl font-bold text-ink-900">Programa de Indicação</h1>
      <p className="mb-6 text-sm text-ink-500">
        Indique amigos e ganhe 15% de desconto na sua próxima fatura. Seu indicado ganha 30% na
        primeira mensalidade.
      </p>

      {erro && (
        <div role="alert" className="mb-4 rounded-xl border border-paw-200 bg-paw-50 p-3 text-sm text-paw-700">
          {erro}
        </div>
      )}

      {carregando && <p className="text-sm text-ink-500">Carregando…</p>}

      {!carregando && link && (
        <>
          <PainelLink
            link={link}
            onCopiar={copiarLink}
            onWhatsApp={compartilharWhatsApp}
            copiado={copiado}
          />

          <ResumoEstatisticas
            total={historico.length}
            convertidas={convertidas}
            pendentes={pendentes}
          />

          <TabelaHistorico indicacoes={historico} apiFetch={apiFetch} onRecarregar={carregar} />

          <Anotacao />
        </>
      )}
    </div>
  );
}

function PainelLink({
  link,
  onCopiar,
  onWhatsApp,
  copiado,
}: {
  link: LinkIndicacao;
  onCopiar: () => void;
  onWhatsApp: () => void;
  copiado: boolean;
}) {
  return (
    <section className="card mb-6 p-6 ring-1 ring-ink-300/60">
      <p className="mb-3 text-xs font-medium uppercase tracking-wide text-ink-500">
        Seu link de indicação
      </p>
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
        <div className="flex-1 rounded-lg border border-ink-300 bg-ink-50 px-4 py-2.5">
          <p className="truncate font-mono text-sm text-ink-700">{link.url}</p>
        </div>
        <div className="flex gap-2">
          <button
            onClick={onCopiar}
            className={
              "btn-ghost min-w-[100px] ring-1 transition " +
              (copiado ? "ring-brand-500 text-brand-700" : "ring-ink-300 hover:ring-brand-400")
            }
          >
            {copiado ? "Copiado!" : "Copiar"}
          </button>
          <button
            onClick={onWhatsApp}
            className="btn-ghost ring-1 ring-green-400 text-green-700 hover:bg-green-50"
          >
            WhatsApp
          </button>
        </div>
      </div>
      <p className="mt-3 text-xs text-ink-400">
        Código: <span className="font-mono font-semibold tracking-widest text-ink-600">{link.codigo}</span>
        &nbsp;·&nbsp;Permanente e intransferível
      </p>
    </section>
  );
}

function ResumoEstatisticas({
  total,
  convertidas,
  pendentes,
}: {
  total: number;
  convertidas: number;
  pendentes: number;
}) {
  return (
    <section className="card mb-6 p-6 ring-1 ring-ink-300/60">
      <p className="mb-4 text-xs font-medium uppercase tracking-wide text-ink-500">Resumo</p>
      <div className="grid grid-cols-3 gap-4 text-center">
        <Bloco rotulo="Total de indicações" valor={total} />
        <Bloco rotulo="Convertidas" valor={convertidas} cor="text-brand-600" />
        <Bloco rotulo="Pendentes" valor={pendentes} cor="text-amber-600" />
      </div>
    </section>
  );
}

function Bloco({
  rotulo,
  valor,
  cor = "text-ink-900",
}: {
  rotulo: string;
  valor: number;
  cor?: string;
}) {
  return (
    <div>
      <p className={`text-3xl font-bold ${cor}`}>{valor}</p>
      <p className="mt-1 text-xs text-ink-500">{rotulo}</p>
    </div>
  );
}

function TabelaHistorico({
  indicacoes,
  apiFetch,
  onRecarregar,
}: {
  indicacoes: Indicacao[];
  apiFetch: (input: string, init?: RequestInit) => Promise<Response>;
  onRecarregar: () => void;
}) {
  const [resgatando, setResgatando] = useState<string | null>(null);
  const [feedbackResgatar, setFeedbackResgatar] = useState<Record<string, string>>({});

  async function resgatar(indicacaoId: string) {
    setResgatando(indicacaoId);
    try {
      const res = await apiFetch(`/api/tutor/indicacao/${indicacaoId}/resgatar-desconto`, {
        method: "POST",
      });
      const body = await res.json().catch(() => ({ mensagem: "" })) as { mensagem: string };
      if (!res.ok) throw new Error(body.mensagem || `Erro HTTP ${res.status}`);
      setFeedbackResgatar(prev => ({ ...prev, [indicacaoId]: body.mensagem }));
      onRecarregar();
    } catch (e) {
      setFeedbackResgatar(prev => ({ ...prev, [indicacaoId]: (e as Error).message }));
    } finally {
      setResgatando(null);
    }
  }

  if (indicacoes.length === 0) {
    return (
      <div className="card mb-6 px-6 py-10 text-center text-sm text-ink-500">
        Você ainda não tem indicações registradas. Compartilhe seu link para começar!
      </div>
    );
  }

  return (
    <div className="card mb-6 overflow-hidden">
      <div className="overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="bg-ink-100 text-left text-xs font-semibold uppercase tracking-wide text-ink-700">
            <tr>
              <Th>CPF Indicado</Th>
              <Th>Status</Th>
              <Th>Data do Clique</Th>
              <Th>Convertida em</Th>
              <Th>Desconto de 15%</Th>
            </tr>
          </thead>
          <tbody className="divide-y divide-ink-300/40">
            {indicacoes.map(ind => (
              <tr key={ind.id} className="hover:bg-ink-100/40">
                <Td className="font-mono">{ocultarCpf(ind.cpfIndicado)}</Td>
                <Td><BadgeStatus status={ind.status} /></Td>
                <Td>{formatarDataHora(ind.dataClique)}</Td>
                <Td>{ind.convertidaEm ? formatarDataHora(ind.convertidaEm) : "—"}</Td>
                <Td className="text-xs">
                  {ind.status === "CONVERTIDA" && ind.cobrancaIndicadorId ? (
                    <span className="text-brand-600 font-medium">Aplicado na fatura ✓</span>
                  ) : ind.status === "CONVERTIDA" && !ind.cobrancaIndicadorId ? (
                    <div className="flex flex-col gap-1">
                      <button
                        onClick={() => resgatar(ind.id)}
                        disabled={resgatando === ind.id}
                        className="rounded-lg bg-brand-600 px-3 py-1.5 text-xs font-semibold text-white hover:bg-brand-700 disabled:opacity-50 transition-colors"
                      >
                        {resgatando === ind.id ? "Aplicando…" : "Resgatar 15%"}
                      </button>
                      {feedbackResgatar[ind.id] && (
                        <span className="text-ink-500">{feedbackResgatar[ind.id]}</span>
                      )}
                    </div>
                  ) : ind.status === "INVALIDA" ? (
                    <span className="text-paw-500">{ind.motivoInvalidacao ?? "Inválida"}</span>
                  ) : (
                    <span className="text-ink-400">Aguardando pagamento do indicado</span>
                  )}
                </Td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function Th({ children }: { children: ReactNode }) {
  return <th className="px-4 py-3">{children}</th>;
}
function Td({ children, className }: { children: ReactNode; className?: string }) {
  return <td className={`px-4 py-3 ${className ?? ""}`}>{children}</td>;
}

function BadgeStatus({ status }: { status: StatusIndicacao }) {
  const map: Record<StatusIndicacao, { cor: string; rotulo: string }> = {
    PENDENTE:   { cor: "text-amber-700",  rotulo: "Pendente" },
    CONVERTIDA: { cor: "text-brand-600",  rotulo: "Convertida" },
    INVALIDA:   { cor: "text-paw-600",    rotulo: "Inválida" },
  };
  const s = map[status];
  return <span className={`font-semibold ${s.cor}`}>{s.rotulo}</span>;
}

function ocultarCpf(cpf: string): string {
  if (cpf.length !== 11) return cpf;
  return `${cpf.slice(0, 3)}.***.***-${cpf.slice(-2)}`;
}

function formatarDataHora(iso: string): string {
  try {
    return new Date(iso).toLocaleString("pt-BR", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  } catch {
    return iso;
  }
}

function Anotacao() {
  return (
    <div className="rounded-xl border border-dashed border-ink-300 bg-white/60 p-4 text-xs leading-relaxed text-ink-500">
      Indicados ganham <strong>30% de desconto</strong> na primeira mensalidade. Ao confirmar o
      pagamento do indicado, você recebe <strong>15% de desconto</strong> na sua próxima fatura e a
      conquista <strong>Lendária</strong> via gamificação. Atribuição por <em>Último Clique</em> em
      caso de múltiplos links acessados. Autoindicação e métodos de pagamento coincidentes são
      bloqueados automaticamente.
    </div>
  );
}
