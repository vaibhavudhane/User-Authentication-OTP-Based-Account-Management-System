# CypherFlow – Secure Authentication & Identity Platform

🚀 CypherFlow – Secure Authentication & Identity Platform

CypherFlow is a production-grade identity platform built with Java and Spring Boot that provides secure authentication, OTP-based verification, account recovery, and role-aware access control.
It is designed to simulate how real-world authentication systems work in modern applications — focusing on security, scalability, and clean architecture.

------------------------------------------------------------

🌟 Why CypherFlow?

Most demo authentication projects only cover login and registration.
CypherFlow goes much further.
It includes:
- Multi-step OTP verification flows
- Account recovery mechanisms
- Role-based authorization
- Account blocking and unblocking
- Secure password reset workflows
- Production-style API structure
- Clean layered architecture
This makes CypherFlow closer to a real identity platform than a sample app.

------------------------------------------------------------

🔐 Core Features

1) Authentication & Identity
- User registration and login
- JWT-based authentication
- Stateless session management
- Role-based access control (USER, ADMIN)

2) OTP & Verification
- Email and mobile OTP support
- OTP verification for sensitive actions
- OTP-based account activation
- OTP-based account unblocking

3) Account Recovery
- Forgot username flow
- Forgot password flow
- Secure password reset
- Token-based recovery

4) Security
- Encrypted password storage
- JWT token validation
- Protected routes
- Centralized exception handling

------------------------------------------------------------

🏗 Architecture

CypherFlow follows a layered backend architecture:
Controller → Service → Repository → Database

Supporting layers:
- DTOs for request/response isolation
- Custom exceptions
- Security filters
- Utility classes

This ensures:
- Separation of concerns
- Maintainability
- Scalability
- Testability

------------------------------------------------------------

🛠 Tech Stack

| Category              | Technology           |
| --------------------- | -------------------- |
| Language              | Java                 |
| Framework             | Spring Boot          |
| Security              | Spring Security, JWT |
| ORM                   | Hibernate / JPA      |
| Database              | PostgreSQL           |
| Build Tool            | Maven                |
| Boilerplate Reduction | Lombok               |
| API Testing           | Postman              |

------------------------------------------------------------

📦 Project Structure

src/main/java
 - config
 - controller
 - dto
 - entity
 - enums
 - exceptions
 - repository
 - service
 - util

------------------------------------------------------------

SETUP INSTRUCTIONS

1. Clone the Repository
git clone https://github.com/vaibhavudhane/vaibhavudhane/CypherFlow.git
cd CypherFlow

2. Configure Application
Copy:
src/main/resources/application.properties.example

Rename to:
application.properties

Then update:
spring.datasource.url=jdbc:mysql://localhost:3306/your_db
spring.datasource.username=your_username
spring.datasource.password=your_password
jwt.secret=your_secret_key

3. Build & Run
mvn clean install
mvn spring-boot:run

Application runs on:
http://localhost:8080

------------------------------------------------------------

API ENDPOINTS

🔐 Authentication & Account APIs

| Method | Endpoint                             | Auth   | Description                     |
| ------ | ------------------------------------ | ------ | ------------------------------- |
| POST   | /api/auth/register                   | Public | Register new user               |
| POST   | /api/auth/verify-registration-otp    | Public | Verify OTP and activate account |
| POST   | /api/auth/login                      | Public | Login and receive JWT           |
| POST   | /api/auth/send-otp                   | Public | Send OTP                        |
| POST   | /api/auth/verify-otp                 | Public | Verify OTP                      |
| POST   | /api/auth/forgot-username            | Public | Recover username                |
| POST   | /api/auth/forgot-password            | Public | Initiate password reset         |
| POST   | /api/auth/reset-password             | Public | Reset password                  |
| POST   | /api/auth/block-account              | Public | Block account using token       |
| POST   | /api/auth/unblock-account/send-otp   | Public | Send OTP for account unblock    |
| POST   | /api/auth/unblock-account/verify-otp | Public | Verify OTP and unblock          |

👤 User Profile APIs
Base Path: /api/v1/profile
| Method | Endpoint                           | Auth       | Description                |
| ------ | ---------------------------------- | ---------- | -------------------------- |
| GET    | /api/v1/profile/me                 | JWT (USER) | Get logged-in user profile |
| PUT    | /api/v1/profile/update             | JWT (USER) | Update profile             |
| POST   | /api/v1/profile/uploadProfilePhoto | JWT (USER) | Upload profile photo       |

🛡 Admin APIs
Base Path: /api/admin/users
| Method | Endpoint                            | Auth        | Description               |
| ------ | ----------------------------------- | ----------- | ------------------------- |
| DELETE | /api/admin/users/delete?identifier= | JWT (ADMIN) | Delete user by identifier |

------------------------------------------------------------

🔒 Security Philosophy

CypherFlow is designed with security-first thinking:
- Stateless JWT authentication
- No session storage
- Encrypted passwords and OTP
- OTP verification for sensitive actions
- No secrets committed to GitHub
- Environment-based configuration
- Role-based authorization

------------------------------------------------------------

🧪 Example Requests

1) LOGIN
- To Strengthen Spring Boot backend skills
- POST /api/auth/login
- Content-Type: application/json
- {
  "username": "vaibhav",
  "password": "Password@123"
  }

2) Access Protected Profile
- GET /api/v1/profile/me
- Authorization: Bearer <JWT_TOKEN>

3) Send OTP
- POST /api/auth/send-otp
- Content-Type: application/json
- {
  "username": "vaibhav",
  "type": "EMAIL",
  "reason": "LOGIN"
  }

------------------------------------------------------------

🛣 Roadmap

Future enhancements planned:
- Refresh tokens
- OAuth2 login (Google/GitHub)
- Device-based trust management
- Redis-backed OTP storage
- Docker support

------------------------------------------------------------

🎯 Why This Project Exists

CypherFlow was built to:
- Understand real-world identity systems
- Implement production-grade security
- Practice scalable backend design
- Build a serious backend portfolio
- Prepare for product-based companies

------------------------------------------------------------

👨‍💻 About the Developer

Vaibhav Udhane
Backend Java Developer

Skills:
- Core Java
- OOPs
- Collections
- Spring Boot
- Spring Security
- Spring Data JPA
- REST APIs
- SQL

Experience:
- 10 months as a Core Java Trainer

------------------------------------------------------------

📬 Contact

Email: vaibhavudhane1@gmail.com
GitHub: https://github.com/vaibhavudhane
Location: Pune, Maharashtra

------------------------------------------------------------

⭐ If you like CypherFlow, give it a star!
