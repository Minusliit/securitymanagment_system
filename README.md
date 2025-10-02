Security Management System (VIMS)
=================================

Requirements
------------
- JDK 22
- Maven 3.9+

Run (H2 In-Memory)
------------------
1. Build and run:
   ```bash
   mvn spring-boot:run
   ```
2. Open `http://localhost:8080/login`
3. Seed users:
   - `secmanager` / `Password1` (ROLE_SECURITY_MANAGER)
   - `admin` / `Password1` (ROLE_ADMIN)
   - `user` / `Password1` (ROLE_USER)

MySQL Setup (optional)
----------------------
1. Create database:
   ```sql
   CREATE DATABASE vims_security CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
2. Update `src/main/resources/application-mysql.properties` with your MySQL credentials.
3. Run with the mysql profile:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=mysql
   ```

Features
--------
- Spring Security with form login, BCrypt passwords, RBAC
- 2FA (TOTP) for sensitive routes via simple session gate
- Password policy (configurable at runtime via `/policy`)
- Login attempt tracking and breach detection (>=5 failures/hour)
- File upload monitoring (PDF/JPG only, 5MB limit)
- Security dashboard (`/dashboard`) for Security Manager/Admin

Testing
-------
```bash
mvn test
```

Notes
-----
- This module is backend-focused with simple Thymeleaf views.
- Policies set via `/policy` are in-memory and reset on restart. Persist if needed.


