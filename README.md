# 💰 Expense Tracker API

A production-grade REST API for personal expense management built with Spring Boot, featuring JWT authentication, real-time budget alerts, and spending analytics.

## 🚀 Live API
**Base URL:** https://expense-tracker-api-qgto.onrender.com

> Note: Free tier — first request may take 30-50 seconds to wake up.

## 🛠 Tech Stack
- **Backend:** Java 17, Spring Boot 4
- **Security:** Spring Security + JWT
- **Database:** MySQL 8 (Railway)
- **ORM:** Spring Data JPA + Hibernate
- **Testing:** JUnit 5 + Mockito (9 tests)
- **Deploy:** Docker + Render.com

## ✅ Features
- [x] User registration and JWT login
- [x] Expense CRUD with category tagging
- [x] Filter expenses by date range
- [x] Monthly budget management per category
- [x] Real-time budget breach alert at 80% threshold
- [x] Monthly analytics — total, top category, daily average
- [x] 6-month spending trend analysis
- [x] Global exception handling
- [x] 9 unit tests passing

## 📌 API Endpoints

### Auth (no token needed)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/v1/auth/register | Register new user |
| POST | /api/v1/auth/login | Login → get JWT token |

### Expenses (token required)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/v1/expenses | Get all expenses |
| POST | /api/v1/expenses | Add expense |
| PUT | /api/v1/expenses/{id} | Update expense |
| DELETE | /api/v1/expenses/{id} | Delete expense |

### Budget (token required)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/v1/budgets | Set monthly budget |
| GET | /api/v1/budgets/status | Budget vs actual + alert |

### Analytics (token required)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/v1/analytics/monthly | Monthly summary |
| GET | /api/v1/analytics/trends | 6-month trends |

## 🔐 How to Use

**Step 1 — Register:**
POST https://expense-tracker-api-qgto.onrender.com/api/v1/auth/register
{
"name": "Your Name",
"email": "you@gmail.com",
"password": "123456"
}
**Step 2 — Login and get token:**
POST https://expense-tracker-api-qgto.onrender.com/api/v1/auth/login
{
"email": "you@gmail.com",
"password": "123456"
}
**Step 3 — Use token in all other requests:**
Authorization: Bearer eyJhbGci...
## 📊 Sample Response — Budget Alert

```json
{
  "category": "Food",
  "budgetAmount": 500,
  "spentAmount": 450,
  "remainingAmount": 50,
  "percentUsed": 90.00,
  "alert": true,
  "alertMessage": "⚠️ Warning: You have used 90.00% of your Food budget!"
}
```

## 🧪 Tests
✅ ExpenseServiceTest — 5 tests
✅ BudgetServiceTest  — 4 tests
Total: 9 unit tests

## ⚙️ Local Setup

```bash
# 1. Clone
git clone https://github.com/namita339/expense-tracker-api.git
cd expense-tracker-api/expense-tracker

# 2. Create MySQL database
CREATE DATABASE expense_tracker_db;

# 3. Configure application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/expense_tracker_db
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
jwt.secret=mySecretKey12345678901234567890123456789012
jwt.expiration=86400000

# 4. Run
./mvnw spring-boot:run
```

## 📁 Project Structure
src/main/java/com/namita/expense_tracker/
├── config/       → Security config
├── controller/   → REST endpoints
├── service/      → Business logic
├── repository/   → Database layer
├── model/        → JPA entities
├── security/     → JWT filter
└── exception/    → Error handling
## 👩‍💻 Author
**Namita Nandan** — B.Tech CSE, KIIT University

[GitHub](https://github.com/namita339)