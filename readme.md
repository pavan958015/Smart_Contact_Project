# Smart Contact Manager (SCM 2.0)

Smart Contact Manager (SCM 2.0) is a premium, cloud-enabled contact management application built with Spring Boot, Thymeleaf, Tailwind CSS, and MySQL. It offers a secure, interactive dashboard for organizing contacts, composing direct emails, gathering user feedback, and logging outbox transmissions.

---

## 🌟 Key Features

### 1. Interactive Console & Stats Dashboard
*   **Dynamic Counters:** Instantly tracks total contacts, starred favorites, and verification badges via Thymeleaf expression bindings.
*   **Quick Action Panels:** Fast navigation cards allowing users to insert contacts, query list views, and configure account configurations with soft transitions.

### 2. Contacts Catalog & Operations
*   **CRUD Operations:** Seamlessly add, view, update, and delete contacts. Supports custom image uploads using Cloudinary.
*   **Pagination & Sorting:** Built-in Spring Data page requests matching standard page size parameters.
*   **Export to Excel:** Client-side conversion of contact tables directly into spreadsheet sheets.

### 3. Safe Contact Search
*   **Null-Safe Filtering:** Search by Name, Email, or Phone number. If empty selections occur, the search system defaults safely to name queries without throwing template engine exceptions.

### 4. Interactive Feedback Desk
*   **5-Star Rating Widget:** Custom hover-state JavaScript star ratings backed by Spring validation models.
*   **Feedback Timeline:** Submissions are written to MySQL and loaded chronologically below the compose form.

### 5. Direct Mail Messaging (Outbox)
*   **Email Dispatch:** Compose messages and send actual emails to your contacts using JavaMailSender (SMTP).
*   **Outbox Logs:** History logs tracking sent mail subjects, recipient addresses, and dispatch times.

### 6. Social Logins & SSO Authentication
*   **Spring Security:** Integrated traditional login forms with custom failure handlers alongside OAuth2 Clients (Google and GitHub OAuth2).

### 7. Git Automation Script (`git_push.bat`)
*   **Staging, Committing & Pushing:** Double-click the helper script in Windows File Explorer or run it in the terminal to automatically check status, prompt for a commit message, stage changes, commit, and push updates directly to GitHub.

---

## 🛠️ Technology Stack

| Layer | Technology |
| :--- | :--- |
| **Backend Framework** | Spring Boot 3.2.5 |
| **Security Layer** | Spring Security 6 (OAuth2 Client) |
| **Persistence (ORM)** | Spring Data JPA / Hibernate |
| **Database** | MySQL |
| **Template Engine** | Thymeleaf 3 |
| **CSS styling** | Tailwind CSS 3.4.3 |
| **Frontend Libraries** | Flowbite UI, SweetAlert2 |
| **Image Hosting** | Cloudinary API |
| **Mail Dispatch** | Spring Mail (Gmail SMTP) |

---

## 🚀 Getting Started

### Prerequisites
1.  **Java SDK:** Oracle JDK 21 or JDK 24.
2.  **Database:** MySQL Server running on port `3306` with a database named `scm20`.
3.  **Tailwind Compiler:** Node.js & npm installed to rebuild stylesheets.

### Environment Properties Configurations
Create or edit your variables inside `src/main/resources/application.properties` (or `application-dev.properties`):
```properties
# Database Configurations
spring.datasource.url=jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3306}/${MYSQL_DB:scm20}
spring.datasource.username=${MYSQL_USER:root}
spring.datasource.password=${MYSQL_PASSWORD:your_password}

# Cloudinary Integration
cloudinary.api.key=${CLOUDINARY_API_KEY:your_api_key}
cloudinary.api.secret=${CLOUDINARY_API_SECRET:your_api_secret}
cloudinary.cloud.name=${CLOUDINARY_CLOUD_NAME:your_cloud_name}

# Mail Configurations
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME:your_email@gmail.com}
spring.mail.password=${MAIL_PASSWORD:your_app_password}
```

### Running Locally

1.  **Compile Java Source:**
    ```bash
    ./mvnw compile
    ```

2.  **Compile Tailwind Style Utilities:**
    ```bash
    npx tailwindcss -i ./src/main/resources/static/css/input.css -o ./src/main/resources/static/css/output.css
    ```

3.  **Launch Dev Server:**
    ```bash
    ./mvnw spring-boot:run
    ```
    Access the application at: `http://localhost:8081`

4.  **Initial Account:**
    A default admin profile is created automatically on start:
    *   **Username:** `admin@gmail.com`
    *   **Password:** `admin`

