export const metadata = {
  title: "FAQ",
  description:
    "Frequently asked questions about Dispatch delivery services in Benin City. Find answers about pricing, coverage, and scheduling.",
};

import PageHeader from "~/components/Section/Common/PageHeader";
import Footer from "~/components/Section/Common/Footer";
import CtaThree from "~/components/Section/Common/Cta-3/CtaThree";
import FaqSection from "~/components/Section/Faq/Faq/Faq";
import Header from "~/components/Section/Common/Header/Header";


const FaqPage = () => {
  return (
    <>
      <Header dark />
      <PageHeader title=" Faq" />
      <FaqSection />
      <CtaThree title="Still, you have any questions?" btnText="Contact Us" />
      <Footer />
    </>
  );
};

export default FaqPage;
