import React from "react";
import { Link } from "react-router-dom";
import { ArrowLeft } from "lucide-react";
import SignInForm from "@/components/Section/SignIn/SignIn/SignIn";
import SignUpForm from "@/components/Section/SingUp/SignUp/SignUp";
import ResetPasswordForm from "@/components/Section/ResetPassword/ResetPassword/ResetPassword";

const AuthShell: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <div className="min-h-screen bg-[#0A0A0A] flex flex-col">
    <header className="border-b border-[#262626] bg-[#111111]/90 backdrop-blur-md sticky top-0 z-50 px-4 sm:px-6 lg:px-8 py-3 flex items-center justify-between gap-4">
      <Link to="/" className="flex items-center gap-3 shrink-0 group">
        <div className="w-9 h-9 rounded-xl bg-[#FFB800] flex items-center justify-center text-black font-black text-sm shadow-md shadow-[#FFB800]/20">
          ES
        </div>
        <div>
          <div className="text-sm font-black tracking-wider text-white group-hover:text-[#FFB800] transition-colors">
            ESDISPATCH
          </div>
          <div className="text-[9px] font-bold tracking-widest text-[#FFB800]">
            PREMIUM LOGISTICS & DISPATCH
          </div>
        </div>
      </Link>

      <Link
        to="/"
        className="h-10 px-3.5 rounded-xl bg-white/5 hover:bg-white/10 border border-white/10 text-xs font-bold text-neutral-300 hover:text-white flex items-center gap-1.5 transition-all"
      >
        <ArrowLeft className="w-3.5 h-3.5" />
        <span>Home</span>
      </Link>
    </header>

    <main className="flex-1 bg-white">{children}</main>

    <footer className="border-t border-[#262626] py-6 text-center text-xs text-neutral-500 font-mono bg-[#0A0A0A]">
      &copy; {new Date().getFullYear()} ESDISPATCH. ALL RIGHTS RESERVED. PREMIUM LOGISTICS & DISPATCH.
    </footer>
  </div>
);

export const SignInPage: React.FC = () => (
  <AuthShell>
    <SignInForm />
  </AuthShell>
);

export const SignUpPage: React.FC = () => (
  <AuthShell>
    <SignUpForm />
  </AuthShell>
);

export const ResetPasswordPage: React.FC = () => (
  <AuthShell>
    <ResetPasswordForm />
  </AuthShell>
);
