# Employee Records Management System

A comprehensive employee management system built with Spring Boot backend and Java Swing frontend.

## Project Overview
Employee Records Management System (ERMS) is a desktop application for managing employee data, departments, and user access control. It features role-based access, employee CRUD operations, and department management.

## Project Structure
```
employee-records-management-system/
├── employee-management-backend/    # Spring Boot backend
├── erms-frontend/                 # Java Swing frontend
└── postman/                       # API collection & environments
```
## API Documentation with Swagger
Access interactive API documentation:
```bash
http://localhost:8080/swagger-ui/index.html
```
## Tech Stack

### Backend
- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA
- Oracle Database
- Maven

### Frontend  
- Java Swing
- MigLayout
- Maven

## Prerequisites
- Java 17 or higher
- Maven
- Oracle Database
- IDE (IntelliJ IDEA recommended)

## Setup and Installation

### Backend Setup
1. Clone repository
2. Navigate to backend directory:
```bash
cd employee-management-backend
```

3. Configure database in `application.properties`:
```properties
spring.datasource.url=jdbc:oracle:thin:@localhost:1521/XEPDB1
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# Oracle specific JPA properties
spring.jpa.database-platform=org.hibernate.dialect.OracleDialect
spring.jpa.hibernate.ddl-auto=update
```

4. Run backend:
```bash
mvn spring-boot:run
```

### Frontend Setup
1. Navigate to frontend directory:
```bash
cd erms-frontend
```

2. Run frontend:
```bash
mvn exec:java
```

## API Documentation

### Authentication
- POST `/api/v1/users/login` - User login
- GET `/api/v1/users` - Get all users (Admin only)
- POST `/api/v1/users` - Create user (Admin only)
- PUT `/api/v1/users/{id}` - Update user
- DELETE `/api/v1/users/{id}` - Delete user

### Employees
- GET `/api/v1/employees` - List employees
- POST `/api/v1/employees` - Create employee
- PUT `/api/v1/employees/{id}` - Update employee
- DELETE `/api/v1/employees/{id}` - Delete employee

### Departments
- GET `/api/v1/departments` - List departments
- POST `/api/v1/departments` - Create department
- PUT `/api/v1/departments/{id}` - Update department
- DELETE `/api/v1/departments/{id}` - Delete department

## User Roles and Permissions
- **Admin**: Full system access, including configuration settings and managing user permissions
- **HR**: Employee and department management, excluding system settings
- **Manager**: Can view and update specific details for employees within their department

## Testing

### Backend Testing
```bash
cd employee-management-backend
mvn test
```

### Frontend Testing
```bash
cd erms-frontend
mvn test
```

## Postman Testing
1. Import the collection from `postman/` directory
2. Import environments:
   - Admin Environment (full access)
   - HR Environment (limited access)
   - Manager Environment (department-specific access)
3. Use environment selector to switch between roles

## Default Credentials
```
Admin:
- Username: admin
- Password: admin123
```

## Building for Production

### Backend
```bash
cd employee-management-backend
mvn clean package
```

### Frontend
```bash
cd erms-frontend
mvn clean package
```

## Common Issues & Troubleshooting
1. Database connection issues: 
   - Verify Oracle service is running
   - Check TNS listener status
   - Verify SID/Service name is correct
   - Confirm user privileges
2. Port conflicts: Ensure port 8080 is available for backend
3. Authentication issues: Check credentials and verify backend URL configuration
