const ContentSection = () => {
  return (
    <div className="section zubuz-section-padding5">
      <div className="container">
        <div className="row">
          <div className="col-lg-7">
            <div className="zubuz-default-content m-right-50">
              <h2>Delivery services for every need</h2>
              <p>
                Whether you are sending documents, gifts, food, or business
                inventory, Dispatch has a service tailored
                for you.
              </p>
              <p>
                Our dedicated team ensures every package is handled with care
                from pickup to drop-off, giving you complete peace of mind.
              </p>
            </div>
          </div>
          <div className="col-lg-5">
            <div className="zubuz-default-content">
              <div className="zubuz-iconbox-wrap-left mw-100">
                <div className="zubuz-iconbox-icon none-bg">
                  <img src="/images/service/icon.png" alt="" />
                </div>
                <div className="zubuz-iconbox-data data-small">
                  <span>Bike Delivery:</span>
                  <p>
                    Fast motorcycle delivery for small to medium packages
                    across Benin City. Perfect for urgent deliveries.
                  </p>
                </div>
              </div>
              <div className="zubuz-iconbox-wrap-left mw-100">
                <div className="zubuz-iconbox-icon none-bg">
                  <img src="/images/v1/icon3.png" alt="" />
                </div>
                <div className="zubuz-iconbox-data data-small">
                  <span>Courier Service:</span>
                  <p>
                    Reliable courier service for documents, parcels, and
                    business packages throughout Benin City and environs.
                  </p>
                </div>
              </div>
              <div className="zubuz-iconbox-wrap-left mw-100">
                <div className="zubuz-iconbox-icon none-bg">
                  <img src="/images/v3/icon10.png" alt="" />
                </div>
                <div className="zubuz-iconbox-data data-small">
                  <span>Same-Day Delivery:</span>
                  <p>
                    Order in the morning and receive before evening. Our riders
                    are strategically positioned for rapid response.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ContentSection;
