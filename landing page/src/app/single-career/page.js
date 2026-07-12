export const metadata = {
  title: "Career Details",
  description: "View career details and apply to join the Dispatch team.",
};

import Footer from "~/components/Section/Common/Footer";
import PageHeader from "~/components/Section/Common/PageHeader";
import SingleCareerSection from "~/components/Section/SingleCareer/SingleCareerSection";
import Header from "~/components/Section/Common/Header/Header";


const SingleCareerPage = () => {
  return (
    <>
      <Header dark />
      <PageHeader title="Career Details" />
      <SingleCareerSection />
      <Footer />
    </>
  );
};

export default SingleCareerPage;
