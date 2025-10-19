import { MinecraftHead } from "./minecraft-head";

const MOB_HEAD_MAP = {
  creeper: "/mobs/creeper.png",
  iron_golem: "/mobs/iron_golem.png",
  blaze: "/mobs/blaze.png",
  enderman: "/mobs/enderman.png",
  skeleton: "/mobs/skeleton.png",
} as const;

type MobHeadProps = {
  mob: keyof typeof MOB_HEAD_MAP;
  className?: string;
};

export function MobHead({ mob, className }: MobHeadProps) {
  return <MinecraftHead textureUrl={MOB_HEAD_MAP[mob]} className={className} />;
}
