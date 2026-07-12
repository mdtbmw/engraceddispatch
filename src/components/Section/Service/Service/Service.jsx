"use client";
import Link from "next/link";
import SerciveCard from "~/components/Ui/Cards/Sercive";
import { useServices } from "~/lib/useCmsData";
import { useSiteContent } from "~/lib/useSiteContent";

const ServiceSection = () => {
  const { content } = useSiteContent();
  const { items: services } = useServices();
  return (
    <div className="section zubuz-section-padding3">
      <div className="container">
        <div className="zubuz-section-title zubuz-two-column-title">
          <div className="row">
            <div className="col-lg-7">
              <h2>{content.servicesTitle}</h2>
            </div>
            <div className="col-lg-5 d-flex align-items-center">
              <p>{content.servicesDescription}</p>
            </div>
          </div>
        </div>
        <div className="row">
          {services?.map((service, index) => (
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
