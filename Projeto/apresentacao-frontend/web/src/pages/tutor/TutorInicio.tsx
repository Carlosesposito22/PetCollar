import { useCallback, useEffect, useState } from "react";
import { useAuth } from "../../auth/AuthContext";

export type Paciente = {
  id: string;
  nome: string;
  especie: string;
  raca: string;
  nascimento: string; // ISO date
  idade: number;
  pesoKg: number | null;
  sexo: string | null;
  vacinaEmAtraso: boolean;
};

const ESPECIES = ["Cão", "Gato", "Ave", "Roedor", "Réptil", "Outro"];

/** Idade legível a partir da data de nascimento (anos, ou meses se < 1 ano). */
function idadeLegivel(nascimento: string): string {
  if (!nascimento) return "—";
  const nasc = new Date(nascimento);
  if (Number.isNaN(nasc.getTime())) return "—";
  const hoje = new Date();
  let meses = (hoje.getFullYear() - nasc.getFullYear()) * 12 + (hoje.getMonth() - nasc.getMonth());
  if (hoje.getDate() < nasc.getDate()) meses--;
  if (meses < 0) return "—";
  if (meses < 1) return "< 1 mês";
  if (meses < 12) return `${meses} ${meses === 1 ? "mês" : "meses"}`;
  const anos = Math.floor(meses / 12);
  return `${anos} ${anos === 1 ? "ano" : "anos"}`;
}

function labelSexo(sexo: string | null): string {
  if (sexo === "MACHO") return "Macho";
  if (sexo === "FEMEA") return "Fêmea";
  return "—";
}

export function TutorInicio() {
  const { apiFetch } = useAuth();
  const [pacientes, setPacientes] = useState<Paciente[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);
  const [editando, setEditando] = useState<Paciente | null>(null);
  const [criando, setCriando] = useState(false);
  const [removendo, setRemovendo] = useState<Paciente | null>(null);

  const recarregar = useCallback(async () => {
    setErro(null);
    try {
      const res = await apiFetch("/api/tutor/pacientes");
      if (!res.ok) throw new Error(`Falha ao carregar pacientes (HTTP ${res.status}).`);
      setPacientes(await res.json());
    } catch (e) {
      setErro((e as Error).message);
    } finally {
      setCarregando(false);
    }
  }, [apiFetch]);

  useEffect(() => { void recarregar(); }, [recarregar]);

  async function excluir(p: Paciente) {
    setErro(null);
    try {
      const res = await apiFetch(`/api/tutor/pacientes/${p.id}`, { method: "DELETE" });
      if (!res.ok && res.status !== 204) throw new Error(`Falha ao excluir (HTTP ${res.status}).`);
      setRemovendo(null);
      await recarregar();
    } catch (e) {
      setErro((e as Error).message);
    }
  }

  return (
    <div>
      <div className="mb-6 flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-ink-900">Meus Pacientes</h1>
          <p className="text-sm text-ink-500">Cadastre e acompanhe os pets sob sua tutoria.</p>
        </div>
        <button onClick={() => setCriando(true)} className="btn-primary w-auto">
          + Adicionar paciente
        </button>
      </div>

      {erro && (
        <div role="alert" className="mb-4 rounded-xl border border-amber-200 bg-amber-50 p-3 text-sm text-amber-900">
          {erro}
        </div>
      )}

      {carregando ? (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {[0, 1, 2].map(i => <div key={i} className="card h-44 animate-pulse bg-white/60" />)}
        </div>
      ) : pacientes.length === 0 ? (
        <div className="card flex flex-col items-center justify-center px-6 py-16 text-center">
          <div className="mb-2 text-4xl">🐾</div>
          <p className="text-ink-700">Você ainda não cadastrou nenhum paciente.</p>
          <button onClick={() => setCriando(true)} className="btn-primary mt-4 w-auto">
            Adicionar o primeiro
          </button>
        </div>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {pacientes.map(p => (
            <PacienteCard
              key={p.id}
              paciente={p}
              onEditar={() => setEditando(p)}
              onExcluir={() => setRemovendo(p)}
            />
          ))}
        </div>
      )}

      {(criando || editando) && (
        <PacienteFormModal
          inicial={editando}
          onFechar={() => { setCriando(false); setEditando(null); }}
          onSalvo={() => { setCriando(false); setEditando(null); void recarregar(); }}
        />
      )}

      {removendo && (
        <ConfirmarExclusaoModal
          paciente={removendo}
          onCancelar={() => setRemovendo(null)}
          onConfirmar={() => excluir(removendo)}
        />
      )}
    </div>
  );
}

