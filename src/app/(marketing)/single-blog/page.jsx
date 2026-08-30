export const metadata = {
  title: "Blog Post",
  description: "Read our latest blog post from Engraced Dispatch.",
};

import Footer from "~/components/Section/Common/Footer";
import PageHeader from "~/components/Section/Common/PageHeader";
import SingleBlogSection from "~/components/Section/SingleBlog/SingleBlogSection";
import Header from "~/components/Section/Common/Header/Header";

const SingleBlogPage = () => {
  return (
    <>
      <Header dark />
      <PageHeader title="Our Blog" />
      <SingleBlogSection />
      <Footer />
    </>
  );
};

export default SingleBlogPage;
