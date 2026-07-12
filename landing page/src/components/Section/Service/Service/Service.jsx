"use client";
import Link from "next/link";
import SerciveCard from "~/components/Ui/Cards/Sercive";
import serviceDatas from "~/data/serviceData.json";

const ServiceSection = () => {

  return (
    <div className="section zubuz-section-padding3">
      <div className="container">
        <div className="zubuz-section-title zubuz-two-column-title">
          <div className="row">
            <div className="col-lg-7">
              <h2>Dispatch delivery services</h2>
            </div>
            <div className="col-lg-5 d-flex align-items-center">
              <p>
                From documents to packages, we offer a full range of delivery
                options to meet your needs across Benin City.
              </p>
            </div>
          </div>
        </div>
        <div className="row">
          {serviceDatas?.map((service, index) => (
            <SerciveCard
              key={index}
              title={service?.title}
              icon={service?.icon}
              description={service?.description}
              link={service?.link}
            />
          ))}
        </div>
      </div>
    </div>
  );
};

export default ServiceSection;
