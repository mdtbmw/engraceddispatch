"use client";
import TeamCard from "~/components/Ui/Cards/Team";
import teamDatas from "~/data/teamData.json";

const TeamSection = () => {

  return (
    <div className="section zubuz-section-padding3">
      <div className="container">
        <div className="zubuz-section-title zubuz-two-column-title">
          <div className="row">
            <div className="col-lg-6">
              <h2>Meet our delivery team</h2>
            </div>
            <div className="col-lg-5 offset-lg-1 d-flex align-items-center">
              <p>
                Our team of dedicated professionals works tirelessly to ensure
                every delivery is fast, safe, and reliable across Benin City.
              </p>
            </div>
          </div>
        </div>
        <div className="row">
          {teamDatas.map((team, index) => (
            <TeamCard
              key={index}
              name={team?.name}
              role={team?.role}
              image={team?.image}
              twitter={team?.social?.twitter}
              facebook={team?.social?.facebook}
              linkedin={team?.social?.linkedin}
            />
          ))}
        </div>
      </div>
    </div>
  );
};

export default TeamSection;
