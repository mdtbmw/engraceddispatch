import React from "react";

interface CustomErrorProps {
  statusCode?: number;
}

export default function CustomError({ statusCode }: CustomErrorProps) {
  return (
    <div className="min-h-screen bg-obsidian text-white flex flex-col items-center justify-center">
      <h1 className="text-xl font-bold text-gold">
        {statusCode ? `Error ${statusCode} Encountered` : "An error occurred"}
      </h1>
      <p className="text-xs text-gray-400 mt-2">Engraced Dispatch Admin Control Center</p>
    </div>
  );
}

CustomError.getInitialProps = ({ res, err }: any) => {
  const statusCode = res ? res.statusCode : err ? err.statusCode : 404;
  return { statusCode };
};
