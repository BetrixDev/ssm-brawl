import { Toaster } from "@/components/ui/sonner";

import {
	HeadContent,
	Outlet,
	Scripts,
	createRootRouteWithContext,
	useRouterState,
	useRouteContext,
} from "@tanstack/react-router";
import { TanStackRouterDevtools } from "@tanstack/react-router-devtools";
import Header from "../components/header";
import appCss from "../index.css?url";
import type { QueryClient } from "@tanstack/react-query";
import type { ConvexQueryClient } from "@convex-dev/react-query";
import type { ConvexReactClient } from "convex/react";
import Loader from "@/components/loader";

import { ClerkProvider, useAuth } from "@clerk/tanstack-react-start";
import { getAuth } from "@clerk/tanstack-react-start/server";
import { createServerFn } from "@tanstack/react-start";
import { getWebRequest } from "@tanstack/react-start/server";
import { ConvexProviderWithClerk } from "convex/react-clerk";

const fetchClerkAuth = createServerFn({ method: "GET" }).handler(async () => {
	const auth = await getAuth(getWebRequest());
	const token = await auth.getToken({ template: "convex" });
	return { userId: auth.userId, token };
});

export interface RouterAppContext {
	queryClient: QueryClient;
	convexClient: ConvexReactClient;
	convexQueryClient: ConvexQueryClient;
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
				title: "My App",
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
	beforeLoad: async (ctx) => {
		const { userId, token } = await fetchClerkAuth();
		if (token) {
			ctx.context.convexQueryClient.serverHttpClient?.setAuth(token);
		}
		return { userId, token };
	},
});

function RootDocument() {
	const isFetching = useRouterState({ select: (s) => s.isLoading });
	const context = useRouteContext({ from: Route.id });
	return (
		<ClerkProvider>
			<ConvexProviderWithClerk client={context.convexClient} useAuth={useAuth}>
				<html lang="en" className="dark">
					<head>
						<HeadContent />
					</head>
					<body>
						<div className="grid h-svh grid-rows-[auto_1fr]">
							<Header />
							{isFetching ? <Loader /> : <Outlet />}
						</div>
						<Toaster richColors />
						<TanStackRouterDevtools position="bottom-left" />
						<Scripts />
					</body>
				</html>
			</ConvexProviderWithClerk>
		</ClerkProvider>
	);
}
