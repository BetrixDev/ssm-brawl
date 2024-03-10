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
  return (
    <div className="bg-background p-2">
      <div className="rounded-lg border border-1 border-input p-2">
        <h1 className="text-gradient font-kanit font-normal tracking-wider text-5xl">
          SSMB
        </h1>
      </div>
    </div>
  );
}
