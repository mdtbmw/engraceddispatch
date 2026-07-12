export const metadata = {
  title: "Blog",
  description:
    "Read the Dispatch blog for delivery tips, courier industry insights, and news about our delivery app in Benin City.",
};

import BlogSection from "~/components/Section/Blog/BlogSection";
import Footer from "~/components/Section/Common/Footer";
import PageHeader from "~/components/Section/Common/PageHeader";
import Header from "~/components/Section/Common/Header/Header";


const BlogPage = () => {
  return (
    <>
      <Header dark />
      <PageHeader title="Our Blog" />
      <BlogSection />
      <Footer />
    </>
  );
};

export default BlogPage;
