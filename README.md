# Online Job Portal

**Java Programming Project** | Galgotias University | Batch 2029 | Project type: Web-based (Rubric B)

An online job portal where employers post jobs, job seekers search and apply with a resume, and an administrator approves job postings and manages users. Each role has its own dashboard.

## Team

**Team name:** `TriCode`

| Name | Roll No | GitHub | Role | 
|---|---|---|---|
| `Ayushi Goyal` | `25SCSE1410298` | `15ayushi-s` | Team Leader | 
| `Aryan Mehta` | `25SCSE1410318` | `aryanmehta7373-collab` | Member | 
| `Suhani Bharti` | `25SCSE1181111` | `SuhaniBharti01` | Member | 

## Layer

| Layer      | Technology |
|------------|------------|
| Frontend   | React + Vite |
| Web layer  | **Java Servlets** (Jakarta Servlet 5), `web.xml`, `Filter`, `HttpSession` |
| Data layer | **JDBC** (`PreparedStatement`, transactions) on **MySQL 8** |
| Language   | **Core Java 17** (OOP, generics, enums, records, collections, custom exceptions, concurrency) |

Full design write-up (problem statement, ER diagram, architecture, API, concept map): **[docs/DESIGN.md](docs/DESIGN.md)**

## Run

Requirements: Java 17+, Maven 3.8+, MySQL 8, Node 18+.

### 1. Database
Nothing to create by hand — the JDBC URL has `createDatabaseIfNotExist=true` and the app runs
`database/schema.sql` at start-up. Just give it your MySQL password, either by editing
`backend/src/main/resources/db.properties` or with environment variables:

```bash
export DB_PASSWORD=your_mysql_password      # Windows (cmd): set DB_PASSWORD=your_mysql_password
# optional: DB_URL, DB_USER
```

> If you ran the older Spring Boot version of this project, drop its database first
> (`DROP DATABASE jobhub;`) — the table layout is different.

### 2. Backend (Servlets, embedded Jetty)
```bash
cd backend
mvn jetty:run
```
Runs at http://localhost:8080. Alternatively `mvn package` and copy `target/jobhub.war` to
Tomcat 10.1's `webapps/` as `ROOT.war`.

### 3. Frontend
```bash
cd frontend
npm install
npm run dev
```
Open http://localhost:5173 (Vite proxies `/api` to the servlets, so the session cookie just works).

## Demo accounts (created on first start)
| Role | Email | Password |
|------|-------|----------|
| Admin | admin@jobhub.com | admin123 |
| Employer | employer@jobhub.com | employer123 |
| Job seeker | seeker@jobhub.com | seeker123 |

Three sample approved jobs are added on the first run.

## Project layout
```
backend/src/main/java/com/jobhub/
  model/      User (abstract) -> Admin, Employer, JobSeeker; Job, Application, enums, ApplicationView (record)
  dao/        Dao<T,ID> + UserDao/JobDao/ApplicationDao interfaces
  dao/jdbc/   JDBC implementations (PreparedStatement, JOINs, transaction)
  service/    business rules + authorisation (no HTTP, no SQL)
  web/        servlets, AuthFilter, AppInitListener
  exception/  custom exception hierarchy
  util/       Db, Json, Passwords (PBKDF2), Validation, Http, LoginThrottle
backend/src/main/webapp/WEB-INF/web.xml   servlet / filter / listener mapping
database/schema.sql                         tables, keys, constraints
docs/DESIGN.md                              design document
backend/selftest/SelfTest.java              offline logic test (see DESIGN.md §9)
```

## Known limitations
- No CSRF tokens (mitigated by the JSON content-type rule and same-origin proxy); add tokens before real deployment.
- Connections are opened per query with `DriverManager`; a production app would use a connection pool.
- The "10K+ jobs / 95% success" figures on the home page are static placeholder text.
