import { convexQuery } from "@convex-dev/react-query";
import { api } from "@ssm-brawl/backend/convex/_generated/api";
import { useQuery } from "@tanstack/react-query";
import { CheckCircleIcon, ClockIcon, UsersIcon, XCircleIcon } from "lucide-react";
import { Pill, PillIcon, PillStatus } from "../ui/pill";
import { Tooltip, TooltipContent, TooltipTrigger } from "../ui/tooltip";

export function ServerStats() {
  const serverStatus = useQuery(convexQuery(api.serverStatus.getServerStatus, {}));
  const playerCount = useQuery(convexQuery(api.players.getOnlinePlayerCount, {}));

  const actualPlayerCount = serverStatus.data?.isOnline ? playerCount.data : 0;

  return (
    <div className="flex flex-col sm:flex-row items-center justify-center gap-8 pt-8 text-sm">
      <Pill variant="outline">
        <PillStatus>
          {serverStatus.data?.isOnline ? (
            <CheckCircleIcon className="text-emerald-500" size={12} />
          ) : (
            <XCircleIcon className="text-red-500" size={12} />
          )}
        </PillStatus>
        {serverStatus.data?.isOnline ? "Server Online" : "Server Offline"}
      </Pill>
      <Pill variant="outline">
        <PillIcon icon={UsersIcon} />
        {actualPlayerCount} player{actualPlayerCount === 1 ? "" : "s"} online
      </Pill>
      <Tooltip>
        <TooltipTrigger>
          <Pill variant="outline">
            <PillIcon icon={ClockIcon} />
            {serverStatus.data?.uptimePercent7d}% uptime
          </Pill>
        </TooltipTrigger>
        <TooltipContent>Server uptime over the last 7 days </TooltipContent>
      </Tooltip>
    </div>
  );
}
