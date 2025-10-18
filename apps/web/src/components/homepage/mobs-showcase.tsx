import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { ChevronLeft, ChevronRight } from "lucide-react";

const mobs = [
  {
    name: "IRON GOLEM",
    description: "Tank class with devastating melee attacks and high defense.",
    image: "/minecraft-iron-golem-warrior-pose.jpg",
  },
  {
    name: "BLAZE",
    description: "Ranged attacker with fire-based abilities and aerial mobility.",
    image: "/minecraft-blaze-combat-pose.jpg",
  },
  {
    name: "ENDERMAN",
    description: "Assassin class with teleportation and high burst damage.",
    image: "/minecraft-enderman-battle-stance.jpg",
  },
  {
    name: "CREEPER",
    description: "Explosive specialist with area denial and surprise tactics.",
    image: "/minecraft-creeper-action-pose.jpg",
  },
  {
    name: "SKELETON",
    description: "Precision archer with long-range attacks and mobility.",
    image: "/minecraft-skeleton-archer-pose.jpg",
  },
];

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
              className="bg-card border-border overflow-hidden group hover:border-primary/50 transition-all hover:scale-105"
            >
              <div className="aspect-[3/4] relative overflow-hidden bg-secondary">
                <img
                  src={mob.image || "/placeholder.svg"}
                  alt={mob.name}
                  className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500"
                />
                <div className="absolute inset-0 bg-gradient-to-t from-background via-background/50 to-transparent" />
              </div>
              <div className="p-4 space-y-2">
                <h3 className="text-lg font-bold text-foreground">{mob.name}</h3>
                <p className="text-sm text-muted-foreground leading-relaxed">{mob.description}</p>
              </div>
            </Card>
          ))}
        </div>
      </div>

      {/* Diagonal Accent */}
      <div className="absolute bottom-0 left-0 w-64 h-64 bg-primary/5 -skew-x-12 -translate-x-32 translate-y-32" />
    </section>
  );
}
