import { Button } from "@/components/ui/button";
import { Link } from "@tanstack/react-router";
import { Copy, DiscIcon, GithubIcon } from "lucide-react";

export function Footer() {
  return (
    <footer className="py-16 px-6 border-t border-border">
      <div className="container mx-auto">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-12 mb-12">
          {/* Brand */}
          <div className="space-y-4">
            <h3 className="text-2xl font-bold text-foreground">SSM BRAWL</h3>
            <p className="text-muted-foreground leading-relaxed">
              The ultimate Minecraft PvP experience. Battle as powerful mobs with unique abilities.
            </p>
          </div>

          {/* Quick Links */}
          <div className="space-y-4">
            <h4 className="text-lg font-semibold text-foreground">Quick Links</h4>
            <ul className="space-y-2">
              <li>
                <a href="#" className="text-muted-foreground hover:text-primary transition-colors">
                  Game Modes
                </a>
              </li>
              <li>
                <a href="#" className="text-muted-foreground hover:text-primary transition-colors">
                  Leaderboards
                </a>
              </li>
              <li>
                <a href="#" className="text-muted-foreground hover:text-primary transition-colors">
                  Rules
                </a>
              </li>
              <li>
                <a href="#" className="text-muted-foreground hover:text-primary transition-colors">
                  Support
                </a>
              </li>
            </ul>
          </div>

          {/* Connect */}
          <div className="space-y-4">
            <h4 className="text-lg font-semibold text-foreground">Connect</h4>
            <div className="space-y-3">
              <div className="flex items-center gap-2 text-muted-foreground">
                <Copy className="h-4 w-4" />
                <span className="font-mono">play.ssmbrawl.com</span>
              </div>
              <div className="flex gap-2">
                <a href="https://discord.gg" target="_blank" rel="noopener noreferrer">
                  <Button variant="outline" size="sm" className="border-primary bg-transparent">
                    <DiscIcon />
                    Discord
                  </Button>
                </a>
                <a
                  href="https://github.com/BetrixDev/ssm-brawl"
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  <Button variant="outline" size="sm" className="border-primary bg-transparent">
                    <GithubIcon />
                    GitHub
                  </Button>
                </a>
              </div>
            </div>
          </div>
        </div>

        {/* Bottom Bar */}
        <div className="pt-8 border-t border-border flex flex-col sm:flex-row items-center justify-between gap-4">
          <p className="text-muted-foreground text-sm">© {new Date().getFullYear()} SSM Brawl.</p>
          <div className="flex gap-6 text-sm">
            <Link
              to="/privacy"
              className="text-muted-foreground hover:text-primary transition-colors"
            >
              Privacy Policy
            </Link>
            <Link
              to="/terms"
              className="text-muted-foreground hover:text-primary transition-colors"
            >
              Terms of Service
            </Link>
          </div>
        </div>
      </div>
    </footer>
  );
}
