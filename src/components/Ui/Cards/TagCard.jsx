import Link from "next/link";

const TagCard = () => {
  return (
    <div className="widget">
      <h3 className="wp-block-heading">Tags:</h3>
      <div className="wp-block-tag-cloud">
        <Link href="">Marketing</Link>
        <Link href="">Business</Link>
        <Link href="">SaaS</Link>
        <Link href="">Development</Link>
        <Link href="">UI/UX</Link>
        <Link href="">Brand</Link>
      </div>
    </div>
  );
};

export default TagCard;
