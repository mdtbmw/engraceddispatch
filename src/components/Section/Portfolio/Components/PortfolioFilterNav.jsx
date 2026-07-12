const PortfolioFilterNav = ({ filterKey, handleFilterKeyChange }) => {
  return (
    <div className="zubuz-portfolio-menu">
      <ul
        id="watch-filter-gallery"
        className="option-set clear-both"
        data-option-key="filter"
      >
        <li
          className={filterKey === "*" ? "active" : ""}
          onClick={handleFilterKeyChange("*")}
        >
          All
        </li>
        <li
          className={filterKey === "branding" ? "active" : ""}
          onClick={handleFilterKeyChange("branding")}
        >
          Branding
        </li>
        <li
          className={filterKey === "ui" ? "active" : ""}
          onClick={handleFilterKeyChange("ui")}
        >
          UI/UX
        </li>
        <li
          className={filterKey === "website" ? "active" : ""}
          onClick={handleFilterKeyChange("website")}
        >
          Website
        </li>
      </ul>
    </div>
  );
};

export default PortfolioFilterNav;
