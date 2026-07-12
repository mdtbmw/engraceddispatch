export const metadata = {
  title: "Portfolio Classic",
  description:
    "Browse our classic portfolio showcasing Engraced Dispatch delivery projects and successful courier operations in Benin City.",
};

import PageHeader from "~/components/Section/Common/PageHeader";
import Footer from "~/components/Section/Common/Footer";
import Header from "~/components/Section/Common/Header/Header";
import PortfolioClassic from "~/components/Section/Portfolio/Classic/PortfolioClassic";


export default function PortfolioClassicPage() {
  return (
    <>
      <Header dark />
      <PageHeader title="Portfolio Classic" />
      <PortfolioClassic />
      <Footer />
    </>
  );
}
