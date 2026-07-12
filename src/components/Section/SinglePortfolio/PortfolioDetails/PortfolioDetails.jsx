"use client";
import Link from "next/link";
import { usePortfolioItems } from "~/lib/useCmsData";

const PortfolioDetailsSection = () => {
  const { items } = usePortfolioItems();
  const portfolio = items?.[0];
  return (
    <div className="section zubuz-section-padding">
      <div className="container">
        {portfolio && (
          <>
            <div className="zubuz-section-title zubuz-two-column-title">
              <div className="row">
                <div className="col-lg-7">
                  <h2>{portfolio.title}</h2>
                </div>
                <div className="col-lg-5 d-flex align-items-center">
                  <p>{portfolio.description}</p>
                </div>
              </div>
            </div>
            <div className="zubuz-portfolio-details-thumb">
              <img src={portfolio.image} alt={portfolio.title} />
            </div>
            <div className="zubuz-portfolio-info">
              <div className="zubuz-portfolio-info-item">
                <p>Client</p>
                <h3>{portfolio.client || "N/A"}</h3>
              </div>
              <div className="zubuz-portfolio-info-item">
                <p>Services:</p>
                <h3>{portfolio.category || "Delivery"}</h3>
              </div>
              <div className="zubuz-portfolio-info-item">
                <p>Duration:</p>
                <h3>{portfolio.duration || "Ongoing"}</h3>
              </div>
              <div className="zubuz-portfolio-info-item">
                <p>Website</p>
                <Link href={portfolio.link || "#"}>
                  <h3>
                    Live preview <img src="/images/icon/arrow-right.svg" alt="" />
                  </h3>
                </Link>
              </div>
            </div>
            <div className="row">
              <div className="col-lg-8">
                <div className="zubuz-portfolio-details-content-wrap">
                  <div className="zubuz-portfolio-details-content">
                    <h2>Project overview:</h2>
                    <p>{portfolio.overview || portfolio.description}</p>
                  </div>
                  <div className="zubuz-portfolio-details-content">
                    <h2>Project execution:</h2>
                    <p>{portfolio.execution || ""}</p>
                  </div>
                  <div className="zubuz-portfolio-details-content">
                    <h2>Project results:</h2>
                    <p>{portfolio.results || ""}</p>
                  </div>
                </div>
              </div>
              <div className="col-lg-4">
                <div className="zubuz-portfolio-details-thumb-wrap">
                  {portfolio.gallery?.map((img, i) => (
                    <div key={i} className="zubuz-portfolio-details-thumb-item">
                      <img src={img} alt="" />
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </>
        )}
      </div>
    </div>
  );
};

export default PortfolioDetailsSection;
