# LuxeLaundry - Mini Laundry Order Management System

LuxeLaundry is a lightweight, aesthetic, and functional Order Management System built for dry cleaning stores. It provides a seamless experience for creating orders, tracking status, and viewing business insights.

## 🚀 Setup Instructions

### Prerequisites
- **Java 17** or higher
- **Maven 3.6+**

### How to Run
1. **Clone the repository** (if applicable) or navigate to the project root.
2. **Build the project**:
   ```bash
   mvn clean install
   ```
3. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```
4. **Access the UI**:
   Open your browser and go to `http://localhost:8080`
5. **Login Credentials**:
   - **Username**: `admin`
   - **Password**: `admin`

## ✨ Features Implemented

1. **Dashboard Overview**:
   - Real-time statistics (Total Orders, Total Revenue, Processing count).
   - Recent orders quick-view table.
   - Interactive revenue charts and top customer analytics.
2. **Order Management**:
   - **Create Order**: Multi-item garment entry, automatic total calculation, and unique Order ID generation.
   - **Status Tracking**: Update status through an intuitive modal (RECEIVED → PROCESSING → READY → DELIVERED).
   - **Smart Search**: Live search by Customer Name, Phone Number, or **Garment Type** (Bonus Task).
3. **Aesthetic UI**:
   - Modern "Glassmorphism" design using Tailwind CSS.
   - Interactive icons using Lucide.
   - Responsive layout with smooth animations.
4. **Backend Security & API**:
   - Basic Authentication implemented for all API endpoints (Bonus Task).
   - H2 In-memory database for rapid testing (Bonus Task).
   - **Interactive API Docs**: Built-in Swagger UI available at `/swagger-ui/index.html`.

## 🤖 AI Usage Report

### Tools Used
- **Gemini CLI (Primary)**: Used for scaffolding, business logic implementation, and UI design.
- **Tailwind CSS / Lucide**: For aesthetic styling.

### Sample Prompts
- *"Create a Spring Boot project with JPA, Security, and H2 for a Laundry management system."*
- *"Implement a Service layer that calculates total bill and generates unique IDs like ORD-123456."*
- *"Design a cool, aesthetic frontend using Tailwind CSS with a dashboard and a sidebar."*
- *"Update the JPA repository to allow searching orders by garment names inside the items list."*

### What AI Got Wrong & Improvements (Post-Audit)
- **Nested Validation Bug**: AI initially skipped `@Valid` on the garment items list in `OrderRequest`. I manually added this to ensure quantities and prices are validated.
- **Error Handling (500 vs 404)**: AI defaulted to throwing `RuntimeException` for missing orders, causing 500 errors. I refactored this to use `NoSuchElementException` and added a `GlobalExceptionHandler` to return a clean **404 Not Found**.
- **Security Hardcoding**: AI hardcoded admin credentials in the security config. I moved these to `application.properties` with `@Value` injection for better environment management.
- **Business Logic Edge Cases**: AI didn't handle negative discount percentages. I added clamping logic (`Math.max(0, ...)` and `Math.min(100, ...)`) to protect revenue.
- **Security Frames**: H2 Console was blocked by Spring Security frame options. I manually added `headers.frameOptions().disable()` to the security config.
- **Port Conflicts**: Initial AI config used port 8081, but I standardized it to 8080 to match Docker and standard Spring Boot conventions.

## 🔹 Tradeoffs

### Tradeoffs
- **In-Memory Storage**: Used H2 in-memory for ease of demonstration. A production app would use PostgreSQL or MySQL.
- **Basic Auth**: Used simple in-memory users. OAuth2 or JWT would be preferred for a real-world multi-user application.
- **Floating Point Math**: Used `Double` for rapid prototyping. In a large-scale financial app, `BigDecimal` would be used to prevent precision loss.

### Future Improvements
- **Automated Notifications**: Send SMS/WhatsApp notifications to customers when status changes to "READY".
- **Printable Invoices**: Add a button to generate PDF bills.
- **Deployment**: Ready for deployment on Railway or Render via Docker.

---


