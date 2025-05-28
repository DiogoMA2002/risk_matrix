
# Risk Matrix

A comprehensive web platform for identifying, assessing, and managing business risks using a risk matrix model. This application enables organisations to systematically evaluate and visualise potential risks, helping them make informed decisions about risk management strategies.

## Features

### Risk Management

- Create and manage risks with custom attributes  
- Assign probability and consequence values to risks  
- Automatically compute and position risks on a matrix  
- Risk categorisation and prioritisation
- Risk recommendations.

### User Interface

- Intuitive and responsive design  
- Interactive risk assessment tools  
- Real-time updates and modifications  

## Tech Stack

### Backend

- Java 17  
- Spring Boot  
- H2 Database (in-memory, configurable)  
- RESTful API architecture  

### Frontend

- Vue.js 3  
- JavaScript/TypeScript  
- Tailwind CSS for styling  

## Getting Started

### Prerequisites

- Java 17 or higher  
- Node.js and npm  
- Maven  

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/DiogoMA2002/risk_matrix.git
   cd risk_matrix


2. Set up the backend:

   ```bash
   cd risk_Matrix
   ./mvnw spring-boot:run
   ```

   The backend server will start on `http://localhost:8080`

3. Set up the frontend:

   ```bash
   cd risk_matrix_frontend
   npm install
   npm run serve
   ```

   The frontend development server will start on `http://localhost:8080`

## Project Structure

```
risk_matrix/
├── risk_Matrix/              # Backend Spring Boot application
├── risk_matrix_frontend/     # Frontend Vue.js application
└── questions/                # Questionnaires Template
 
```

## Contributing

We welcome contributions. Please see our [CONTRIBUTING.md](CONTRIBUTING.md) file for details on how to contribute to this project.

## License

This project is licensed under the terms of the license included in the [LICENSE](LICENSE) file.

## Authors

* [DiogoMA2002](https://github.com/DiogoMA2002)
* [WhyN0t101](https://github.com/WhyN0t101)

## Security

For security concerns, please refer to our [SECURITY.md](SECURITY.md) file.

## Support

If you encounter any issues or have questions, please open an issue in the GitHub repository.


