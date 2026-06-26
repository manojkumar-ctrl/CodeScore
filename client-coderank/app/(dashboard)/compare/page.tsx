"use client";

import { useState, useEffect, useRef } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Trophy, Code2, GitBranch as GithubIcon, Activity, Search } from "lucide-react";
import api from "@/services/api";
import { AnalysisResponse, DashboardResponse } from "@/types";

interface ComparisonData {
  me: AnalysisResponse & DashboardResponse;
  friend: AnalysisResponse & DashboardResponse;
}

export default function ComparePage() {
  const [friendIdentifier, setFriendIdentifier] = useState("");
  const [comparison, setComparison] = useState<ComparisonData | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [suggestions, setSuggestions] = useState<any[]>([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const wrapperRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (wrapperRef.current && !wrapperRef.current.contains(event.target as Node)) {
        setShowSuggestions(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  useEffect(() => {
    const fetchSuggestions = async () => {
      if (friendIdentifier.trim().length < 2) {
        setSuggestions([]);
        return;
      }
      try {
        const res = await api.get(`/users/search?query=${encodeURIComponent(friendIdentifier)}`);
        setSuggestions(res.data);
      } catch (err) {
        console.error("Failed to fetch suggestions", err);
      }
    };
    
    const timeoutId = setTimeout(() => {
      fetchSuggestions();
    }, 300);
    
    return () => clearTimeout(timeoutId);
  }, [friendIdentifier]);

  const handleCompare = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!friendIdentifier.trim()) return;

    setLoading(true);
    setError("");
    try {
      // Assuming a dedicated compare endpoint that returns both users' stats
      const response = await api.get(`/analysis/compare?username=${encodeURIComponent(friendIdentifier)}`);
      setComparison(response.data);
    } catch (err: any) {
      setError(err.response?.data?.message || "Could not find user or fetch comparison data.");
      setComparison(null);
    } finally {
      setLoading(false);
    }
  };

  const renderComparisonRow = (title: string, Icon: React.ElementType, myValue: number, friendValue: number) => {
    const iWin = myValue > friendValue;
    const friendWins = friendValue > myValue;
    const tie = myValue === friendValue;

    return (
      <div className="flex items-center justify-between p-4 border rounded-md mb-4 bg-card">
        <div className="flex-1 flex flex-col items-center">
          <span className="text-2xl font-bold">{Math.round(myValue)}</span>
          <span className="text-xs text-muted-foreground uppercase tracking-wider">You</span>
          {iWin && <span className="text-xs font-bold text-primary mt-1 flex items-center"><Trophy className="h-3 w-3 mr-1"/> Winner</span>}
        </div>
        
        <div className="flex-1 flex flex-col items-center border-x px-4">
          <Icon className="h-6 w-6 text-muted-foreground mb-2" />
          <span className="text-sm font-semibold text-center">{title}</span>
          {tie && <span className="text-xs text-muted-foreground mt-1">Tie</span>}
        </div>

        <div className="flex-1 flex flex-col items-center">
          <span className="text-2xl font-bold">{Math.round(friendValue)}</span>
          <span className="text-xs text-muted-foreground uppercase tracking-wider">Friend</span>
          {friendWins && <span className="text-xs font-bold text-primary mt-1 flex items-center"><Trophy className="h-3 w-3 mr-1"/> Winner</span>}
        </div>
      </div>
    );
  };

  return (
    <div className="container mx-auto px-4 py-8 max-w-4xl space-y-8">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Compare Profiles</h1>
        <p className="text-muted-foreground">See how your developer stats stack up against others.</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Find a Developer</CardTitle>
          <CardDescription>Enter a username or email to compare stats.</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleCompare} className="flex gap-4">
            <div className="flex-1 relative" ref={wrapperRef}>
              <Input 
                placeholder="Username or email" 
                value={friendIdentifier}
                onChange={(e) => {
                  setFriendIdentifier(e.target.value);
                  setShowSuggestions(true);
                }}
                onFocus={() => setShowSuggestions(true)}
                autoComplete="off"
              />
              {showSuggestions && suggestions.length > 0 && (
                <div className="absolute z-10 w-full mt-1 bg-background border rounded-md shadow-lg max-h-60 overflow-auto">
                  {suggestions.map((user) => (
                    <div
                      key={user.id}
                      className="px-4 py-2 cursor-pointer hover:bg-muted"
                      onClick={() => {
                        setFriendIdentifier(user.name);
                        setShowSuggestions(false);
                      }}
                    >
                      <div className="font-medium">{user.name}</div>
                      <div className="text-xs text-muted-foreground">{user.email}</div>
                    </div>
                  ))}
                </div>
              )}
            </div>
            <Button type="submit" disabled={loading || !friendIdentifier.trim()}>
              {loading ? "Comparing..." : (
                <>
                  <Search className="mr-2 h-4 w-4" />
                  Compare
                </>
              )}
            </Button>
          </form>
        </CardContent>
      </Card>

      {comparison && (
        <div className="space-y-2 mt-8 animate-in fade-in slide-in-from-bottom-4 duration-500">
          <h2 className="text-xl font-bold mb-4">Comparison Results</h2>
          
          {renderComparisonRow("Overall Score", Activity, comparison.me.developerScore, comparison.friend.developerScore)}
          {renderComparisonRow("DSA Score", Code2, comparison.me.dsaScore, comparison.friend.dsaScore)}
          {renderComparisonRow("GitHub Score", GithubIcon, comparison.me.githubScore, comparison.friend.githubScore)}
          {renderComparisonRow("Contest Score", Trophy, comparison.me.contestScore, comparison.friend.contestScore)}
        </div>
      )}
    </div>
  );
}