### 🔄 Git Push Automation Script
The project includes a custom helper script [git_push.bat](file:///c:/Users/Pavan%20Soni/Downloads/scm2.0-main_Final/scm2.0-main/git_push.bat) at the root level to automate Git commits and pushes:

1. **How to Run (via Terminal):**
   ```powershell
   .\git_push.bat
   ```
2. **How to Run (via Windows Explorer):**
   * Go to the project root directory in Windows Explorer.
   * Double-click `git_push.bat` to launch it in a command window.
   * *(Note: Double-clicking it inside VS Code or another IDE file tree will only open the file for editing. Make sure to double-click from Windows File Explorer).*

The script will show current `git status`, ask you for a commit message (with a fallback default), stage all changes, commit, and push directly to `origin main`.

---

## 📐 Project Architecture
*   `com.scm.config` - Security, Authentication handlers, and Cloudinary Configurations.
*   `com.scm.controllers` - Web mappings and Form processing.
*   `com.scm.entities` - JPA Entities mapping DB tables (User, Contact, Feedback, DirectMessage).
*   `com.scm.forms` - Validation structures.
*   `com.scm.helpers` - Shared sessions and context helpers.
*   `com.scm.repsitories` - Spring Data JPA Interfaces.
*   `com.scm.services` - Interface declarations and Implementations.
*   `src/main/resources/templates` - HTML views structured by context folders.

---

## 🎨 Excalidraw design links
[SCM Architecture Canvas](https://excalidraw.com/#json=SIuQrQnGQr9DGkCVc8BWD,JqVceoohF0UsTfllkzdRmw)

---

## ☁️ AWS Elastic Beanstalk Deployment

The application is deployed to **AWS Elastic Beanstalk** on a **Java SE platform (Corretto 21)** and connects to an external **Amazon RDS MySQL** database.

### 1. Build and Package instructions
To deploy the application, you need to compile tailwind styles, package the JAR, and create a deployment zip:
```powershell
# Compile Tailwind stylesheet
npx tailwindcss -i ./src/main/resources/static/css/input.css -o ./src/main/resources/static/css/output.css

# Build clean production executable JAR
./mvnw clean package -DskipTests

# Copy JAR to root folder
copy target\scm2.0-0.0.1-SNAPSHOT.jar scm2.0.jar

# Create flat ZIP file scm2.0-deployment.zip containing Procfile and scm2.0.jar
powershell -Command "Compress-Archive -Path Procfile, scm2.0.jar -DestinationPath scm2.0-deployment.zip -Force"
```

### 2. Environment Properties configuration in Elastic Beanstalk

Configure the following Environment properties on Elastic Beanstalk console under **Software/Configuration**:

| Key | Example Value | Description |
| :--- | :--- | :--- |
| `SERVER_PORT` | `5000` | Port on which Nginx reverse proxies traffic to Spring Boot |
| `SPRING_PROFILES_ACTIVE` | `prod` | Activates production configuration |
| `MYSQL_HOST` | `scm-db.cbq80wao2479.ap-south-1.rds.amazonaws.com` | RDS Endpoint host |
| `MYSQL_PORT` | `3306` | RDS database port |
| `MYSQL_DB` | `scm20` | Database name |
| `MYSQL_USER` | `admin` | RDS Master username |
| `MYSQL_PASSWORD` | `Pavan9580` | RDS Master password (case sensitive!) |
| `GOOGLE_CLIENT_ID` | `xxxx` | Google OAuth client ID |
| `GOOGLE_CLIENT_SECRET` | `xxxx` | Google OAuth client secret |
| `GITHUB_CLIENT_ID` | `xxxx` | GitHub OAuth client ID |
| `GITHUB_CLIENT_SECRET` | `xxxx` | GitHub OAuth client secret |
| `CLOUDINARY_API_KEY` | `xxxx` | Cloudinary API Key |
| `CLOUDINARY_API_SECRET` | `xxxx` | Cloudinary API Secret |
| `CLOUDINARY_CLOUD_NAME` | `xxxx` | Cloudinary Cloud Name |
| `MAIL_USERNAME` | `xxxx@gmail.com` | Gmail SMTP username |
| `MAIL_PASSWORD` | `xxxx` | Gmail App password |

### 3. Key Troubleshooting Steps
- **Database Auto-Creation**: The connection URL is configured with `?createDatabaseIfNotExist=true` to automatically create the database schema `scm20` on RDS if it doesn't exist during startup.
- **Port Matching**: AWS Elastic Beanstalk defaults to routing traffic to port `5000`. Spring Boot is configured to listen on port `5000` via `SERVER_PORT=5000` in the environment variables and `Procfile`.
- **OAuth Callback Configuration**: Make sure to whitelist the Elastic Beanstalk URL in the OAuth configurations:
  - Google: Add `http://<YOUR_EB_URL>/login/oauth2/code/google` to **Authorized redirect URIs**.
  - GitHub: Add `http://<YOUR_EB_URL>/login/oauth2/code/github` to **Authorization callback URL**.

---

## 🖥️ Desktop Executable Wrapper (Windows)

The AWS Elastic Beanstalk web application has been successfully packaged into a standalone Windows Desktop application using **Nativefier**.

### 1. Build and Package Desktop App
To rebuild the executable wrapper with a custom icon (Note: Windows needs a `.ico` format for the icon):
```powershell
npx nativefier --name "SmartContactManager" --platform "win32" --arch "x64" --icon "path/to/your/icon.ico" "http://scm-pavan-210129.ap-south-1.elasticbeanstalk.com/"
```
*Note: If script execution is disabled on your Windows PowerShell, use `npx.cmd` instead of `npx`:*
```powershell
npx.cmd --yes nativefier --name "SmartContactManager" --platform "win32" --arch "x64" --icon "c:\Users\Pavan Soni\Downloads\scm2.0-main_Final\scm2.0-main\src\main\resources\static\images\telephone.ico" "http://scm-pavan-210129.ap-south-1.elasticbeanstalk.com/"
```

### 2. Location of Built Files
The generated build outputs are located in:
`C:\Users\Pavan Soni\Downloads\scm2.0-main_Final\SmartContactManager-win32-x64\`

### 3. How to Run
Double-click the **`SmartContactManager.exe`** file inside the folder above. It will launch the application in a borderless app window without needing a browser tab.

> [!NOTE]
> Since this app is a wrapper for your AWS URL, an active internet connection is required on the user's machine to load and use the application.


