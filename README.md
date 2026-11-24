# AI-Powered Smart Recruitment & Talent Management System

Two-application monorepo:

- `backend/`: Spring Boot REST API
- `frontend/`: React + Vite client
- `database/`: MySQL scripts
- `documentation/`: API and architecture documentation

## Prerequisites
- JDK 21+
- Maven 3.9+
- Node.js 20+
- MySQL 8+
- [Ollama](https://ollama.com/download) with the `qwen2.5:1.5b` model (the optional `qwen2.5:3b` model can be used for higher-quality, slower responses)
- Python 3.10+ for local audio transcription

## Backend
```powershell
cd recruitment-platform/backend
$env:DB_URL="jdbc:mysql://localhost:3306/hack_01?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your-password"
$env:JWT_SECRET="a-long-random-secret"
py -3.10 -m pip install -r requirements-speech.txt
py -3.10 -c "from faster_whisper import WhisperModel; WhisperModel('tiny.en', device='cpu', compute_type='int8')" # download speech model once
ollama pull qwen2.5:1.5b # run once; default fast model for all AI features
ollama pull qwen2.5:3b # optional, for higher-quality, slower general AI responses
mvn spring-boot:run
```

Speech recognition uses the local `tiny.en` Whisper model with CPU int8 inference.
The first model setup downloads it once; recorded audio is transcribed on this
machine and is not sent to an external API. Set `SPEECH_MODEL` or
`SPEECH_LANGUAGE` to change the speech model or language. Video answers are
recorded locally and evaluated using the transcript typed by the candidate.

The backend uses Ollama locally by default and does not need a Gemini API key.
Ollama must be running before using AI features. The default `qwen2.5:1.5b`
model is used across AI features for faster responses. To use another model or
server, set `OLLAMA_MODEL`, `OLLAMA_INTERVIEW_MODEL`, or `OLLAMA_BASE_URL` before starting the backend.
Models are kept loaded for 10 minutes between requests to reduce repeat-call
latency. The optional `qwen2.5:3b` model may provide richer responses but is slower.

## Interview invitations
Recruiters can shortlist applicants using the status menu, then schedule an
interview separately. The schedule is saved in SmartHire even when email is not
configured. To also send the candidate an email with the interview date, time
zone, meeting details, and an optional note, configure SMTP in the same
PowerShell window before starting the backend:

```powershell
$env:MAIL_ENABLED="true"
$env:MAIL_HOST="smtp.gmail.com"
$env:MAIL_PORT="587"
$env:MAIL_USERNAME="your-sending-address@example.com"
$env:MAIL_PASSWORD="your-provider-app-password"
$env:MAIL_FROM="your-sending-address@example.com"
```

For Gmail, use an app password rather than your regular account password. Keep
mail credentials in environment variables; do not commit them. Interview emails
remain disabled until `MAIL_ENABLED` is set to `true`.

For a guided local Gmail setup, stop the existing backend and run
`.\run-with-gmail-smtp.ps1` from the backend PowerShell terminal. Enter the
sender Gmail address and its App Password when prompted. The script keeps the
password out of source files and clears the mail environment variables when
the backend process exits.

Recruiter sign-up requires a company name, which appears in interview emails.
Existing recruiters can add or update it from **Company profile**.

## Applicant screening
Recruiters can use **Screen & rank applicants** on a job's Applicants page to
compare each uploaded resume (or candidate profile when no resume was attached)
with that job's skills, qualifications, experience, and description using the
local Ollama model. Results show match evidence and sort applicants by score.
The highest-scoring applicant or tied applicants are automatically shortlisted
and emailed a notice that interview details will follow. Re-running screening
does not resend notices already sent. Interview dates remain a recruiter
decision. Ollama must be running; unreadable or empty resumes are flagged for
manual review rather than assigned a match score.

If MySQL reports `Access denied for user 'root'@'localhost'`, set your local
MySQL password in the same PowerShell window before starting Maven.

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/hack_01?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your-local-mysql-password"
mvn spring-boot:run
```

## Frontend
```powershell
cd recruitment-platform/frontend
npm install
npm run dev
```
