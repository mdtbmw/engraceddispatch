export const metadata = {
  title: "Careers",
  description:
    "Join the Engraced Dispatch team. Explore career opportunities in delivery, operations, and customer service.",
};

import CareerSection from "~/components/Section/Career/CareerSection/CareerSection";
import ContentSection from "~/components/Section/Career/Content/ContentSection";
import FeatureSection from "~/components/Section/Career/Feature/FeatureSection";
import StatSection from "~/components/Section/Career/Stat/StatSection";
import CtaThree from "~/components/Section/Common/Cta-3/CtaThree";
import Footer from "~/components/Section/Common/Footer";
import PageHeader from "~/components/Section/Common/PageHeader";
import Header from "~/components/Section/Common/Header/Header";


const CareerPage = () => {
  return (
    <>
      <Header dark />
      <PageHeader title="Career" />
      <CareerSection />
      <ContentSection />
      <StatSection />
      <FeatureSection />
      <CtaThree title="Still, you have any questions?" btnText="Contact Us" />
      <Footer />
    </>
  );
};

export default CareerPage;
