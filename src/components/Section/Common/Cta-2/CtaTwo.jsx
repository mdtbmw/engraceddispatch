import { PlayStoreBadge, AppStoreBadge } from "~/components/Ui/StoreBadge";

const CtaTwo = () => {
  return (
    <div className="zubuz-cta-section2">
      <div className="container">
        <div
          className="zubuz-cta-wrap"
          style={{ backgroundImage: "url(/images/v2/cta-bg.png)" }}
        >
          <div className="zubuz-cta-content">
            <h2>Download the app &amp; start delivering</h2>
            <div
              style={{
                display: "flex",
                justifyContent: "center",
                gap: 16,
                flexWrap: "wrap",
              }}
            >
              <PlayStoreBadge dark />
              <AppStoreBadge dark />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CtaTwo;
