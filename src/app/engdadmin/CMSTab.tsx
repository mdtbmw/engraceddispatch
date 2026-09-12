"use client";
import React, { useState, useEffect } from "react";
import { doc, onSnapshot, setDoc, Timestamp, collection, query, orderBy, onSnapshot as onSnap2, addDoc, updateDoc, deleteDoc } from "firebase/firestore";
import { FileText, Plus, Trash2, Edit3, Save, X, RefreshCw } from "lucide-react";

interface SiteContent {
  heroTitle: string; heroSubtitle: string; heroDescription: string;
  aboutTitle: string; aboutDescription: string; aboutMission: string; aboutVision: string;
  servicesTitle: string; servicesDescription: string;
  teamTitle: string; teamDescription: string;
  contactAddress: string; contactEmail: string; contactPhone: string;
  socialTwitter: string; socialFacebook: string; socialInstagram: string; socialLinkedin: string; socialGithub: string;
  footerCopyright: string;
  updatedAt?: any;
}
const defaultContent: SiteContent = {
  heroTitle: "Engraced Dispatch", heroSubtitle: "Premium Logistics & Dispatch", heroDescription: "Engraced Dispatch brings fast, reliable bike and courier delivery services to Benin City. Track your package in real-time and enjoy seamless deliveries across the city.",
  aboutTitle: "Our journey with Engraced Dispatch", aboutDescription: "We connect people and businesses across Benin City with fast, reliable courier services. Built to close the gap between you and your destination.", aboutMission: "To make delivery seamless for every business and individual in Benin City.", aboutVision: "To become the most trusted logistics platform in Nigeria.",
  servicesTitle: "Engraced Dispatch delivery services", servicesDescription: "From same-day express to bulk logistics, we have a service tailored to your needs.",
  teamTitle: "Meet the Engraced Dispatch team", teamDescription: "Dedicated professionals committed to providing premium logistics services.",
  contactAddress: "No 18, Sakponba Road, Benin City, Edo State.", contactEmail: "hello@engraceddispatch.com", contactPhone: "+234 800 123 4567",
  socialTwitter: "https://twitter.com/engraceddispatch", socialFacebook: "https://facebook.com/engraceddispatch", socialInstagram: "https://instagram.com/engraceddispatch", socialLinkedin: "https://linkedin.com/company/engraceddispatch", socialGithub: "https://github.com/engraceddispatch",
  footerCopyright: "Engraced Dispatch. All rights reserved.",
};

const field = (label: string, value: string, onChange: (v: string) => void, opts?: { multiline?: boolean; large?: boolean; type?: string }) => (
  <div className={opts?.large ? "sm:col-span-2" : ""}>
    <label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 mb-1 uppercase tracking-wider">{label}</label>
    {opts?.multiline ? (
      <textarea value={value} onChange={e => onChange(e.target.value)}
        className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3.5 py-2.5 text-xs text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFB800]/40 min-h-[80px] resize-y" />
    ) : (
      <input type={opts?.type || "text"} value={value} onChange={e => onChange(e.target.value)}
        className="w-full h-10 bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3.5 text-xs text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFB800]/40" />
    )}
  </div>
);

