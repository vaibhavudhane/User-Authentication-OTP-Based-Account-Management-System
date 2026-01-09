# User-Authentication-OTP-Based-Account-Management-System

🌟 User Authentication & OTP-Based Account Management System

A secure, scalable backend application built using Java & Spring Boot that provides JWT-based authentication, OTP verification, and role-based access control.
This project demonstrates real-world backend practices such as layered architecture, clean code structure, centralized exception handling, and secure authentication flows.

------------------------------------------------------------

FEATURES

Authentication & Security
- User Registration & Login
- JWT Token-based Authentication
- OTP-based Account Verification
- Role-based Authorization
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
Database: MySQL
Build Tool: Maven
Boilerplate Reduction: Lombok
API Testing: Postman
API Documentation: Swagger (SpringDoc)

------------------------------------------------------------

PROJECT STRUCTURE

src/main/java
 ├── config
 ├── controller
 ├── dto
 ├── entity
 ├── enums
 ├── exceptions
 ├── repository
 ├── service
 └── util

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

POST   /auth/register     → Register new user
POST   /auth/login        → Login & receive JWT
POST   /auth/send-otp     → Send OTP
POST   /auth/verify-otp   → Verify OTP
GET    /user/profile      → Fetch user profile (secured)

------------------------------------------------------------

SECURITY NOTES

- JWT is used for stateless authentication
- Sensitive configuration is ignored using .gitignore
- Passwords are encrypted
- OTP-based verification ensures extra security
- Example config file is provided

------------------------------------------------------------

WHY I BUILT THIS PROJECT

- Strengthen Spring Boot backend skills
- Implement real-world authentication flows
- Practice JWT & OTP systems
- Write production-style code
- Prepare for backend developer roles

------------------------------------------------------------

ABOUT ME

Name: Vaibhav Udhane
Role: Backend Java Developer
Experience: 10 months as a Core Java Trainer
Skills:
- Core Java
- DSA
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
