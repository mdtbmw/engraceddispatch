export const metadata = {
  title: "Portfolio Grid",
  description:
    "Explore our grid portfolio of Engraced Dispatch delivery solutions and courier service projects across Benin City.",
};

import PageHeader from "~/components/Section/Common/PageHeader";
import Footer from "~/components/Section/Common/Footer";
import Header from "~/components/Section/Common/Header/Header";
import PortfolioGrid from "~/components/Section/Portfolio/Grid/PortfolioGrid";


export default function PortfolioGridPage() {
  return (
    <>
      <Header dark />
      <PageHeader title="Portfolio Grid" />
      <PortfolioGrid />
      <Footer />
    </>
  );
}
