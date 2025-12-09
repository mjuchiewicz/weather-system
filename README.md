# 🌤️ Weather System - Distributed Microservices Architecture

Kompleksowy system pogodowy wykorzystujący architekturę mikroserwisów z różnymi protokołami komunikacji.

---

## 📋 Spis Treści

1. [Architektura Systemu](#architektura-systemu)
2. [Technologie](#technologie)
3. [Struktura Projektu](#struktura-projektu)
4. [Przepływ Danych](#przepływ-danych)
5. [Protokoły Komunikacji](#protokoły-komunikacji)
6. [Bazy Danych](#bazy-danych)

---

## 🏗️ Architektura Systemu

### Diagram Architektury
```
┌─────────────────────────────────────────────────────────────┐
│                      USER (Browser)                          │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
        ┌────────────────────────┐
        │   Frontend (HTML/JS)   │
        │   Ajax Client          │
        │   Port: Live Server    │
        └────────────────────────┘
                     │
        ┌────────────┼────────────┐
        │            │            │
        ▼            ▼            ▼
┌──────────┐  ┌──────────┐  ┌──────────┐
│ gRPC     │  │ SOAP     │  │ XML-RPC  │
│ :8085    │  │ :8085    │  │ :8088    │
└──────────┘  └──────────┘  └──────────┘
        │            │            │
        ▼            ▼            ▼
┌─────────────────────────────────────────┐
│         Frontend Service (:8085)        │
│  • gRPC Client                          │
│  • SOAP Client                          │
│  • REST Controller                      │
└─────────────────────────────────────────┘
        │            │
        ▼            ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│Weather       │  │Report        │  │XML-RPC       │
│Provider      │  │Service       │  │Service       │
│:8084, :9090  │  │:8087         │  │:8088         │
└──────────────┘  └──────────────┘  └──────────────┘
        │
        ├─────────┐
        ▼         ▼
    ┌─────┐  ┌─────────┐
    │ H2  │  │RabbitMQ │
    │ DB  │  │ :5672   │
    └─────┘  └─────────┘
                 │
                 ▼
        ┌──────────────┐
        │Alert Service │
        │   :8086      │
        └──────────────┘
                 │
                 ▼
        ┌──────────────┐
        │Eureka Server │
        │   :8761      │
        └──────────────┘
```

---

## 🛠️ Technologie

### Backend
- **Java 17** - Główny język programowania
- **Spring Boot 3.4.12** - Framework aplikacyjny
- **Spring Cloud 2024.0.2** - Mikroserwisy
- **Maven** - Zarządzanie projektem i zależnościami

### Protokoły Komunikacji
- **gRPC 1.62.2** - Wysokowydajne RPC (blocking + async)
- **SOAP/WSDL** - Web Services (JAX-WS)
- **XML-RPC 3.1.3** - Lekkie RPC
- **REST** - RESTful API z HATEOAS
- **RabbitMQ 3** - Message Queue (AMQP)

### Bazy Danych
- **H2 Database** - In-memory (WeatherAlert, WeatherHistory)

### Service Discovery
- **Netflix Eureka** - Rejestracja i odkrywanie serwisów

### Frontend
- **HTML5/CSS3** - Interfejs użytkownika
- **JavaScript (ES6+)** - Logika klienta
- **Fetch API** - Ajax requests

### Zewnętrzne API
- **OpenWeatherMap API** - Rzeczywiste dane pogodowe

### Containerization
- **Docker** - Konteneryzacja
- **Docker Compose** - Orkiestracja

---

## 📁 Struktura Projektu
```
WeatherSystem/
├── eureka-server/          # Service Discovery
├── weather-provider/       # Główny serwis pogodowy
│   ├── gRPC Server (9090)
│   ├── REST API (8084)
│   ├── H2 Database
│   └── RabbitMQ Producer
├── frontend-service/       # API Gateway
│   ├── gRPC Client
│   ├── SOAP Client
│   └── REST Controllers (8085)
├── report-service/         # SOAP Web Service
│   └── Weather Reports (8087)
├── xmlrpc-service/         # XML-RPC Service
│   └── Historical Data (8088)
├── alert-service/          # RabbitMQ Consumer
│   └── Alert Notifications (8086)
├── web-client/             # Frontend
│   └── index.html
└── docker-compose.yaml
```

---

## 🔄 Przepływ Danych

### 1. Sprawdzanie Pogody (gRPC)
```
User → Frontend (Ajax)
  → Frontend Service (REST :8085)
    → Weather Provider (gRPC :9090)
      → OpenWeatherMap API
      → Zapisz do H2 (WeatherHistory)
      → Sprawdź alerty (H2: WeatherAlert)
      → Jeśli alert → RabbitMQ
        → Alert Service (Consumer)
          → Log do konsoli
```

**Technologie:**
- Frontend → Backend: `Fetch API` (Ajax)
- Frontend Service → Weather Provider: `gRPC` (blocking)
- Weather Provider → External: `RestTemplate` (HTTP)
- Weather Provider → DB: `Spring Data JPA` (H2)
- Weather Provider → Queue: `Spring AMQP` (RabbitMQ)

---

### 2. Generowanie Raportu (SOAP)
```
User → Frontend (Ajax)
  → Frontend Service (REST :8085)
    → Report Service (SOAP :8087)
      → Weather Provider (REST :8084/api/history/{city}/stats)
        → Pobierz statystyki z H2
      → Oblicz AVG/MIN/MAX
      → Zwróć SOAP Response
```

**Technologie:**
- Frontend Service → Report Service: `SOAP` (JAX-WS)
- Report Service → Weather Provider: `RestTemplate` (HTTP)
- Response: `WSDL/XML`

---

### 3. Historia Pogody (XML-RPC)
```
User → Frontend (Ajax)
  → XML-RPC Service (:8088)
    → Weather Provider (REST :8084/api/history/{city})
      → Pobierz z H2 (WeatherHistory)
    → Zwróć JSON
```

**Technologie:**
- Frontend → XML-RPC Service: `Fetch API` (REST wrapper)
- XML-RPC Service → Weather Provider: `RestTemplate` (HTTP)
- Response: `JSON`

---

### 4. Zarządzanie Alertami (REST CRUD)
```
User → Frontend (Ajax)
  → Weather Provider (REST :8084/api/alert-rules)
    → H2 Database (WeatherAlert)
    → CRUD Operations
    → HATEOAS Links
```

**Technologie:**
- Frontend → Backend: `Fetch API` (Ajax)
- Backend: `Spring MVC` (REST)
- Database: `Spring Data JPA` (H2)
- Response: `JSON + HATEOAS`

---

### 5. Async gRPC Demo
```
User → Frontend (Ajax)
  → Frontend Service (:8085/api/weather/async/{city})
    → Weather Provider (gRPC Async :9090)
      → Non-blocking Stub
      → StreamObserver
      → Return immediately (~10ms)
      → Process in background
```

**Technologie:**
- gRPC: `Async Stub` + `StreamObserver`
- Pattern: Non-blocking I/O

---

## 🔌 Protokoły Komunikacji

### 1. gRPC (Port 9090)

**Lokalizacja:** `weather-provider/src/main/proto/weather.proto`

**Blocking Call:**
```java
// Frontend Service
WeatherServiceBlockingStub stub = WeatherServiceGrpc.newBlockingStub(channel);
WeatherResponse response = stub.getWeather(request);
```

**Async Call:**
```java
// Frontend Service
WeatherServiceStub asyncStub = WeatherServiceGrpc.newStub(channel);
asyncStub.getWeather(request, new StreamObserver<WeatherResponse>() {
    @Override
    public void onNext(WeatherResponse response) {
        // Handle response
    }
});
```

**Server:**
```java
// Weather Provider
public class WeatherGrpcService extends WeatherServiceGrpc.WeatherServiceImplBase {
    @Override
    public void getWeather(WeatherRequest request, StreamObserver<WeatherResponse> responseObserver) {
        // Process and respond
    }
}
```

---

### 2. SOAP (Port 8087)

**WSDL:** `http://localhost:8087/ws/weather-report?wsdl`

**Client:**
```java
// Frontend Service
String soapRequest = "<?xml version=\"1.0\"?>...";
RestTemplate restTemplate = new RestTemplate();
String response = restTemplate.postForObject(soapUrl, request, String.class);
```

**Server:**
```java
// Report Service
@Endpoint
@PayloadRoot(namespace = NAMESPACE_URI, localPart = "getWeatherReportRequest")
@ResponsePayload
public GetWeatherReportResponse getWeatherReport(@RequestPayload GetWeatherReportRequest request) {
    // Generate report
}
```

---

### 3. XML-RPC (Port 8088)

**Endpoint:** `http://localhost:8088/xmlrpc/historical`

**Client (Frontend):**
```javascript
const response = await fetch(`${XMLRPC_API}?city=${city}&days=5`);
const data = await response.json();
```

**Server:**
```java
// XML-RPC Service
@GetMapping("/historical")
public List<Map<String, Object>> getHistoricalWeatherRest(@RequestParam String city, @RequestParam int days) {
    String url = weatherProviderUrl + "/api/history/" + city + "?limit=" + days;
    return restTemplate.getForObject(url, List.class);
}
```

---

### 4. REST (Port 8084)

**Endpoints:**
- `POST /api/alert-rules` - Create alert
- `GET /api/alert-rules` - List alerts
- `GET /api/alert-rules/{id}` - Get alert
- `PUT /api/alert-rules/{id}` - Update alert
- `DELETE /api/alert-rules/{id}` - Delete alert
- `GET /api/history/{city}` - Get history
- `GET /api/history/{city}/stats` - Get statistics

**HATEOAS Example:**
```json
{
  "id": 1,
  "alertType": "TEMPERATURE",
  "threshold": 30.0,
  "_links": {
    "self": {
      "href": "http://localhost:8084/api/alert-rules/1"
    },
    "all": {
      "href": "http://localhost:8084/api/alert-rules"
    }
  }
}
```

---

### 5. RabbitMQ (Port 5672)

**Queue:** `weather-alerts`

**Producer:**
```java
// Weather Provider
rabbitTemplate.convertAndSend(queueName, alertMessage);
```

**Consumer:**
```java
// Alert Service
@RabbitListener(queues = "${weather.alert.queue}")
public void receiveAlert(String alertMessage) {
    System.out.println("⚠️ WEATHER ALERT: " + alertMessage);
}
```

---

## 💾 Bazy Danych

### H2 Database (In-Memory)

**Lokalizacja:** Weather Provider

**Tabele:**

#### 1. weather_alerts
```sql
CREATE TABLE weather_alerts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    alert_type VARCHAR(50) NOT NULL,
    threshold DOUBLE NOT NULL,
    operator VARCHAR(5) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    color VARCHAR(10) NOT NULL,
    message VARCHAR(255) NOT NULL,
    image_url VARCHAR(255),
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL
);
```

**Pola:**
- `alert_type`: TEMPERATURE, HUMIDITY, WIND
- `operator`: >, <, >=, <=
- `threshold`: Wartość progowa
- `severity`: LOW, MEDIUM, HIGH, EXTREME
- `active`: Alert aktywny/nieaktywny

#### 2. weather_history
```sql
CREATE TABLE weather_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    city VARCHAR(100) NOT NULL,
    temperature DOUBLE NOT NULL,
    description VARCHAR(255),
    humidity INTEGER,
    timestamp TIMESTAMP NOT NULL
);
```

**Zastosowanie:**
- Automatyczny zapis każdego zapytania o pogodę
- Źródło danych dla raportów SOAP
- Źródło danych dla historii XML-RPC

---

## 🔗 Service Discovery (Eureka)

**Port:** 8761  
**URL:** http://localhost:8761

**Zarejestrowane serwisy:**
- WEATHER-PROVIDER
- FRONTEND-SERVICE
- REPORT-SERVICE
- XMLRPC-SERVICE
- ALERT-SERVICE

**Konfiguracja (każdy serwis):**
```yaml
eureka:
  client:
    serviceUrl:
      defaultZone: http://eureka-server:8761/eureka
```

---

## 🌐 Zewnętrzne API

### OpenWeatherMap API

**Endpoint:** `https://api.openweathermap.org/data/2.5/weather`

**Użycie:**
```java
String url = String.format("%s?q=%s&appid=%s&units=metric", 
    apiUrl, city, apiKey);
Map<String, Object> response = restTemplate.getForObject(url, Map.class);
```

**Konfiguracja:**
```properties
weather.api.key=YOUR_API_KEY
weather.api.url=https://api.openweathermap.org/data/2.5/weather
```

---

## 📊 Logika Biznesowa

### Alert System

**Workflow:**
1. User dodaje regułę alertu (REST CRUD)
2. Reguła zapisana w H2 (weather_alerts)
3. User sprawdza pogodę (gRPC)
4. Weather Provider:
    - Pobiera dane z OpenWeatherMap
    - Zapisuje do historii (weather_history)
    - Sprawdza aktywne alerty
    - Jeśli warunek spełniony → wysyła do RabbitMQ
5. Alert Service odbiera z kolejki i loguje

**Przykład:**
```
Alert: temperature > 30°C, severity: HIGH
Pogoda: Warsaw 35°C
Rezultat: Wyślij "🚨 HIGH | Warsaw: 35°C (threshold: 30°C)"
```

---

## 🐳 Docker

**Sieć:** `weather-network` (bridge)

**Kontenery:**
- eureka-server
- rabbitmq (+ Management UI :15672)
- weather-provider
- frontend-service
- report-service
- xmlrpc-service
- alert-service

**Komunikacja między kontenerami:**
- Używają nazw serwisów zamiast `localhost`
- Np. `http://weather-provider:8084`

---

## 📝 Podsumowanie Technologii

| Technologia | Gdzie | Do czego |
|-------------|-------|----------|
| **gRPC** | Frontend Service ↔ Weather Provider | Szybka komunikacja RPC |
| **SOAP** | Frontend Service ↔ Report Service | Web Services, raporty |
| **XML-RPC** | Frontend ↔ XML-RPC Service | Historia pogody |
| **REST** | Wszędzie | CRUD, komunikacja HTTP |
| **RabbitMQ** | Weather Provider → Alert Service | Asynchroniczne powiadomienia |
| **H2** | Weather Provider | Przechowywanie alertów i historii |
| **Eureka** | Wszystkie serwisy | Service Discovery |
| **Docker** | Deployment | Konteneryzacja |
| **Ajax** | Frontend | Asynchroniczne zapytania |

---

**Autor:** Malwina Juchiewicz  
**Data:** Grudzień 2025  
**Kurs:** Systemy i Aplikacje Rozproszone