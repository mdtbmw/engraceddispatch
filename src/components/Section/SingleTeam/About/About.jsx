import Link from "next/link";
import { FaFacebookF, FaLinkedin, FaTwitter } from "react-icons/fa";

const AboutSection = () => {
  return (
    <div className="section zubuz-section-padding2">
      <div className="container">
        <div className="row">
          <div className="col-lg-5">
            <div className="zubuz-team-details-thumb">
              <img src="/images/team/team-details.png" alt="" />
            </div>
          </div>
          <div className="col-lg-7 d-flex align-items-center">
            <div className="zubuz-default-content m-left-85">
              <h2>Founder & CEO of Engraced Dispatch</h2>
              <p>
                Daniel Innocent is the visionary behind Engraced Dispatch.
                He founded the company with a mission to transform delivery
                services in Benin City through speed and reliability.
              </p>
              <p>
                With deep roots in the community, he understood the need for a
                trusted delivery partner and built a team that shares his
                commitment to excellence and customer satisfaction.
              </p>
              <div className="zubuz-extara-mt">
                <div className="zubuz-social-icon social-box">
                  <ul>
                    <li>
                      <Link href="" target="_blank">
                        <FaTwitter />
                      </Link>
                    </li>
                    <li>
                      <Link href="" target="_blank">
                        <FaFacebookF />
                      </Link>
                    </li>
                    <li>
                      <Link href="" target="_blank">
                        <FaLinkedin />
                      </Link>
                    </li>
                  </ul>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AboutSection;
