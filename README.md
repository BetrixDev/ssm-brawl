# super-smash-mobs-brawl

![GitHub last commit](https://img.shields.io/github/last-commit/BetrixDev/ssm-brawl)
[![GitHub milestone details](https://img.shields.io/github/milestones/progress/BetrixDev/ssm-brawl/1)](https://github.com/BetrixDev/ssm-brawl/milestone/1)

> A modern recreation of the beloved Mineplex minigame Super Smash Mobs

Super Smash Mobs Brawl is a full-stack Minecraft server implementation that brings back the classic Super Smash Mobs experience. This project includes a custom Minecraft server, web dashboard, API backend, and documentation wiki.

---

## ✨ Features

- 🎮 Custom Minecraft server with tweaked Super Smash Mobs gameplay
- 🌐 Web dashboard for server statistics and player profiles
- 📚 Comprehensive wiki for game mechanics and documentation
- 🔌 RESTful API for api -> plugin communication
- 🔌 RPC API for api -> TypeScript services
- 🐳 Containerized production deployments

## 🏗️ Architecture

This monorepo contains multiple interconnected services:

- **Minecraft Server** - The core game server (port `25565`)
- **Web Dashboard** - Next.js frontend (port `3001`)
- **API Backend** - RESTful API service (port `3000`)
- **Wiki** - Documentation site (port `1337`)

## 🚀 Getting Started

### Prerequisites

Ensure you have the following tools installed:

| Tool                                      | Purpose                 | Download         |
| ----------------------------------------- | ----------------------- | ---------------- |
| [Node.js](https://nodejs.org/en/download) | Runtime environment     | v24+ recommended |
| [Bun](https://bun.sh/)                    | Fast JavaScript runtime | Latest version   |
| [PNPM](https://pnpm.io/installation)      | Package manager         | v9.1.0           |
| [Docker](https://www.docker.com/)         | Containerization        | Latest version   |
| Latest JDK Version                        | Building plugin         | Latest version   |

<details>
<summary>Downloading Latest JDK Version</summary>

macOS:

```bash
brew install openjdk
```

Windows:

```bash
winget install Microsoft.OpenJDK.25
```

Linux:

```bash
sudo apt update
sudo apt install openjdk-25-jdk
```

</details>

### Installation

1. **Clone the repository**

```bash
   git clone https://github.com/BetrixDev/ssm-brawl.git
   cd ssm-brawl
```

2. **Install dependencies**

```bash
   pnpm install
```

3. **Start all services**

```bash
   pnpm dev
```

### Accessing Services

Once running, the following services will be available:

| Service   | URL                   | Description                                |
| --------- | --------------------- | ------------------------------------------ |
| Website   | http://localhost:3001 | Web dashboard and player portal            |
| API       | http://localhost:3000 | Backend API endpoints                      |
| Wiki      | http://localhost:1337 | Game documentation                         |
| Minecraft | `localhost:25565`     | Game server (connect via Minecraft client) |

## 📦 Project Structure

```
super-smash-mobs-brawl/
├── tooling/
│   └── database-dev/        # Tooling for local database development
├── apps/
│   ├── web/                 # Frontend application with Tanstack Start
│   ├── backend/             # Backend service with Elysia and ORPC
│   └── wiki/                # Documentation site with Astro Starlight __(subject to change)__
│       └── src/content/docs # Wiki content Authoring Content Guide
├── plugin/                  # Minecraft server plugin
├── docker-compose.yml       # Service orchestration
└── package.json             # Workspace configuration
```

## 🛠️ Development

### Available Scripts

- `pnpm dev` - Start all services in development mode
- `pnpm build` - Build all packages for production
- `pnpm test` - Run test suites across all packages
- `pnpm format` - Format code with Prettier

### Environment Variables

Most environment variables needed for local development are defaulted to reasonable values. You can check out the `.env.development.example` file for variables you can tweak in your local environment.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Inspired by the original Mineplex Super Smash Mobs minigame
- Early development is based on [SSMOS](https://github.com/Whoneedspacee/SSMOS)
- Built with modern web technologies and Minecraft server frameworks

---

**Note:** This is a fan project and is not affiliated with Mineplex or Mojang.
