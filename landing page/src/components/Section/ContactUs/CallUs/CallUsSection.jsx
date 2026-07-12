const CallUsSection = () => {
  return (
    <div className="section dark-bg zubuz-section-padding6 dark-bg">
      <div className="container">
        <div className="row">
          <div className="col-lg-4 col-md-6">
            <a href="tel:123">
              <div className="zubuz-iconbox-wrap-left text-center-lg rt-mb-24">
                <div className="zubuz-iconbox-icon">
                  <img src="/images/icon/call2.svg" />
                </div>
                <div className="zubuz-iconbox-data light">
                  <h3>Call us directly</h3>
                  <p>
                    +234-806-123-4567
                    <br />
                    +234-807-765-4321
                  </p>
                </div>
              </div>
            </a>
          </div>
          <div className="col-lg-4 col-md-6">
            <a href="mailto:name@email.com">
              <div className="zubuz-iconbox-wrap-left text-center-lg rt-mb-24">
                <div className="zubuz-iconbox-icon">
                  <img src="/images/icon/email3.svg" />
                </div>
                <div className="zubuz-iconbox-data light">
                  <h3>Email us</h3>
                  <p>
                    dispatch@engracedsmiles.com
                    <br />
                    support@engracedsmiles.com
                  </p>
                </div>
              </div>
            </a>
          </div>
          <div className="col-lg-4 col-md-6">
            <div className="zubuz-iconbox-wrap-left text-center-lg rt-mb-24">
              <div className="zubuz-iconbox-icon">
                <img src="/images/icon/map2.svg" />
              </div>
              <div className="zubuz-iconbox-data light">
                <h3>Our office address</h3>
                <p>
                  No 18, Sakponba Road,
                  <br /> Benin City, Edo State.
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CallUsSection;
