export const metadata = {
  title: "Portfolio Details",
  description: "View portfolio details from Dispatch courier services.",
};

import PageHeader from "~/components/Section/Common/PageHeader";
import Footer from "~/components/Section/Common/Footer";
import PortfolioDetailsSection from "~/components/Section/SinglePortfolio/PortfolioDetails/PortfolioDetails";
import Header from "~/components/Section/Common/Header/Header";

export default function SinglePortfolioPage() {
  return (
    <>
      <Header dark />
      <PageHeader title="Portfolio Details" />
      <PortfolioDetailsSection />
      <Footer />
    </>
  );
}
