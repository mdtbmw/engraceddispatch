"use client";
import { useSiteContent } from "~/lib/useSiteContent";
import Icon from "~/components/Ui/Icon";

const ContentSection = () => {
  const { content } = useSiteContent();
  return (
    <div className="section zubuz-section-padding2">
      <div className="container">
        <div className="row">
          <div className="col-lg-5">
            <div className="zubuz-v3-thumb">
              <img src="/images/v3/thumb-v3-2.png" alt="" />
              <div className="zubuz-v3-card">
                <img src="/images/v3/card-v3-4.png" alt="" />
              </div>
            </div>
          </div>
          <div className="col-lg-7">
            <div className="zubuz-default-content m-left">
              <h2>{content.aboutTitle}</h2>
              <p>{content.aboutMission}</p>
              <div className="zubuz-extara-mt">
                <div className="zubuz-iconbox-wrap-left mw-100">
                  <div className="zubuz-iconbox-icon none-bg">
                    <Icon name="speed" size={28} />
                  </div>
                  <div className="zubuz-iconbox-data data-small">
                    <span>Operational Excellence:</span>
                    <p>
                      We prioritise swift execution and uncompromised safety in all our maritime and overland logistics.
                      Your energy commodities arrive securely, strictly on schedule.
                    </p>
                  </div>
                </div>
                <div className="zubuz-iconbox-wrap-left mw-100">
                  <div className="zubuz-iconbox-icon none-bg">
                    <Icon name="trust" size={28} />
                  </div>
                  <div className="zubuz-iconbox-data data-small">
                    <span>Integrity & Transparency:</span>
                    <p>
                      From clear, competitive pricing to honest communication across the supply chain,
                      we build long-term partnerships anchored in absolute trust.
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ContentSection;
