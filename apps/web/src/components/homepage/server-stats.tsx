export function ServerStats() {
  return (
    <div className="flex flex-col sm:flex-row items-center justify-center gap-8 pt-8 text-sm">
      <div className="flex items-center gap-2">
        <div className="w-2 h-2 rounded-full bg-green-500 animate-pulse" />
        <span className="text-muted-foreground">ONLINE</span>
      </div>
      <div className="flex items-center gap-2">
        <span className="text-foreground font-semibold">1,247</span>
        <span className="text-muted-foreground">PLAYERS</span>
      </div>
      <div className="flex items-center gap-2">
        <span className="text-foreground font-semibold">24/7</span>
        <span className="text-muted-foreground">UPTIME</span>
      </div>
    </div>
  );
}
