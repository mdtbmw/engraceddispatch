"use client";
import { useRef, useState, useEffect } from "react";
import Link from "next/link";
import PortfolioFilterNav from "../Components/PortfolioFilterNav";
import { usePortfolioItems } from "~/lib/useCmsData";

const PortfolioClassic = () => {
  const { items: portfolioItems } = usePortfolioItems();
  const isotopeRef = useRef(null);
  const [filterKey, setFilterKey] = useState("*");

  useEffect(() => {
    // Dynamically import Isotope only on the client-side
    import("isotope-layout").then((Isotope) => {
      if (isotopeRef.current) {
        const iso = new Isotope.default(isotopeRef.current, {
          itemSelector: ".filter-item",
          layoutMode: "fitRows",
        });

        // Store the Isotope instance for later use
        isotopeRef.current.isoInstance = iso;

        return () => {
          isotopeRef.current.isoInstance.destroy();
        };
      }
    });
  }, []);

  useEffect(() => {
    if (isotopeRef.current && isotopeRef.current.isoInstance) {
      if (filterKey === "*") {
        isotopeRef.current.isoInstance.arrange({ filter: "*" });
      } else {
        isotopeRef.current.isoInstance.arrange({ filter: `.${filterKey}` });
      }
    }
  }, [filterKey]);

  const handleFilterKeyChange = (key) => () => {
    setFilterKey(key);
  };

  return (
    <div className="section zubuz-section-padding3 overflow-hidden">
      <div className="container">
        <div className="zubuz-section-title center">
          <h2>Our delivery success stories</h2>
        </div>
        <PortfolioFilterNav
          filterKey={filterKey}
          handleFilterKeyChange={handleFilterKeyChange}
        />
        <div
          ref={isotopeRef}
          className="zubuz-portfolio-column zubuz-portfolio-column2 filter-container"
          id="zubuz-one-column"
        >
          {portfolioItems.map((item) => (
            <div key={item.id} className={`filter-item ${item.category}`}>
              <div className="collection-grid-item zubuz-portfolio-wrap2">
                <div className="zubuz-portfolio-thumb2">
                  <img src={item.image} alt={item.title} />
                </div>
                <div className="zubuz-portfolio-data2">
                  <Link href={item.link || "single-portfolio"}>
                    <h3>{item.title}</h3>
                  </Link>
                  <p>{item.description}</p>
                  <Link className="zubuz-portfolio-icon2" href={item.link || "single-portfolio"}>
                    <svg
                      width="48"
                      height="38"
                      viewBox="0 0 48 38"
                      fill="none"
                      xmlns="http://www.w3.org/2000/svg"
                    >
                      <path
                        d="M29 1.5L46.5 19M46.5 19L29 36.5M46.5 19L1.5 19"
                        stroke="white"
                        stroke-width="3"
                        stroke-linecap="round"
                        stroke-linejoin="round"
                      />
                    </svg>
                  </Link>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default PortfolioClassic;
