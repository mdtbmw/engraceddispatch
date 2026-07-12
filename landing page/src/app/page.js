import Cta from "~/components/Section/Common/Cta";
import Footer from "~/components/Section/Common/Footer";
import BrandSection from "~/components/Section/Home-1/Brand/Brand";
import ContentSectionOne from "~/components/Section/Home-1/Content/ContentOne";
import ContentSectionTwo from "~/components/Section/Home-1/Content/ContentTwo";
import Faq from "~/components/Section/Home-1/Faq/Faq";
import FeatureSection from "~/components/Section/Home-1/Feature/Feature";
import HeroSection from "~/components/Section/Home-1/Hero/Hero";
import NewsSection from "~/components/Section/Home-1/News/News";
import TestimonialSection from "~/components/Section/Common/Testimonial/Testimonial";
import Header from "~/components/Section/Common/Header/Header";

export default function Home() {
  return (
    <>
      <Header />
      <main>
        <HeroSection />
        <BrandSection />
        <ContentSectionOne />
        <ContentSectionTwo />
        <FeatureSection />
        <Faq />
        <TestimonialSection />
        <NewsSection />
        <Cta />
      </main>
      <Footer />
    </>
  );
}
