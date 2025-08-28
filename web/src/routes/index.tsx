import { useQuery } from "@tanstack/react-query";
import { createFileRoute } from "@tanstack/react-router";
import { useTRPC } from "~/trpc";

export const Route = createFileRoute("/")({
  component: App,
  loader: async ({ context }) => {
    await context.queryClient.ensureQueryData(context.trpc.ping.queryOptions());
  },
});

function App() {
  const trpc = useTRPC();

  const { data } = useQuery(trpc.ping.queryOptions());

  return (
    <div>
      <h1>Super Smash Mobs Brawl YOOOO</h1>
      <div>
        Response from server: <span className="text-blue-500">{data ?? "loading..."}</span>
      </div>
    </div>
  );
}
