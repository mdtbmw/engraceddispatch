"use client";
import ArticleCard from "~/components/Ui/Cards/ArticleCard";
import articleData from "~/data/article.json";

const ArticleSection = () => {
  return (
    <div className="section zubuz-section-padding3 bg-light">
      <div className="container">
        <div className="row">
          <div className="col-lg-8">
            <div className="zubuz-section-title">
              <h2>Latest articles</h2>
            </div>
          </div>
        </div>
        <div className="row">
          {articleData.map((article) => (
            <ArticleCard key={article.id} article={article} />
          ))}
        </div>
      </div>
    </div>
  );
};

export default ArticleSection;
