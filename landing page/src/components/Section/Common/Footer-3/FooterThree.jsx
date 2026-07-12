/* eslint-disable react/no-unescaped-entities */

import Link from "next/link";
import {
  FaFacebookF,
  FaGithub,
  FaInstagram,
  FaLinkedin,
  FaTwitter,
} from "react-icons/fa";

const FooterSectionThree = () => {
  return (
    <footer className="zubuz-footer-section dark-bg">
      <div className="container">
        <div className="zubuz-footer-extra-top">
          <div className="row">
            <div className="col-lg-7">
              <div className="zubuz-footer-extra-title">
                <h2>Ready to experience fast delivery?</h2>
              </div>
            </div>
            <div className="col-lg-5 d-flex align-items-center">
              <div className="zubuz-footer-btn">
                <Link className="zubuz-default-btn pill" href="contact-us">
                  <span>Request a delivery</span>
                </Link>
              </div>
            </div>
          </div>
        </div>

        <div className="zubuz-footer-top">
          <div className="row">
            <div className="col-xl-4 col-lg-12">
              <div className="zubuz-footer-textarea light">
                <Link href="/">
                  <img src="/images/logo/logo-white.svg" alt="" />
                </Link>
                <p>
                  Your trusted delivery partner in Benin City. Fast, reliable,
                  and affordable package delivery across the city.
                </p>
                <div className="zubuz-social-icon social-box social-box-white">
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
              <div className="zubuz-footer-menu light extar-margin">
                <div className="zubuz-footer-title light">
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
              <div className="zubuz-footer-menu light">
                <div className="zubuz-footer-title light">
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
              <div className="zubuz-footer-menu light info extar-margin">
                <div className="zubuz-footer-title light">
                  <p>Contact</p>
                </div>
                <ul>
                  <li>
                    <Link href="">
                      <img src="/images/icon/call.svg" alt="" />
                      +234-806-123-4567
                    </Link>
                  </li>
                  <li>
                    <Link href="">
                      <img src="/images/icon/email.svg" alt="" />
                      dispatch@engracedsmiles.com
                    </Link>
                  </li>
                  <li>
                    <Link href="">
                      <img src="/images/icon/map.svg" alt="" />
                      Sakponba Road, Benin City
                    </Link>
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </div>
        <div className="zubuz-footer-bottom center light">
          <div className="zubuz-copywright light">
            <p> &copy;Copyright 2024, Dispatch. All rights reserved.</p>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default FooterSectionThree;
