"use client";
import Link from "next/link";
import TwoBlogCard from "~/components/Ui/Cards/TwoBlogCard";
import Icon from "~/components/Ui/Icon";
import { useBlogPosts } from "~/lib/useCmsData";

const BlogSection = () => {
  const { items: blogData } = useBlogPosts();
  return (
    <div className="section zubuz-section-padding2">
      <div className="container">
        <div className="row">
          <div className="col-lg-8">
            {blogData.map((post) => (
              <TwoBlogCard key={post?.id} post={post} />
            ))}
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
              <div className="widget">
                <h3 className="wp-block-heading">Categories:</h3>
                <ul>
                  <li>
                    <Link href="">Delivery Tips</Link>
                  </li>
                  <li>
                    <Link href="">Rider Stories</Link>
                  </li>
                  <li>
                    <Link href="">Benin City</Link>
                  </li>
                  <li>
                    <Link href="">Logistics</Link>
                  </li>
                  <li>
                    <Link href="">Business</Link>
                  </li>
                </ul>
              </div>
              <div className="widget zubuz_recent_posts_Widget">
                <h3 className="wp-block-heading">Recent Posts:</h3>
                <div className="post-item">
                  <div className="post-thumb">
                    <Link href="">
                      <img src="/images/blog/blog1.png" alt="" />
                    </Link>
                  </div>
                  <div className="post-text">
                    <div className="post-date">June 18, 2024</div>
                    <Link className="post-title" href="">
                      How same-day delivery works in Benin City
                    </Link>
                  </div>
                </div>
                <div className="post-item">
                  <div className="post-thumb">
                    <Link href="">
                      <img src="/images/blog/blog2.png" alt="" />
                    </Link>
                  </div>
                  <div className="post-text">
                    <div className="post-date">June 18, 2024</div>
                    <Link className="post-title" href="">
                      5 tips for safe package delivery
                    </Link>
                  </div>
                </div>
                <div className="post-item">
                  <div className="post-thumb">
                    <Link href="">
                      <img src="/images/blog/blog3.png" alt="" />
                    </Link>
                  </div>
                  <div className="post-text">
                    <div className="post-date">June 18, 2024</div>
                    <Link className="post-title" href="">
                      A day in the life of a delivery rider
                    </Link>
                  </div>
                </div>
              </div>
              <div className="widget">
                <h3 className="wp-block-heading">Tags:</h3>
                <div className="wp-block-tag-cloud">
                  <Link href="">Delivery</Link>
                  <Link href="">Benin City</Link>
                  <Link href="">Riders</Link>
                  <Link href="">Logistics</Link>
                  <Link href="">Business</Link>
                  <Link href="">Tips</Link>
                </div>
              </div>
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
