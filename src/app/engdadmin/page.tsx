import nextDynamic from "next/dynamic";

export const dynamic = "force-dynamic";

const AdminDashboard = nextDynamic(() => import("./AdminDashboard"), {
  ssr: false,
  loading: () => (
    <div className="min-h-screen bg-obsidian text-white flex flex-col items-center justify-center space-y-4">
      <div className="w-12 h-12 border-4 border-gold border-t-transparent rounded-full animate-spin" />
      <p className="text-xs font-bold tracking-wider text-gold uppercase">Loading Operations Dashboard...</p>
    </div>
  ),
});

export default function Page() {
  return <AdminDashboard />;
}
