import type { HeadersFunction, MetaFunction } from "@vercel/remix";

export const meta: MetaFunction = () => {
  return [
    { title: "Super Smash Mobs Brawl" },
    { name: "description", content: "Soon" },
  ];
};

export const headers: HeadersFunction = () => ({
  "Cache-Control": "s-maxage=1, stale-while-revalidate=59",
});

export default function Index() {
  return <div>SSMB</div>;
}
