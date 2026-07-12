/* eslint-disable react/no-unescaped-entities */

import Link from "next/link";
import {
  FaFacebookF,
  FaGithub,
  FaInstagram,
  FaLinkedin,
  FaTwitter,
} from "react-icons/fa";

const FooterTwo = () => {
  return (
    <footer className="zubuz-footer-section">
      <div className="container">
        <div className="zubuz-footer-top">
          <div className="row">
            <div className="col-xl-4 col-lg-12">
              <div className="zubuz-footer-textarea">
                <Link href="/">
                  <img src="/images/logo/logo-dark.svg" alt="" />
                </Link>
                <p>
                  Your trusted delivery partner in Benin City. Fast, reliable,
                  and affordable package delivery across the city.
                </p>
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
            <div className="col-xl-3 col-md-4">
              <div className="zubuz-footer-menu extar-margin">
                <div className="zubuz-footer-title">
                  <p>Quick links</p>
                </div>
                <ul>
                  <li>
                    <Link href="">Home</Link>
                  </li>
                  <li>
                    <Link href="">About Us</Link>
                  </li>
                  <li>
                    <Link href="">Services</Link>
                  </li>
                  <li>
                    <Link href="">Pricing</Link>
                  </li>
                  <li>
                    <Link href="">Contact</Link>
                  </li>
                </ul>
              </div>
            </div>
            <div className="col-xl-2 col-md-4">
              <div className="zubuz-footer-menu">
                <div className="zubuz-footer-title">
                  <p>Services</p>
                </div>
                <ul>
                  <li>
                    <Link href="">Same-day delivery</Link>
                  </li>
                  <li>
                    <Link href="">Scheduled delivery</Link>
                  </li>
                  <li>
                    <Link href="">Bulk delivery</Link>
                  </li>
                  <li>
                    <Link href="">Food delivery</Link>
                  </li>
                  <li>
                    <Link href="">Document courier</Link>
                  </li>
                </ul>
              </div>
            </div>
            <div className="col-xl-3 col-md-4">
              <div className="zubuz-footer-menu extar-margin">
                <div className="zubuz-footer-title">
                  <p>Support</p>
                </div>
                <ul>
                  <li>
                    <Link href="">Help centre</Link>
                  </li>
                  <li>
                    <Link href="">Privacy policy</Link>
                  </li>
                  <li>
                    <Link href="">Terms &amp; Conditions</Link>
                  </li>
                  <li>
                    <Link href="">Become a rider</Link>
                  </li>
                  <li>
                    <Link href="">FAQs</Link>
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </div>
        <div className="zubuz-footer-bottom center">
          <div className="zubuz-copywright">
            <p> &copy;Copyright 2024, Engraced Dispatch. All rights reserved.</p>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default FooterTwo;
