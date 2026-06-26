"use client";

import { useEffect, useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { LeaderboardEntry } from "@/types";
import api from "@/services/api";

export default function LeaderboardPage() {
  const [activeCategory, setActiveCategory] = useState("overall");
  const [leaderboardData, setLeaderboardData] = useState<LeaderboardEntry[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchLeaderboard(activeCategory);
  }, [activeCategory]);

  const fetchLeaderboard = async (category: string) => {
    setLoading(true);
    try {
      // Assuming backend has an endpoint like /analysis/leaderboard?category=overall
      const response = await api.get(`/analysis/leaderboard?category=${category}`);
      setLeaderboardData(response.data || []);
    } catch (error) {
      console.error("Failed to fetch leaderboard", error);
      setLeaderboardData([]); // Fallback to empty
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container mx-auto px-4 py-8 max-w-5xl space-y-8">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Leaderboard</h1>
        <p className="text-muted-foreground">See how you rank against other developers.</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Top Developers</CardTitle>
          <CardDescription>Top 10 users across different categories</CardDescription>
        </CardHeader>
        <CardContent>
          <Tabs defaultValue="overall" onValueChange={setActiveCategory} className="w-full">
            <TabsList className="grid w-full grid-cols-4 mb-6">
              <TabsTrigger value="overall">Overall</TabsTrigger>
              <TabsTrigger value="dsa">DSA</TabsTrigger>
              <TabsTrigger value="github">GitHub</TabsTrigger>
              <TabsTrigger value="contest">Contest</TabsTrigger>
            </TabsList>
            
            <div className="rounded-md border">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead className="w-[100px] text-center">Rank</TableHead>
                    <TableHead>Username</TableHead>
                    <TableHead className="text-right">Score</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {loading ? (
                    <TableRow>
                      <TableCell colSpan={3} className="text-center py-8 text-muted-foreground">
                        Loading leaderboard...
                      </TableCell>
                    </TableRow>
                  ) : leaderboardData.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={3} className="text-center py-8 text-muted-foreground">
                        No data available for this category yet.
                      </TableCell>
                    </TableRow>
                  ) : (
                    leaderboardData.map((entry) => (
                      <TableRow key={entry.username}>
                        <TableCell className="text-center font-medium">
                          {entry.rank === 1 ? "🥇" : entry.rank === 2 ? "🥈" : entry.rank === 3 ? "🥉" : entry.rank}
                        </TableCell>
                        <TableCell className="font-medium">{entry.username}</TableCell>
                        <TableCell className="text-right font-bold">{Math.round(entry.score)}</TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            </div>
          </Tabs>
        </CardContent>
      </Card>
    </div>
  );
}
