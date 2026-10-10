# Jobs4AI Landing Page

## Overview

Jobs4AI is an open-source AI-powered job recommendation platform that helps users discover job opportunities matched to their skills, experience, and career goals.

The project includes a main job recommendation application and a separate landing page website.

## Problem Statement

Traditional job platforms often show users many irrelevant job listings. Searching manually can be time-consuming, and users may struggle to identify which opportunities best match their background.

## Our Solution

Jobs4AI uses resume information, skills, and user profiles to provide more personalized job recommendations. It also helps users organize their job search by saving jobs, tracking applications, and asking an AI assistant for job-related guidance.

## Key Features

- User registration and login
- JWT-based authentication
- PDF resume upload
- AI-assisted resume parsing
- Skill and experience extraction
- Personalized job recommendations
- Job search and filtering
- Job match scores
- Save jobs for later
- Track applied jobs
- User profile management
- AI assistant for job-related questions

## How It Works

1. Create an account.
2. Upload a PDF resume.
3. Let the platform extract skills and experience.
4. Receive personalized job recommendations.
5. Browse and filter available jobs.
6. Save jobs or mark them as applied.
7. Ask the AI assistant for help finding relevant opportunities.

## Technology Stack

- Java
- Spring Boot
- React
- Vite
- Tailwind CSS
- JavaScript
- REST APIs
- JWT authentication

## Project Architecture

Jobs4AI uses a frontend and backend architecture:

- The React frontend provides the user interface.
- The Spring Boot backend provides authentication, job data, resume processing, recommendations, and AI assistant functionality.
- The frontend communicates with the backend through REST APIs.

The landing page is maintained separately from the main application.

## Live Demo

Website: https://jobs4ai.me

The project is currently an early-stage open-source platform under development.

## Screenshots

Screenshots can be added here as the application and landing page continue to develop.

## Getting Started

### Prerequisites

- Node.js
- npm
- Git

### Install dependencies

```bash
git clone https://github.com/Satzz001/jobs4ai.git
cd jobs4ai
npm install
```

### Run locally

```bash
npm run dev
```

The frontend normally runs at:

```text
http://localhost:5173
```

## Project Structure

```text
jobs4ai/
├── src/
│   ├── index.css
│   └── main.jsx
├── index.html
├── package.json
├── vite.config.js
├── tailwind.config.js
├── postcss.config.js
├── README.md
└── dist/
```

## Deployment

This landing page can be deployed with:

- Vercel
- Netlify
- GitHub Pages

The intended domain is:

```text
https://jobs4ai.me
```

## Future Enhancements

- More advanced recommendation models
- Improved resume analysis
- Additional job sources
- Job alerts and notifications
- Enhanced profile customization
- Application status management
- AI career guidance

## Contributing

Contributions, suggestions, and feedback are welcome.

## Contact

Email: contact@jobs4ai.me

Main application repository:
https://github.com/Satzz001/Job_Recommendation_Platform

## License

No license has currently been specified for this project.

Jobs4AI is not currently certified by an external organization or certification authority. It is an early-stage open-source project under development.
