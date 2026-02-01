***

# Spring Boot Security Login – Template

![Java](https://img.shields.io/badge/Java%2017-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![JPA Hibernate](https://img.shields.io/badge/JPA%20%2F%20Hibernate-59666C?style=for-the-badge&logo=hibernate&logoColor=white)
![SMTP Mail](https://img.shields.io/badge/SMTP%20Mail-EA4335?style=for-the-badge&logo=gmail&logoColor=white)
![i18n](https://img.shields.io/badge/i18n-444444?style=for-the-badge&logo=googletranslate&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![License](https://img.shields.io/badge/License-UNLICENSED-green?style=for-the-badge)

A minimal template to start a **Spring Boot** application with **secure authentication**, **i18n**, **REST API**, **session & uage cookies**, and **email integration**.

## Screenshots

### Login Screen

<img width="391" height="343" alt="Screenshot 2026-02-01 at 14 50 46" src="https://github.com/user-attachments/assets/fff9dfd7-8994-4ad3-a06d-6d31c64a957a" />

### Register Screen

<img width="471" height="535" alt="Screenshot 2026-02-01 at 14 51 37" src="https://github.com/user-attachments/assets/6db954aa-bae4-4549-bf8e-a4751f8315e6" />

### Forgot password Screen

<img width="386" height="251" alt="Screenshot 2026-02-01 at 14 55 16" src="https://github.com/user-attachments/assets/9a2a6a84-4e1a-47d6-9724-108ab473ce01" />

### Secure page Screen

<img width="641" height="306" alt="Screenshot 2026-02-01 at 14 57 36" src="https://github.com/user-attachments/assets/54462215-7c38-4443-b5b0-773ebf65a3f4" />

***

## 🏗️ Architecture

*   Spring Boot (MVC)
*   Spring Security
*   JPA/Hibernate
*   Controllers / Services / Repositories / DTO
*   i18n + language cookie
*   Email sending (SMTP)

***

## 🧰 Technologies

*   Java JDK 17+
*   Spring Boot 3+
*   Spring Security
*   Spring Data JPA
*   Spring Mail

***

## 📦 Requirements

*   Java JDK 17
*   Maven
*   PostgreSQL or MySQL
*   SMTP email account with **application password** (for 2FA accounts)

***

## 🚀 Installation

```bash
git clone https://github.com/clementvenot/spring-boot-security-login-template.git
cd spring-boot-security-login-template
./mvnw spring-boot:run
```

Backend default port: **8080**  
Frontend (optional): **8081**

***

## ⚙️ Configuration (`application.properties`)

### Database

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/db_name
spring.datasource.username=db_user
spring.datasource.password=db_password
```

### Email (SMTP)

```properties
spring.mail.host=smtp.gmail.com
spring.mail.username=your.email@gmail.com
spring.mail.password=APP_PASSWORD   # not your regular password
```

### i18n & Language Cookie

*   Messages stored in `messages.properties`
*   `locale` cookie automatically updated using `?=en`, `?=fr`, etc.

***

## 🔌 REST API 

*   `POST /api/auth/login`
*   `POST /api/auth/register`
*   `POST /api/auth/logout`
*   `POST /api/auth/reset-password`
*   `POST /api/auth/forgot-password`

***

## 🔐 Security

*   Spring Security (form login)
*   Session authentication (`access_token`)
*   BCrypt password hashing
*   CSRF enabled
*   Protected REST endpoints

***

## 📦 Deployment

*   Build as executable JAR
*   Use environment variables for prod config
*   Recommended: deploy behind reverse proxy (Nginx/Caddy) with HTTPS

***

## 📜 License

There is no license; you're free to use it.

---

Feel free to contribute to this project by submitting issues or pull requests.

For any questions or support, please contact [Clément Venot](mailto:clement.venooot@gmail.com).

---
