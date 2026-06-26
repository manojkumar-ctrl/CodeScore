"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { Button } from "@/components/ui/button";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
  FormDescription,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { useRouter } from "next/navigation";
import api from "@/services/api";

const onboardingSchema = z.object({
  githubUsername: z.string().min(1, { message: "GitHub username is required" }),
  leetcodeUsername: z.string().min(1, { message: "LeetCode username is required" }),
  codeforcesHandle: z.string().optional(),
});

export default function OnboardingPage() {
  const router = useRouter();
  const [error, setError] = useState("");
  const [loadingState, setLoadingState] = useState("");

  const form = useForm<z.infer<typeof onboardingSchema>>({
    resolver: zodResolver(onboardingSchema),
    defaultValues: {
      githubUsername: "",
      leetcodeUsername: "",
      codeforcesHandle: "",
    },
  });

  async function onSubmit(values: z.infer<typeof onboardingSchema>) {
    setError("");
    try {
      setLoadingState("Connecting profiles...");
      await api.post("/profile/connect", {
        githubUsername: values.githubUsername,
        leetcodeUsername: values.leetcodeUsername,
        codeforcesHandle: values.codeforcesHandle || undefined,
      });

      setLoadingState("Fetching GitHub profile...");
      await new Promise(resolve => setTimeout(resolve, 1000)); // Simulating stages for UI
      
      setLoadingState("Fetching LeetCode statistics...");
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      setLoadingState("Generating Developer Score...");
      await api.post("/analysis/run");
      
      router.push("/dashboard");
    } catch (err: any) {
      setError(err.response?.data?.message || "Failed to analyze profile. Please ensure usernames are correct.");
      setLoadingState("");
    }
  }

  return (
    <div className="flex items-center justify-center min-h-[calc(100vh-8rem)] px-4 py-8">
      <Card className="w-full max-w-xl">
        <CardHeader className="space-y-1">
          <CardTitle className="text-2xl font-bold">Connect Your Profiles</CardTitle>
          <CardDescription>
            We need your coding platform usernames to generate your developer score.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <FormField
                  control={form.control}
                  name="githubUsername"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>GitHub Username *</FormLabel>
                      <FormControl>
                        <Input placeholder="torvalds" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name="leetcodeUsername"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>LeetCode Username *</FormLabel>
                      <FormControl>
                        <Input placeholder="tourist" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name="codeforcesHandle"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Codeforces Handle</FormLabel>
                      <FormControl>
                        <Input placeholder="Optional" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />

              </div>

              {error && <p className="text-sm font-medium text-destructive mt-4">{error}</p>}
              
              <div className="pt-4">
                <Button type="submit" className="w-full" disabled={!!loadingState}>
                  {loadingState || "Analyze My Profile"}
                </Button>
                {loadingState && (
                  <p className="text-sm text-center text-muted-foreground mt-2 animate-pulse">
                    {loadingState}
                  </p>
                )}
              </div>
            </form>
          </Form>
        </CardContent>
      </Card>
    </div>
  );
}
