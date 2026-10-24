import { Link } from 'react-router'

export function AttractView() {
  return (
    <div className="flex flex-col items-center justify-center min-h-[85vh] text-center p-8 space-y-8">
      <div className="space-y-4">
        <span className="px-4 py-1.5 rounded-full text-xs font-semibold uppercase tracking-wider bg-purple-500/10 text-purple-400 border border-purple-500/20">
          Arena Gaming Kiosk
        </span>
        <h1 className="text-5xl md:text-6xl font-extrabold tracking-tight bg-gradient-to-r from-white via-neutral-200 to-purple-400 bg-clip-text text-transparent">
          Tocca per Giocare
        </h1>
        <p className="text-lg text-neutral-400 max-w-md mx-auto">
          Inquadra il QR Code con il tuo smartphone o iscriviti direttamente al totem per partecipare al torneo.
        </p>
      </div>

      <div className="flex gap-4">
        <Link
          to="/queue"
          className="px-6 py-3 bg-purple-600 hover:bg-purple-500 text-white font-semibold rounded-xl text-base shadow-lg shadow-purple-900/40 transition transform active:scale-95"
        >
          Unisciti alla Coda
        </Link>
        <Link
          to="/leaderboard"
          className="px-6 py-3 bg-neutral-900 hover:bg-neutral-800 text-neutral-200 font-semibold rounded-xl text-base border border-neutral-800 transition"
        >
          Classifica Live
        </Link>
      </div>
    </div>
  )
}
