"use client";
import FeatureCardThree from "~/components/Ui/Cards/FeatureCardThree";
import features from "~/data/featureDataTwo.json";

const FeatureSection = () => {

  return (
    <div className="section zubuz-section-padding3">
      <div className="container">
        <div className="zubuz-section-title zubuz-two-column-title">
          <div className="row">
            <div className="col-lg-7">
              <h2>Perks of being a delivery rider</h2>
            </div>
            <div className="col-lg-5 d-flex align-items-center">
              <p>
                We value our riders and offer flexible work options, performance
                bonuses, and a supportive team environment across Benin City.
              </p>
            </div>
          </div>
        </div>
        <div className="row">
          {features.map((feature, index) => (
            <FeatureCardThree key={index} feature={feature} />
          ))}
        </div>
      </div>
    </div>
  );
};

export default FeatureSection;
