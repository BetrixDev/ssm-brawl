import { AccountView } from "@daveyplate/better-auth-ui";
import { createFileRoute } from "@tanstack/react-router";

export const Route = createFileRoute("/account/$accountView")({
  component: RouteComponent,
});

function RouteComponent() {
  const { accountView } = Route.useParams();
  return (
    <main className="h-screen flex items-start pt-32 px-32 justify-center w-screen">
      <AccountView pathname={accountView} />
    </main>
  );
}
