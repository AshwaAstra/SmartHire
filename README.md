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
- MySQL

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

Create the MySQL database:

```sql
CREATE DATABASE hack_01;
```

Configure the database connection in the Spring Boot `application.properties` file using your local MySQL credentials.

Do not commit actual database passwords or other secrets to GitHub.

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
