"use client";
import Link from "next/link";
import TeamCard from "~/components/Ui/Cards/TeamCard";
import { useTeamMembers } from "~/lib/useCmsData";
import { useSiteContent } from "~/lib/useSiteContent";

const TeamSection = () => {
  const { content } = useSiteContent();
  const { items: teamData } = useTeamMembers();
  return (
    <div className="section zubuz-section-padding3 dark-bg">
      <div className="container">
        <div className="zubuz-section-title light zubuz-two-column-title">
          <div className="row">
            <div className="col-lg-6">
              <h2>{content.teamTitle}</h2>
            </div>
            <div className="col-lg-6 d-flex align-items-center">
              <div className="zubuz-title-btn">
                <Link className="zubuz-default-btn pill" href="team">
                  <span>Join Our Team</span>
                </Link>
              </div>
            </div>
          </div>
        </div>
        <div className="row">
          {teamData?.slice(0, 3).map((member, index) => (
            <TeamCard key={index} member={{ ...member, social: { twitter: member.twitter, facebook: member.facebook, linkedin: member.linkedin } }} />
          ))}
        </div>
      </div>
    </div>
  );
};

export default TeamSection;
