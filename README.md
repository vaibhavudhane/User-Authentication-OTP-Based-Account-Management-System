# User-Authentication-OTP-Based-Account-Management-System

🌟 User Authentication & OTP-Based Account Management System

A secure, scalable backend system built with Java & Spring Boot that provides JWT-based authentication, OTP verification, account recovery, and role-based access control.
This project demonstrates real-world backend engineering practices including layered architecture, centralized exception handling, secure authentication flows, and clean API design.

------------------------------------------------------------

FEATURES

Authentication & Security
- User Registration & Login
- JWT-based authentication
- OTP-based verification (Email & Mobile)
- Account blocking & unblocking
- Forgot username & password recovery
- Secure password reset flow
- Role-based authorization (USER, ADMIN)
- Secure Password Encryption
- Protected Endpoints

Backend Architecture
- Layered Architecture (Controller → Service → Repository)
- DTO-based Request/Response Handling
- Centralized Exception Handling
- Input Validation
- Enums for constants
- Clean Code Organization

------------------------------------------------------------

TECH STACK

Language: Java
Framework: Spring Boot
Security: Spring Security, JWT
ORM: Hibernate / JPA
Database: PostgreSQL
Build Tool: Maven
Boilerplate Reduction: Lombok
API Testing: Postman

------------------------------------------------------------

PROJECT STRUCTURE

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

git clone https://github.com/vaibhavudhane/User-Authentication-OTP-Based-Account-Management-System.git
cd User-Authentication-OTP-Based-Account-Management-System

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

🧪 Example Requests

LOGIN
POST /api/auth/login
Content-Type: application/json
{
  "username": "vaibhav",
  "password": "Password@123"
}

Access Protected Profile
GET /api/v1/profile/me
Authorization: Bearer <JWT_TOKEN>

Send OTP
POST /api/auth/send-otp
Content-Type: application/json
{
  "username": "vaibhav",
  "type": "EMAIL",
  "reason": "LOGIN"
}

------------------------------------------------------------

🔒 SECURITY NOTES

- JWT is used for stateless authentication
- Sensitive configuration is ignored using .gitignore
- Passwords are encrypted
- OTP-based verification ensures extra security
- Example config file is provided

------------------------------------------------------------

🎯 WHY I BUILT THIS PROJECT

- To Strengthen Spring Boot backend skills
- To implement real-world authentication flows
- To understand JWT-based security
- To practice OTP-based workflows
- To write production-grade backend code

------------------------------------------------------------

👨‍💻 ABOUT ME

Name: Vaibhav Udhane
Role: Backend Java Developer
Experience: 10 months as a Core Java Trainer
Skills:
- Core Java
- OOPs
- Collections
- Spring Boot
- REST APIs
- SQL

------------------------------------------------------------

CONTACT

Email: vaibhavudhane1@gmail.com
GitHub: https://github.com/vaibhavudhane
Location: Pune, Maharashtra

------------------------------------------------------------

If you like this project, give it a ⭐ on GitHub!
