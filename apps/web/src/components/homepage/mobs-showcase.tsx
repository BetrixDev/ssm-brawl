import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { MobHead } from "../mob-head";

const mobs = [
  {
    id: "iron_golem",
    name: "IRON GOLEM",
    description: "Tank class with devastating melee attacks and high defense.",
  },
  {
    name: "BLAZE",
    id: "blaze",
    description: "Ranged attacker with fire-based abilities and aerial mobility.",
  },
  {
    name: "ENDERMAN",
    id: "enderman",
    description: "Assassin class with teleportation and high burst damage.",
  },
  {
    name: "CREEPER",
    id: "creeper",
    description: "Explosive specialist with area denial and surprise tactics.",
  },
  {
    name: "SKELETON",
    id: "skeleton",
    description: "Precision archer with long-range attacks and mobility.",
  },
] as const;

export function MobsShowcase() {
  return (
    <section id="mobs" className="relative py-24 px-6 bg-secondary/30">
      <div className="container mx-auto">
        {/* Section Header */}
        <div className="flex items-center justify-between mb-12">
          <div>
            <h2 className="text-5xl md:text-6xl font-bold mb-4 text-primary-foreground">
              CHOOSE YOUR <span className="text-primary">FIGHTER</span>
            </h2>
            <p className="text-muted-foreground text-lg leading-relaxed">
              Each mob has unique abilities and playstyles. Master them all to become unstoppable.
            </p>
          </div>
          <div className="hidden md:flex gap-2 text-primary-foreground">
            <Button variant="outline" size="icon" className="border-primary bg-transparent">
              <ChevronLeft className="h-5 w-5" />
            </Button>
            <Button variant="outline" size="icon" className="border-primary bg-transparent">
              <ChevronRight className="h-5 w-5" />
            </Button>
          </div>
        </div>

        {/* Mobs Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-6">
          {mobs.map((mob, index) => (
            <Card
              key={index}
              className="bg-card border-border overflow-hidden group hover:border-primary/50 transition-all hover:scale-105 p-0"
            >
              <div className="aspect-[3/4] relative overflow-hidden bg-radial from-primary to-secondary">
                <MobHead mob={mob.id} className="w-full h-full absolute pointer-events-none" />
              </div>
              <div className="p-4 space-y-2">
                <h3 className="text-lg font-bold text-foreground">{mob.name}</h3>
                <p className="text-sm text-muted-foreground leading-relaxed">{mob.description}</p>
              </div>
            </Card>
          ))}
        </div>
      </div>
    </section>
  );
}
