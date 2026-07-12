import Link from "next/link";

const PricingSectionTwo = () => {
  return (
    <>
      <div className="zubuz-divider"></div>

      <div className="section zubuz-section-padding3">
        <div className="container">
          <div className="zubuz-section-title zubuz-two-column-title">
            <div className="row">
              <div className="col-lg-7">
                <h2>Compare our delivery plans</h2>
              </div>
              <div className="col-lg-5 d-flex align-items-center">
                  <p>
                    Choose the plan that fits your delivery needs. From single
                    packages to bulk business deliveries, we have you covered.
                  </p>
              </div>
            </div>
          </div>

          <div className="zubuz-table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Feature</th>
                  <th>Basic</th>
                  <th>Standard</th>
                  <th>Premium</th>
                </tr>
              </thead>
              <tbody>
                  <tr>
                    <td>Same-day delivery</td>
                    <td>
                      <img src="/images/icon/check.svg" alt="" />
                    </td>
                    <td>
                      <img src="/images/icon/check.svg" alt="" />
                    </td>
                    <td>
                      <img src="/images/icon/check.svg" alt="" />
                    </td>
                  </tr>
                  <tr>
                    <td>Real-time tracking</td>
                    <td>
                      <img src="/images/icon/minus.svg" alt="" />
                    </td>
                    <td>
                      <img src="/images/icon/check.svg" alt="" />
                    </td>
                    <td>
                      <img src="/images/icon/check.svg" alt="" />
                    </td>
                  </tr>
                  <tr>
                    <td>Free pickup</td>
                    <td>
                      <img src="/images/icon/check.svg" alt="" />
                    </td>
                    <td>
                      <img src="/images/icon/check.svg" alt="" />
                    </td>
                    <td>
                      <img src="/images/icon/check.svg" alt="" />
                    </td>
                  </tr>
                  <tr>
                    <td>Max distance (km)</td>
                    <td>5 km</td>
                    <td>15 km</td>
                    <td>Unlimited</td>
                  </tr>
                  <tr>
                    <td>Package insurance</td>
                    <td>
                      <img src="/images/icon/minus.svg" alt="" />
                    </td>
                    <td>
                      <img src="/images/icon/check.svg" alt="" />
                    </td>
                    <td>
                      <img src="/images/icon/check.svg" alt="" />
                    </td>
                  </tr>
                  <tr>
                    <td>Bulk delivery discount</td>
                    <td>
                      <img src="/images/icon/minus.svg" alt="" />
                    </td>
                    <td>
                      <img src="/images/icon/minus.svg" alt="" />
                    </td>
                    <td>
                      <img src="/images/icon/check.svg" alt="" />
                    </td>
                  </tr>
                  <tr>
                    <td>Priority support</td>
                    <td>
                      <img src="/images/icon/minus.svg" alt="" />
                    </td>
                    <td>
                      <img src="/images/icon/check.svg" alt="" />
                    </td>
                    <td>
                      <img src="/images/icon/check.svg" alt="" />
                    </td>
                  </tr>
                <tr>
                  <td></td>
                  <td>
                    <Link className="zubuz-default-btn" href="contact-us">
                      <span>Get Started Now</span>
                    </Link>
                  </td>
                  <td>
                    <Link className="zubuz-default-btn" href="contact-us">
                      <span>Get Started Now</span>
                    </Link>
                  </td>
                  <td>
                    <Link className="zubuz-default-btn" href="contact-us">
                      <span>Get Started Now</span>
                    </Link>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </>
  );
};

export default PricingSectionTwo;
