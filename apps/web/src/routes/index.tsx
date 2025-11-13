import { FeaturesSection } from "@/components/homepage/features-section";
import { HeroSection } from "@/components/homepage/hero-section";
import { MobsShowcase } from "@/components/homepage/mobs-showcase";
import { StatsSection } from "@/components/homepage/stats-section";
import { createFileRoute } from "@tanstack/react-router";

export const Route = createFileRoute("/")({
  component: HomeComponent,
});

function HomeComponent() {
  return (
    <main className="min-h-screen bg-background overflow-hidden">
      <HeroSection />
      <FeaturesSection />
      <MobsShowcase />
      <StatsSection />
    </main>
  );
}
