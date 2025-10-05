import { useRpc } from "@/contexts/rpc-context";
import { useQuery } from "@tanstack/react-query";
import { createFileRoute } from "@tanstack/react-router";

export const Route = createFileRoute("/")({
  component: HomeComponent,
});

function HomeComponent() {
  const rpc = useRpc();

  console.log(rpc);

  const healthCheck = useQuery(rpc.healthCheck.queryOptions());

  console.log(healthCheck);

  return (
    <div className="container mx-auto max-w-3xl px-4 py-2">
      <pre className="overflow-x-auto font-mono text-sm">Super Smash Mobs Brawl</pre>
      <div className="grid gap-6">
        <section className="space-y-2 rounded-lg border p-4">
          <h2 className="mb-2 font-medium">API Status</h2>
          <div className="flex items-center gap-2">
            <div
              className={`h-2 w-2 rounded-full ${healthCheck.data?.status === "OK" ? "bg-green-500" : healthCheck.isLoading ? "bg-orange-400" : "bg-red-500"}`}
            />
            <span className="text-muted-foreground text-sm">
              {healthCheck.isLoading
                ? "Checking..."
                : healthCheck.data?.status === "OK"
                  ? "Connected"
                  : "Error"}
            </span>
          </div>
          <div className="text-muted-foreground text-sm">
            DB Check Duration: {healthCheck.data?.dbCheckDurationMs}ms
          </div>
        </section>
      </div>
    </div>
  );
}
