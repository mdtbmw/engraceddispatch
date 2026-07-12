export const metadata = {
  title: "Integrations",
  description:
    "Discover how Dispatch integrates with your workflow. Seamless delivery solutions for businesses in Benin City.",
};

import Footer from "~/components/Section/Common/Footer";
import PageHeader from "~/components/Section/Common/PageHeader";
import IntegrationSection from "~/components/Section/Integrations/Integration/Integration";
import Header from "~/components/Section/Common/Header/Header";


export default function IntegrationPage() {
  return (
    <>
      <Header dark />
      <PageHeader title="Integrations" />
      <IntegrationSection />
      <Footer />
    </>
  );
}
