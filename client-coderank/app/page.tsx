"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { ArrowRight, BarChart3, GitBranch, Trophy, Users, Activity } from "lucide-react";
import Link from "next/link";

export default function LandingPage() {
  const { isAuthenticated, isLoading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!isLoading && isAuthenticated) {
      router.push("/dashboard");
    }
  }, [isAuthenticated, isLoading, router]);
  const features = [
    {
      title: "Developer Score",
      description: "Get a comprehensive score based on your DSA skills, open-source contributions, and contest ratings.",
      icon: Activity,
    },
    {
      title: "LeetCode Analysis",
      description: "Track your problem-solving progress across different difficulties and monitor your contest performance.",
      icon: BarChart3,
    },
    {
      title: "GitHub Analysis",
      description: "Analyze your repository impact, stars, and contribution consistency over time.",
      icon: GitBranch,
    },
    {
      title: "Profile Comparison",
      description: "Compare your developer metrics side-by-side with friends or colleagues.",
      icon: Users,
    },
    {
      title: "Leaderboards",
      description: "See where you stand among top developers in overall score, DSA, and open-source contributions.",
      icon: Trophy,
    },
  ];

  return (
    <div className="flex flex-col min-h-screen">
      {/* Hero Section */}
      <section className="flex-1 flex flex-col items-center justify-center text-center px-4 py-32 border-b">
        <div className="max-w-3xl space-y-8">
          <h1 className="text-5xl md:text-7xl font-bold tracking-tight">
            Analyze Your Developer Journey
          </h1>
          <p className="text-xl text-muted-foreground max-w-2xl mx-auto">
            Connect GitHub and LeetCode to generate your Developer Score, compare with friends, and track your growth.
          </p>
          <div className="flex flex-col sm:flex-row items-center justify-center gap-4 pt-4">
            <Link href="/register">
              <Button size="lg" className="h-12 px-8">
                Get Started
                <ArrowRight className="ml-2 h-4 w-4" />
              </Button>
            </Link>
            <Link href="/login">
              <Button size="lg" variant="outline" className="h-12 px-8">
                Login
              </Button>
            </Link>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-24 px-4 container mx-auto">
        <div className="mb-16 text-center">
          <h2 className="text-3xl font-bold tracking-tight mb-4">Everything you need to track your growth</h2>
          <p className="text-muted-foreground">Comprehensive analytics for the modern developer.</p>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 max-w-6xl mx-auto">
          {features.map((feature) => (
            <Card key={feature.title} className="bg-background border-border">
              <CardHeader>
                <feature.icon className="h-10 w-10 mb-4 text-primary" />
                <CardTitle>{feature.title}</CardTitle>
              </CardHeader>
              <CardContent>
                <CardDescription className="text-base">{feature.description}</CardDescription>
              </CardContent>
            </Card>
          ))}
        </div>
      </section>
    </div>
  );
}
