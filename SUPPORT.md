# Project Setup Guide
 
A comprehensive guide to setting up the complete microservices architecture with Java backends, Python backend, and Next.js frontends.
 
---
 
## Table of Contents
 
- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Setup Instructions](#setup-instructions)
  - [Step 1: Clone the Repository](#step-1-clone-the-repository)
  - [Step 2: Open Projects in IDEs](#step-2-open-projects-in-ides)
  - [Step 3: Setup Docker Containers](#step-3-setup-docker-containers)
  - [Step 4: Database Configuration](#step-4-database-configuration)
  - [Step 5: Python Backend Setup](#step-5-python-backend-setup)
  - [Step 6: Frontend Setup](#step-6-frontend-setup)
  - [Step 7: API Gateway Routing (Kong)](#step-7-api-gateway-routing-kong)
 
---
 
## Prerequisites
 
Before starting the setup, ensure you have the following installed on your system:
 
| Tool                | Version    | Purpose                  | Download Link                                                      |
| ------------------- | ---------- | ------------------------ | ------------------------------------------------------------------ |
| **Docker Desktop**  | Latest     | Container management     | [Download Docker](https://www.docker.com/products/docker-desktop)  |
| **DBeaver**         | Latest     | Database management      | [Download DBeaver](https://dbeaver.io/download/)                   |
| **JDK**             | 11+        | Java backend runtime     | [Download JDK](https://www.oracle.com/java/technologies/downloads/) |
| **Node.js**         | 16+        | Next.js frontend runtime | [Download Node.js](https://nodejs.org/)                            |
| **Python**          | 3.8+       | Python backend runtime   | [Download Python](https://www.python.org/downloads/)               |
| **IntelliJ IDEA**   | Latest     | Java IDE (recommended)   | [Download IntelliJ](https://www.jetbrains.com/idea/download/)      |
| **VS Code**         | Latest     | Python/Next.js IDE       | [Download VS Code](https://code.visualstudio.com/)                 |
 
---
 
## Project Structure
 
After cloning, your project will contain the following structure:
 
```
project-root/
├── analyser-app-backend/          # Java backend (Spring Boot)
├── analyser-app-frontend/         # Next.js Main App frontend
├── code-contribution-analyser/    # Python backend (Django)
├── version-control-data-fetcher/  # Java backend (Spring Boot)
├── email-sender/
│   ├── email-backend/             # Java backend (Spring Boot)
│   └── email-frontend/            # Next.js Email frontend
├── assets/                        # Contains Images
├── load-testing-scripts/          # Contains JMeter Scripts for Load Testing
└── devtools/                      # Docker setup scripts
```
 
### Technology Stack
 
| Component              | Technology  | Count        |
| ---------------------- | ----------- | ------------ |
| **Backend (Java)**     | Spring Boot | 3 services   |
| **Backend (Python)**   | Django      | 1 service    |
| **Frontend**           | Next.js     | 2 apps       |
 
---
 
## Setup Instructions
 
### Step 1: Clone the Repository
 
Clone the repository to your preferred location:
 
```bash
git clone <repository-url>
cd <project-folder>
```
 
---
 
## Step 2: Open Projects in IDEs
 
### Recommended IDE Setup
 
| Project Type          | Recommended IDE    | Projects                                                                      |
| --------------------- | ------------------ | ----------------------------------------------------------------------------- |
| **Java Projects**     | IntelliJ IDEA      | `analyser-app-backend`, `version-control-data-fetcher`, `email-sender/email-backend` |
| **Python Project**    | VS Code            | `code-contribution-analyser`                                                  |
| **Next.js Projects**  | VS Code            | `analyser-app-frontend`, `email-sender/email-frontend`                        |
 
---
 
## Step 3: Setup Docker Containers
 
### Prerequisites
 
- Ensure **Docker Desktop** is running before executing the script
- Verify Docker is running: `docker --version`
 
### Steps
 
1. **Navigate to the devtools folder:**
   ```bash
   cd devtools
   ```
 
2. **Give execute permissions to the Docker script:**
   ```bash
   chmod +x dockerScript.sh
   ```
 
3. **Execute the Docker script with command:**
   
   **To start containers:**
   ```bash
   ./dockerScript.sh start
   ```
 
   **To stop containers:**
   ```bash
   ./dockerScript.sh stop
   ```
 
   **To remove containers:**
   ```bash
   ./dockerScript.sh remove
   ```
 
4. **Wait** for all containers to be created and started (this may take a few minutes)
 
---
 
## Step 4: Database Configuration
 
Now we'll configure the databases using **DBeaver**.
 
### Connection 1: Main Application Database
 
1. Open **DBeaver**
2. Click on **New Database Connection** (or press `Ctrl+Shift+N` / `Cmd+Shift+N`)
3. Select **PostgreSQL**
4. Configure with the following settings:
 
 
 
   | Field          | Value                          |
   | -------------- | ------------------------------ |
   | **Host**       | `localhost`                    |
   | **Port**       | `9233`                         |
   | **Database**   | `dev-impact-db`                  |
   | **Username**   | `user`                         |
   | **Password**   | `pass`                         |
 
5. Click **Test Connection** (bottom left)
6. You should see a success popup:
7. Click **Finish**
 
---
 
### Connection 2: Analyser Database
 
Repeat the same process with these credentials:
 
**Connection Details:**
 
| Field          | Value                          |
| -------------- | ------------------------------ |
| **Host**       | `localhost`                    |
| **Port**       | `9234`                         |
| **Database**   | `dev-impact-processor`                  |
| **Username**   | `user`                         |
| **Password**   | `pass`                         |
 
---
 
### Connection 3: Version Control Database
 
**Connection Details:**
 
| Field          | Value                          |
| -------------- | ------------------------------ |
| **Host**       | `localhost`                    |
| **Port**       | `9231`                         |
| **Database**   | `version_control_db`           |
| **Username**   | `version_control_user`         |
| **Password**   | `version_control_pass`         |
 
---
 
### Verification
 
- All three database connections should appear in DBeaver's Database Navigator
- You should be able to expand each connection and view the database schemas
- **Java backends are now configured!**
 
---
 
## Step 5: Python Backend Setup
 
### Navigate to Python Project
 
```bash
cd code-contribution-analyser
```
 
---
 
### Create Virtual Environment
 
Create a Python virtual environment to isolate project dependencies:
 
```bash
python -m venv venv
```
 
> **Note:** Use `python3` instead of `python` if you have multiple Python versions installed.
 
---
 
### Activate Virtual Environment
 
**macOS/Linux:**
```bash
source venv/bin/activate
```
 
**Windows (Command Prompt):**
```bash
venv\Scripts\activate
```
 
**Windows (PowerShell):**
```bash
venv\Scripts\Activate.ps1
```
 
> **Tip:** You should see `(venv)` prefix in your terminal after successful activation.
 
---
 
### Install Required Packages
 
#### Option 1: Install from Requirements File (Recommended)
 
```bash
pip install -r requirements.txt
```
 
#### Option 2: Install Packages Individually
 
```bash
pip install django
pip install psycopg2-binary
pip install requests
pip install drf-spectacular
```
 
### Package List
 
| Package                  | Version | Purpose                              |
| ------------------------ | ------- | ------------------------------------ |
| `django`                 | 4.2     | Web framework                        |
| `psycopg2-binary`        | 2.9.3   | PostgreSQL database adapter          |
| `requests`               | 2.28.1  | HTTP library for API calls           |
| `drf-spectacular`        | 0.27.3  | OpenAPI schema generation            |
 
---
 
### Run Migrations and Start Server
 
1. **Navigate to the inner project folder:**
   ```bash
   cd codeContributionProcessor
   ```
 
2. **Apply database migrations:**
   ```bash
   python manage.py migrate
   ```
 
   Expected output:
   ```
   Operations to perform:
     Apply all migrations: admin, auth, contenttypes, sessions
   Running migrations:
     Applying contenttypes.0001_initial... OK
     Applying auth.0001_initial... OK
     ...
   ```
 
3. **Create a superuser (Optional but recommended):**
   ```bash
   python manage.py createsuperuser
   ```
 
4. **Start the development server:**
   ```bash
   python manage.py runserver 8085
   ```
---
 
## Step 6: Frontend Setup
 
### Main App Frontend
 
#### 1. Navigate to the frontend folder:
 
```bash
cd analyser-app-frontend
```
 
#### 2. Install dependencies:
 
```bash
npm install
```
 
> **Note:** This may take a few minutes. If you encounter errors, try `npm install --legacy-peer-deps`
 
#### 3. Create environment configuration:
 
Create `.env.local` file in the root folder:
 
```bash
touch .env.local
```
 
#### 4. Add environment variables:
 
Open `.env.local` and add the following content:
 
```env
# API Gateway Configuration
# Use port 8000 if routing through Kong API Gateway
# Use port 8083 to connect directly to the backend service
NEXT_PUBLIC_API_BASE=http://localhost:8083
```
 
> **Tip:** Change port to `8000` when using Kong API Gateway in production.
 
#### 5. Start the development server:
 
```bash
npm run dev
```
 
---
 
## Step 7: API Gateway Routing (Kong)
 
### Access Kong Admin GUI
 
Open your browser and navigate to:
```
http://localhost:8002
```
 
> **Note:** Ensure the Kong container is running. Verify with `docker ps | grep kong`
 
---
 
### Create Gateway Service
 
#### 1. Navigate to Gateway Services
 
- Click on **Gateway Services** in the left side navigation panel
- Click on **+ New Gateway Service** button
 
#### 2. Configure Service Settings
 
Fill in the service configuration as shown below:
 
**Service Configuration Details:**
 
| Field                | Value                                    |
| -------------------- | ---------------------------------------- |
| **Name**             | `service-a`               |
| **Protocol**         | `http`                                   |
| **Host**             | `host.docker.internal` (Mac/Windows) or `172.17.0.1` (Linux) |
| **Port**             | `8083`                                   |
 
#### 3. Save the Service
 
- Click **Create** or **Save** button at the bottom
- You should see a success message
 
---
 
### Create Route for the Service
 
#### 1. Navigate to Routes
 
- From the service details page, click on the **Routes** tab
- Alternatively, click **Routes** in the left navigation and then **+ New Route**
 
#### 2. Configure Route Settings
 
![Kong Route Configuration](./assets/kong-route-config.png)
 
**Route Configuration Details:**
 
| Field                | Value                                    |
| -------------------- | ---------------------------------------- |
| **Name**             | `servica-a-route`                 |
| **Service**             | `servica-a`                 |
| **Protocols**        | `http`, `https`                          |
| **Paths**            | `/api`                  |
| **Methods**          | *(Configure in next step)*               |
| **Strip Path**       | **UNCHECKED** (Important!)            |
 
#### 3. Click **Create** to save the route
 
---
 
### Enable HTTP Methods
 
#### 1. Configure Methods
 
From the route configuration page, locate the **Methods** section:
 
**Enable the following HTTP methods:**
- GET
- POST
- PUT
- PATCH
- DELETE
- OPTIONS
- HEAD
 
> **Important:** Do **NOT** use custom function - select all standard HTTP methods manually
 
#### 2. Save the methods configuration
 
---
 
### Advanced Configuration
 
#### 1. Access Advanced Settings
 
- Scroll down to **Advanced Settings** or click **Advanced Filters**
- Locate the **Strip Path** option
 
#### 2. Configure Strip Path
 
**CRITICAL:** Ensure **Strip Path** is **UNCHECKED** (disabled)
 
> **Why?** Unchecking "Strip Path" ensures the full API path is forwarded to the backend service.
 
#### 3. Save all changes
 
---
 
### Update Frontend Environment Variables
 
> **IMPORTANT:** Only proceed with this step if Kong routing is successfully working!
 
#### If Kong Setup is Successful:
 
1. **Open `.env.local` in your frontend projects:**
   
   **Analyser App Frontend:**
   ```bash
   cd analyser-app-frontend
   nano .env.local  # or use your preferred editor
   ```
 
2. **Update the API base URL:**
   ```env
   # Before (Direct Backend Connection)
   NEXT_PUBLIC_API_BASE=http://localhost:8083
 
   # After (Through Kong Gateway)
   NEXT_PUBLIC_API_BASE=http://localhost:8000
   ```
 
3. **Restart the development servers:**
   ```bash
   # Stop the running server (Ctrl+C)
   # Then restart
   npm run dev
   ```
 
#### If Kong Setup Has Issues:
 
- **Leave the configuration as `localhost:8083`** to connect directly to the backend
- Your application will work without the API Gateway
 
---
 
### Verify Kong Gateway Integration
 
Open your Postman / Bruno and test:
 
| Test                 | Direct Backend URL            | Through Kong Gateway URL      |
| -------------------- | ----------------------------- | ----------------------------- |
| **API Endpoint**     | http://localhost:8083/api/... | http://localhost:8000/api/... |
 
Both should return identical responses.
 
---