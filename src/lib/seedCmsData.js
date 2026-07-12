import { db } from "~/lib/firebase";
import { collection, addDoc, deleteDoc, getDocs, Timestamp } from "firebase/firestore";
import serviceData from "~/data/serviceData.json";
import teamData from "~/data/teamData.json";
import testimonialData from "~/data/testimonialData.json";
import blogData from "~/data/blogData.json";
import blogs from "~/data/blogs.json";
import blogsTwo from "~/data/blogsTwo.json";
import article from "~/data/article.json";

export async function seedCmsData() {
  const results = [];

  const cols = ["cms_services", "cms_team", "cms_blog", "cms_portfolio", "cms_testimonials"];
  for (const col of cols) {
    const snap = await getDocs(collection(db, col));
    const promises = snap.docs.map(d => deleteDoc(d.ref));
    await Promise.all(promises);
  }

  for (let i = 0; i < serviceData.length; i++) {
    const s = serviceData[i];
    await addDoc(collection(db, "cms_services"), {
      title: s.title, icon: s.icon, description: s.description, link: s.link, order: i, createdAt: Timestamp.now(),
    });
  }
  results.push("Seeded " + serviceData.length + " services");

  for (let i = 0; i < teamData.length; i++) {
    const t = teamData[i];
    await addDoc(collection(db, "cms_team"), {
      name: t.name, role: t.role, image: t.image,
      twitter: t.social?.twitter || "", facebook: t.social?.facebook || "", linkedin: t.social?.linkedin || "",
      order: i, createdAt: Timestamp.now(),
    });
  }
  results.push("Seeded " + teamData.length + " team members");

  for (let i = 0; i < testimonialData.length; i++) {
    const t = testimonialData[i];
    await addDoc(collection(db, "cms_testimonials"), {
      title: t.title, description: t.description,
      authorName: t.author?.name || "", authorRole: t.author?.role || "", authorAvatar: t.author?.avatar || "",
      rating: t.rating || 5, order: i, createdAt: Timestamp.now(),
    });
  }
  results.push("Seeded " + testimonialData.length + " testimonials");

  const allBlogs = [...blogData, ...blogs, ...blogsTwo, ...article];
  const seen = new Set();
  let blogCount = 0;
  for (let i = 0; i < allBlogs.length; i++) {
    const b = allBlogs[i];
    if (seen.has(b.title)) continue;
    seen.add(b.title);
    await addDoc(collection(db, "cms_blog"), {
      title: b.title, category: b.category || "", date: b.date || "",
      image: b.image || "", description: b.description || "", link: b.link || "",
      order: i, createdAt: Timestamp.now(),
    });
    blogCount++;
  }
  results.push("Seeded " + blogCount + " blog posts");

  const portfolioItems = [
    { title: "Oba Market Delivery", category: "delivery", image: "/images/portfolio/p_1.png", description: "Same-day delivery partnership with Oba Market traders for fast, reliable package movement across Benin City.", link: "single-portfolio" },
    { title: "GRA Food Run", category: "food", image: "/images/portfolio/p_2.png", description: "Hot and fresh food delivery from GRA restaurants to homes and offices within 45 minutes.", link: "single-portfolio" },
    { title: "Corporate Courier", category: "business", image: "/images/portfolio/p_3.png", description: "End-to-end document and parcel courier services for corporate clients in Benin City.", link: "single-portfolio" },
    { title: "Bulk Delivery Run", category: "logistics", image: "/images/portfolio/p_4.png", description: "Large-volume delivery operations for businesses moving inventory across multiple locations.", link: "single-portfolio" },
  ];
  for (let i = 0; i < portfolioItems.length; i++) {
    const p = portfolioItems[i];
    await addDoc(collection(db, "cms_portfolio"), {
      title: p.title, category: p.category, image: p.image, description: p.description, link: p.link,
      order: i, createdAt: Timestamp.now(),
    });
  }
  results.push("Seeded " + portfolioItems.length + " portfolio items");

  return results;
}
