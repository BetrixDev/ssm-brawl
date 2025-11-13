import { Button } from "@/components/ui/button";
import { SignedIn, SignedOut, UserButton } from "@daveyplate/better-auth-ui";
import { Link, useLocation } from "@tanstack/react-router";
import { LinkIcon, Menu, X } from "lucide-react";
import { useState } from "react";

export function Header() {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const navLinks = [
    { href: "/", label: "HOME" },
    { href: "/#features", label: "FEATURES" },
    { href: "/#mobs", label: "MOBS" },
    { href: "/leaderboard", label: "LEADERBOARD" },
    { href: "/#stats", label: "STATS" },
  ];

  return (
    <header className="fixed top-0 left-0 right-0 z-50 bg-background/50 backdrop-blur-md border-b border-border">
      <nav className="container mx-auto px-6 py-4">
        <div className="flex items-center justify-between">
          {/* Logo */}
          <Link to="/" className="flex items-center gap-2">
            <div className="text-2xl font-bold tracking-wider">
              <span className="text-foreground">SSM</span>
              <span className="text-primary"> BRAWL</span>
            </div>
          </Link>

          {/* Desktop Navigation */}
          <div className="hidden lg:flex items-center gap-8">
            {navLinks.map((link) => (
              <Link
                key={link.href}
                to={link.href}
                className="text-sm font-semibold text-muted-foreground hover:text-primary transition-colors tracking-wide"
              >
                {link.label}
              </Link>
            ))}
          </div>

          {/* Desktop CTA */}
          <div className="hidden lg:flex items-center gap-3">
            <UserButtons />
          </div>

          {/* Mobile Menu Button */}
          <Button
            variant="ghost"
            size="icon"
            className="lg:hidden text-foreground"
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          >
            {mobileMenuOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
          </Button>
        </div>

        {/* Mobile Menu */}
        {mobileMenuOpen && (
          <div className="lg:hidden mt-4 pb-4 space-y-4 animate-in slide-in-from-top-5 duration-200">
            {navLinks.map((link) => (
              <Link
                key={link.href}
                to={link.href}
                className="block text-sm font-semibold text-muted-foreground hover:text-primary transition-colors tracking-wide py-2"
                onClick={() => setMobileMenuOpen(false)}
              >
                {link.label}
              </Link>
            ))}
            <div className="flex justify-center">
              <UserButtons />
            </div>
          </div>
        )}
      </nav>
    </header>
  );
}

export function UserButtons({ muted }: { muted?: boolean }) {
  const pathname = useLocation({
    select: (location) => location.pathname,
  });

  return (
    <div className="flex items-center gap-2 z-10">
      <SignedIn>
        <UserButton
          size="sm"
          className="hidden md:inline-flex text-foreground hover:bg-secondary/75 bg-secondary/50 border"
          additionalLinks={[
            {
              href: "/auth/link-account",
              icon: <LinkIcon className="h-4 w-4" />,
              label: "Link Account",
              signedIn: true,
              separator: true,
            },
          ]}
        />
      </SignedIn>
      <SignedOut>
        <Link
          to="/auth/$authView"
          search={{ redirectTo: pathname }}
          params={{ authView: "sign-in" }}
        >
          <Button variant="ghost" className="hidden md:inline-flex">
            Log In
          </Button>
        </Link>
        <Link
          to="/auth/$authView"
          search={{ redirectTo: pathname }}
          params={{ authView: "sign-up" }}
        >
          <Button variant={muted ? "outline" : "default"}>Sign Up</Button>
        </Link>
      </SignedOut>
      <Button variant="ghost" size="icon" className="md:hidden">
        <Menu className="h-5 w-5" />
        <span className="sr-only">Open menu</span>
      </Button>
    </div>
  );
}
