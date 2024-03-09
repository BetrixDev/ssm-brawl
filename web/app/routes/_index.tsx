import type { MetaFunction } from "@remix-run/node";

export const meta: MetaFunction = () => {
  return [
    { title: "Super Smash Mobs Brawl" },
    { name: "description", content: "Soon" },
  ];
};

export default function Index() {
  return <div>SSMB</div>;
}
