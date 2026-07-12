const ContentSection = () => {
  return (
    <div className="section zubuz-section-padding2">
      <div className="container">
        <div className="row">
          <div className="col-lg-5">
            <div className="zubuz-v3-thumb">
              <img src="/images/v3/thumb-v3-2.png" alt="" />
              <div className="zubuz-v3-card">
                <img src="/images/v3/card-v3-4.png" alt="" />
              </div>
            </div>
          </div>
          <div className="col-lg-7">
            <div className="zubuz-default-content m-left">
              <h2>Why Dispatch works</h2>
              <p>
                Dispatch is built to make courier delivery seamless, reliable,
                and stress-free — whether you are sending a parcel across town
                or a package across Benin City.
              </p>
              <div className="zubuz-extara-mt">
                <div className="zubuz-iconbox-wrap-left mw-100">
                  <div className="zubuz-iconbox-icon none-bg">
                    <img src="/images/about/icon1.png" alt="" />
                  </div>
                  <div className="zubuz-iconbox-data data-small">
                    <span>Speed & Reliability:</span>
                    <p>
                      We prioritise quick turnaround times without compromising
                      on safety. Your package arrives when promised, every time.
                    </p>
                  </div>
                </div>
                <div className="zubuz-iconbox-wrap-left mw-100">
                  <div className="zubuz-iconbox-icon none-bg">
                    <img src="images/about/icon2.png" alt="" />
                  </div>
                  <div className="zubuz-iconbox-data data-small">
                    <span>Trust & Transparency:</span>
                    <p>
                      Real-time tracking, clear pricing, and honest
                      communication. We earn your trust with every delivery.
                    </p>
                  </div>
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
