export const metadata = {
  title: "Testimonials",
  description:
    "Hear from our satisfied customers. Read testimonials about Engraced Dispatch's premium logistics and courier delivery service.",
};

import PageHeader from "~/components/Section/Common/PageHeader";
import Footer from "~/components/Section/Common/Footer";
import Testimonial from "~/components/Section/Common/Testimonial";
import CtaThree from "~/components/Section/Common/Cta-3/CtaThree";
import Header from "~/components/Section/Common/Header/Header";


export default function TestimonialPage() {
  return (
    <>
      <Header dark />
      <PageHeader title="Testimonial" />
      <Testimonial button="false" />
      <CtaThree title="Still, you have any questions?" btnText="Contact Us" />
      <Footer />
    </>
  );
}
