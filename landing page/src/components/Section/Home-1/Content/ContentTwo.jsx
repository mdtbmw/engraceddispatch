"use client";
import { ScrollReveal, StaggerContainer, StaggerItem } from "~/components/Animations";

const ContentSectionTwo = () => {
  return (
    <div className="section zubuz-section-padding5 white-bg">
      <div className="container">
        <div className="row">
          <div className="col-lg-5 order-lg-2">
            <ScrollReveal variant="slideLeft">
              <div className="zubuz-thumb thumb-pl">
                <img src="/images/v1/mocup2.png" alt="" />
                <div className="zubuz-thumb-card2">
                  <img src="/images/v1/card2.png" alt="" />
                </div>
              </div>
            </ScrollReveal>
          </div>
          <div className="col-lg-7 d-flex align-items-center">
            <ScrollReveal variant="slideRight">
              <div className="zubuz-default-content">
                <h2>Real-time tracking at your fingertips</h2>
                <p>
                  Know exactly where your package is at all times. The Dispatch
                  app gives you live GPS tracking across all major areas of
                  Benin City and surrounding suburbs.
                </p>
                <StaggerContainer staggerDelay={0.15}>
                  <div className="zubuz-extara-mt">
                    <StaggerItem>
                      <div className="zubuz-iconbox-wrap-left">
                        <div className="zubuz-iconbox-icon">
                          <img src="/images/v1/icon1.png" alt="" />
                        </div>
                        <div className="zubuz-iconbox-data">
                          <span>Live GPS Tracking</span>
                          <p>
Track your delivery in real time from pickup to
                              drop-off with the Dispatch app&rsquo;s GPS-enabled system.
                          </p>
                        </div>
                      </div>
                    </StaggerItem>
                    <StaggerItem>
                      <div className="zubuz-iconbox-wrap-left">
                        <div className="zubuz-iconbox-icon">
                          <img src="/images/v1/icon2.png" alt="" />
                        </div>
                        <div className="zubuz-iconbox-data">
                          <span>City-Wide Coverage</span>
                          <p>
                            The Dispatch app delivers to every neighbourhood in Benin City, from
                            GRA to Uselu, Ekosodin to Oregbeni.
                          </p>
                        </div>
                      </div>
                    </StaggerItem>
                  </div>
                </StaggerContainer>
              </div>
            </ScrollReveal>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ContentSectionTwo;
