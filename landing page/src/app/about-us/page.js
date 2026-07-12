export const metadata = {
  title: "About Us",
  description:
    "Learn about Dispatch — your trusted delivery and courier app. Our mission, team, and commitment to fast, reliable delivery.",
};

import AboutSection from "~/components/Section/AboutUs/About/AboutSection";
import ContentSection from "~/components/Section/AboutUs/Content/ContentSection";
import FAQSection from "~/components/Section/Common/FAQ/FAQSection";
import StateSection from "~/components/Section/AboutUs/State/StateSection";
import TeamSection from "~/components/Section/AboutUs/Team/TeamSection";
import Footer from "~/components/Section/Common/Footer";
import PageHeader from "~/components/Section/Common/PageHeader";
import Header from "~/components/Section/Common/Header/Header";


const AboutPage = () => {
  return (
    <>
      <Header dark />
      <PageHeader title="About Us" />
      <AboutSection />
      <StateSection />
      <ContentSection />
      <TeamSection />
      <FAQSection />
      <Footer />
    </>
  );
};

export default AboutPage;
