# SYOS Point of Sale System

## Synex Outlet Store - Billing & Stock Management System

### Academic Project - Clean Architecture Implementation

---

## 📋 Project Overview

This is a **production-quality**, console-based billing and stock management system for a grocery store, built as an academic project demonstrating **Clean Architecture**, **SOLID principles**, and **design patterns** in Java.

### Key Features

- ✅ In-store billing (POS transactions)
- ✅ Online sales management with customer details
- ✅ Batch-aware and expiry-aware stock management
- ✅ FEFO/FIFO stock selection strategies
- ✅ Flexible discount strategies
- ✅ Comprehensive reporting (daily sales, stock status)
- ✅ SQLite database persistence
- ✅ Full test coverage with JUnit 5

---

## 🏗️ Architecture

This project strictly follows **Clean Architecture** with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                     Presentation Layer                       │
│                    (Console Interface)                       │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────┴─────────────────────────────────┐
│                     Application Layer                        │
│         (Use Cases, Services, Strategies, Builders)          │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────┴─────────────────────────────────┐
│                       Domain Layer                           │
│          (Entities, Value Objects, Repositories)             │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────┴─────────────────────────────────┐
│                   Infrastructure Layer                       │
│              (SQLite Repositories, Database)                 │
└─────────────────────────────────────────────────────────────┘
```

### Layer Responsibilities

#### 1. **Domain Layer** (`com.syos.domain`)

- **Entities**: `Product`, `StockBatch`, `Bill`, `BillItem`
- **Value Objects**: `Money`, `ProductId`, `BatchNumber`, `BillNumber`
- **Repository Interfaces**: Define contracts for data persistence

#### 2. **Application Layer** (`com.syos.application`)

- **Use Cases**: `CheckoutCommand`, `AddProductUseCase`, `AddStockUseCase`
- **Strategies**: `DiscountStrategy`, `StockSelectionStrategy` (FEFO, FIFO)
- **Services**: `BillNumberGenerator` (Singleton)
- **Factories**: `ProductFactory`, `StockBatchFactory`
- **Builders**: `BillBuilder`
- **Reports**: `DailySalesReport`, `StockStatusReport` (Template Method)

#### 3. **Infrastructure Layer** (`com.syos.infrastructure`)

- **Repositories**: SQLite implementations using JDBC
- **Database**: `DatabaseManager` (Singleton) for connection management

#### 4. **Presentation Layer** (`com.syos.presentation`)

- **Console UI**: `SYOSApplication` - main entry point with demo scenarios

---

## 🎨 Design Patterns Used

| Pattern             | Usage                                  | Location                                 |
| ------------------- | -------------------------------------- | ---------------------------------------- |
| **Repository**      | Data persistence abstraction           | `domain.repository.*`                    |
| **Factory**         | Object creation for Products & Stock   | `application.factory.*`                  |
| **Builder**         | Complex Bill construction              | `application.builder.BillBuilder`        |
| **Strategy**        | Discount calculation & stock selection | `application.strategy.*`                 |
| **Command**         | Checkout transaction execution         | `application.usecase.CheckoutCommand`    |
| **Singleton**       | Bill number generation, DB management  | `BillNumberGenerator`, `DatabaseManager` |
| **Template Method** | Report generation workflow             | `application.report.ReportGenerator`     |

---

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+

### Build the Project

```bash
mvn clean install
```

### Run Tests

```bash
mvn test
```

### Run the Application

```bash
mvn exec:java
```

Or compile and run directly:

```bash
mvn clean compile
java -cp target/classes:~/.m2/repository/org/xerial/sqlite-jdbc/3.45.1.0/sqlite-jdbc-3.45.1.0.jar com.syos.presentation.console.SYOSApplication
```

---

## 📊 Database Schema

The system uses SQLite with the following schema:

### Tables

- **products**: Product catalog (id, name, category, unit_price, unit)
- **stock_batches**: Stock inventory (batch_number, product_id, quantity, expiry_date, received_date)
- **bills**: Transaction headers (bill_number, timestamp, sale_type, totals, customer info)
- **bill_items**: Transaction line items (product details, quantities, prices, batch references)

---

## 🧪 Testing

Comprehensive JUnit 5 test suite covering:

### Test Coverage

- ✅ **Domain Logic**: Value objects, entities, business rules
- ✅ **Discount Strategies**: No discount, percentage, threshold
- ✅ **Stock Selection**: FIFO, FEFO, expiry handling
- ✅ **Bill Building**: In-store, online, validation
- ✅ **Singleton Behavior**: Bill number generation
- ✅ **Calculations**: Money operations, totals, discounts

### Run Tests

```bash
mvn test
```

All tests are **independent**, **repeatable**, and do not rely on console output.

---

## 📝 Sample Output

When you run the application, you'll see:

```
================================================================================
                    SYNEX OUTLET STORE (SYOS)
               Billing & Stock Management System
================================================================================

>>> INITIALIZING SYSTEM <<<

Adding products to catalog...
✓ Added 5 products to catalog

Adding stock batches...
✓ Added 6 stock batches
✓ System initialized successfully

>>> SCENARIO 1: IN-STORE TRANSACTION <<<

Processing in-store sale at POS terminal...

Customer Cart:
  - Basmati Rice (P001): 5 kg
  - Full Cream Milk (P002): 3 liters
  - Whole Wheat Bread (P003): 2 loaves

--------------------------------------------------------------------------------
                         SYNEX OUTLET STORE
                    GST No: 29XXXXX1234X1Z5
