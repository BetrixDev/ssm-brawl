# super-smash-mobs-brawl

![GitHub last commit](https://img.shields.io/github/last-commit/BetrixDev/ssm-brawl)

###### Milestones

![GitHub milestone details](https://img.shields.io/github/milestones/progress/BetrixDev/ssm-brawl/1)

A Minecraft server based on the Mineplex minigame Super Smash Mobs.

---

## Running Locally

This project uses [Alchemy](https://alchemy.run) to deploy the website, backend, and wiki, and runs the plugin / server with Gradle locally and a Docker container in production.

1. Download [Node.js](https://nodejs.org/en/download) LTS version
2. Download [PNPM](https://pnpm.io/installation) using the instructions on the website
3. Ensure you have a Cloudflare account
4. Clone the repository
5. Run the following commands:

```bash
pnpm install
pnpm run dev
```

---

Ensure you have the following environment variables set in your `.env` file:

```bash
ALCHEMY_PASSWORD=
ALCHEMY_STATE_TOKEN=
NEON_API_KEY=
```

###### Obtain the NEON_API_KEY from https://neon.com

#### Running just the plugin

```bash
pnpm run dev:plugin
```

#### Running just the alchemy services

```bash
pnpm run dev:ts
```
