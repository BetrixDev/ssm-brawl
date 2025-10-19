import { Button } from "@/components/ui/button";
import { Copy, Play } from "lucide-react";
import { useState } from "react";
import { ServerStats } from "./server-stats";

export function HeroSection() {
  const [copied, setCopied] = useState(false);

  const copyIP = () => {
    navigator.clipboard.writeText("play.ssmbrawl.com");
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

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
          className="w-full h-full object-cover opacity-40 blur-xs"
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
            <Button
              size="lg"
              className="bg-primary text-primary-foreground hover:bg-primary/90 font-semibold px-8 text-lg"
            >
              <Play className="mr-2 h-5 w-5" />
              PLAY NOW
            </Button>
            <Button
              size="lg"
              variant="outline"
              onClick={copyIP}
              className="border-primary text-foreground hover:bg-primary/10 font-semibold px-8 text-lg bg-transparent"
            >
              <Copy className="mr-2 h-5 w-5" />
              {copied ? "COPIED!" : "play.ssmbrawl.com"}
            </Button>
          </div>
        </div>

        <ServerStats />
      </div>

      {/* Bottom Gradient */}
      <div className="absolute bottom-0 left-0 right-0 h-32 bg-gradient-to-t from-background to-transparent z-10" />
    </section>
  );
}
