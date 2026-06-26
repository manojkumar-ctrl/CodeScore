"use client";

import { useEffect, useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/context/AuthContext";
import { useRouter } from "next/navigation";
import api from "@/services/api";
import { DashboardResponse, AnalysisResponse } from "@/types";
import { Trophy, GitBranch as GithubIcon, Code2, RefreshCcw, CheckCircle2, XCircle } from "lucide-react";

export default function DashboardPage() {
  const { isAuthenticated, isLoading: authLoading } = useAuth();
  const router = useRouter();
  const [dashboardData, setDashboardData] = useState<DashboardResponse | null>(null);
  const [analysisData, setAnalysisData] = useState<AnalysisResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push("/login");
    } else if (isAuthenticated) {
      fetchData();
    }
  }, [isAuthenticated, authLoading, router]);

  const fetchData = async () => {
    try {
      const [dashRes, analysisRes] = await Promise.all([
        api.get("/analysis/dashboard"),
        api.get("/analysis/latest") // Fetch latest saved analysis without re-running
      ]);
      setDashboardData(dashRes.data);
      setAnalysisData(analysisRes.data);
    } catch (error: any) {
      // 400 or 404 is expected when no profiles are connected yet.
      // We will handle the empty state gracefully in the render method below.
    } finally {
      setLoading(false);
    }
  };

  const handleRefresh = async () => {
    setRefreshing(true);
    try {
      const response = await api.post("/analysis/run");
      setAnalysisData(response.data);
      // Fetch dashboard again to sync
      const dashRes = await api.get("/analysis/dashboard");
      setDashboardData(dashRes.data);
    } catch (error) {
      console.error("Failed to refresh analysis", error);
    } finally {
      setRefreshing(false);
    }
  };

  if (loading || authLoading) {
    return <div className="flex items-center justify-center min-h-[50vh]">Loading dashboard...</div>;
  }

  if (!dashboardData || !analysisData) {
    return (
      <div className="container mx-auto px-4 flex flex-col items-center justify-center min-h-[70vh] max-w-2xl text-center space-y-6">
        <div className="bg-muted p-6 rounded-full mb-4">
          <Code2 className="h-12 w-12 text-muted-foreground" />
        </div>
        <h2 className="text-3xl font-bold tracking-tight">Your Dashboard is Empty</h2>
        <p className="text-muted-foreground text-lg">
          It looks like you haven't connected your profiles yet or your data hasn't been generated. 
          Please connect your GitHub and LeetCode accounts to generate your developer stats.
        </p>
        <Button onClick={() => router.push("/onboarding")} size="lg" className="mt-4">
          Connect Profiles
        </Button>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8 max-w-6xl space-y-8">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Dashboard</h1>
          <p className="text-muted-foreground">Overview of your developer journey</p>
        </div>
        <Button onClick={handleRefresh} disabled={refreshing} variant="outline" className="flex items-center gap-2">
          <RefreshCcw className={`h-4 w-4 ${refreshing ? "animate-spin" : ""}`} />
          Refresh Analysis
        </Button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Main Developer Score Card */}
        <Card className="col-span-1 md:col-span-3 border-2">
          <CardContent className="flex flex-col items-center justify-center py-12">
            <h2 className="text-sm font-medium text-muted-foreground mb-2 uppercase tracking-wider">Overall Developer Score</h2>
            <div className="text-6xl md:text-8xl font-black mb-4">
              {Math.round(dashboardData.developerScore)}<span className="text-3xl text-muted-foreground">/100</span>
            </div>
            <div className="inline-flex items-center rounded-full border px-6 py-2 text-lg font-semibold transition-colors focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2">
              {dashboardData.classification}
            </div>
          </CardContent>
        </Card>

        {/* Score Breakdown */}
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-lg flex items-center gap-2"><Code2 className="h-5 w-5" /> DSA Score</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-4xl font-bold">{Math.round(analysisData.dsaScore)}</div>
            <p className="text-sm text-muted-foreground mt-1">Based on LeetCode problems</p>
          </CardContent>
        </Card>
        
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-lg flex items-center gap-2"><GithubIcon className="h-5 w-5" /> GitHub Score</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-4xl font-bold">{Math.round(analysisData.githubScore)}</div>
            <p className="text-sm text-muted-foreground mt-1">Based on repositories & stars</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-lg flex items-center gap-2"><Trophy className="h-5 w-5" /> Contest Score</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-4xl font-bold">{Math.round(analysisData.contestScore)}</div>
            <p className="text-sm text-muted-foreground mt-1">Based on contest ratings</p>
          </CardContent>
        </Card>
      </div>

      {/* Detailed Stats */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2"><Code2 className="h-5 w-5" /> Detailed LeetCode Stats</CardTitle>
          </CardHeader>
          <CardContent className="grid grid-cols-2 sm:grid-cols-3 gap-6">
            <div className="space-y-1">
              <p className="text-sm text-muted-foreground">Total Solved</p>
              <p className="text-3xl font-bold">{dashboardData.leetcodeSolved}</p>
            </div>
            <div className="space-y-1">
              <p className="text-sm text-muted-foreground">Contests</p>
              <p className="text-3xl font-bold">{dashboardData.leetcodeContests}</p>
            </div>
            <div className="space-y-1">
              <p className="text-sm text-muted-foreground">Rating</p>
              <p className="text-3xl font-bold">{Math.round(dashboardData.contestRating)}</p>
            </div>
            <div className="space-y-1 border-t pt-4">
              <p className="text-sm font-medium text-black">Easy</p>
              <p className="text-2xl font-semibold">{dashboardData.leetcodeEasy}</p>
            </div>
            <div className="space-y-1 border-t pt-4">
              <p className="text-sm font-medium text-black">Medium</p>
              <p className="text-2xl font-semibold">{dashboardData.leetcodeMedium}</p>
            </div>
            <div className="space-y-1 border-t pt-4">
              <p className="text-sm font-medium text-black">Hard</p>
              <p className="text-2xl font-semibold">{dashboardData.leetcodeHard}</p>
            </div>
          </CardContent>
        </Card>
        
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2"><GithubIcon className="h-5 w-5" /> Detailed GitHub Stats</CardTitle>
          </CardHeader>
          <CardContent className="grid grid-cols-2 gap-6">
            <div className="space-y-1">
              <p className="text-sm text-muted-foreground">Total Commits</p>
              <p className="text-3xl font-bold">{dashboardData.githubCommits}</p>
            </div>
            <div className="space-y-1">
              <p className="text-sm text-muted-foreground">Public Repos</p>
              <p className="text-3xl font-bold">{dashboardData.githubRepos}</p>
            </div>
            <div className="space-y-1 border-t pt-4">
              <p className="text-sm font-medium text-black">Stars Earned</p>
              <p className="text-2xl font-semibold">{dashboardData.githubStars}</p>
            </div>
            <div className="space-y-1 border-t pt-4">
              <p className="text-sm font-medium text-black">Followers</p>
              <p className="text-2xl font-semibold">{dashboardData.githubFollowers}</p>
            </div>
          </CardContent>
        </Card>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Strengths */}
        <Card>
          <CardHeader>
            <CardTitle>Strengths</CardTitle>
            <CardDescription>Areas where you excel</CardDescription>
          </CardHeader>
          <CardContent>
            {analysisData.strengths && analysisData.strengths.length > 0 ? (
              <ul className="space-y-3">
                {analysisData.strengths.map((strength, idx) => (
                  <li key={idx} className="flex items-start gap-3 text-sm">
                    <CheckCircle2 className="h-5 w-5 text-primary shrink-0" />
                    <span>{strength}</span>
                  </li>
                ))}
              </ul>
            ) : (
              <p className="text-sm text-muted-foreground">Keep coding to build up your strengths!</p>
            )}
          </CardContent>
        </Card>

        {/* Improvement Areas */}
        <Card>
          <CardHeader>
            <CardTitle>Improvement Areas</CardTitle>
            <CardDescription>Focus on these to increase your score</CardDescription>
          </CardHeader>
          <CardContent>
            {analysisData.weaknesses && analysisData.weaknesses.length > 0 ? (
              <ul className="space-y-3">
                {analysisData.weaknesses.map((weakness, idx) => (
                  <li key={idx} className="flex items-start gap-3 text-sm">
                    <XCircle className="h-5 w-5 text-muted-foreground shrink-0" />
                    <span>{weakness}</span>
                  </li>
                ))}
              </ul>
            ) : (
              <p className="text-sm text-muted-foreground">You are doing great across the board!</p>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
