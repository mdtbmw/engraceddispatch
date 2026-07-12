import Link from "next/link";
import {
  FaFacebookF,
  FaGithub,
  FaLinkedin,
  FaTwitter,
} from "react-icons/fa";

const SingleBlogSection = () => {
  return (
    <div className="section zubuz-section-padding2 post-details-page">
      <div className="container">
        <div className="row">
          <div className="col-lg-8">
            <div className="single-post-content-wrap">
              <div className="post-thumbnail">
                <img src="/images/blog/blog5.png" alt="" />
              </div>
              <div className="post-meta">
                <div className="post-category">
                  <Link href="">Business</Link>
                </div>
                <div className="post-date">June 18, 2024</div>
              </div>
              <div className="entry-content">
                <h3>
                  Fintech for millennials: How apps are meeting the best
                  financial needs of a new generation
                </h3>
                <p>
                  We’ve been embracing information technology in various facets
                  of our lives. From manufacturing to healthcare, IT is
                  omnipresent and is revolutionizing the way we perform tasks
                  and operations.
                </p>
                <p>
                  Fintech is one industry that has constantly embraced IT & has
                  tried to provide the best solution to its users. From
                  passbooks to account logins in our mobile banking app, the
                  finance industry has undergone a 360-degree change.
                </p>

                <h3>Who are millennials and what is their relationship?</h3>
                <p>
                  Millennials are the population it was born on or between the
                  years 1980 to 1996. This generation is technologically adept
                  since this population has seen the tech renaissance, and is
                  now witnessing the rise of AI.
                </p>

                <h3>
                  Key insights and markets for fintech apps for millennials
                </h3>

                <ul className="single-list">
                  <li>
                    With global inflation, the cost of living is also rising.
                    This is compelling millennials to seek more savings and
                    investment options
                  </li>
                  <li>
                    The number of mobile devices as per Exploding Topics is
                    close to 16.8 billion
                  </li>
                  <li>
                    With the increased usage of the internet, the awareness
                    related to the benefits of using fintech applications and
                    personal finance apps is also increasing
                  </li>
                </ul>

                <img
                  className="single-thumb"
                  src="/images/blog/blog-single.png"
                  alt=""
                />

                <h3>Different fintech solutions for millennials</h3>
                <p>
                  There are several types of fintech solutions that are
                  available in the market as of now. Let’s check out the ones
                  that millennials can avail of today:
                </p>

                <ul className="single-list">
                  <li>
                    <strong>Acorns:</strong> The idea behind the application is
                    exceptional. It takes your spare change and invests it for
                    you.
                  </li>
                  <li>
                    <strong>Robinhood:</strong> It is amongst the best trading
                    apps that are out there. It is commission-free and makes the
                    task of buying and selling stocks much easier.
                  </li>
                  <li>
                    <strong>Chime:</strong> Chime is a banking application that
                    offers features like direct deposit, no overdraft fees, and
                    free ATM withdrawals. heir finances.
                  </li>
                </ul>
                <div className="post-tag-wrap">
                  <div className="post-tag">
                    <h3>Tags:</h3>
                    <div className="wp-block-tag-cloud">
                      <Link href="">SaaS</Link>
                      <Link href="">Business</Link>
                    </div>
                  </div>
                  <div className="post-tag">
                    <h3>Share:</h3>
                    <div className="zubuz-social-icon social-box">
                      <ul>
                        <li>
                          <a href="https://twitter.com/" target="_blank">
                            <FaTwitter />
                          </a>
                        </li>
                        <li>
                          <a href="https://facebook.com/" target="_blank">
                            <FaFacebookF />
                          </a>
                        </li>
                        <li>
                          <a href="https://www.linkedin.com/" target="_blank">
                            <FaLinkedin />
                          </a>
                        </li>
                        <li>
                          <a href="https://github.com/" target="_blank">
                            <FaGithub />
                          </a>
                        </li>
                      </ul>
                    </div>
                  </div>
                </div>
                <div className="post-comment">
                  <h3>Comments:</h3>
                  <ul>
                    <li>
                      <div className="post-comment-wrap">
                        <div className="post-comment-thumb">
                          <img src="/images/team/team1.png" alt="" />
                        </div>
                        <div className="post-comment-data">
                          <Link className="comment-reply" href="">
                            Reply
                          </Link>
                          <strong>Ricky Smith</strong>
                          <span>June 21, 2023</span>
                          <p>
                            It is a long established fact that a reader will be
                            distrac readable content of a page when looking at
                            its layout. The point of using is that it has two.
                          </p>
                        </div>
                      </div>
                    </li>
                    <li className="children">
                      <div className="post-comment-wrap">
                        <div className="post-comment-thumb">
                          <img src="/images/team/team2.png" alt="" />
                        </div>
                        <div className="post-comment-data">
                          <Link className="comment-reply" href="">
                            Reply
                          </Link>
                          <strong>Joshua Jones</strong>
                          <span>September 22, 2023</span>
                          <p>
                            It is a long established fact that a reader will be
                            distra readable content of a page when looking its
                            layout. The point of using.
                          </p>
                        </div>
                      </div>
                    </li>
                    <li>
                      <div className="post-comment-wrap">
                        <div className="post-comment-thumb">
                          <img src="/images/team/team3.png" alt="" />
                        </div>
                        <div className="post-comment-data">
                          <Link className="comment-reply" href="">
                            Reply
                          </Link>
                          <strong>Mark Jones</strong>
                          <span>June 21, 2023</span>
                          <p>
                            It is a long established fact that a reader will be
                            distrac readable content of a page when looking at
                            its layout. The point of using is that it has two.
                          </p>
                        </div>
                      </div>
                    </li>
                  </ul>
                </div>
                <div className="comment-box">
                  <h3>Leave a comments:</h3>
                  <form action="#">
                    <div className="comment-box-form">
                      <div className="row">
                        <div className="col-lg-6">
                          <div className="comment-form">
                            <input type="text" placeholder="Your Name*" />
                          </div>
                        </div>
                        <div className="col-lg-6">
                          <div className="comment-form">
                            <input type="email" placeholder="Email Address*" />
                          </div>
                        </div>
                      </div>
                      <div className="comment-form">
                        <textarea
                          name="textarea"
                          placeholder="Write us your comment"
                        ></textarea>
                      </div>
                      <button id="comment-btn" type="submit">
                        <span>Send Message</span>
                      </button>
                    </div>
                  </form>
                </div>
              </div>
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
              <div className="widget">
                <h3 className="wp-block-heading">Categories:</h3>
                <ul>
                  <li>
                    <Link href="">SaaS</Link>
                  </li>
                  <li>
                    <Link href="">Marketing</Link>
                  </li>
                  <li>
                    <Link href="">Technology</Link>
                  </li>
                  <li>
                    <Link href="">SaaS Metrics</Link>
                  </li>
                  <li>
                    <Link href="">Development</Link>
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
                      7 businesses for easy money
                    </Link>
                  </div>
                </div>
                <div className="post-item">
                  <div className="post-thumb">
                    <a href="">
                      <img src="/images/blog/blog2.png" alt="" />
                    </a>
                  </div>
                  <div className="post-text">
                    <div className="post-date">June 18, 2024</div>
                    <Link className="post-title" href="">
                      My 3 tips for business ideas
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
                      12 Halloween costume ideas
                    </Link>
                  </div>
                </div>
              </div>
              <div className="widget">
                <h3 className="wp-block-heading">Tags:</h3>
                <div className="wp-block-tag-cloud">
                  <Link href="">Marketing</Link>
                  <Link href="">Business</Link>
                  <Link href="">SaaS</Link>
                  <Link href="">Development</Link>
                  <Link href="">UI/UX</Link>
                  <Link href="">Brand</Link>
                </div>
              </div>
              <div className="zubuz-blog-contact">
                <h3>How can we help you?</h3>
                <p>
                  We are here to help you! Tell us how we can help and we’ll get
                  in touch within next 24hrs
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

export default SingleBlogSection;
