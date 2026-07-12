export const metadata = {
  title: "Sign Up",
  description: "Create a Dispatch account for fast courier delivery services in Benin City.",
};

import SignUpForm from "~/components/Section/SingUp/SignUp/SignUp";
import Header from "~/components/Section/Common/Header/Header";

export default function SignUpPage() {
  return (
    <>
      <Header dark />
      <SignUpForm />
    </>
  );
}
