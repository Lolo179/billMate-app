const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? "";

export const env = {
  apiBaseUrl,
  useMsw: import.meta.env.VITE_USE_MSW === "true"
};
