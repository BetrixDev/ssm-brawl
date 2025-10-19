import { useState } from "react";
import { FlipButton } from "./ui/flip-button";

export function ServerIPButton() {
  const [copied, setCopied] = useState(false);

  const copyIP = () => {
    navigator.clipboard.writeText("play.ssmbrawl.com");
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <FlipButton
      className="font-mono"
      frontClassName="border-border border shadow-s"
      frontText="play.ssmbrawl.com"
      backText={copied ? "Copied!" : "Copy IP"}
      onClick={copyIP}
    />
  );
}
