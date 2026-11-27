# AI-Powered Smart Recruitment & Talent Management System

## 📌 Project Overview

SmartHire is an AI-powered recruitment and talent management platform designed to simplify the hiring process. It connects candidates and recruiters through resume analysis, job matching, application screening, candidate shortlisting, and interview management.

The frontend is developed using React.js, the backend uses Java and Spring Boot REST APIs, and MySQL is used for database management.

## 🚀 Features

### 👨‍🎓 Candidate Module
- Candidate registration and login
- Profile management and resume upload
- Browse and apply for jobs
- AI-based resume analysis and improvement
- AI-powered job matching
- AI-generated interview questions
- Audio and video mock interviews
- Speech-to-text transcription
- Interview evaluation and reports

### 🏢 Recruiter Module
- Recruiter registration and company profile management
- Create, update, and manage job postings
- View and manage job applicants
- AI-powered application screening
- Candidate ranking and automatic shortlisting
- Interview scheduling
- Email notifications

### 🛠️ Admin Module
- Admin dashboard
- User management
- Recruitment analytics
- Monitor platform activities

## 🧰 Technologies Used

**Frontend**
- React.js
- JavaScript
- Vite
- CSS

**Backend**
- Java
- Spring Boot
- Spring Web (REST APIs)
- Spring Data JPA
- Hibernate

**Database**
- MySQL for local development; PostgreSQL (including Supabase) is supported for deployment.
- Cloudinary stores uploaded files when enabled.

**AI Technologies**
- Ollama
- Qwen 2.5
- Faster-Whisper

**Security**
- Spring Security
- JWT Authentication
- Role-Based Authorization

**Development Tools**
- Eclipse IDE
- Visual Studio Code
- Maven
- Postman
- Git and GitHub

## 🏗️ Project Architecture

```text
SmartHire/
│
├── Frontend - React.js
│   └── src/
│
├── Backend - Spring Boot
│   └── src/main/
│       ├── java/
│       └── resources/
│
├── Database - MySQL
│
└── README.md
```

## 🧩 Architecture Components

- **Frontend:** React components provide interfaces for candidates, recruiters, and administrators.
- **Controller Layer:** Handles HTTP requests and exposes REST API endpoints.
- **Service Layer:** Implements business logic for recruitment, applications, AI processing, and interviews.
- **Repository Layer:** Uses Spring Data JPA to perform database operations.
- **Entity Layer:** Defines application data models.
- **Security Layer:** Handles JWT authentication and role-based access.
- **AI Integration:** Uses Ollama for AI-powered features and Faster-Whisper for audio transcription.
- **Database:** MySQL stores user, job, application, resume, and interview information.

## 🔄 Application Workflow

1. Candidates and recruiters register or log in.
2. Candidates complete their profiles and upload resumes.
3. Recruiters create job postings with required skills and qualifications.
4. Candidates browse available jobs and submit applications.
5. AI analyzes resumes against job requirements and generates matching scores.
6. Applicants are ranked based on their screening scores.
7. The highest-scoring candidate or tied candidates are automatically shortlisted.
8. Recruiters review screening results and schedule interviews.
9. Candidates attend interviews and receive evaluation results.

## 📋 Application Screening Workflow

1. Recruiters create job postings with skills, qualifications, experience, and job descriptions.
2. Candidates apply for available jobs.
3. The AI screening system analyzes uploaded resumes or candidate profiles.
4. Matching scores and supporting evidence are generated.
5. Applicants are ranked based on their scores.
6. The highest-scoring candidate or tied candidates are automatically shortlisted.
7. Shortlisted candidates receive email notifications when Gmail is configured.

Unreadable or empty resumes are marked for manual review. Interview scheduling remains the recruiter's decision.

## 🔐 Security

- JWT-based authentication for secure login and API access.
- Role-based authorization for Candidates, Recruiters, and Admins.
- Protected APIs based on user roles.
- Secure password handling.
- Database credentials and JWT secrets are kept outside source code.

## 🗄️ Database Configuration

For local development, create the MySQL database:

```sql
CREATE DATABASE hack_01;
```

Configure `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` for your local MySQL instance. The backend also supports PostgreSQL by setting `DB_URL` to a JDBC PostgreSQL connection string; Hibernate detects the database dialect automatically.

Do not commit actual database passwords or other secrets to GitHub.

## ☁️ Deploy with Supabase, Render, Vercel, and Cloudinary

