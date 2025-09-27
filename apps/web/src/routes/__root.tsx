import Footer from "@/components/footer";
import Loader from "@/components/loader";
import { Toaster } from "@/components/ui/sonner";
import type { createORPCReactQueryUtils } from "@orpc/react-query";
import type { QueryClient } from "@tanstack/react-query";
import {
  HeadContent,
  Outlet,
  Scripts,
  createRootRouteWithContext,
  useRouterState,
} from "@tanstack/react-router";
import { TanStackRouterDevtools } from "@tanstack/react-router-devtools";
import type { AppRouterClient } from "../../../backend/src/routers";
import Header from "../components/header";
import appCss from "../index.css?url";

export interface RouterAppContext {
  queryClient: QueryClient;
  rpc: ReturnType<typeof createORPCReactQueryUtils<AppRouterClient>>;
}

export const Route = createRootRouteWithContext<RouterAppContext>()({
  head: () => ({
    meta: [
      {
        charSet: "utf-8",
      },
      {
        name: "viewport",
        content: "width=device-width, initial-scale=1",
      },
      {
        title: "Super Smash Mobs Brawl",
      },
    ],
    links: [
      {
        rel: "stylesheet",
        href: appCss,
      },
    ],
  }),

  component: RootDocument,
});

function RootDocument() {
  const isFetching = useRouterState({ select: (s) => s.isLoading });

  return (
    <html lang="en" className="dark">
      <head>
        <HeadContent />
      </head>
      <body>
        <div className="grid h-svh grid-rows-[auto_1fr]">
          <Header />
          <div className="flex-1 min-h-[calc(100vh-9rem-4rem)]">
            {isFetching ? <Loader /> : <Outlet />}
          </div>
          <Footer />
        </div>
        <Toaster richColors />
        <TanStackRouterDevtools position="bottom-left" />
        <Scripts />
      </body>
    </html>
  );
}