function CmsEditor({ content, setContent, sections, activeSection, setActiveSection }: {
  content: SiteContent; setContent: (c: SiteContent) => void;
  sections: { id: string; label: string }[]; activeSection: string; setActiveSection: (s: string) => void;
}) {
  const upd = (k: keyof SiteContent) => (v: string) => setContent({ ...content, [k]: v });
  return <>
    <div className="flex gap-2 flex-wrap border-b border-black/10 dark:border-white/10 pb-3">
      {sections.map(s => (
        <button key={s.id} onClick={() => setActiveSection(s.id)}
          className={"px-3 py-1.5 text-xs font-bold rounded-xl transition-colors " + (activeSection === s.id ? "bg-[#FFB800] text-[#111]" : "bg-gray-100 dark:bg-gray-800 text-black/60 dark:text-white/60 hover:bg-gray-200 dark:hover:bg-gray-700")}>
          {s.label}
        </button>
      ))}
    </div>
    <div className="bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl p-5 shadow-sm">
      {activeSection === "hero" && <div className="grid sm:grid-cols-2 gap-4">
        {field("Hero Title", content.heroTitle, upd("heroTitle"), { large: true })}
        {field("Hero Subtitle", content.heroSubtitle, upd("heroSubtitle"), { large: true })}
        {field("Hero Description", content.heroDescription, upd("heroDescription"), { multiline: true, large: true })}
      </div>}
      {activeSection === "about" && <div className="grid sm:grid-cols-2 gap-4">
        {field("About Title", content.aboutTitle, upd("aboutTitle"), { large: true })}
        {field("About Description", content.aboutDescription, upd("aboutDescription"), { multiline: true, large: true })}
        {field("Mission Statement", content.aboutMission, upd("aboutMission"), { multiline: true })}
        {field("Vision Statement", content.aboutVision, upd("aboutVision"), { multiline: true })}
      </div>}
      {activeSection === "services" && <div className="grid sm:grid-cols-2 gap-4">
        {field("Services Title", content.servicesTitle, upd("servicesTitle"), { large: true })}
        {field("Services Description", content.servicesDescription, upd("servicesDescription"), { multiline: true, large: true })}
      </div>}
      {activeSection === "team" && <div className="grid sm:grid-cols-2 gap-4">
        {field("Team Title", content.teamTitle, upd("teamTitle"), { large: true })}
        {field("Team Description", content.teamDescription, upd("teamDescription"), { multiline: true, large: true })}
      </div>}
      {activeSection === "contact" && <div className="grid sm:grid-cols-2 gap-4">
        {field("Office Address", content.contactAddress, upd("contactAddress"), { large: true })}
        {field("Contact Email", content.contactEmail, upd("contactEmail"))}
        {field("Contact Phone", content.contactPhone, upd("contactPhone"))}
        <div className="sm:col-span-2 border-t border-black/10 dark:border-white/10 pt-4 mt-2">
          <p className="text-[10px] font-bold text-black/40 dark:text-white/40 mb-3 uppercase">Social Media Links</p>
        </div>
        {field("Twitter URL", content.socialTwitter, upd("socialTwitter"), { large: true })}
        {field("Facebook URL", content.socialFacebook, upd("socialFacebook"), { large: true })}
        {field("Instagram URL", content.socialInstagram, upd("socialInstagram"), { large: true })}
        {field("LinkedIn URL", content.socialLinkedin, upd("socialLinkedin"), { large: true })}
        {field("GitHub URL", content.socialGithub, upd("socialGithub"), { large: true })}
      </div>}
      {activeSection === "footer" && <div className="grid sm:grid-cols-2 gap-4">
        {field("Copyright Text", content.footerCopyright, upd("footerCopyright"), { large: true })}
      </div>}
    </div>
  </>;
}

