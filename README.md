# CPEN 321 M1 Application

This repository contains the mobile application (frontend) and Node.js server (backend) for the **CPEN 321 M1 Milestone**.

---

## Live Deployed Cloud Server

The backend service is deployed and running live on a Google Cloud Compute Engine VM:
* **Deployed Backend URL:** `http://136.69.174.49:3000`
* **Health Check Endpoint:** `http://136.69.174.49:3000/health`
* **WebSocket Pixel Stream:** `ws://136.69.174.49:3000/ws/pixels`

---

## Requirements

Install the following before setting up the frontend or backend:

- [git](https://git-scm.com/install/)
- [Java 17](https://adoptium.net/temurin/releases/?version=17)
- [Android Studio](https://developer.android.com/studio) (latest version)
- [Node.js](https://nodejs.org/en/download/) 22+ and npm 10+
- [Docker](https://docs.docker.com/desktop/setup/install) and Docker Compose v2.24+ (optional, for running backend via Docker)

--- 

## Frontend Setup

### Setup

1. **Open Project**: Open the `frontend/` directory in Android Studio.
2. **Sync Gradle**: Android Studio will automatically prompt you to sync the project. Click **Sync Now** (or run `cd frontend && ./gradlew build` in your terminal).
3. **Configure `local.properties`**:
   Copy the example file:
   ```bash
   cp frontend/local.properties.example frontend/local.properties
   ```
   Edit `frontend/local.properties` and configure:
   ```properties
   sdk.dir=/Users/<username>/Library/Android/sdk
   API_BASE_URL=http://136.69.174.49:3000
   GOOGLE_CLIENT_ID=830640124782-a8sshrs131f8kn2gb765otnv1b6e9hut.apps.googleusercontent.com
   ```
   *(Note: To connect to a local backend running on your machine's emulator instead of the deployed cloud server, set `API_BASE_URL=http://10.0.2.2:3000`).*

4. **Set Up Emulator or Physical Device**:
   * Create an AVD in Android Studio (Device Manager) selecting **Pixel 9** with **Android 16 / Baklava (API level 36)**.
   * Ensure the emulator image has **Google Play Services** enabled.

### Build and Run

- **Via Script (Recommended)**:
  Run the frontend script from the project root:
  ```bash
  ./scripts/run-frontend.sh
  ```
- **Via Android Studio**:
  Select your emulator/device and click the green **Play (▶)** button in the top toolbar.

---

## Backend Setup

You can run the backend either locally via Node.js or via Docker Compose.

### Environment Configuration

From the project root, copy the environment template:
```bash
cp backend/.env.example backend/.env
```

Configure `backend/.env`:
```env
PORT=3000
NODE_ENV=production
JWT_SECRET=c8f1e3a9b7d2f4e6a0c5b8d1e3f7a9c2b4d6e8f0a2c4b6d8e0f2a4c6b8d0e2f4
GOOGLE_CLIENT_ID=830640124782-a8sshrs131f8kn2gb765otnv1b6e9hut.apps.googleusercontent.com
MONGODB_URI=mongodb://localhost:27017/cpen321
PUBLIC_IP=136.69.174.49
```

### Option 1: Run Locally (Node.js)

1. **Install Dependencies**:
   ```bash
   cd backend
   npm install
   ```

2. **Start Development Server**:
   ```bash
   npm run dev
   ```

### Option 2: Run via Docker Compose

1. **Start Backend Service**:
   ```bash
   ./scripts/run-backend.sh
   ```
   *(Or run `docker compose up --build -d` directly).*

2. **Stop Backend Service**:
   ```bash
   docker compose down
   ```

---

## Feature Overview

1. **Button 1 (Server & Google Auth):** Authenticates via Google OAuth using Android `CredentialManager` and displays Server Public IP, Server Time, Client IP, Client Time, Developer Name, and Logged-in Google User details.
2. **Button 2 (Live Pixel Stream):** Relays live pixel updates from `wss://8.229.22.124` through the backend's `/ws/pixels` WebSocket service to a 16x16 Jetpack Compose Canvas screen.
3. **Button 3 (Timer & Interactive Surprise):** User-defined countdown timer that triggers an interactive Trivia Surprise Challenge (powered by Open Trivia DB) upon reaching `00:00`.
