// @ts-check
import cloudflare from "@astrojs/cloudflare";
import starlight from "@astrojs/starlight";
import { defineConfig } from "astro/config";
import starlightThemeRapide from "starlight-theme-rapide";

// https://astro.build/config
export default defineConfig({
  server: {
    port: 1337,
  },
  integrations: [
    starlight({
      plugins: [starlightThemeRapide()],
      title: "SSMBrawl Wiki",
      social: [
        { icon: "external", label: "Website", href: "https://ssmbrawl.com" },
        { icon: "github", label: "GitHub", href: "https://github.com/BetrixDev/ssm-brawl" },
        { icon: "discord", label: "Discord", href: "https://discord.gg/ssm-brawl" },
      ],
      sidebar: [
        {
          label: "Getting Started",
          slug: "getting-started",
        },
        {
          label: "Kits",
          autogenerate: { directory: "kits" },
        },
      ],
    }),
  ],
  adapter: cloudflare(),
});
