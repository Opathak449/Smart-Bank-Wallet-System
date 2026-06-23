# 🏦 SmartBank Wallet — Full Stack Banking Application

A production-ready Digital Banking & Wallet Management System built with **Java Spring Boot 3**, **React 18**, and **PostgreSQL 16**.

---

## 📋 Table of Contents
1. [Technology Stack](#technology-stack)
2. [Project Structure](#project-structure)
3. [Features](#features)
4. [Prerequisites](#prerequisites)
5. [CLICK-BY-CLICK SETUP GUIDE](#-click-by-click-setup-guide)
   - [Option A: Docker Setup (Recommended)](#option-a-docker-setup-recommended)
   - [Option B: Manual Local Setup](#option-b-manual-local-setup)
6. [API Documentation](#api-documentation)
7. [Default Credentials](#default-credentials)
8. [Troubleshooting](#troubleshooting)

---

## Technology Stack

| Layer       | Technology                          |
|-------------|-------------------------------------|
| Backend     | Java 21, Spring Boot 3.2, Maven     |
| Security    | Spring Security 6, JWT, BCrypt      |
| Database    | PostgreSQL 16, Spring Data JPA      |
| Frontend    | React 18, MUI v5, Chart.js          |
| PDF         | iText 8                             |
| Email       | JavaMailSender (SMTP/Gmail)         |
| Deployment  | Docker, Docker Compose, Nginx       |

---

## Project Structure

```
smartbank/
├── backend/                  # Spring Boot Application
│   ├── src/main/java/com/smartbank/
│   │   ├── config/           # Security, CORS config
│   │   ├── controller/       # REST Controllers
│   │   ├── dto/              # Request & Response DTOs
│   │   ├── entity/           # JPA Entities
│   │   ├── exception/        # Global exception handling
│   │   ├── repository/       # Spring Data repositories
│   │   ├── security/         # JWT, UserDetails
│   │   ├── service/          # Business logic
│   │   └── util/             # Utilities
│   ├── src/main/resources/
│   │   └── application.properties
│   ├── src/test/             # JUnit 5 tests
│   ├── Dockerfile
│   └── pom.xml
│
├── frontend/                 # React Application
│   ├── src/
│   │   ├── context/          # Auth context
│   │   ├── pages/
│   │   │   ├── auth/         # Login, Register
│   │   │   ├── user/         # Dashboard, Wallet, Transfer, etc.
│   │   │   └── admin/        # Admin pages
│   │   ├── services/         # Axios API calls
│   │   ├── components/       # Reusable components
│   │   └── utils/            # MUI Theme
│   ├── public/
│   ├── Dockerfile
│   ├── nginx.conf
│   └── package.json
│
├── database/
│   └── schema.sql            # Complete PostgreSQL schema + sample data
├── docker-compose.yml
├── .env.example
└── README.md
```

---

## Features

### User Features
- ✅ Register / Login with JWT
- ✅ Auto-generated wallet & account number
- ✅ Add Money (Debit Card, Credit Card, UPI, Net Banking - simulated)
- ✅ Transfer Money (wallet-to-wallet)
- ✅ Transaction History with pagination & search
- ✅ PDF Bank Statement download (iText)
- ✅ Email notifications (Welcome, Transfer, Add Money)
- ✅ Dashboard with Charts (income vs expenses, spending breakdown)
- ✅ Profile management
- ✅ Dark/Light mode

### Admin Features
- ✅ Admin Dashboard with analytics
- ✅ User management (activate, block, delete)
- ✅ View all transactions
- ✅ Audit logs

---

## Prerequisites

Before setup, make sure these are installed:

| Tool       | Version | Check Command         | Download Link                          |
|------------|---------|-----------------------|----------------------------------------|
| Java JDK   | 21+     | `java -version`       | https://adoptium.net/                  |
| Maven      | 3.9+    | `mvn -version`        | https://maven.apache.org/download.cgi  |
| Node.js    | 20+     | `node -version`       | https://nodejs.org/                    |
| npm        | 9+      | `npm -version`        | (comes with Node.js)                   |
| PostgreSQL | 16      | `psql --version`      | https://www.postgresql.org/download/   |
| Docker     | 24+     | `docker -version`     | https://www.docker.com/get-started     |
| Git        | 2.40+   | `git --version`       | https://git-scm.com/                   |

---

---

# 🚀 CLICK-BY-CLICK SETUP GUIDE

---

## OPTION A: Docker Setup (Recommended — 5 Steps)

> This runs the entire application with one command. No manual DB setup needed.

---

### STEP 1 — Install Docker Desktop

**Windows/Mac:**
1. Go to → https://www.docker.com/products/docker-desktop/
2. Click **Download Docker Desktop**
3. Run the installer (.exe or .dmg)
4. Start Docker Desktop
5. Wait for the whale icon to stop animating (ready)

**Linux (Ubuntu):**
```bash
sudo apt update
sudo apt install -y docker.io docker-compose-plugin
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker $USER
# Log out and log back in
```

**Verify:** Open terminal → type `docker --version` → should show version number

---

### STEP 2 — Get the Project Files

```bash
# Clone or extract the project to any folder, then:
cd smartbank
```

---

### STEP 3 — Configure Environment Variables

```bash
# Copy the example env file
cp .env.example .env
```

Now open `.env` in any text editor and fill in:

```
DB_PASSWORD=YourChosenPassword
JWT_SECRET=AnyLongRandomStringAtLeast64CharactersLong
MAIL_USERNAME=your-gmail@gmail.com
MAIL_PASSWORD=your-gmail-app-password
```

> **Gmail App Password setup:**
> 1. Go to Google Account → Security
> 2. Enable 2-Step Verification (if not done)
> 3. Search "App passwords" → create one for "Mail"
> 4. Copy the 16-character password → paste in MAIL_PASSWORD

> **Note:** Email is optional for first run. The app works without it (just skips email sending).

---

### STEP 4 — Build & Start All Services

```bash
# In the smartbank/ root folder:
docker compose up --build
```

You will see logs from 3 containers starting up:
- `smartbank-postgres` → Database
- `smartbank-backend` → Spring Boot API
- `smartbank-frontend` → React + Nginx

Wait until you see: `Started SmartBankApplication in X seconds`

This takes **3–8 minutes** on first run (downloading images + building).

---

### STEP 5 — Open the Application

| Service       | URL                              |
|---------------|----------------------------------|
| **Frontend**  | http://localhost:3000            |
| **Backend API** | http://localhost:8080/api      |
| **Swagger UI** | http://localhost:8080/api/swagger-ui.html |
| **API Docs**  | http://localhost:8080/api/v3/api-docs |

**Login credentials:**

| Role  | Username      | Password   |
|-------|---------------|------------|
| Admin | `admin`       | `Admin@123`|
| User  | `rahul.sharma`| `User@123` |
| User  | `priya.patel` | `User@123` |

---

### Docker Management Commands

```bash
# Stop all services
docker compose down

# Stop and remove data (clean slate)
docker compose down -v

# View logs
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f postgres

# Restart a single service
docker compose restart backend
```

---
---

## OPTION B: Manual Local Setup

> For development without Docker. Requires Java, Node.js, and PostgreSQL installed.

---

### STEP 1 — Install Java 21

**Windows:**
1. Go to → https://adoptium.net/
2. Select: **Java 21 (LTS)** → **Windows** → **x64** → **JDK**
3. Download and run the `.msi` installer
4. During install, check: ✅ "Set JAVA_HOME variable"
5. Open **new** Command Prompt
6. Type: `java -version` → should show `openjdk 21`

**Mac:**
```bash
brew install openjdk@21
echo 'export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
java -version
```

**Linux:**
```bash
sudo apt update
sudo apt install -y openjdk-21-jdk
java -version
```

---

### STEP 2 — Install Maven

**Windows:**
1. Go to → https://maven.apache.org/download.cgi
2. Download `apache-maven-3.9.x-bin.zip`
3. Extract to `C:\Program Files\Maven\`
4. Add to PATH:
   - Search "Environment Variables" in Start Menu
   - Click "Environment Variables" button
   - Under System Variables → Path → Edit → New
   - Add: `C:\Program Files\Maven\apache-maven-3.9.x\bin`
5. Open new Command Prompt → type `mvn -version`

**Mac/Linux:**
```bash
brew install maven     # Mac
sudo apt install maven  # Ubuntu
mvn -version
```

---

### STEP 3 — Install Node.js

**Windows/Mac:**
1. Go to → https://nodejs.org/
2. Download **LTS version** (v20+)
3. Run installer, click Next through all steps
4. Open new Command Prompt → type `node -version`

**Linux:**
```bash
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt install -y nodejs
node -version
npm -version
```

---

### STEP 4 — Install PostgreSQL

**Windows:**
1. Go to → https://www.postgresql.org/download/windows/
2. Download the installer (PostgreSQL 16)
3. Run installer:
   - Set password for `postgres` user (remember this!)
   - Port: **5432** (default, keep as-is)
   - Click through to finish
4. Open **pgAdmin** (installed automatically)

**Mac:**
```bash
brew install postgresql@16
brew services start postgresql@16
```

**Linux:**
```bash
sudo apt install -y postgresql postgresql-contrib
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

---

### STEP 5 — Set Up the Database

**Windows (using pgAdmin):**
1. Open pgAdmin from Start Menu
2. Connect to server (enter the password you set)
3. Right-click **Databases** → **Create** → **Database**
4. Name: `smartbank_db` → Save
5. Right-click `smartbank_db` → **Query Tool**
6. Click the folder icon (open file) → select `database/schema.sql`
7. Click the ▶ Play button to run

**Mac/Linux (using terminal):**
```bash
# Connect to PostgreSQL
sudo -u postgres psql    # Linux
psql postgres            # Mac

# Inside psql prompt:
CREATE DATABASE smartbank_db;
\c smartbank_db
\i /full/path/to/smartbank/database/schema.sql
\q
```

---

### STEP 6 — Configure Backend

Open `backend/src/main/resources/application.properties` and update:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/smartbank_db
spring.datasource.username=postgres
spring.datasource.password=YOUR_POSTGRES_PASSWORD

spring.mail.username=your-gmail@gmail.com
spring.mail.password=your-app-password
```

Also change:
```properties
spring.jpa.hibernate.ddl-auto=validate
```
to:
```properties
spring.jpa.hibernate.ddl-auto=none
```
(since we already ran schema.sql manually)

---

### STEP 7 — Run the Backend

```bash
cd smartbank/backend

# First build (downloads dependencies — takes 2-4 minutes)
mvn clean install -DskipTests

# Start the server
mvn spring-boot:run
```

✅ Success: You will see → `Started SmartBankApplication in X.XXX seconds`

The API is now at: **http://localhost:8080/api**

---

### STEP 8 — Run the Frontend

Open a **new terminal window** (keep backend running):

```bash
cd smartbank/frontend

# Install dependencies (takes 1-2 minutes)
npm install

# Start the React development server
npm start
```

✅ Your browser will automatically open → **http://localhost:3000**

---

### STEP 9 — Log In & Explore

| Role  | Username      | Password   |
|-------|---------------|------------|
| Admin | `admin`       | `Admin@123`|
| User  | `rahul.sharma`| `User@123` |

---

## API Documentation

Swagger UI available at: **http://localhost:8080/api/swagger-ui.html**

### Key Endpoints

| Method | Endpoint                      | Auth     | Description              |
|--------|-------------------------------|----------|--------------------------|
| POST   | `/auth/register`              | Public   | Register new user        |
| POST   | `/auth/login`                 | Public   | Login, get JWT tokens    |
| POST   | `/auth/refresh`               | Public   | Refresh access token     |
| POST   | `/auth/logout`                | User     | Logout & revoke token    |
| GET    | `/wallet/details`             | User     | Get wallet info          |
| POST   | `/wallet/add-money`           | User     | Add money (simulated)    |
| POST   | `/wallet/transfer`            | User     | Transfer to another user |
| GET    | `/wallet/transactions`        | User     | Transaction history      |
| GET    | `/wallet/statement/pdf`       | User     | Download PDF statement   |
| GET    | `/admin/dashboard`            | Admin    | Admin analytics          |
| GET    | `/admin/users`                | Admin    | All users (paginated)    |
| PUT    | `/admin/users/{id}/status`    | Admin    | Block/activate user      |
| GET    | `/admin/audit-logs`           | Admin    | Audit trail              |

---

## Troubleshooting

### ❌ `Port 5432 already in use`
```bash
# Find process using port
lsof -i :5432    # Mac/Linux
netstat -ano | findstr :5432   # Windows
# Kill it or use a different port in application.properties
```

### ❌ `java.lang.ClassNotFoundException` during build
```bash
mvn clean install -U -DskipTests
```

### ❌ React `Module not found` error
```bash
rm -rf node_modules package-lock.json
npm install
```

### ❌ `Connection refused` to PostgreSQL
- Check PostgreSQL is running: `sudo systemctl status postgresql`
- Check the password in application.properties matches what you set
- Make sure `smartbank_db` database exists

### ❌ Email not sending
- App still works without email (it's async and caught)
- Verify Gmail App Password (not your regular password)
- Check `MAIL_USERNAME` and `MAIL_PASSWORD` in .env or application.properties

### ❌ `401 Unauthorized` in browser
- Token may be expired — log out and log in again
- Check that backend is running on port 8080
- Check browser console for CORS errors (update `app.cors.allowed-origins`)

---

## Running Tests

```bash
cd backend
mvn test
```

---

## Building for Production

```bash
# Backend JAR
cd backend
mvn clean package -DskipTests
# Output: target/smartbank-wallet-1.0.0.jar

# Frontend build
cd frontend
npm run build
# Output: build/ folder (serve with Nginx)
```

---

*Built with ❤️ — SmartBank Wallet v1.0.0*
