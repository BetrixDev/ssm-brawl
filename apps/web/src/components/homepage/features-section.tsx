import { Card } from "@/components/ui/card";
import { Link } from "@tanstack/react-router";
import { ChevronRightIcon, ChevronsLeftRightEllipsis, Sword, Trophy, Users } from "lucide-react";
import { motion } from "motion/react";

const features = [
  {
    icon: Sword,
    title: "UNIQUE ABILITIES",
    description:
      "Master powerful mob abilities and devastating combos to dominate the battlefield.",
  },
  {
    icon: Users,
    title: "TEAM BATTLES",
    description: "Join forces with friends in intense team-based combat modes.",
  },
  {
    icon: Trophy,
    title: "RANKED SYSTEM",
    description: "Climb the leaderboards and prove you're the ultimate brawler.",
    learnMoreUrl: "/",
  },
  {
    icon: ChevronsLeftRightEllipsis,
    title: "CUSTOM MODES",
    description: "Create customized minigames for everyone to enjoy!",
    learnMoreUrl: "/",
  },
];

export function FeaturesSection() {
  return (
    <section id="features" className="relative py-24 px-6">
      <div className="container mx-auto">
        {/* Section Header */}
        <div className="text-center mb-16 space-y-4">
          <h2 className="text-5xl md:text-6xl font-bold text-primary-foreground">
            THE ULTIMATE <span className="text-primary">PVP EXPERIENCE</span>
          </h2>
          <p className="text-muted-foreground text-lg max-w-2xl mx-auto leading-relaxed">
            Battle as your favorite Minecraft mobs with unique abilities and special moves. Every
            match is a new challenge in this action-packed arena.
          </p>
        </div>

        {/* Features Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {features.map((feature, index) => (
            <Card
              key={index}
              className="bg-card border-border p-6 hover:border-primary/50 transition-colors group"
            >
              <div className="space-y-4">
                <div className="w-12 h-12 rounded-lg bg-primary/10 flex items-center justify-center group-hover:bg-primary/20 transition-colors">
                  <feature.icon className="h-6 w-6 text-primary" />
                </div>
                <h3 className="text-xl font-bold text-foreground">{feature.title}</h3>
                <p className="text-muted-foreground leading-relaxed">{feature.description}</p>
                {feature.learnMoreUrl !== undefined && (
                  <Link
                    to={feature.learnMoreUrl}
                    className="text-xs font-light text-primary tracking-wider hover:underline"
                  >
                    <motion.div className="flex items-center" initial="rest" whileHover="hover">
                      Learn More
                      <motion.span
                        variants={{
                          rest: { opacity: 0, x: -10 },
                          hover: { opacity: 1, x: 0, rotate: 180 },
                        }}
                        transition={{ duration: 0.15, ease: "easeOut" }}
                      >
                        <ChevronRightIcon className="w-4 h-4 ml-1" />
                      </motion.span>
                    </motion.div>
                  </Link>
                )}
              </div>
            </Card>
          ))}
        </div>
      </div>
    </section>
  );
}
