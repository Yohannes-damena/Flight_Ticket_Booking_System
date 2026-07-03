# Flight Ticket Tracking & Booking System

A desktop-based Flight Ticket Tracking and Booking application built entirely in Java using **Java Swing** for the graphical user interface and an **In-Memory Data Architecture** for robust, database-free data persistence. 

This project is designed to explicitly demonstrate core Object-Oriented Programming (OOP) principles, clean architectural layering, and defensive software design.

---

## 🛠️ Key Architectural Highlights
* **No Database Required:** Replaces heavy SQL configurations with native Java Collections (`List`, `Map`) paired with File Serialization streams.
* **Decoupled 3-Tier Layering:** Clean separation of concerns between UI Layouts (View), Operational Rules (Service), and Data States (Repository/Model).
* **Robust Exception Layer:** Incorporates custom domain exceptions preventing invalid actions (e.g., booking over aircraft capacities).

---

## 📂 Package & Class Structure

The project directory is structured to reflect professional enterprise layout boundaries:

```text
src/
└── com/
    └── flighttracking/
        ├── model/          # Core Data Blueprints (OOP Entities)
        │   ├── Flight.java            # Flight specs (route, base capacity)
        │   ├── Passenger.java         # Customer contact & identification profiles
        │   ├── Ticket.java            # Abstract base booking definition
        │   ├── EconomyTicket.java     # Class extension with basic baggage rules
        │   └── BusinessTicket.java    # Class extension with lounge premium modifiers
        │
        ├── repository/     # Data Storage & Access Management (The "Virtual DB")
        │   └── DataContext.java       # Manages in-memory maps/lists & binary File I/O
        │
        ├── service/        # Domain Business Logic & Security Assertions
        │   └── BookingService.java    # Coordinates capacity checking, rules, and triggers
        │
        └── view/           # Java Swing Graphical User Interface (Presentation)
            ├── MainDashboard.java     # Primary application container window
            ├── FlightListPanel.java   # Custom JTable viewer rendering live flights
            └── BookingFormDialog.java # Modal capture panel for secure ticket issuance
