export const metadata = {
  title: "Blog - Column One",
  description: "Read the latest articles and updates from the Dispatch delivery app.",
};

import Footer from "~/components/Section/Common/Footer";
import PageHeader from "~/components/Section/Common/PageHeader";
import Blog from "~/components/Section/blogColumnOne/Blog";
import Header from "~/components/Section/Common/Header/Header";

const BlogOnePage = () => {
  return (
    <>
      <Header dark />
      <PageHeader title="Our Blog" />
      <Blog />
      <Footer />
    </>
  );
};

export default BlogOnePage;
