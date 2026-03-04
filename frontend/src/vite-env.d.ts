/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_DASHBOARD_CONTENT_URL?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
