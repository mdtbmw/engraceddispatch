const BASE_URL = "https://www.engraceddispatch.com";

const routes = [
  "/",
  "/about-us",
  "/service",
  "/blog",
  "/blog-column-one",
  "/blog-column-two",
  "/contact-us",
  "/faq",
  "/integrations",
  "/portfolio-classic",
  "/portfolio-grid",
  "/career",
  "/team",
  "/testimonials",
  "/coming-soon",
  "/sign-in",
  "/sign-up",
  "/reset-password",
  "/single-blog",
  "/single-career",
  "/single-portfolio",
  "/single-service",
  "/single-team",
];

export default function sitemap() {
  return routes.map((route) => ({
    url: `${BASE_URL}${route}`,
    lastModified: new Date(),
    changeFrequency: route === "/" ? "weekly" : "monthly",
    priority: route === "/" ? 1.0 : 0.8,
  }));
}