function PacienteCard({
  paciente, onEditar, onExcluir,
}: { paciente: Paciente; onEditar: () => void; onExcluir: () => void }) {
  const alerta = paciente.vacinaEmAtraso;
  return (
    <div className={"card flex h-full flex-col p-5 " + (alerta ? "ring-2 ring-paw-300" : "")}>
      {/* Nome sempre no topo, alinhado entre todos os cards */}
      <div className="flex items-start justify-between">
        <h3 className="text-lg font-bold text-ink-900">{paciente.nome}</h3>
        <span className="text-2xl" aria-hidden>{emoji(paciente.especie)}</span>
      </div>

      {alerta && (
        <div className="mt-3 flex items-center gap-2 rounded-lg bg-paw-50 px-3 py-1.5 text-sm font-medium text-paw-700 ring-1 ring-paw-200">
          ⚠️ Vacina em atraso
        </div>
      )}

      <dl className="mt-3 space-y-1 text-sm text-ink-700">
        <Linha rotulo="Espécie" valor={paciente.especie} />
        <Linha rotulo="Raça" valor={paciente.raca} />
        <Linha rotulo="Sexo" valor={labelSexo(paciente.sexo)} />
        <Linha rotulo="Peso" valor={paciente.pesoKg != null ? `${paciente.pesoKg} kg` : "—"} />
        <Linha rotulo="Idade" valor={idadeLegivel(paciente.nascimento)} />
      </dl>

      {/* Botões fixados na base, alinhados entre todos os cards */}
      <div className="mt-auto flex gap-2 pt-4">
        <button onClick={onEditar} className="btn-ghost flex-1 ring-1 ring-ink-300">Editar</button>
        <button onClick={onExcluir} className="btn-ghost ring-1 ring-paw-200 text-paw-700 hover:bg-paw-50">
          Excluir
        </button>
      </div>
    </div>
  );
}

function Linha({ rotulo, valor }: { rotulo: string; valor: string }) {
  return (
    <div className="flex justify-between gap-2">
      <dt className="text-ink-500">{rotulo}:</dt>
      <dd className="font-medium text-ink-800">{valor}</dd>
    </div>
  );
}

function emoji(especie: string) {
  const e = especie.toLowerCase();
  if (e.includes("gato")) return "🐱";
  if (e.includes("cão") || e.includes("cao")) return "🐶";
  if (e.includes("ave")) return "🐦";
  if (e.includes("roedor")) return "🐹";
  if (e.includes("réptil") || e.includes("reptil")) return "🦎";
  return "🐾";
}

