// Dummy data for the application
export const dummyUser = {
  firstName: "John",
  lastName: "Doe",
  email: "john.doe@example.com",
  token: "dummy-jwt-token-12345",
}

export const dummyRepositories = [
  {
    id: 1,
    name: "react-dashboard",
    url: "https://github.com/johndoe/react-dashboard",
    analyzedAt: "2024-01-15T10:30:00Z",
    contributors: 5,
    commits: 234,
  },
  {
    id: 2,
    name: "node-api-server",
    url: "https://github.com/johndoe/node-api-server",
    analyzedAt: "2024-01-14T15:45:00Z",
    contributors: 3,
    commits: 156,
  },
  {
    id: 3,
    name: "python-ml-toolkit",
    url: "https://github.com/johndoe/python-ml-toolkit",
    analyzedAt: "2024-01-13T09:20:00Z",
    contributors: 8,
    commits: 412,
  },
  {
    id: 4,
    name: "vue-ecommerce",
    url: "https://github.com/johndoe/vue-ecommerce",
    analyzedAt: "2024-01-12T14:15:00Z",
    contributors: 4,
    commits: 189,
  },
  {
    id: 5,
    name: "flutter-mobile-app",
    url: "https://github.com/johndoe/flutter-mobile-app",
    analyzedAt: "2024-01-11T11:30:00Z",
    contributors: 6,
    commits: 298,
  },
]

export const dummyAnalysisData = {
  repository: {
    name: "react-dashboard",
    url: "https://github.com/johndoe/react-dashboard",
    description: "A modern React dashboard with analytics and user management",
    stars: 1234,
    forks: 89,
    language: "TypeScript",
  },
  contributors: [
    {
      id: 1,
      name: "John Doe",
      username: "johndoe",
      avatar: "/developer-avatar.png",
      commits: 145,
      additions: 12450,
      deletions: 3200,
      percentage: 45.2,
      score: 92,
    },
    {
      id: 2,
      name: "Jane Smith",
      username: "janesmith",
      avatar: "/female-developer-avatar.png",
      commits: 89,
      additions: 8900,
      deletions: 2100,
      percentage: 28.7,
      score: 85,
    },
    {
      id: 3,
      name: "Mike Johnson",
      username: "mikej",
      avatar: "/male-developer-avatar.png",
      commits: 56,
      additions: 5600,
      deletions: 1800,
      percentage: 18.1,
      score: 78,
    },
    {
      id: 4,
      name: "Sarah Wilson",
      username: "sarahw",
      avatar: "/woman-developer-avatar.png",
      commits: 34,
      additions: 3400,
      deletions: 900,
      percentage: 8.0,
      score: 71,
    },
  ],
  summary: {
    totalCommits: 324,
    totalAdditions: 30350,
    totalDeletions: 8000,
    activeDays: 89,
    firstCommit: "2023-06-15T10:30:00Z",
    lastCommit: "2024-01-15T16:45:00Z",
  },
}

export const dummyGithubConnectionStatus = {
  isConnected: false, // Change this to true to simulate connected state
  message: "Connect your GitHub account to analyze repositories",
}
