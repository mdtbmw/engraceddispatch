"use client";

import Link from "next/link";
import Icon from "~/components/Ui/Icon";
import BlogCard from "~/components/Ui/Cards/BlogCard";
import CategoryCard from "~/components/Ui/Cards/CategoryCard";
import PostCard from "~/components/Ui/Cards/PostCard";
import TagCard from "~/components/Ui/Cards/TagCard";
import { useBlogPosts } from "~/lib/useCmsData";

const BlogSection = () => {
  const { items: blogData } = useBlogPosts();
  return (
    <div className="section zubuz-section-padding2">
      <div className="container">
        <div className="row">
          <div className="col-lg-8">
            {blogData.map((post) => (
              <BlogCard key={post?.id} post={post} />
            ))}
            <div className="zubuz-navigation">
              <nav className="navigation pagination" aria-label="Posts">
                <div className="nav-links">
                  <span aria-current="page" className="page-numbers current">
                    1
                  </span>
                  <Link className="page-numbers" href="">
                    2
                  </Link>
                  <Link className="next page-numbers" href="">
                    next
                  </Link>
                </div>
              </nav>
            </div>
          </div>

          <div className="col-lg-4">
            <div className="right-sidebar">
              <div className="widget">
                <div className="wp-block-search__inside-wrapper">
                  <input
                    type="search"
                    placeholder="Search..."
                    className="wp-block-search__input"
                  />
                  <button id="wp-block-search__button" type="submit">
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
                <Link href="">Contact Us</Link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default BlogSection;
