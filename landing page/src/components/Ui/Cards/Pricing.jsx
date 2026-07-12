import Link from "next/link";

const PricingCard = () => {
  return (
    <div className="zubuz-pricing-wrap active">
      <div className="zubuz-pricing-header">
        <h5>Standard</h5>
      </div>
      <div className="zubuz-pricing-price">
        <h2>$</h2>
        <div
          className="zubuz-price dynamic-value"
          data-active="49"
          data-monthly="49"
          data-yearly="69"
        ></div>
        <p
          className="dynamic-value"
          data-active="USD/Billed monthly"
          data-monthly="USD/Billed monthly"
          data-yearly="USD/Billed Yearly"
        ></p>
      </div>
      <div className="zubuz-pricing-description">
        <p>Great for small and medium teams</p>
      </div>
      <div className="zubuz-pricing-body">
        <ul>
          <li>
            <img src="/images/v2/check2.png" alt="" />
            All multimedia channels
          </li>
          <li>
            <img src="/images/v2/check2.png" alt="" />
            All advanced CRM features
          </li>
          <li>
            <img src="/images/v2/check2.png" alt="" />
            Up to 15,000 contacts
          </li>
          <li>
            <img src="/images/v2/check2.png" alt="" />
            Flow + AI base chatbot
          </li>
          <li>
            <img src="/images/v2/check2.png" alt="" />
            24/7 Support (Live, Email, Chat)
          </li>
        </ul>
      </div>
      <Link href="pricing" className="zubuz-pricing-btn">
        Select the plan
      </Link>
    </div>
  );
};

export default PricingCard;
