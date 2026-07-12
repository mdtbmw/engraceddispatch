"use client";
import FeatureCard from "~/components/Ui/Cards/Feature";
import { ScrollReveal, StaggerContainer, StaggerItem } from "~/components/Animations";
import featureDatas from "~/data/featureData.json";

const FeatureSection = () => {

  return (
    <div className="section zubuz-section-padding3 light-bg">
      <div className="container">
        <ScrollReveal variant="slideUp">
          <div className="zubuz-section-title center">
            <h2>Everything you need in one app</h2>
          </div>
        </ScrollReveal>
        <StaggerContainer staggerDelay={0.1}>
          <div className="row">
            {featureDatas?.map((feature, index) => (
              <StaggerItem key={index} className="col-xl-4 col-md-6">
                <FeatureCard
                  title={feature?.title}
                  icon={feature?.icon}
                  description={feature?.description}
                />
              </StaggerItem>
            ))}
          </div>
        </StaggerContainer>
      </div>
    </div>
  );
};

export default FeatureSection;