function PacienteFormModal({
  inicial, onFechar, onSalvo,
}: {
  inicial: Paciente | null;
  onFechar: () => void;
  onSalvo: () => void;
}) {
  const { apiFetch } = useAuth();
  const [nome, setNome] = useState(inicial?.nome ?? "");
  const [especie, setEspecie] = useState(inicial?.especie ?? ESPECIES[0]);
  const [raca, setRaca] = useState(inicial?.raca ?? "");
  const [nascimento, setNascimento] = useState(inicial?.nascimento ?? "");
  const [sexo, setSexo] = useState(inicial?.sexo ?? "");
  const [peso, setPeso] = useState(inicial?.pesoKg != null ? String(inicial.pesoKg) : "");
  const [erro, setErro] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  async function salvar(e: React.FormEvent) {
    e.preventDefault();
    setErro(null);
    setEnviando(true);
    try {
      const pesoNum = peso.trim() === "" ? null : Number(peso.replace(",", "."));
      const corpo = JSON.stringify({
        nome, especie, raca, nascimento,
        pesoKg: pesoNum != null && !Number.isNaN(pesoNum) ? pesoNum : null,
        sexo,
      });
      const res = inicial
        ? await apiFetch(`/api/tutor/pacientes/${inicial.id}`, { method: "PUT", body: corpo })
        : await apiFetch("/api/tutor/pacientes", { method: "POST", body: corpo });
      if (!res.ok) throw new Error(`Falha ao salvar (HTTP ${res.status}).`);
      onSalvo();
    } catch (e2) {
      setErro((e2 as Error).message);
    } finally {
      setEnviando(false);
    }
  }

  return (
    <ModalShell onFechar={onFechar} titulo={inicial ? "Editar paciente" : "Adicionar paciente"}>
      {erro && (
        <div role="alert" className="mb-4 rounded-xl border border-amber-200 bg-amber-50 p-3 text-sm text-amber-900">
          {erro}
        </div>
      )}
      <form onSubmit={salvar} className="grid gap-4">
        <div>
          <label className="label" htmlFor="nome">Nome</label>
          <input id="nome" required className="input" value={nome} onChange={e => setNome(e.target.value)} />
        </div>
        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="label" htmlFor="especie">Espécie</label>
            <select id="especie" className="input" value={especie} onChange={e => setEspecie(e.target.value)}>
              {ESPECIES.map(es => <option key={es} value={es}>{es}</option>)}
            </select>
          </div>
          <div>
            <label className="label" htmlFor="raca">Raça</label>
            <input id="raca" required className="input" value={raca} onChange={e => setRaca(e.target.value)} />
          </div>
        </div>
        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="label" htmlFor="sexo">Sexo</label>
            <select id="sexo" className="input" value={sexo} onChange={e => setSexo(e.target.value)}>
              <option value="">—</option>
              <option value="MACHO">Macho</option>
              <option value="FEMEA">Fêmea</option>
            </select>
          </div>
          <div>
            <label className="label" htmlFor="peso">Peso (kg)</label>
            <input id="peso" type="number" step="0.1" min="0" inputMode="decimal" className="input"
                   value={peso} onChange={e => setPeso(e.target.value)} placeholder="0.0" />
          </div>
        </div>
        <div>
          <label className="label" htmlFor="nascimento">Data de nascimento</label>
          <input id="nascimento" type="date" required className="input" max={new Date().toISOString().slice(0, 10)}
                 value={nascimento} onChange={e => setNascimento(e.target.value)} />
          {nascimento && (
            <p className="mt-1 text-xs text-ink-500">Idade calculada: <strong>{idadeLegivel(nascimento)}</strong></p>
          )}
        </div>
        <div className="mt-2 flex justify-end gap-2">
          <button type="button" onClick={onFechar} className="btn-ghost ring-1 ring-ink-300">Cancelar</button>
          <button type="submit" disabled={enviando} className="btn-primary w-auto">
            {enviando ? "Salvando…" : "Salvar"}
          </button>
        </div>
      </form>
    </ModalShell>
  );
}

function ConfirmarExclusaoModal({
  paciente, onCancelar, onConfirmar,
}: { paciente: Paciente; onCancelar: () => void; onConfirmar: () => void }) {
  return (
    <ModalShell onFechar={onCancelar} titulo="Excluir paciente">
      <p className="text-sm text-ink-700">
        Tem certeza que deseja excluir <strong>{paciente.nome}</strong>? Essa ação remove
        também a carteira de vacinação e não pode ser desfeita.
      </p>
      <div className="mt-6 flex justify-end gap-2">
        <button onClick={onCancelar} className="btn-ghost ring-1 ring-ink-300">Cancelar</button>
        <button onClick={onConfirmar} className="btn-primary w-auto bg-red-600 hover:bg-red-700">
          Excluir
        </button>
      </div>
    </ModalShell>
  );
}

function ModalShell({
  titulo, children, onFechar,
}: { titulo: string; children: React.ReactNode; onFechar: () => void }) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4" onClick={onFechar}>
      <div className="w-full max-w-md card p-6" onClick={e => e.stopPropagation()}>
        <h3 className="mb-5 text-lg font-bold text-ink-900">{titulo}</h3>
        {children}
      </div>
    </div>
  );
}
