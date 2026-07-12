export const metadata = {
  title: "Blog - Column Two",
  description: "Explore more articles and insights from Dispatch on delivery and courier services.",
};

import Footer from "~/components/Section/Common/Footer";
import PageHeader from "~/components/Section/Common/PageHeader";
import ArticleSection from "~/components/Section/blogColumnTwo/Article/ArticleSection";
import BlogSection from "~/components/Section/blogColumnTwo/Blog/BlogSection";
import Header from "~/components/Section/Common/Header/Header";


const BlogTwoPage = () => {
  return (
    <>
      <Header dark />
      <PageHeader title="Our Blog" />
      <BlogSection />
      <ArticleSection />
      <Footer />
    </>
  );
};

export default BlogTwoPage;
