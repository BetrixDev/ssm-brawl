import { cronJobs } from "convex/server";
import { internal } from "./_generated/api";

const crons = cronJobs();

crons.interval("prune server status", { hours: 24 }, internal.serverStatus.pruneServerStatus, {});

export default crons;
