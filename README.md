# Risk Matrix

A comprehensive web platform for identifying, assessing, and managing business risks using a risk matrix model. This application enables organisations to systematically evaluate and visualise potential risks, helping them make informed decisions about risk management strategies.

## Features

### Risk Management

- Create and manage risks with custom attributes
- Assign probability and consequence values to risks
- Automatically compute and position risks on a matrix
- Risk categorisation and prioritisation
- Risk recommendations

### User Interface

- Intuitive and responsive design
- Interactive risk assessment tools
- Real-time updates and modifications

## Tech Stack

### Backend

- Java 23
- Spring Boot
- H2 Database (file-based, dev) / PostgreSQL (production)
- RESTful API architecture

### Frontend

- Vue.js 3
- JavaScript
- Tailwind CSS for styling

## Getting Started

### Prerequisites

- **JDK 23** (e.g. [Eclipse Temurin 23](https://adoptium.net/)) — Java 24+ will cause a Lombok `TypeTag :: UNKNOWN` compile error
- Node.js and npm
- Maven

> **IntelliJ IDEA users:** make sure both *File → Project Structure → Project → SDK* and the run configuration JRE are set to JDK 23. Using a newer JDK (e.g. 26) picked up from the system PATH will break compilation.

### Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/DiogoMA2002/risk_matrix.git
   cd risk_matrix
   ```

2. Set up environment variables:

   ```bash
   cd risk_Matrix
   cp .env.example .env
   ```

   Edit the `.env` file with the required values:

   ```bash
   JWT_SECRET=your_secure_jwt_secret_here_make_it_long_and_random
   JWT_EXPIRATION_MS=86400000
   JWT_PUBLIC_EXPIRATION_MS=7200000
   ADMIN_USERNAME=admin
   ADMIN_EMAIL=admin@example.com
   ADMIN_PASSWORD=your_secure_password
   COOKIE_SECURE=false
   TRUSTED_PROXIES=127.0.0.1,::1
   ```

   **Important**: Never commit your `.env` file to version control!

3. Set up the backend:

   ```bash
   cd risk_Matrix
   ./mvnw spring-boot:run
   ```

   The backend server will start on `http://localhost:8080`

4. Set up the frontend:

   ```bash
   cd risk_matrix_frontend
   npm install
   npm run serve
   ```

   The frontend development server will start on `http://localhost:8081`

## Contributing

We welcome contributions. Please see our [CONTRIBUTING.md](CONTRIBUTING.md) file for details on how to contribute to this project.

## License

This project is licensed under the terms of the license included in the [LICENSE](LICENSE) file.

## Authors

- [DiogoMA2002](https://github.com/DiogoMA2002)
- [WhyN0t101](https://github.com/WhyN0t101)

## Security

For security concerns, please refer to our [SECURITY.md](SECURITY.md) file.

## Support

If you encounter any issues or have questions, please open an issue in the GitHub repository.
