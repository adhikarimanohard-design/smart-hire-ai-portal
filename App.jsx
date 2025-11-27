import React from "react";

export default function App() {
  const jobs = [
    {
      id: 1,
      title: "Frontend Developer",
      company: "TechVision Labs",
      salary: "₹12–18 LPA",
      type: "Full-time",
    },
    {
      id: 2,
      title: "Backend Engineer",
      company: "SmartHire Systems",
      salary: "₹15–22 LPA",
      type: "Full-time",
    },
    {
      id: 3,
      title: "AI/ML Intern",
      company: "DeepCompute AI",
      salary: "₹35k/month",
      type: "Internship",
    },
  ];

  return (
    <div className="min-h-screen p-6 bg-gray-100">
      <div className="max-w-6xl mx-auto">

        {/* Header */}
        <header className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Smart Hire Dashboard</h1>
          <p className="text-gray-600">Your personalized job portal with AI-powered recommendations.</p>
        </header>

        {/* Stats */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-10">
          <div className="bg-white p-6 rounded-xl shadow">
            <h2 className="text-gray-600">Total Applications</h2>
            <p className="text-3xl font-bold mt-2">24</p>
          </div>

          <div className="bg-white p-6 rounded-xl shadow">
            <h2 className="text-gray-600">Shortlisted</h2>
            <p className="text-3xl font-bold mt-2">8</p>
          </div>

          <div className="bg-white p-6 rounded-xl shadow">
            <h2 className="text-gray-600">Interviews Scheduled</h2>
            <p className="text-3xl font-bold mt-2">3</p>
          </div>
        </div>

        {/* Jobs Section */}
        <h2 className="text-2xl font-semibold mb-4">Recommended Jobs</h2>
        <div className="grid grid-cols-1 gap-6">
          {jobs.map((job) => (
            <div key={job.id} className="bg-white p-6 rounded-xl shadow hover:shadow-lg transition">
              <h3 className="text-xl font-bold">{job.title}</h3>
              <p className="text-gray-600">{job.company}</p>
              <p className="mt-2 font-semibold">{job.salary}</p>
              <p className="text-sm text-blue-700 mt-1">{job.type}</p>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}