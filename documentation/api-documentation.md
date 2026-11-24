# API Documentation

| Area | Method | Endpoint | Authorization |
|---|---|---|---|
| Register | POST | `/api/auth/register` | Public |
| Login | POST | `/api/auth/login` | Public |
| Candidate profile | GET/PUT | `/api/candidates/profile` | Candidate/Admin |
| Resume | POST/GET | `/api/candidates/resume` | Candidate/Admin |
| Recruiter profile | GET/PUT | `/api/recruiters/profile` | Recruiter/Admin |
| Jobs | GET/POST | `/api/jobs` | Public/Recruiter |
| Job detail/update/delete/status | GET/PUT/DELETE/PATCH | `/api/jobs/{id}` and `/api/jobs/{id}/status` | Public/Recruiter |
| Applications | POST/GET/PATCH | `/api/applications`, `/api/applications/submit`, `/api/applications/my`, `/api/applications/job/{jobId}`, `/api/applications/{id}/status` | Role-specific |
| Applicant resume | GET | `/api/applications/{id}/resume` | Owning recruiter/Admin |
| Resume analysis | POST | `/api/ai/resume/analyze` | Candidate |
| Job matching | POST | `/api/ai/job-match/{jobId}` | Candidate |
| Interview questions | POST | `/api/ai/interview/generate` | Candidate |
| Resume improvement | POST | `/api/ai/resume/improve` | Candidate |
| Resume analysis history | GET | `/api/ai/resume/analyses` | Candidate |
| Job match history | GET | `/api/ai/job-matches` | Candidate |
| Interview question history | GET | `/api/ai/interview/questions` | Candidate |
| Resume improvement history | GET | `/api/ai/resume/improvements` | Candidate |
| Mock interviews | POST/GET/DELETE | `/api/mock-interviews` | Candidate |
| Recording upload | POST | `/api/mock-interviews/{id}/upload-audio` or `/upload-video` | Candidate |
| Audio answer and transcription | POST | `/api/mock-interviews/{id}/responses/audio` | Candidate |
| Report | POST/GET | `/api/mock-interviews/{id}/evaluate` or `/report` | Candidate |
| Analytics | GET | `/api/analytics/{admin,recruiter,candidate}` | Role-specific |
| Admin users | GET/PATCH | `/api/admin/users` | Admin |
