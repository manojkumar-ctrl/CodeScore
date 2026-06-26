"use client";

import Link from "next/link";
import { useAuth } from "@/context/AuthContext";
import { Button } from "@/components/ui/button";
import { LayoutDashboard, LogOut, Code2, Users, Trophy, UserCircle } from "lucide-react";
import { usePathname } from "next/navigation";

export default function Navbar() {
  const { isAuthenticated, logout } = useAuth();
  const pathname = usePathname();

  const navItems = [
    { name: "Dashboard", href: "/dashboard", icon: LayoutDashboard },
    { name: "Compare", href: "/compare", icon: Users },
    { name: "Leaderboard", href: "/leaderboard", icon: Trophy },
    { name: "Profile", href: "/profile", icon: UserCircle },
  ];

  return (
    <nav className="border-b bg-background sticky top-0 z-50">
      <div className="container mx-auto px-4 h-16 flex items-center justify-between">
        <Link href={isAuthenticated ? "/dashboard" : "/"} className="flex items-center gap-2 font-bold text-xl">
          <Code2 className="h-6 w-6" />
          <span>CodeScore</span>
        </Link>

        {isAuthenticated ? (
          <div className="flex items-center gap-6">
            <div className="hidden md:flex items-center gap-1">
              {navItems.map((item) => (
                <Link key={item.name} href={item.href}>
                  <Button
                    variant={pathname === item.href ? "secondary" : "ghost"}
                    className="flex items-center gap-2"
                  >
                    <item.icon className="h-4 w-4" />
                    {item.name}
                  </Button>
                </Link>
              ))}
            </div>
            <Button variant="outline" onClick={logout} className="flex items-center gap-2">
              <LogOut className="h-4 w-4" />
              <span className="hidden sm:inline">Logout</span>
            </Button>
          </div>
        ) : (
          <div className="flex items-center gap-4">
            <Link href="/login">
              <Button variant="ghost">Login</Button>
            </Link>
            <Link href="/register">
              <Button>Get Started</Button>
            </Link>
          </div>
        )}
      </div>
    </nav>
  );
}
