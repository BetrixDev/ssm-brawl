import { AuthView } from "@daveyplate/better-auth-ui";
import { createFileRoute } from "@tanstack/react-router";

export const Route = createFileRoute("/auth/$authView")({
  component: RouteComponent,
});

function RouteComponent() {
  const { authView } = Route.useParams();

  return (
    <main className="h-screen flex items-center justify-center w-screen">
      <div className="w-[400px] flex items-center justify-center">
        <AuthView
          pathname={authView}
          classNames={{
            base: "border-border shadow-s",
            form: { button: "shadow-s", input: "shadow-s" },
          }}
        />
      </div>
    </main>
  );
}
