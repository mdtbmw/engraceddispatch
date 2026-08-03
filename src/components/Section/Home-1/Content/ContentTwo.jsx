"use client";
import { ScrollReveal, StaggerContainer, StaggerItem } from "~/components/Animations";
import Icon from "~/components/Ui/Icon";

const ContentSectionTwo = () => {
  return (
    <div className="section zubuz-section-padding5 white-bg">
      <div className="container">
        <div className="row">
          <div className="col-lg-5 order-lg-2">
            <ScrollReveal variant="slideLeft">
              <div className="zubuz-thumb thumb-pl" style={{ borderRadius: '16px', overflow: 'hidden' }}>
                <img src="/images/v1/oil-refinery.jpg" alt="Oil Refinery" style={{ width: '100%', height: 'auto', objectFit: 'cover' }} />
              </div>
            </ScrollReveal>
          </div>
          <div className="col-lg-7 d-flex align-items-center">
            <ScrollReveal variant="slideRight">
              <div className="zubuz-default-content">
                <h2>Refined Products & Global Distribution</h2>
                <p>
                  We partner directly with leading private-sector refineries to secure high-quality refined petroleum products. Our robust distribution networks ensure reliable supply chains to commercial buyers globally.
                </p>
                <StaggerContainer staggerDelay={0.15}>
                  <div className="zubuz-extara-mt">
                    <StaggerItem>
                      <div className="zubuz-iconbox-wrap-left">
                        <div className="zubuz-iconbox-icon" style={{ overflow: 'hidden', padding: 0 }}>
                          <img src="https://placehold.co/56x56/F26522/FFFFFF?text=Quality" alt="Quality Check" style={{ width: '100%', height: '100%' }} />
                        </div>
                        <div className="zubuz-iconbox-data">
                          <span>Quality Assurance</span>
                          <p>
                            Every batch of jet fuel, diesel, and gasoline we trade meets stringent international industry standards for safety and performance.
                          </p>
                        </div>
                      </div>
                    </StaggerItem>
                    <StaggerItem>
                      <div className="zubuz-iconbox-wrap-left">
                        <div className="zubuz-iconbox-icon" style={{ overflow: 'hidden', padding: 0 }}>
                          <img src="https://placehold.co/56x56/0B2046/FFFFFF?text=Scale" alt="Global Scale" style={{ width: '100%', height: '100%' }} />
                        </div>
                        <div className="zubuz-iconbox-data">
                          <span>Global Scale</span>
                          <p>
                            We provide dependable energy supply solutions capable of supporting the growing operational needs of businesses and governments worldwide.
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
