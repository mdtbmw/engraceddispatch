const PostCard = () => {
    return (
        <div className="widget zubuz_recent_posts_Widget">
        <h3 className="wp-block-heading">Recent Posts:</h3>
        <div className="post-item">
          <div className="post-thumb">
            <a href="">
              <img src="/images/blog/blog1.png" alt=""/>
            </a>
          </div>
          <div className="post-text">
            <div className="post-date">
              June 18, 2024
            </div>
            <a className="post-title" href="">7 businesses for easy money</a>
          </div>
        </div>
        <div className="post-item">
          <div className="post-thumb">
            <a href="">
              <img src="/images/blog/blog2.png" alt=""/>
            </a>
          </div>
          <div className="post-text">
            <div className="post-date">
              June 18, 2024
            </div>
            <a className="post-title" href="">My 3 tips for business ideas</a>
          </div>
        </div>
        <div className="post-item">
          <div className="post-thumb">
            <a href="">
              <img src="/images/blog/blog3.png" alt=""/>
            </a>
          </div>
          <div className="post-text">
            <div className="post-date">
              June 18, 2024
            </div>
            <a className="post-title" href="">12 Halloween costume ideas</a>
          </div>
        </div>
      </div>
    );
};

export default PostCard;