function CrudTable({ collectionName, label, icon, fields, items, onAdd, onUpdate, onDelete }: {
  collectionName: string; label: string; icon: React.ReactNode;
  fields: { key: string; label: string; type?: string }[];
  items: any[]; onAdd: (data: any) => Promise<any>; onUpdate: (id: string, data: any) => Promise<any>; onDelete: (id: string) => Promise<any>;
}) {
  const [editing, setEditing] = useState<any>(null);
  const [adding, setAdding] = useState(false);
  const [form, setForm] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(false);

  const initForm = () => {
    const f: Record<string, string> = {};
    fields.forEach(fld => f[fld.key] = "");
    return f;
  };

  const handleSave = async () => {
    setLoading(true);
    try {
      if (adding) {
        await onAdd({ ...form, order: items.length });
      } else if (editing) {
        await onUpdate(editing.id, form);
      }
      setAdding(false); setEditing(null);
    } finally { setLoading(false); }
  };

  const handleEdit = (item: any) => {
    const f: Record<string, string> = {};
    fields.forEach(fld => f[fld.key] = String(item[fld.key] ?? ""));
    setForm(f);
    setEditing(item);
    setAdding(false);
  };

  const startAdd = () => {
    setForm(initForm());
    setAdding(true);
    setEditing(null);
  };

  return <div className="space-y-4">
    <div className="flex items-center justify-between">
      <p className="text-xs font-bold text-black/60 dark:text-white/60">{items.length} {label.toLowerCase()}</p>
      <button onClick={startAdd} className="flex items-center gap-1.5 px-4 py-2.5 min-h-[38px] bg-[#FFB800] text-[#111] text-xs font-bold rounded-xl hover:bg-[#e6b13b] transition-colors">
        <Plus size={14} /> Add {label.slice(0, -1)}
      </button>
    </div>
    {items.length === 0 && <p className="text-xs text-black/30 dark:text-white/30 text-center py-8">No {label.toLowerCase()} yet.</p>}
    <div className="grid gap-3">
      {items.map((item, i) => (
        <div key={item.id} className="bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-2xl p-4 flex items-start justify-between gap-4">
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-2 mb-1">
              <span className="text-[10px] font-bold text-black/30 dark:text-white/30">{i + 1}.</span>
              <p className="text-xs font-bold text-[#111] dark:text-white truncate">{item.title || item.name || "Untitled"}</p>
            </div>
            <div className="flex flex-wrap gap-1">
              {fields.filter(f => f.key !== "title" && f.key !== "name" && f.key !== "image" && f.key !== "icon").slice(0, 3).map(f => (
                <span key={f.key} className="text-[9px] text-black/40 dark:text-white/40 bg-white dark:bg-[#111] px-2 py-0.5 rounded-full">{f.label}: {String(item[f.key] || "").substring(0, 40)}</span>
              ))}
            </div>
          </div>
          <div className="flex items-center gap-1 flex-shrink-0">
            <button onClick={() => handleEdit(item)} className="p-1.5 text-[#FFB800] hover:bg-[#FFB800]/10 rounded-lg transition-colors"><Edit3 size={14} /></button>
            <button onClick={() => { if (confirm("Delete this item?")) onDelete(item.id); }} className="p-1.5 text-red-400 hover:bg-red-50 dark:hover:bg-red-900/30 rounded-lg transition-colors"><Trash2 size={14} /></button>
          </div>
        </div>
      ))}
    </div>
    {(adding || editing) && <div className="fixed inset-0 bg-black/40 backdrop-blur-sm flex items-center justify-center p-4 z-50" onClick={() => { setAdding(false); setEditing(null); }}>
      <div className="animate-scale-in bg-white dark:bg-[#1a1a1a] border border-black/10 dark:border-white/10 rounded-3xl p-6 w-full max-w-lg shadow-2xl space-y-5" onClick={e => e.stopPropagation()}>
        <h3 className="text-base font-black text-[#111] dark:text-white flex items-center gap-2">{icon}{adding ? "Add" : "Edit"} {label.slice(0, -1)}</h3>
        <div className="grid sm:grid-cols-2 gap-3">
          {fields.map(f => (
            <div key={f.key} className={f.key === "description" || f.key === "image" || f.key === "icon" ? "sm:col-span-2" : ""}>
              <label className="block text-[10px] font-extrabold text-gray-700 dark:text-gray-300 mb-1 uppercase tracking-wider">{f.label}</label>
              {f.key === "description" ? (
                <textarea value={form[f.key] || ""} onChange={e => setForm(p => ({ ...p, [f.key]: e.target.value }))}
                  className="w-full bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3.5 py-2.5 text-xs text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFB800]/40 min-h-[60px] resize-y" />
              ) : (
                <input type={f.type || "text"} value={form[f.key] || ""} onChange={e => setForm(p => ({ ...p, [f.key]: e.target.value }))}
                  className="w-full h-10 bg-gray-50 dark:bg-[#222] border border-black/10 dark:border-white/10 rounded-xl px-3.5 text-xs text-[#111] dark:text-white focus:outline-none focus:ring-2 focus:ring-[#FFB800]/40" />
              )}
            </div>
          ))}
        </div>
        <div className="flex items-center justify-end gap-3 pt-2">
          <button onClick={() => { setAdding(false); setEditing(null); }} className="h-10 px-4 bg-gray-100 dark:bg-gray-800 text-gray-800 dark:text-gray-200 border border-gray-200 dark:border-white/10 rounded-xl text-xs font-bold hover:bg-gray-200 dark:hover:bg-gray-700 cursor-pointer">Cancel</button>
          <button onClick={handleSave} disabled={loading} className="h-10 px-5 bg-[#FFB800] hover:bg-[#FFB800]/90 text-[#111] rounded-xl text-xs font-black disabled:opacity-50 transition-all shadow-xs cursor-pointer">
            {loading ? "Saving..." : "Save"}
          </button>
        </div>
      </div>
    </div>}
  </div>;
}

