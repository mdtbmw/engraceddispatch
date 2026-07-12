export const metadata = {
  title: "Our Services",
  description:
    "Explore Engraced Dispatch delivery services. Same-day delivery, scheduled pickups, and bulk courier solutions for businesses and individuals.",
};

import IntegrationSection from "~/components/Section/Common/Integration-2/IntegrationTwo";
import PageHeader from "~/components/Section/Common/PageHeader";
import Footer from "~/components/Section/Common/Footer";
import ServiceSection from "~/components/Section/Service/Service/Service";
import Header from "~/components/Section/Common/Header/Header";


export default function ServicePage() {
  return (
    <>
      <Header dark />
      <PageHeader title="Our Services" />
      <ServiceSection />
      <IntegrationSection />
      <Footer />
    </>
  );
}
