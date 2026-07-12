import Link from "next/link";

const CtaThree = ({
  title = "Ready to deliver with Engraced Dispatch?",
  btnText = "Join our rider team",
}) => {
  return (
    <div className="section zubuz-section-padding dark-bg">
      <div className="container">
        <div className="row">
          <div className="col-lg-7">
            <div className="zubuz-default-content cta light">
              <h2>{title}</h2>
            </div>
          </div>
          <div className="col-lg-5 d-flex align-items-center">
            <div className="zubuz-title-btn">
              <Link className="zubuz-default-btn" href="single-team">
                <span>{btnText}</span>
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CtaThree;
