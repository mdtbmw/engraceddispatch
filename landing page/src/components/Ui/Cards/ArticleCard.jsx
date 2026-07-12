import Link from "next/link";
import ArrowRightIcon from "../Icon/ArrowRight";

const ArticleCard = ({ article }) => {
  return (
    <div className="col-xl-4 col-lg-6">
      <div className="zubuz-blog-wrap">
        <Link href={article.link}>
          <div className="zubuz-blog-thumb">
            <img src={article.image} alt="" />
            <div className="zubuz-blog-categorie">{article.category}</div>
          </div>
        </Link>
        <div className="zubuz-blog-data">
          <p>{article.date}</p>
          <Link href={article.link}>
            <h3>{article.title}</h3>
          </Link>
          <Link className="zubuz-blog-btn" href={article.link}>
            <ArrowRightIcon />
          </Link>
        </div>
      </div>
    </div>
  );
};

export default ArticleCard;
