export const metadata = {
  title: "Reset Password",
  description: "Reset your Engraced Dispatch courier account password.",
};

import ResetPasswordForm from "~/components/Section/ResetPassword/ResetPassword/ResetPassword";
import Header from "~/components/Section/Common/Header/Header";

export default function ResetPasswordPage() {
  return (
    <>
      <Header dark />
      <ResetPasswordForm />;
    </>
  );
}