--------------------------------------------------------------------------------
Bill No: POS-20260201-00001
Date/Time: 2026-02-01T10:30:45.123
Type: IN_STORE
--------------------------------------------------------------------------------
Item                           Qty        Rate         Amount
--------------------------------------------------------------------------------
Basmati Rice                   5          Rs. 85.00    Rs. 425.00
  Batch: B001
Full Cream Milk                3          Rs. 65.00    Rs. 195.00
  Batch: B003
Whole Wheat Bread              2          Rs. 45.00    Rs. 90.00
  Batch: B004
--------------------------------------------------------------------------------
Subtotal:                                              Rs. 710.00
Discount:                                              Rs. 0.00
--------------------------------------------------------------------------------
TOTAL:                                                 Rs. 710.00
--------------------------------------------------------------------------------
                         Thank you for shopping!
--------------------------------------------------------------------------------

>>> SCENARIO 2: ONLINE TRANSACTION <<<

[Similar output for online order with customer details]

>>> GENERATING REPORTS <<<

[Daily Sales Report and Stock Status Report displayed]
```

---

## 🎓 Academic Evaluation Points

### Clean Code Principles ✅

- Meaningful names following conventions
- Small, focused methods (Single Responsibility)
- Minimal comments (self-documenting code)
- Consistent formatting and structure

### SOLID Principles ✅

- **S**ingle Responsibility: Each class has one reason to change
- **O**pen/Closed: Strategies allow extension without modification
- **L**iskov Substitution: Repository implementations are interchangeable
- **I**nterface Segregation: Small, focused interfaces
- **D**ependency Inversion: Dependencies on abstractions, not implementations

### Design Patterns ✅

- 7 patterns naturally integrated into the architecture
- Justified usage based on requirements
- Not forced or over-engineered

### Testing ✅

- Comprehensive JUnit 5 test suite
- Independent, repeatable tests
- High coverage of business logic
- No console output dependencies

---

## 📂 Project Structure

```
SYOS_POS/
├── pom.xml
├── README.md
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── syos/
│   │               ├── domain/
│   │               │   ├── entity/
│   │               │   ├── valueobject/
│   │               │   └── repository/
│   │               ├── application/
│   │               │   ├── builder/
│   │               │   ├── factory/
│   │               │   ├── report/
│   │               │   ├── service/
│   │               │   ├── strategy/
│   │               │   └── usecase/
│   │               ├── infrastructure/
│   │               │   ├── database/
│   │               │   └── persistence/
│   │               └── presentation/
│   │                   └── console/
│   └── test/
│       └── java/
│           └── com/
│               └── syos/
│                   ├── domain/
│                   └── application/
└── syos.db (generated at runtime)
```

---

## 🔧 Technology Stack

- **Language**: Java 17
- **Build Tool**: Maven
- **Database**: SQLite 3.45.1.0 (via JDBC)
- **Testing**: JUnit 5.10.1
- **Architecture**: Clean Architecture
- **Principles**: SOLID, DRY, KISS

---

## 📖 Key Concepts Demonstrated

### Domain-Driven Design

- Rich domain model with entities and value objects
- Ubiquitous language throughout codebase
- Business logic encapsulated in domain layer

### Clean Architecture

- Dependency rule strictly enforced
- Framework independence
- Testability at every layer

### Design Patterns

- Multiple patterns working together cohesively
- Natural emergence from design, not forced
- Clear justification for each pattern

### Testing Strategy

- Unit tests for domain logic
- Strategy pattern validation
- Builder pattern verification
- Singleton behavior testing

---

## 👨‍🎓 For Academic Evaluation

This project demonstrates:

1. ✅ **Architecture**: Clean Architecture with proper layer separation
2. ✅ **Design Patterns**: 7 patterns naturally integrated
3. ✅ **SOLID Principles**: Applied throughout the codebase
4. ✅ **Clean Code**: Self-documenting, maintainable code
5. ✅ **Testing**: Comprehensive JUnit 5 test coverage
6. ✅ **Persistence**: SQLite with JDBC (no frameworks)
7. ✅ **Console Interface**: Professional, user-friendly output
8. ✅ **Domain Modeling**: Rich entities and value objects
9. ✅ **Business Logic**: Batch management, expiry handling, discount calculation
10. ✅ **Production Quality**: Ready for demo, viva, and evaluation

---

## 📄 License

This is an academic project for educational purposes.

---

## 👤 Author

Developed for academic evaluation at university level.

**Date**: February 2026

---

## 🔍 Viva Preparation Questions

### Architecture

- Q: Why Clean Architecture?
- A: Separation of concerns, testability, maintainability, framework independence

### Design Patterns

- Q: Why Strategy pattern for discounts?
- A: Allows runtime selection of discount algorithms without modifying client code (Open/Closed Principle)

### Domain Model

- Q: Why value objects?
- A: Immutability, type safety, encapsulation of validation logic

### Testing

- Q: How are tests independent?
- A: Each test sets up its own fixtures, no shared state, no database dependencies in domain tests

---

## 🎯 Demo Scenarios

The application includes two pre-configured scenarios:

1. **In-Store Transaction**: Walk-in customer purchases items, FEFO stock selection
2. **Online Transaction**: Remote order with customer details, threshold discount applied

Both scenarios demonstrate:

- Stock reduction from appropriate batches
- Discount strategy application
- Bill generation with proper formatting
- Persistence to SQLite database
- Report generation with comprehensive data

---

**End of README**
