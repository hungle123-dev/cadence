import type { NextConfig } from "next";

/**
 * Image hosts are configuration, not code: product photos are mock images for now and will move to
 * our own object storage later (SRS A-04, architecture 9.7). Comma-separated, e.g. "images.unsplash.com".
 */
const imageHosts = (process.env.IMAGE_HOSTS ?? "images.unsplash.com")
  .split(",")
  .map((host) => host.trim())
  .filter(Boolean);

const nextConfig: NextConfig = {
  cacheComponents: true,
  partialPrefetching: true,
  output: "standalone", // small Docker image (deploy/)
  poweredByHeader: false,
  images: {
    remotePatterns: imageHosts.map((hostname) => ({ protocol: "https" as const, hostname })),
  },
};

export default nextConfig;
