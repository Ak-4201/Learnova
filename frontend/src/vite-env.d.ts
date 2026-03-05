/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** Backend API base URL (e.g. http://localhost:8081). Set in .env from .env.example. */
  readonly VITE_API_URL?: string;
  readonly VITE_DASHBOARD_CONTENT_URL?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
