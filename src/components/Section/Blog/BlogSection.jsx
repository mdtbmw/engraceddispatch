"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import Icon from "~/components/Ui/Icon";
import BlogCard from "~/components/Ui/Cards/BlogCard";
import CategoryCard from "~/components/Ui/Cards/CategoryCard";
import PostCard from "~/components/Ui/Cards/PostCard";
import TagCard from "~/components/Ui/Cards/TagCard";
import { useBlogPosts } from "~/lib/useCmsData";

const perPage = 6;

const BlogSection = () => {
  const { items: blogData } = useBlogPosts();
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(1);
  const filtered = search
    ? blogData.filter(p => (p.title || "").toLowerCase().includes(search.toLowerCase()) || (p.description || "").toLowerCase().includes(search.toLowerCase()))
    : blogData;
  const totalPages = Math.max(1, Math.ceil(filtered.length / perPage));
  const paged = filtered.slice((page - 1) * perPage, page * perPage);
  useEffect(() => { setPage(1); }, [search]);
  return (
    <div className="section zubuz-section-padding2">
      <div className="container">
        <div className="row">
          <div className="col-lg-8">
            {paged.length === 0 && <p className="text-center py-5" style={{ opacity: 0.5 }}>No posts found.</p>}
            {paged.map((post) => (
              <BlogCard key={post?.id} post={post} />
            ))}
            {totalPages > 1 && <div className="zubuz-navigation">
              <nav className="navigation pagination" aria-label="Posts">
                <div className="nav-links">
                  <button className="prev page-numbers" onClick={() => setPage(p => Math.max(1, p - 1))} disabled={page <= 1} style={{ background: "none", border: "none", cursor: page <= 1 ? "default" : "pointer", opacity: page <= 1 ? 0.3 : 1 }}>
                    prev
                  </button>
                  {Array.from({ length: totalPages }, (_, i) => (
                    <button key={i + 1} onClick={() => setPage(i + 1)} className={"page-numbers" + (page === i + 1 ? " current" : "")} style={{ background: "none", border: "none", cursor: "pointer", fontWeight: page === i + 1 ? 700 : 400 }}>
                      {i + 1}
                    </button>
                  ))}
                  <button className="next page-numbers" onClick={() => setPage(p => Math.min(totalPages, p + 1))} disabled={page >= totalPages} style={{ background: "none", border: "none", cursor: page >= totalPages ? "default" : "pointer", opacity: page >= totalPages ? 0.3 : 1 }}>
                    next
                  </button>
                </div>
              </nav>
            </div>}
          </div>

          <div className="col-lg-4">
            <div className="right-sidebar">
              <div className="widget">
                <div className="wp-block-search__inside-wrapper">
                  <input
                    type="search" value={search} onChange={e => setSearch(e.target.value)}
                    placeholder="Search posts..."
                    className="wp-block-search__input"
                  />
                  <button id="wp-block-search__button" type="button">
                    <Icon name="search" size={18} />
                  </button>
                </div>
              </div>
              <CategoryCard />
              <PostCard />
              <TagCard />
              <div className="zubuz-blog-contact">
                <h3>Need a delivery?</h3>
                <p>
                  We are here to help! Tell us what you need delivered and we&apos;ll
                  get in touch within 24 hours
                </p>
                <Link href="/contact-us">Contact Us</Link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default BlogSection;
