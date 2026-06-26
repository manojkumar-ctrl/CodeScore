export interface User {
  id: number;
  name: string;
  email: string;
  role: string;
  createdAt: string;
  updatedAt: string;
}

export interface Profile {
  githubUsername: string | null;
  leetcodeUsername: string | null;
  codeforcesHandle: string | null;
}

export interface AnalysisResponse {
  developerScore: number;
  dsaScore: number;
  githubScore: number;
  contestScore: number;
  classification: string;
  strengths: string[];
  weaknesses: string[];
}

export interface DashboardResponse {
  developerScore: number;
  leetcodeSolved: number;
  contestRating: number;
  githubRepos: number;
  classification: string;
  githubCommits: number;
  githubStars: number;
  githubFollowers: number;
  leetcodeEasy: number;
  leetcodeMedium: number;
  leetcodeHard: number;
  leetcodeContests: number;
}

export interface LeaderboardEntry {
  rank: number;
  username: string;
  score: number;
  category?: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}
