
package com.jobgraph.backend;

import java.util.List;
import java.util.Map;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
public class JobGraphController {

    private final Driver driver;

    public JobGraphController(Driver driver) {
        this.driver = driver;
    }

    @GetMapping("/api/test")
    public Map<String, String> testConnection() {

        try (Session session = driver.session()) {

            session.run(
                "RETURN 'CognoDB connection successful!' AS message"
            );

            return Map.of(
                "status", "success",
                "message", "CognoDB connection successful!"
            );

        } catch (Exception e) {

            return Map.of(
                "status", "error",
                "message", e.getMessage()
            );
        }
    }

    @GetMapping("/api/create-job")
    public Map<String, String> createJobGraph() {

        try (Session session = driver.session()) {

            String query = """
                MERGE (c:Company {name: 'Google'})
                MERGE (j:Job {title: 'Java Developer'})
                MERGE (s1:Skill {name: 'Java'})
                MERGE (s2:Skill {name: 'Spring Boot'})
                MERGE (s3:Skill {name: 'SQL'})

                MERGE (c)-[:OFFERS]->(j)
                MERGE (j)-[:REQUIRES]->(s1)
                MERGE (j)-[:REQUIRES]->(s2)
                MERGE (j)-[:REQUIRES]->(s3)

                RETURN c, j, s1, s2, s3
                """;

            session.run(query);

            return Map.of(
                "status", "success",
                "message", "Job graph created successfully!"
            );

        } catch (Exception e) {

            return Map.of(
                "status", "error",
                "message", e.getMessage()
            );
        }
    }

    @GetMapping("/api/jobs")
    public Object getJobs() {

        try (Session session = driver.session()) {

            String query = """
                MATCH (c:Company)-[:OFFERS]->(j:Job)
                OPTIONAL MATCH (j)-[:REQUIRES]->(s:Skill)
                RETURN c.name AS company,
                       j.title AS job,
                       collect(s.name) AS skills
                """;

            return session.run(query)
                .list(record -> Map.of(
                    "company", record.get("company").asString(),
                    "job", record.get("job").asString(),
                    "skills", record.get("skills").asList()
                ));

        } catch (Exception e) {

            return Map.of(
                "status", "error",
                "message", e.getMessage()
            );
        }
    }

    @GetMapping("/api/jobs/skill/{skill}")
    public Object getJobsBySkill(@PathVariable String skill) {

        try (Session session = driver.session()) {

            String query = """
                MATCH (c:Company)-[:OFFERS]->(j:Job)-[:REQUIRES]->(s:Skill)
                WHERE toLower(s.name) = toLower($skill)
                RETURN c.name AS company,
                       j.title AS job,
                       collect(s.name) AS skills
                """;

            return session.run(
                    query,
                    Map.of("skill", skill)
                )
                .list(record -> Map.of(
                    "company", record.get("company").asString(),
                    "job", record.get("job").asString(),
                    "skills", record.get("skills").asList()
                ));

        } catch (Exception e) {

            return Map.of(
                "status", "error",
                "message", e.getMessage()
            );
        }
    }

    @PostMapping("/api/jobs")
    public Map<String, String> addJob(
            @RequestBody Map<String, Object> jobData) {

        try (Session session = driver.session()) {

            String company = (String) jobData.get("company");
            String job = (String) jobData.get("job");

            @SuppressWarnings("unchecked")
            List<String> skills =
                (List<String>) jobData.get("skills");

            String query = """
                MERGE (c:Company {name: $company})
                MERGE (j:Job {title: $job})

                MERGE (c)-[:OFFERS]->(j)

                WITH j
                UNWIND $skills AS skillName
                MERGE (s:Skill {name: skillName})
                MERGE (j)-[:REQUIRES]->(s)

                RETURN j
                """;

            session.run(
                query,
                Map.of(
                    "company", company,
                    "job", job,
                    "skills", skills
                )
            );

            return Map.of(
                "status", "success",
                "message", "Job added successfully!"
            );

        } catch (Exception e) {

            return Map.of(
                "status", "error",
                "message", e.getMessage()
            );
        }
    }
}

