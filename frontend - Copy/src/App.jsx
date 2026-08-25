import { useEffect, useState } from "react";
import "./App.css";

const API_URL = "http://localhost:8081";

function App() {
  const [jobs, setJobs] = useState([]);
  const [company, setCompany] = useState("");
  const [job, setJob] = useState("");
  const [skills, setSkills] = useState("");
  const [searchSkill, setSearchSkill] = useState("");
  const [message, setMessage] = useState("");

  const getJobs = async () => {
    try {
      const response = await fetch(`${API_URL}/api/jobs`);
      const data = await response.json();
      setJobs(data);
    } catch (error) {
      setMessage("Backend connection failed.");
    }
  };

  useEffect(() => {
    getJobs();
  }, []);

  const addJob = async (e) => {
    e.preventDefault();

    if (!company || !job || !skills) {
      setMessage("Please fill all fields.");
      return;
    }

    try {
      const response = await fetch(`${API_URL}/api/jobs`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          company: company,
          job: job,
          skills: skills.split(",").map((skill) => skill.trim()),
        }),
      });

      const data = await response.json();

      if (data.status === "success") {
        setMessage("Job added successfully!");
        setCompany("");
        setJob("");
        setSkills("");
        getJobs();
      } else {
        setMessage("Failed to add job.");
      }
    } catch (error) {
      setMessage("Backend connection failed.");
    }
  };

  const searchJobs = async () => {
    if (!searchSkill) {
      getJobs();
      return;
    }

    try {
      const response = await fetch(
        `${API_URL}/api/jobs/skill/${searchSkill}`
      );

      const data = await response.json();
      setJobs(data);
    } catch (error) {
      setMessage("Search failed.");
    }
  };

  return (
    <div className="container">
      <h1>Job Graph Application</h1>

      <div className="form-card">
        <h2>Add New Job</h2>

        <form onSubmit={addJob}>
          <input
            type="text"
            placeholder="Company"
            value={company}
            onChange={(e) => setCompany(e.target.value)}
          />

          <input
            type="text"
            placeholder="Job Title"
            value={job}
            onChange={(e) => setJob(e.target.value)}
          />

          <input
            type="text"
            placeholder="Skills (Java, Spring Boot, SQL)"
            value={skills}
            onChange={(e) => setSkills(e.target.value)}
          />

          <button type="submit">Add Job</button>
        </form>

        {message && <p className="message">{message}</p>}
      </div>

      <div className="search-card">
        <h2>Search Jobs by Skill</h2>

        <input
          type="text"
          placeholder="Enter skill e.g. Java"
          value={searchSkill}
          onChange={(e) => setSearchSkill(e.target.value)}
        />

        <button onClick={searchJobs}>Search</button>

        <button onClick={getJobs}>Show All Jobs</button>
      </div>

      <div className="jobs-section">
        <h2>Available Jobs</h2>

        {jobs.length === 0 ? (
          <p>No jobs found.</p>
        ) : (
          <div className="jobs">
            {jobs.map((item, index) => (
              <div className="job-card" key={index}>
                <h3>{item.job}</h3>

                <p>
                  <strong>Company:</strong> {item.company}
                </p>

                <p>
                  <strong>Skills:</strong>
                </p>

                <div className="skills">
                  {item.skills.map((skill, skillIndex) => (
                    <span key={skillIndex}>{skill}</span>
                  ))}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

export default App;