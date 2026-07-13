"use client";
import Icon from "~/components/Ui/Icon";
import { useSiteContent } from "~/lib/useSiteContent";

const CallUsSection = () => {
  const { content } = useSiteContent();
  const phone = content?.contactPhone || "+234-806-123-4567";
  const email = content?.contactEmail || "dispatch@engracedsmiles.com";
  const address = content?.contactAddress || "No 18, Sakponba Road, Benin City, Edo State.";
  const phones = phone.split(",").map(p => p.trim()).filter(Boolean);
  const emails = email.split(",").map(e => e.trim()).filter(Boolean);
  return (
    <div className="section dark-bg zubuz-section-padding6 dark-bg">
      <div className="container">
        <div className="row">
          <div className="col-lg-4 col-md-6">
            <a href={`tel:${phones[0] || "123"}`}>
              <div className="zubuz-iconbox-wrap-left text-center-lg rt-mb-24">
                <div className="zubuz-iconbox-icon">
                  <Icon name="phone" size={28} />
                </div>
                <div className="zubuz-iconbox-data light">
                  <h3>Call us directly</h3>
                  <p>{phones.map((p, i) => <span key={i}>{p}<br /></span>)}</p>
                </div>
              </div>
            </a>
          </div>
          <div className="col-lg-4 col-md-6">
            <a href={`mailto:${emails[0] || "name@email.com"}`}>
              <div className="zubuz-iconbox-wrap-left text-center-lg rt-mb-24">
                <div className="zubuz-iconbox-icon">
                  <Icon name="email" size={28} />
                </div>
                <div className="zubuz-iconbox-data light">
                  <h3>Email us</h3>
                  <p>{emails.map((e, i) => <span key={i}>{e}<br /></span>)}</p>
                </div>
              </div>
            </a>
          </div>
          <div className="col-lg-4 col-md-6">
            <div className="zubuz-iconbox-wrap-left text-center-lg rt-mb-24">
              <div className="zubuz-iconbox-icon">
                <Icon name="map-pin" size={28} />
              </div>
              <div className="zubuz-iconbox-data light">
                <h3>Our office address</h3>
                <p>{address.split(",").map((l, i) => <span key={i}>{l.trim()}<br /></span>)}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CallUsSection;
