export const metadata = {
  title: "Coming Soon",
  description: "Something exciting is coming soon from Dispatch courier services. Stay tuned!",
};

import ComingSoonSection from "~/components/Section/ComingSoon/ComingSoonSection";
import Header from "~/components/Section/Common/Header/Header";


const ComingSoonPage = () => {
  return (
    <>
      <Header dark />
      <ComingSoonSection />
    </>
  );
};

export default ComingSoonPage;
