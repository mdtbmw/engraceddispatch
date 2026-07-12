export const metadata = {
  title: "Team Member",
  description: "Meet our team member at Engraced Dispatch.",
};

import PageHeader from "~/components/Section/Common/PageHeader";
import Footer from "~/components/Section/Common/Footer";
import AboutSection from "~/components/Section/SingleTeam/About/About";
import ExperienceSection from "~/components/Section/SingleTeam/Experience/Experience";
import Header from "~/components/Section/Common/Header/Header";


export default function SingleTeamPage() {
  return (
    <>
      <Header dark />
      <PageHeader title="Team Details" />
      <AboutSection />
      <ExperienceSection />
      <Footer />
    </>
  );
}
