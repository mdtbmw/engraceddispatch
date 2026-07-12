const ContentSection = () => {
  return (
    <div className="section zubuz-section-padding2 dark-bg">
      <div className="container">
        <div className="row">
          <div className="col-lg-5">
            <div className="zubuz-career-thumb">
              <img src="/images/v3/thumb-v3-3.png" alt="" />
            </div>
          </div>
          <div className="col-lg-7">
            <div className="zubuz-default-content m-left light">
              <h2>Dispatch rider hiring process</h2>
              <div className="zubuz-extara-mt">
                <div className="zubuz-iconbox-wrap-left">
                  <div className="zubuz-iconbox-number">1</div>
                  <div className="zubuz-iconbox-data light">
                    <span>Application submission</span>
                    <p>
                      Submit your application with valid ID and driving license
                      to join our delivery team in Benin City.
                    </p>
                  </div>
                </div>
                <div className="zubuz-iconbox-wrap-left">
                  <div className="zubuz-iconbox-number">2</div>
                  <div className="zubuz-iconbox-data light">
                    <span>Screening &amp; road test</span>
                    <p>
                      We verify your documents and conduct a road test to assess
                      your riding skills and knowledge of Benin City routes.
                    </p>
                  </div>
                </div>
                <div className="zubuz-iconbox-wrap-left">
                  <div className="zubuz-iconbox-number">3</div>
                  <div className="zubuz-iconbox-data light">
                    <span>Final selection &amp; onboarding</span>
                    <p>
                      Successful riders receive onboarding, safety gear, and are
                      assigned to their delivery zones immediately.
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
