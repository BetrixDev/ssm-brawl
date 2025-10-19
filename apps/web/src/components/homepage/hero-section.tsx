import { Button } from "@/components/ui/button";
import { Play } from "lucide-react";
import { ServerIPButton } from "../server-ip-button";
import { ServerStats } from "./server-stats";

export function HeroSection() {
  return (
    <section
      id="home"
      className="relative min-h-screen flex items-center justify-center overflow-hidden pt-20"
    >
      {/* Background Image */}
      <div className="absolute inset-0 z-0">
        <img
          src="/background.jpg"
          alt="Background"
          className="w-full h-full object-cover opacity-20 blur-xs"
        />
        <div className="absolute inset-0 bg-gradient-to-b from-background/80 via-background/60 to-background" />
      </div>

      {/* Diagonal Accent Stripes */}
      <div className="absolute top-0 left-0 w-32 h-full bg-primary/20 -skew-x-12 -translate-x-16" />
      <div className="absolute top-0 right-0 w-32 h-full bg-primary/20 -skew-x-12 translate-x-16" />

      {/* Hero Content */}
      <div className="relative z-10 container mx-auto px-6 text-center">
        <div className="max-w-5xl mx-auto space-y-8">
          <h1 className="text-7xl md:text-9xl font-bold tracking-tighter text-balance">
            <span className="text-foreground">SUPER SMASH</span>
            <br />
            <span className="text-primary">MOBS BRAWL</span>
          </h1>

          <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
            <Button className="bg-primary text-primary-foreground hover:bg-primary/90 font-semibold px-8 shadow-l">
              <Play className="h-5 w-5" />
              PLAY NOW
            </Button>
            <ServerIPButton />
          </div>
        </div>

        <ServerStats />
      </div>

      {/* Bottom Gradient */}
      <div className="absolute bottom-0 left-0 right-0 h-32 bg-gradient-to-t from-background to-transparent z-10" />
    </section>
  );
}
