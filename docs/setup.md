# Learnova – Setup Guide

Step-by-step setup for local development.

## 1. Prerequisites

- **Node.js** v18 or higher (for frontend)
- **npm** (comes with Node)
- **Java** 17 or higher (for backend)
- **Maven** 3.8 or higher (for backend)
- **Aiven** account (or any PostgreSQL instance)

## 2. Database (Aiven PostgreSQL)

1. Log in to [Aiven](https://aiven.io) and create a **PostgreSQL** service.
2. After the service is ready, open its details and note:
   - Host
   - Port
   - Database name (e.g. `defaultdb`)
   - User (e.g. `avnadmin`)
   - Password
3. In the Aiven project, add your **backend server’s public IP** to the service **allowlist** so the Spring Boot app can connect. (For local dev, use your machine’s public IP or the IP of the host running the backend.)
4. Connection URL format:  
   `postgres://USER:PASSWORD@HOST:PORT/DATABASE?sslmode=require`  
   Example:  
   `postgres://avnadmin:YOUR_PASSWORD@learnova-xxx.aivencloud.com:12345/defaultdb?sslmode=require`  
   **Do not commit the password.** You will set it via environment variable.

## 3. Backend

1. Clone or open the repo and go to the backend:
   ```bash
   cd backend
   ```
2. Set the database password (required for non-empty password):
   - **Windows (PowerShell):**
     ```powershell
     $env:SPRING_DATASOURCE_PASSWORD="your_aiven_password"
     ```
   - **Windows (CMD):**
     ```cmd
     set SPRING_DATASOURCE_PASSWORD=your_aiven_password
     ```
   - **Unix/macOS:**
     ```bash
     export SPRING_DATASOURCE_PASSWORD=your_aiven_password
     ```
3. (Optional) If your Aiven URL differs from the one in `application.properties`, set:
   ```bash
   export SPRING_DATASOURCE_URL=jdbc:postgresql://HOST:PORT/DATABASE?sslmode=require
   export SPRING_DATASOURCE_USERNAME=avnadmin
   ```
4. Run the backend:
   ```bash
   mvn spring-boot:run
   ```
5. Wait until the app starts. Default port: **8081**. Tables are created/updated automatically (JPA `ddl-auto=update`). On first run, seed data creates 10 sample courses if the database is empty.

## 4. Frontend

1. In a new terminal, go to the frontend:
   ```bash
   cd frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. (Optional) If the API is not at `http://localhost:8081/api`, create a `.env` file (see `.env.example`) and set:
   ```
   VITE_API_URL=http://localhost:8081/api
   ```
4. Start the dev server:
   ```bash
   npm run dev
   ```
5. Open **http://localhost:5173** in your browser.

## 5. First use

1. Open the app; you should see the Courses page (search and category filter).
2. Click **Sign up** and create an account (full name, email, password).
3. After signup you are logged in and redirected to the Dashboard, which shows “My learning” and “Browse courses”.
4. Click a course → **Enroll** (on course details) → **Go to course** to open the Learning page. Watch a lesson and click **Mark as complete** to update progress.

## 6. Troubleshooting

- **Backend cannot connect to DB**: Check allowlist (backend server IP), URL, username, and password. Ensure `sslmode=require` in the URL for Aiven.
- **Frontend “Failed to load courses”**: Ensure the backend is running on 8081 and CORS allows your frontend origin (default `http://localhost:5173`). Check `VITE_API_URL` if you use a different API URL.
- **No courses shown**: Run the backend at least once so the DataSeeder runs (it seeds when course count &lt; 10). If you use a fresh DB, restart the backend to trigger seeding.
