"use client";
import IntegrationCard from "~/components/Ui/Cards/Integration";
import platformDatas from "~/data/platformData.json";

const IntegrationSection = () => {

  return (
    <div className="section zubuz-section-padding3">
      <div className="container">
        <div className="zubuz-section-title center w-large">
          <h2>Integrated with Benin City&apos;s favourite platforms</h2>
        </div>
        <div className="row">
          {platformDatas?.map((platform, index) => (
            <IntegrationCard
              key={index}
              icon={platform?.icon}
              name={platform?.name}
              category={platform?.category}
              description={platform?.description}
              link={platform?.link}
            />
          ))}
        </div>
      </div>
    </div>
  );
};

export default IntegrationSection;
