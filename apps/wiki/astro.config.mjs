// @ts-check
import cloudflare from "@astrojs/cloudflare";
import starlight from "@astrojs/starlight";
import { defineConfig } from "astro/config";
import starlightThemeRapide from "starlight-theme-rapide";

// https://astro.build/config
export default defineConfig({
  output: "server",
  adapter: cloudflare({
    imageService: "compile",
  }),
  integrations: [
    starlight({
      title: "Super Smash Mobs Brawl Wiki",
      plugins: [starlightThemeRapide()],
      social: [
        { icon: "github", label: "GitHub", href: "https://ssmbrawl.com/social/github" },
        { icon: "discord", label: "Discord", href: "https://ssmbrawl.com/social/discord" },
        { icon: "external", label: "Website", href: "https://ssmbrawl.com" },
      ],
      sidebar: [
        {
          label: "Guides",
          autogenerate: { directory: "guides" },
        },
        {
          label: "Kits",
          autogenerate: { directory: "kits" },
        },
      ],
      customCss: ["./src/custom.css"],
    }),
  ],
});
