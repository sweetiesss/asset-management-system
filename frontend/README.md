# 💻 Online Asset Management – Frontend

A modern, responsive frontend for managing company assets, assignments, and return requests — built with React + TypeScript and styled using TailwindCSS. Fully integrated with a Spring Boot backend and secured with JWT-based authentication.

## 🌟 Features

- Access token in-memory + refresh token via HTTP-only cookies
- Role-based views (Admin vs Staff)
- Search across multiple columns
- Sorting & pagination
- View personal asset assignments
- Users can submit return requests for assigned assets
- Admin can view and process these requests
- Admin-only reports page with scheduled summaries
- API Caching & Revalidation

## 🛠️ Tech Stack

- Frontend: React, TypeScript, Vite, TailwindCSS
- UI Components: ShadCN UI, Material-UI (MUI), Lucide-react
- State & Data: Axios, SWR (data fetching/caching), API integration
- Authentication: JWT (access & refresh tokens)
- Testing: Vitest, Testing Library
- Utilities: React Router, React Toastify
- Deployment: Azure (previously hosted; currently offline)

## 📁 Project Structure

```
src/
├── __tests__/       # Unit and integration tests
├── components/      # Reusable UI components
├── configs/         # App configuration
├── context/         # React context providers
├── hooks/           # Custom React hooks
├── lib/             # Shared libraries or helpers
├── pages/           # Route-based page components
├── test/            # Test setup and utilities
├── App.tsx          # Main app component
├── index.css        # Global styles
├── main.tsx         # App entry point
└── vite-env.d.ts    # Vite environment types
```

## 🚀 Getting Started

Install dependencies:

```
npm install
# or
yarn install
```

Run the development server
```
npm run dev
# or
yarn dev
```

Open http://localhost:3000 to see the app.

## 🧾 Project Pages
- /login – Secure login with JWT
- /assets – Asset management (Admin only)
- /assignments – Manage asset-user assignments (Admin only)
- /returning-requests – Handle return requests
- /report – Monthly report scheduler (Admin only)
- / – Home dashboard (role-based)

## 🔐 Auth Flow

- Login: Sends credentials → receives access + refresh token
- Access Token: Stored in memory (short-lived)
- Refresh Token: Stored in cookie (secure)
- Auto Re-login: Access token refresh handled silently with SWR middleware
