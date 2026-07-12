"use client";
import CareerCard from "~/components/Ui/Cards/CareerCard";
import jobs from "~/data/careerData.json";

const CareerSection = () => {

  return (
    <div className="section zubuz-section-padding3">
      <div className="container">
        <div className="zubuz-section-title center">
          <h2>Join the Dispatch team</h2>
        </div>
        <div className="zubuz-jobs-wrap">
          {jobs.map((job, index) => (
            <CareerCard key={index} job={job} />
          ))}
        </div>
      </div>
    </div>
  );
};

export default CareerSection;
