export class ApiClient {
  constructor(private readonly baseUrl: string) {}

  async get<T>(path: string, token?: string): Promise<T> {
    return this.request<T>(path, { method: "GET" }, token, "json");
  }

  async post<T>(path: string, body: unknown, token?: string): Promise<T> {
    return this.request<T>(
      path,
      {
        method: "POST",
        body: JSON.stringify(body)
      },
      token,
      "json"
    );
  }

  async postWithoutResponse(path: string, body: unknown, token?: string): Promise<void> {
    await this.request<undefined>(
      path,
      {
        method: "POST",
        body: JSON.stringify(body)
      },
      token,
      "void"
    );
  }

  async put<T>(path: string, body: unknown, token?: string): Promise<T> {
    return this.request<T>(
      path,
      {
        method: "PUT",
        body: JSON.stringify(body)
      },
      token,
      "json"
    );
  }

  async putForBlob(path: string, token?: string): Promise<Blob> {
    return this.request<Blob>(path, { method: "PUT" }, token, "blob");
  }

  async getBlob(path: string, token?: string): Promise<Blob> {
    return this.request<Blob>(path, { method: "GET" }, token, "blob");
  }

  async delete(path: string, token?: string): Promise<void> {
    await this.request<void>(path, { method: "DELETE" }, token, "void");
  }

  private async request<T>(
    path: string,
    init: RequestInit,
    token: string | undefined,
    responseType: "json" | "blob" | "void"
  ): Promise<T> {
    const headers = new Headers(init.headers);

    if (init.body !== undefined) {
      headers.set("Content-Type", "application/json");
    }

    if (token) {
      headers.set("Authorization", `Bearer ${token}`);
    }

    const response = await fetch(`${this.baseUrl}${path}`, {
      ...init,
      headers
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(errorText || `HTTP ${response.status}`);
    }

    if (responseType === "void" || response.status === 204) {
      return undefined as T;
    }

    if (responseType === "blob") {
      return (await response.blob()) as T;
    }

    if (response.headers.get("Content-Length") === "0") {
      return undefined as T;
    }

    return (await response.json()) as T;
  }
}
