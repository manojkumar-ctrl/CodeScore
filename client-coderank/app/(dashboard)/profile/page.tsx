"use client";

import { useEffect, useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/context/AuthContext";
import { useRouter } from "next/navigation";
import api from "@/services/api";
import { Profile } from "@/types";
import { GitBranch as Github, Code2, LogOut, Settings } from "lucide-react";

export default function ProfilePage() {
  const { user, logout } = useAuth();
  const router = useRouter();
  const [profile, setProfile] = useState<Profile | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const response = await api.get("/profile/me");
        setProfile(response.data);
      } catch (error) {
        console.error("Failed to fetch profile connections", error);
      } finally {
        setLoading(false);
      }
    };
    fetchProfile();
  }, []);

  const handleUpdateProfiles = () => {
    router.push("/onboarding"); // Reuse onboarding or create a specific update page
  };

  if (!user) return null;

  return (
    <div className="container mx-auto px-4 py-8 max-w-3xl space-y-8">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Profile Settings</h1>
        <p className="text-muted-foreground">Manage your account and connected profiles.</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Account Information</CardTitle>
          <CardDescription>Your basic account details.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="text-sm font-medium text-muted-foreground">Name</div>
            <div className="md:col-span-2 font-medium">{user.name}</div>
            
            <div className="text-sm font-medium text-muted-foreground">Email</div>
            <div className="md:col-span-2 font-medium">{user.email}</div>
          </div>
        </CardContent>
        <CardFooter className="border-t pt-6">
          <Button variant="destructive" onClick={logout} className="flex items-center gap-2">
            <LogOut className="h-4 w-4" />
            Logout
          </Button>
        </CardFooter>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Connected Profiles</CardTitle>
          <CardDescription>Accounts used to generate your Developer Score.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {loading ? (
            <div className="text-sm text-muted-foreground">Loading connected profiles...</div>
          ) : (
            <div className="space-y-4">
              <div className="flex items-center justify-between p-4 border rounded-md">
                <div className="flex items-center gap-3">
                  <Github className="h-5 w-5" />
                  <div>
                    <div className="font-medium">GitHub</div>
                    <div className="text-sm text-muted-foreground">
                      {profile?.githubUsername ? `@${profile.githubUsername}` : "Not connected"}
                    </div>
                  </div>
                </div>
                <div className="text-sm font-medium text-muted-foreground">
                  {profile?.githubUsername ? "Connected" : ""}
                </div>
              </div>

              <div className="flex items-center justify-between p-4 border rounded-md">
                <div className="flex items-center gap-3">
                  <Code2 className="h-5 w-5" />
                  <div>
                    <div className="font-medium">LeetCode</div>
                    <div className="text-sm text-muted-foreground">
                      {profile?.leetcodeUsername ? `@${profile.leetcodeUsername}` : "Not connected"}
                    </div>
                  </div>
                </div>
                <div className="text-sm font-medium text-muted-foreground">
                  {profile?.leetcodeUsername ? "Connected" : ""}
                </div>
              </div>

              <div className="flex items-center justify-between p-4 border rounded-md">
                <div className="flex items-center gap-3">
                  <span className="font-bold text-lg leading-none tracking-tighter">CF</span>
                  <div>
                    <div className="font-medium">Codeforces</div>
                    <div className="text-sm text-muted-foreground">
                      {profile?.codeforcesHandle ? `@${profile.codeforcesHandle}` : "Not connected"}
                    </div>
                  </div>
                </div>
                <div className="text-sm font-medium text-muted-foreground">
                  {profile?.codeforcesHandle ? "Connected" : ""}
                </div>
              </div>
            </div>
          )}
        </CardContent>
        <CardFooter className="border-t pt-6">
          <Button variant="outline" onClick={handleUpdateProfiles} className="flex items-center gap-2">
            <Settings className="h-4 w-4" />
            Update Profiles
          </Button>
        </CardFooter>
      </Card>
    </div>
  );
}
