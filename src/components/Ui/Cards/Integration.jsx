import Link from "next/link";

const IntegrationCard = ({ icon, name, category, description, link }) => {
  return (
    <div className="col-xl-4 col-md-6">
      <div className="zubuz-iconbox-wrap-left d-block iconbox-left-border">
        <div className="zubuz-iconbox-header">
          <div className="zubuz-iconbox-icon">
            <img src={icon} alt="" />
          </div>
          <div className="zubuz-iconbox-header-data">
            <h4>{name}</h4>
            <p>{category}</p>
          </div>
        </div>
        <div className="zubuz-iconbox-data">
          <p>{description}</p>
          <Link className="zubuz-iconbox-btn" href="">
            <span>Read more</span>
            <img src="/images/icon/arrow-right2.svg" alt="" />
          </Link>
        </div>
      </div>
    </div>
  );
};

export default IntegrationCard;
