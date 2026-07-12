import Icon from "~/components/Ui/Icon";

const FeatureCardThree = ({ feature }) => {
  const isImage = feature.icon && feature.icon.startsWith("/");
  return (
    <div className="col-xl-4 col-md-6">
      <div className="zubuz-iconbox-wrap-left iconbox-left-border">
        <div className="zubuz-iconbox-icon none-bg">
          {isImage ? (
            <img src={feature.icon} alt={feature.title} />
          ) : (
            <Icon name={feature.icon} size={28} />
          )}
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