# LuxeLaundry - Mini Laundry Order Management System

LuxeLaundry is a lightweight, aesthetic, and functional Order Management System built for dry cleaning stores. It provides a seamless experience for creating orders, tracking status, and viewing business insights.

## Setup Instructions

### Prerequisites
- **Java 17** (Temurin recommended)
- **Maven 3.6+**
- **Docker** (optional, for containerized run)

### How to Run Locally
1. **Clone the repository** or navigate to the project root.
2. **Build the project**:
   ```bash
   mvn clean install
   ```
3. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```
4. **Access the UI**:
   Open your browser and go to `http://localhost:8080` (or the port specified in your environment)
5. **Login Credentials**:
   - **Username**: `admin`
   - **Password**: `admin`

## ✨ Features Implemented

1. **Intelligent Dashboard**:
   - Actionable metrics (Ready to Collect, In Progress, Priority, Net Revenue).
   - Dynamic charts: **Revenue By Category** (Bar) and **Service Distribution** (Doughnut).
   - Elite Customer tracking based on lifetime spend.
2. **Order Lifecycle Management**:
   - **New Booking**: Streamlined entry with auto-opening receipts for immediate printing.
   - **Pricing Library**: Save garment presets with default rates to speed up booking.
   - **Order Repository**: Centralized hub to search, manage status, and **Delete Orders**.
   - **Print-Ready Receipts**: Specialized CSS for professional, distraction-free receipt printing.
3. **Smart Search & UI**:
   - Advanced search by Name, ID, or **Garment Category**.
   - Modern "Glassmorphism" UI with dynamic sidebar status and personalized initials.
   - Real-time bill calculation with priority surcharges and discount clamping.
4. **DevOps & Stability**:
   - **Render Ready**: Optimized `Dockerfile` using multi-stage builds and `eclipse-temurin`.
   - **Dynamic Port Binding**: Automatically adapts to Render's `PORT` environment variable.
   - **Verified Codebase**: Comprehensive unit and integration tests covering business logic and API integrity.

## 🤖 AI Usage Report

### Tools Used
- **Gemini CLI (Primary)**: Orchestrated the entire stabilization and cleanup phase.
- **Tailwind CSS / Lucide / Chart.js**: For a professional, logic-driven UI.

### Sample Prompts
- *"Initialize a Spring Boot project with Data JPA, Security, and H2 for a laundry management system."*
- *"Create a dashboard UI with Glassmorphism effects and Chart.js integration for revenue tracking."*
- *"Refactor the order service to handle priority surcharges and discount logic automatically."*
- *"Audit the codebase for missing test dependencies and fix Docker image deprecation warnings."*

### What AI Got Wrong & Improvements
- **Docker Image**: Initially suggested a deprecated OpenJDK image; I manually updated it to `eclipse-temurin` for better security and stability.
- **Port Binding**: AI hardcoded port 8080; I modified it to use `${PORT:8080}` to ensure compatibility with cloud platforms like Render.
- **Test Scope**: The initial scaffold missed `spring-security-test`, causing test failures; I identified and added the missing dependency.
- **Data Integrity**: AI used placeholder metrics in the dashboard; I refactored the backend to provide real-time JPA-backed aggregates.


## 🔹 Tradeoffs

### Tradeoffs
- **In-Memory Storage**: Used H2 in-memory for ease of demonstration. A production app would use PostgreSQL or MySQL.
- **Basic Auth**: Used simple in-memory users. OAuth2 or JWT would be preferred for a real-world multi-user application.
- **Floating Point Math**: Used `Double` for rapid prototyping. In a large-scale financial app, `BigDecimal` would be used to prevent precision loss.

### Future Improvements
- **Automated Notifications**: Send SMS/WhatsApp notifications to customers when status changes to "READY".
- **Advanced Analytics**: Monthly and yearly revenue forecasting.
- **Persistence**: Migration to PostgreSQL for production data durability.

---