The backend can use Supabase PostgreSQL and private Cloudinary storage while the frontend is deployed separately on Vercel. Local development continues to use MySQL and local file storage by default.

### Supabase database

1. Create a Supabase project and open **Connect** to get the database connection details.
2. Use the Session Pooler connection if your hosting environment cannot connect to Supabase's direct IPv6 endpoint.
3. Configure the Render backend with:
   - `DB_URL`: `jdbc:postgresql://<host>:<port>/<database>?sslmode=require`
   - `DB_USERNAME`: the username from Supabase Connect (pooler usernames can include the project reference).
   - `DB_PASSWORD`: the database password.

The application creates/updates its tables through Hibernate on startup. A new Supabase database starts empty; existing MySQL data is not copied automatically. Keep `spring.jpa.hibernate.ddl-auto=update` for this initial deployment; use reviewed migrations before changing that production behavior.

### Cloudinary file storage

Create a Cloudinary account and set these Render environment variables. Files are uploaded as authenticated assets and are fetched through the backend's authenticated endpoints; do not make resumes, photos, or interview recordings public.

- `FILE_STORAGE_PROVIDER=cloudinary`
- `CLOUDINARY_CLOUD_NAME`
- `CLOUDINARY_API_KEY`
- `CLOUDINARY_API_SECRET`

Resumes, profile photos, and interview audio/video are stored in Cloudinary. Keep the Cloudinary API secret only in Render's environment settings.

### Render backend

Create a Render Blueprint from the repository root using [render.yaml](./render.yaml). It configures the Docker service, health check, Cloudinary storage, and prompts for the Supabase, Cloudinary, CORS, and Ollama settings. Render generates `JWT_SECRET` automatically. The same settings can be entered manually when creating a Docker Web Service with `backend/Dockerfile` and `backend` as the Docker build context.

- `JWT_SECRET`: a unique, random secret (at least 32 bytes); do not use the development default.
- `CORS_ALLOWED_ORIGINS`: the exact Vercel frontend origin, e.g. `https://your-app.vercel.app` (no trailing slash).
- `OLLAMA_BASE_URL`: for Ollama Cloud, use `https://ollama.com`. The backend calls its `/api/chat` endpoint.
- `OLLAMA_API_KEY`: your Ollama Cloud API key. Keep it secret; the backend sends it as a bearer token.
- `OLLAMA_MODEL` and `OLLAMA_INTERVIEW_MODEL`: both default to `deepseek-v4.1-flash:cloud`, a cloud model listed by Ollama. Confirm its availability and current pricing in the [Ollama model catalog](https://ollama.com/library/deepseek-v4.1-flash) before using it.

Render supplies `PORT` automatically. The Docker image includes Python and the speech dependencies for audio transcription; the transcription model is downloaded when first used. Set optional mail settings (`MAIL_ENABLED`, `MAIL_FROM`, `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`) only if email notifications are required.

### Vercel frontend

Import the repository into Vercel and set the project **Root Directory** to `frontend`. Add this build-time environment variable:

- `VITE_API_URL`: the public Render backend URL, for example `https://your-api.onrender.com`.

The included `frontend/vercel.json` sends client-side routes back to the React app entry point. After the first Vercel deployment, copy its exact production origin into Render's `CORS_ALLOWED_ORIGINS`, then redeploy the backend.

## 📧 Gmail Configuration

Gmail SMTP is used for sending interview scheduling and candidate shortlisting notifications.

- Uses Gmail SMTP for sending emails.
- Requires a Google App Password.
- Email notifications are optional.
- Interview schedules can be saved even when email notifications are disabled.

## ▶️ How to Run the Project

### 1. Start the Backend

Open a terminal in the backend directory:

```bash
cd backend
mvn spring-boot:run
```

Make sure MySQL and Ollama are running and the required application settings are configured.

### 2. Start the Frontend

Open another terminal:

```bash
cd frontend
npm install
npm run dev
```

Open the local URL displayed in the terminal. The default Vite development URL is usually:

```text
http://localhost:5173
```

## 🧪 Testing

Postman is used to test backend functionality, including:

- User registration and login
- Job management
- Application management
- Resume processing
- AI-based screening
- Interview workflows

## 🎯 Project Objective

The objective of SmartHire is to simplify recruitment by combining AI-powered resume analysis, job matching, application screening, candidate shortlisting, and interview management in a single platform.
