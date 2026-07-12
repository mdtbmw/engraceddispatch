const FeatureCardTwo = ({ title, description, image, icon }) => {
  return (
    <div className="zubuz-iconbox-wrap2">
      <div className="zubuz-iconbox-icon">
        <img src={icon} alt="" />
      </div>
      <div className="zubuz-iconbox-data">
        <h3>{title}</h3>
        <p>{description}</p>
      </div>
      <div className="zubuz-iconbox-thumb">
        <img src={image} alt="" />
      </div>
    </div>
  );
};

export default FeatureCardTwo;
