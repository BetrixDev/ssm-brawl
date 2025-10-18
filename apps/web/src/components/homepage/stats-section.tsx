import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";

export function StatsSection() {
  return (
    <section id="stats" className="relative py-24 px-6">
      <div className="container mx-auto">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
          {/* Left Column - Image */}
          <div className="relative">
            <div className="aspect-video relative overflow-hidden rounded-lg border border-border">
              <img
                src="/minecraft-pvp-arena-battle-scene.png"
                alt="Arena Battle"
                className="w-full h-full object-cover"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-background/80 to-transparent" />
            </div>
            {/* Accent Box */}
            <div className="absolute -bottom-6 -right-6 w-48 h-48 bg-primary/10 -skew-x-12 rounded-lg" />
          </div>

          {/* Right Column - Content */}
          <div className="space-y-8">
            <div>
              <h2 className="text-5xl md:text-6xl font-bold mb-6 text-primary-foreground">
                NEVER BE THE <span className="text-primary">SAME</span>
              </h2>
              <p className="text-muted-foreground text-lg leading-relaxed mb-8">
                Every match brings new challenges and opportunities. With constantly evolving
                strategies, unique mob combinations, and skill-based gameplay, no two battles are
                ever alike. Adapt, overcome, and dominate the arena.
              </p>
              <Button
                size="lg"
                className="bg-primary text-primary-foreground hover:bg-primary/90 font-semibold"
              >
                FULL STORY
              </Button>
            </div>

            {/* Stats Grid */}
            <div className="grid grid-cols-2 gap-6 pt-8">
              <Card className="bg-card border-border p-6">
                <div className="text-4xl font-bold text-primary mb-2">50+</div>
                <div className="text-muted-foreground">Unique Abilities</div>
              </Card>
              <Card className="bg-card border-border p-6">
                <div className="text-4xl font-bold text-primary mb-2">10+</div>
                <div className="text-muted-foreground">Game Modes</div>
              </Card>
              <Card className="bg-card border-border p-6">
                <div className="text-4xl font-bold text-primary mb-2">24/7</div>
                <div className="text-muted-foreground">Active Servers</div>
              </Card>
              <Card className="bg-card border-border p-6">
                <div className="text-4xl font-bold text-primary mb-2">100K+</div>
                <div className="text-muted-foreground">Players</div>
              </Card>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
