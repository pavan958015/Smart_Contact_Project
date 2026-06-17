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
