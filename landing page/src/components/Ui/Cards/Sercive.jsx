import Link from "next/link";
import {
  FaMotorcycle,
  FaTruck,
  FaClock,
  FaBoxes,
  FaUtensils,
  FaSearch,
  FaFileAlt,
  FaGift,
  FaBuilding,
  FaArrowRight,
} from "react-icons/fa";

const iconMap = {
  FaMotorcycle,
  FaTruck,
  FaClock,
  FaBoxes,
  FaUtensils,
  FaSearch,
  FaFileAlt,
  FaGift,
  FaBuilding,
};

const SerciveCard = ({ title, description, icon, link }) => {
  const Icon = iconMap[icon] || FaTruck;
  return (
    <div className="col-xl-4 col-md-6">
      <div className="zubuz-iconbox-wrap-left iconbox-left-border">
        <div className="zubuz-iconbox-icon none-bg">
          <Icon size={32} />
        </div>
        <div className="zubuz-iconbox-data data-small">
          <span>{title}</span>
          <p>{description}</p>
          <Link className="zubuz-iconbox-btn" href={link}>
            <span>Read more</span>
            <FaArrowRight size={12} />
          </Link>
        </div>
      </div>
    </div>
  );
};

export default SerciveCard;
