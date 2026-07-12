"use client";
import Link from "next/link";
import CategoryCard from "~/components/Ui/Cards/CategoryCard";
import PostCard from "~/components/Ui/Cards/PostCard";
import TagCard from "~/components/Ui/Cards/TagCard";
import TwoBlogCard from "~/components/Ui/Cards/TwoBlogCard";
import blogData from "~/data/blogsTwo.json";

const BlogSection = () => {
  return (
    <div className="section zubuz-section-padding2">
      <div className="container">
        <div className="row">
          <div div className="col-lg-8">
            {blogData.map((post) => (
              <TwoBlogCard key={post?.id} post={post} />
            ))}
            <div class="zubuz-navigation">
              <nav class="navigation pagination" aria-label="Posts">
                <div class="nav-links">
                  <span aria-current="page" class="page-numbers current">
                    1
                  </span>
                  <Link class="page-numbers" href="">
                    2
                  </Link>
                  <Link class="next page-numbers" href="">
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
                    <img src="/images/icon/search.svg" alt="" />
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
                <Link href="contact-us">Contact Us</Link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default BlogSection;
