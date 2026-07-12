export const metadata = {
  title: "Sign In",
  description: "Sign in to your Engraced Dispatch account for courier deliveries.",
};

import SignInForm from "~/components/Section/SignIn/SignIn/SignIn";
import Header from "~/components/Section/Common/Header/Header";

export default function SignInPage() {
  return (
    <>
      <Header dark />
      <SignInForm />
    </>
  );
}