export default function CMSTab({ db, addLog, userRole }: { db: any; addLog: any; userRole?: string }) {
  const [content, setContent] = useState<SiteContent>(defaultContent);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [seeding, setSeeding] = useState(false);
  const [seedResult, setSeedResult] = useState<string[] | null>(null);
  const [activeSection, setActiveSection] = useState("hero");

  const [services, setServices] = useState<any[]>([]);
  const [teamMembers, setTeamMembers] = useState<any[]>([]);
  const [blogPosts, setBlogPosts] = useState<any[]>([]);
  const [portfolioItems, setPortfolioItems] = useState<any[]>([]);
  const [testimonials, setTestimonials] = useState<any[]>([]);

  const [mainTab, setMainTab] = useState("text");

  useEffect(() => {
    const unsub = onSnapshot(doc(db, "site_content", "settings"), snap => {
      if (snap.exists()) setContent({ ...defaultContent, ...snap.data() } as SiteContent);
      setLoading(false);
    });
    return () => unsub();
  }, [db]);

  useEffect(() => {
    const unsub1 = onSnapshot(collection(db, "cms_services"), s => setServices(s.docs.map(d => ({ id: d.id, ...d.data() }))));
    const unsub2 = onSnapshot(collection(db, "cms_team"), s => setTeamMembers(s.docs.map(d => ({ id: d.id, ...d.data() }))));
    const unsub3 = onSnapshot(collection(db, "cms_blog"), s => setBlogPosts(s.docs.map(d => ({ id: d.id, ...d.data() }))));
    const unsub4 = onSnapshot(collection(db, "cms_portfolio"), s => setPortfolioItems(s.docs.map(d => ({ id: d.id, ...d.data() }))));
    const unsub5 = onSnapshot(collection(db, "cms_testimonials"), s => setTestimonials(s.docs.map(d => ({ id: d.id, ...d.data() }))));
    return () => { unsub1(); unsub2(); unsub3(); unsub4(); unsub5(); };
  }, [db]);

  const saveContent = async () => {
    setSaving(true);
    try {
      await setDoc(doc(db, "site_content", "settings"), { ...content, updatedAt: Timestamp.now() }, { merge: true });
      addLog("Update Content", "Updated site landing text content");
    } catch (e) {
      console.error(e);
    } finally { setSaving(false); }
  };

  const addTo = async (col: string, data: any) => {
    try {
      await addDoc(collection(db, col), { ...data, createdAt: Timestamp.now() });
      addLog("Add CMS Item", `Added item to ${col}`);
    } catch (e) { console.error(e); }
  };
  const updateIn = async (col: string, id: string, data: any) => {
    try {
      await updateDoc(doc(db, col, id), { ...data, updatedAt: Timestamp.now() });
      addLog("Update CMS Item", `Updated item ${id} in ${col}`);
    } catch (e) { console.error(e); }
  };
  const deleteFrom = async (col: string, id: string) => {
    try {
      await deleteDoc(doc(db, col, id));
      addLog("Delete CMS Item", `Deleted item ${id} from ${col}`);
    } catch (e) { console.error(e); }
  };

  const sections = [
    { id: "hero", label: "Hero Banner" },
    { id: "about", label: "About Section" },
    { id: "services", label: "Services Header" },
    { id: "cta", label: "Call to Action" },
    { id: "footer", label: "Footer Info" },
    { id: "meta", label: "SEO & Brand" },
  ];

  const serviceFields = [
    { key: "title", label: "Service Title" },
    { key: "description", label: "Description", type: "textarea" },
    { key: "iconName", label: "Icon Identifier" },
    { key: "accentColor", label: "Accent Color Hex" },
  ];
  const teamFields = [
    { key: "name", label: "Full Name" },
    { key: "role", label: "Job Title" },
    { key: "bio", label: "Bio Summary", type: "textarea" },
    { key: "avatarUrl", label: "Photo Image URL" },
  ];
  const blogFields = [
    { key: "title", label: "Article Title" },
    { key: "snippet", label: "Preview Snippet", type: "textarea" },
    { key: "author", label: "Author Name" },
    { key: "publishDate", label: "Publish Date" },
    { key: "coverImage", label: "Cover Image URL" },
  ];
  const portfolioFields = [
    { key: "title", label: "Case Study / Fleet Title" },
    { key: "category", label: "Category" },
    { key: "description", label: "Details", type: "textarea" },
    { key: "imageUrl", label: "Media URL" },
  ];
  const testimonialFields = [
    { key: "quote", label: "Quote", type: "textarea" },
    { key: "authorName", label: "Client Name" },
    { key: "authorCompany", label: "Company / Location" },
    { key: "authorAvatar", label: "Author Avatar Path" },
    { key: "rating", label: "Rating (1-5)", type: "number" },
  ];

  const handleSeed = async () => {
    if (userRole !== "super_admin") {
      alert("Super Administrator role required to restore content defaults.");
      return;
    }
    if (!confirm("This will restore standard default content for all public landing pages. Continue?")) return;
    setSeeding(true); setSeedResult(null);
    try {
      const { seedCmsData } = await import("~/lib/seedCmsData");
      const r = await seedCmsData();
      setSeedResult(r);
      addLog("Reset Content", "Restored standard default landing page content");
    } catch (e) {
      setSeedResult(["Error: " + (e instanceof Error ? e.message : String(e))]);
    } finally { setSeeding(false); }
  };

  if (loading) return <div className="tab-content flex items-center justify-center h-48"><div className="animate-pulse text-black/40 dark:text-white/40">Loading...</div></div>;

  const tabs = [
    { id: "text", label: "Page Text" },
    { id: "services", label: "Service Cards", icon: "S" },
    { id: "team", label: "Team Profiles", icon: "T" },
    { id: "blog", label: "Blog Posts", icon: "B" },
    { id: "portfolio", label: "Portfolio", icon: "P" },
    { id: "testimonials", label: "Testimonials", icon: "R" },
  ];

  return <div className="tab-content space-y-6">
    <div className="flex items-center justify-between flex-wrap gap-4">
      <div><h1 className="text-xl font-black text-[#111] dark:text-white flex items-center gap-2"><FileText className="w-5 h-5 text-[#FFB800]" /> Site Content</h1>
        <p className="text-xs text-gray-600 dark:text-gray-400 mt-1">Edit all landing page content</p></div>
      <div className="flex items-center gap-2.5">
        {userRole === "super_admin" && (
          <button onClick={handleSeed} disabled={seeding} className="h-10 px-3.5 bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300 rounded-xl text-xs font-bold hover:bg-gray-200 dark:hover:bg-gray-700 disabled:opacity-50 transition-colors flex items-center gap-1.5 cursor-pointer">
            <RefreshCw size={14} className={seeding ? "animate-spin" : ""} /> {seeding ? "Restoring..." : "Restore Defaults"}
          </button>
        )}
        <button onClick={saveContent} disabled={saving} className="h-10 px-5 bg-[#FFB800] hover:bg-[#FFB800]/90 text-[#111] rounded-xl text-xs font-black shadow-xs flex items-center gap-2 disabled:opacity-50 transition-all cursor-pointer">
          <Save size={14} /> {saving ? "Saving..." : "Save All Changes"}
        </button>
      </div>
    </div>

    <div className="flex gap-2 flex-wrap border-b border-black/10 dark:border-white/10 pb-3">
      {tabs.map(t => (
        <button key={t.id} onClick={() => setMainTab(t.id)}
          className={"flex items-center gap-1.5 px-3 py-1.5 text-xs font-bold rounded-xl transition-colors " + (mainTab === t.id ? "bg-[#FFB800] text-[#111]" : "bg-gray-100 dark:bg-gray-800 text-black/60 dark:text-white/60 hover:bg-gray-200 dark:hover:bg-gray-700")}>
          {t.icon && t.id !== "text" && <span className="w-4 h-4 flex items-center justify-center bg-black/10 dark:bg-white/10 rounded text-[9px]">{t.icon}</span>}
          {t.label}
        </button>
      ))}
    </div>

    {seedResult && <div className="bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 rounded-2xl p-4">
      <p className="text-xs font-bold text-green-700 dark:text-green-300 mb-1">Synchronization Status:</p>
      {seedResult.map((r, i) => <p key={i} className="text-[10px] text-green-600 dark:text-green-400">{r}</p>)}
    </div>}

    {mainTab === "text" && <>
      <CmsEditor content={content} setContent={setContent} sections={sections} activeSection={activeSection} setActiveSection={setActiveSection} />
    </>}

    {mainTab === "services" && <CrudTable collectionName="cms_services" label="Service Cards" icon={<span className="text-[#FFB800]">S</span>}
      fields={serviceFields} items={services} onAdd={d => addTo("cms_services", d)} onUpdate={(id, d) => updateIn("cms_services", id, d)} onDelete={id => deleteFrom("cms_services", id)} />
    }
    {mainTab === "team" && <CrudTable collectionName="cms_team" label="Team Members" icon={<span className="text-[#FFB800]">T</span>}
      fields={teamFields} items={teamMembers} onAdd={d => addTo("cms_team", d)} onUpdate={(id, d) => updateIn("cms_team", id, d)} onDelete={id => deleteFrom("cms_team", id)} />
    }
    {mainTab === "blog" && <CrudTable collectionName="cms_blog" label="Blog Posts" icon={<span className="text-[#FFB800]">B</span>}
      fields={blogFields} items={blogPosts} onAdd={d => addTo("cms_blog", d)} onUpdate={(id, d) => updateIn("cms_blog", id, d)} onDelete={id => deleteFrom("cms_blog", id)} />
    }
    {mainTab === "portfolio" && <CrudTable collectionName="cms_portfolio" label="Portfolio Items" icon={<span className="text-[#FFB800]">P</span>}
      fields={portfolioFields} items={portfolioItems} onAdd={d => addTo("cms_portfolio", d)} onUpdate={(id, d) => updateIn("cms_portfolio", id, d)} onDelete={id => deleteFrom("cms_portfolio", id)} />
    }
    {mainTab === "testimonials" && <CrudTable collectionName="cms_testimonials" label="Testimonials" icon={<span className="text-[#FFB800]">R</span>}
      fields={testimonialFields} items={testimonials} onAdd={d => addTo("cms_testimonials", d)} onUpdate={(id, d) => updateIn("cms_testimonials", id, d)} onDelete={id => deleteFrom("cms_testimonials", id)} />
    }
  </div>;
}
