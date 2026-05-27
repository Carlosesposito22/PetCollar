export function EmBreve({ titulo }: { titulo: string }) {
  return (
    <div className="card flex flex-col items-center justify-center px-6 py-20 text-center">
      <div className="mb-3 text-4xl">🚧</div>
      <h1 className="text-xl font-bold text-ink-900">{titulo}</h1>
      <p className="mt-1 max-w-md text-sm text-ink-500">
        Esta área faz parte do roadmap do petCollar e será disponibilizada em breve.
      </p>
    </div>
  );
}
