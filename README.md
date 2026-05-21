# Margin Tracker

Advanced personal expense tracking and monthly real margin calculator.

## Author & Project Identity

* **Creator / Developer**: **f-estero**
* **Email / Contact**: [f-estero@proton.me](mailto:f-estero@proton.me)
* **Status**: Configured and integrated with Google AI Studio

---

## Features

- **Dashboard**: Real-time overview of monthly dynamic margins, net worth, assets, and liabilities.
- **Trend Charts**: Dynamic Material 3 bar indicators representing monthly cash-flow health over the last 6 months.
- **Interactive Historical Log**: Fully filterable archive list of all transactions based on category, dynamic time ranges (months), and value increments.
- **Local Persistence & Security**: High-performance local SQLite caching and structured querying powered by **Room Database** and Android SQLite engines.
- **Background Budget Alerts**: Integrated system background worker leveraging the Android **WorkManager** framework to automatically trigger notifications whenever expenses exceed safety thresholds.

---

## Development & Build Instructions

### Tooling Requirements
- **Kotlin**: 1.9+
- **Gradle**: Kotlin DSL
- **Jetpack Compose**: Modern Material Design 3 (M3) Toolkit

### Build Steps
To assemble and package the project:
```bash
# Compile and build the application debug APK
gradle assembleDebug

# Run Unit tests
gradle test
```

### Running inside Google AI Studio Project Ecosystem
This project has been customized and labeled under developer identity **f-estero <f-estero@proton.me>**. All standard platform-sync hooks and launcher labeling align with this workspace profile.
