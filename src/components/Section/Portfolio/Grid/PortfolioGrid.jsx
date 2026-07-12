"use client";
import { useRef, useState, useEffect } from "react";
import Link from "next/link";
import PortfolioFilterNav from "../Components/PortfolioFilterNav";
import { usePortfolioItems } from "~/lib/useCmsData";

const PortfolioGrid = () => {
  const { items: portfolioItems } = usePortfolioItems();
  const isotopeRef = useRef(null);
  const [filterKey, setFilterKey] = useState("*");

  useEffect(() => {
    // Check if window is defined (i.e., in the browser environment)
    if (typeof window !== "undefined") {
      // Dynamically import Isotope only in the client-side code
      import("isotope-layout").then((Isotope) => {
        // Initialize Isotope grid
        isotopeRef.current = new Isotope.default(".filter-container", {
          itemSelector: ".filter-item",
          layoutMode: "fitRows",
        });
      });
    }
  }, []);

  useEffect(() => {
    // Check if isotopeRef.current is defined (i.e., Isotope is initialized)
    if (isotopeRef.current) {
      // Arrange items based on filter key
      if (filterKey === "*") isotopeRef.current.arrange({ filter: `*` });
      else isotopeRef.current.arrange({ filter: `.${filterKey}` });
    }
  }, [filterKey]);

  // Event handler for filter key change
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
          className="zubuz-portfolio-column filter-container"
          id="zubuz-two-column"
        >
          {portfolioItems.map((item) => (
            <div key={item.id} className={`filter-item ${item.category}`}>
              <div className="collection-grid-item zubuz-portfolio-wrap">
                <div className="zubuz-portfolio-thumb">
                  <img src={item.image} alt={item.title} />
                  <div className="zubuz-portfolio-data">
                    <Link href={item.link || "single-portfolio"}>
                      <h3>{item.title}</h3>
                    </Link>
                    <p>{item.description}</p>
                    <Link
                      className="zubuz-portfolio-icon"
                      href={item.link || "single-portfolio"}
                    >
                      <img src="/images/portfolio/arrow-right.svg" alt="" />
                    </Link>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default PortfolioGrid;
