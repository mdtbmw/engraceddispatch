const FeatureCardThree = ({ feature }) => {
    return (
        <div className="col-xl-4 col-md-6">
        <div className="zubuz-iconbox-wrap-left iconbox-left-border">
          <div className="zubuz-iconbox-icon none-bg">
            <img src={feature.icon} alt={feature.title} />
          </div>
          <div className="zubuz-iconbox-data data-small">
            <span>{feature.title}</span>
            <p>{feature.description}</p>
          </div>
        </div>
      </div>
    );
};

export default FeatureCardThree;