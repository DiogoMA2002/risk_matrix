# Municipios Vue Application

## Description
The **Municipios Vue Application** is a modern Vue.js-based web interface designed to analyze and display detailed information about municipal domains. It integrates seamlessly with a Flask backend API to fetch and present comprehensive domain-related data, such as:

- DNSSEC validity and TLSA records.
- HTTPS configuration and SSL/TLS support.
- Email security details (SPF, DMARC, DKIM, and STARTTLS).
- HTTP security headers and other configurations.

The application features a responsive, user-friendly interface and implements caching mechanisms to optimize performance and reduce server load.

---

## Features
1. **Domain Analysis Dashboard**:
   - Displays DNSSEC and TLSA records for the domain.
   - Shows HTTPS redirection status, supported SSL/TLS versions, and HSTS configuration.
   - Provides detailed email security information, including SPF, DMARC, DKIM, and STARTTLS.
   - Highlights HTTP security headers like Content Security Policy (CSP) and X-Frame-Options.

2. **Real-Time API Integration**:
   - Fetches domain-specific details dynamically from the Flask backend.

3. **Caching**:
   - Implements caching at both backend and frontend levels to improve efficiency.

4. **Responsive UI**:
   - Built with TailwindCSS for a modern, mobile-friendly design.

---

## Backend Integration
This Vue app communicates with a Flask API that performs the following tasks:
- **DNS Analysis**: Validates DNSSEC, retrieves TLSA records, and checks MX records for email security.
- **HTTPS Validation**: Checks for HTTPS redirection, HSTS, and supported SSL/TLS versions.
- **HTTP Header Analysis**: Inspects HTTP security headers for best practices.
- **Caching**: Optimizes performance by caching responses to minimize redundant API calls.

---

## Dependencies

### Frontend
- **Vue 3**: The core framework for building the user interface.
- **Vue Router**: For client-side routing and dynamic navigation.
- **Axios**: For making HTTP requests to the backend.
- **TailwindCSS**: For styling the application with utility-first CSS.
- **Core-JS**: For polyfills to ensure compatibility with modern JavaScript features.

### Dev Dependencies
- **@babel/core**: For JavaScript transpilation.
- **ESLint & Plugins**: To ensure code quality and consistency.
- **PostCSS & Autoprefixer**: For CSS transformations and vendor prefixing.

### Backend Requirements
- **Flask**: Web framework for building the API.
- **Flask-Caching**: For implementing caching in API responses.
- **Flask-CORS**: To enable cross-origin resource sharing.
- **DNSPython**: For DNS lookups and DNSSEC validation.
- **Requests**: For making HTTP/HTTPS requests.

---

## How It Works

### Frontend Workflow
1. The Vue app retrieves domain details from the Flask backend via Axios.
2. Data is dynamically displayed in sections for DNSSEC, HTTPS, Email Security, and HTTP Headers.
3. TailwindCSS ensures a clean and responsive design.

### Backend Workflow
1. Flask handles domain requests through the `/check_domain/<domain>` endpoint.
2. The API performs checks using DNSPython, socket/SSL, and Requests libraries.
3. Caching is applied to serve recent results quickly and reduce redundant computations.

---
## Installation and Setup

### Frontend & Backend
```bash
# Clone this repository
git clone <repository-url>
cd <repository-folder>

# alter the ip in the .env.development file

# Install dependencies
npm install

# Start the development server
npm run serve

# Clone the Flask backend repository
git clone <backend-repository-url>

# Set up a Python virtual environment and install the required dependencies
pip install -r requirements.txt

# Run the Flask server
python app.py

