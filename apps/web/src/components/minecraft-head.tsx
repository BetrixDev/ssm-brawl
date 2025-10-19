import { cn } from "@/lib/utils";
import * as headview3d from "headview3d";
import { useEffect, useRef } from "react";

interface MinecraftHeadProps {
  textureUrl: string;
  className?: string;
}

export function MinecraftHead({ textureUrl, className }: MinecraftHeadProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const viewerRef = useRef<headview3d.SkinViewer | null>(null);

  useEffect(() => {
    if (!canvasRef.current) return;

    // Create the skin viewer
    const viewer = new headview3d.SkinViewer({
      canvas: canvasRef.current,
      width: canvasRef.current.clientWidth,
      height: canvasRef.current.clientHeight,
      skin: textureUrl,
    });

    // Remove background to make it transparent
    viewer.background = null;

    // Enable transparency on the renderer
    if (viewer.renderer) {
      viewer.renderer.setClearColor(0x000000, 0);
    }

    viewer.autoRotate = true;
    viewer.autoRotateSpeed = 0.5;

    viewer.camera.position.set(0, 12.5, 25);

    viewerRef.current = viewer;

    // Handle resize
    const handleResize = () => {
      if (canvasRef.current && viewerRef.current) {
        viewerRef.current.width = canvasRef.current.clientWidth;
        viewerRef.current.height = canvasRef.current.clientHeight;
      }
    };

    window.addEventListener("resize", handleResize);

    // Cleanup
    return () => {
      window.removeEventListener("resize", handleResize);
      if (viewerRef.current) {
        viewerRef.current.dispose();
        viewerRef.current = null;
      }
    };
  }, [textureUrl]);

  return (
    <div className={cn("w-full h-full", className)}>
      <canvas ref={canvasRef} className="w-full h-full" />
    </div>
  );
}
