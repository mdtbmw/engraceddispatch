const ResetPasswordForm = () => {
  return (
    <div className="section zubuz-extra-section">
      <div className="container">
        <div className="zubuz-section-title center">
          <h2>Reset your delivery portal password</h2>
        </div>
        <div className="zubuz-account-wrap">
          <form>
            <div className="zubuz-account-field">
              <label>Email address</label>
              <input type="email" placeholder="example@gmail.com" />
            </div>
            <div className="zubuz-account-field">
              <label>New password</label>
              <input type="password" placeholder="Enter new password" />
            </div>
            <button id="zubuz-account-btn" type="submit">
              <span>Change password</span>
            </button>
            <div className="zubuz-account-bottom m-0">
              <p>
                If you didn&rsquo;t request a password reset link, please ignore
                this email.
              </p>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default ResetPasswordForm;
