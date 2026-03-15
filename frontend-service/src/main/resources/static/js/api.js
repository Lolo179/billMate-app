function buildGatewayUrl(path) {
    const baseUrl = (window.billMateGatewayBaseUrl || "").replace(/\/$/, "");
    const normalizedPath = path.startsWith("/") ? path : `/${path}`;
    return `${baseUrl}${normalizedPath}`;
}
