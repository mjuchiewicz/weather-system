# 🚀 Weather System - Quick Start

---

## 📋 Wymagania

- Java 17+
- Maven 3.8+
- Docker Desktop
- IntelliJ IDEA
- OpenWeatherMap API Key: https://openweathermap.org/api

---

## ⚙️ Konfiguracja API Key

**Edytuj:** `weather-provider/src/main/resources/application.properties`
```properties
weather.api.key=TWÓJ_KLUCZ_TUTAJ
```

---

## 🐳 Uruchomienie (Docker)
```bash
# Build obrazów
docker build -t eureka-server:latest ./eureka-server
docker build -t weather-provider:latest ./weather-provider
docker build -t frontend-service:latest ./frontend-service
docker build -t report-service:latest ./report-service
docker build -t xmlrpc-service:latest ./xmlrpc-service
docker build -t alert-service:latest ./alert-service

# Uruchom
docker-compose up -d

# Sprawdź status
docker ps

# Logi
docker-compose logs -f

# Stop
docker-compose down
```

**Poczekaj 2-3 minuty na start!**

---

## 💻 Uruchomienie (Lokalne)

### 1. RabbitMQ
```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

### 2. Kolejność startowania w IntelliJ:
1. **Eureka Server** → poczekaj 30s
2. **Weather Provider** → poczekaj 20s
3. **Alert Service**
4. **Report Service**
5. **XML-RPC Service**
6. **Frontend Service**

### 3. Frontend
Prawy klik na `web-client/index.html` → **Open in Browser**

---

## 🔗 Linki

### Aplikacja
| Co | URL |
|----|-----|
| **Frontend** | http://localhost:63342/.../index.html |
| **Eureka** | http://localhost:8761 |
| **RabbitMQ** | http://localhost:15672 (guest/guest) |

### REST API
| Serwis | Port | Endpoint |
|--------|------|----------|
| **Weather Provider** | 8084 | `/api/alert-rules`, `/api/history/{city}` |
| **Frontend Service** | 8085 | `/api/weather/{city}`, `/api/report/{city}` |
| **XML-RPC Service** | 8088 | `/xmlrpc/historical?city={city}` |
| **Report Service** | 8087 | `/ws` (SOAP) |
| **Alert Service** | 8086 | Consumer only |
| **gRPC** | 9090 | gRPC protocol |

### Przykłady
```bash
# Pogoda
curl http://localhost:8085/api/weather/Warsaw

# Historia
curl http://localhost:8088/xmlrpc/historical?city=Warsaw&days=5

# Statystyki
curl http://localhost:8084/api/history/Warsaw/stats

# Lista alertów
curl http://localhost:8084/api/alert-rules
```

---

## 💾 Bazy Danych

**H2 (in-memory)** w Weather Provider:
- `weather_alerts` - Reguły alertów
- `weather_history` - Historia pogody

**⚠️ Dane znikają po restarcie!**

---

## 🐰 RabbitMQ

**Queue:** `weather-alerts`

**Jak przetestować:**
1. Dodaj alert (temp > 5°C)
2. Sprawdź pogodę Warsaw
3. Sprawdź logi `alert-service`:
```
⚠️ WEATHER ALERT: Warsaw 7.5°C
```

---

## ✅ Quick Test

### 1. Sprawdź Eureka
http://localhost:8761 → 5 serwisów

### 2. Sprawdź pogodę
Frontend → **Live Weather** → Warsaw → temp ~7°C ✅

### 3. Dodaj alert
**Alert Rules** → temp > 5 → Add ✅

### 4. Wywołaj alert
Sprawdź pogodę ponownie → Status: ALERT ✅  
Logi alert-service → komunikat ✅

### 5. Historia
**Historical Data** → Warsaw → prawdziwe dane ✅

### 6. Raport
**Weather Report** → Warsaw → statystyki z bazy ✅

### 7. Async
**Async gRPC** → Warsaw → response ~10ms ✅

---

## 🐛 Problemy?

**Port zajęty:**
```bash
# Windows
netstat -ano | findstr :8084

# Mac/Linux
lsof -i :8084
```

**Connection refused:**
- Sprawdź czy Eureka działa (http://localhost:8761)
- Poczekaj 2-3 min na pełny start
- Sprawdź logi: `docker-compose logs -f`

**Brak danych w historii:**
- Najpierw sprawdź pogodę kilka razy
- Potem dopiero sprawdzaj historię/raport

**RabbitMQ nie działa:**
```bash
docker restart rabbitmq
```

---

## 📊 Architektura (skrót)
```
Frontend (Ajax)
  ↓
Frontend Service (8085)
  ├─ gRPC → Weather Provider (9090)
  ├─ SOAP → Report Service (8087)
  └─ REST → XML-RPC Service (8088)
         ↓
    Weather Provider (8084)
      ├─ OpenWeatherMap API
      ├─ H2 Database
      └─ RabbitMQ → Alert Service (8086)
```

**Technologie:**
- gRPC: blocking + async (StreamObserver)
- SOAP: JAX-WS + WSDL
- XML-RPC: REST wrapper
- REST: CRUD + HATEOAS
- RabbitMQ: Producer/Consumer
- H2: In-memory SQL
- Eureka: Service Discovery

---

## 🎯 Porty (podsumowanie)

| Port | Serwis |
|------|--------|
| 8761 | Eureka |
| 8084 | Weather Provider (REST) |
| 8085 | Frontend Service |
| 8086 | Alert Service |
| 8087 | Report Service (SOAP) |
| 8088 | XML-RPC Service |
| 9090 | gRPC Server |
| 5672 | RabbitMQ (AMQP) |
| 15672 | RabbitMQ Management |

---

**Autor:** Malwina Juchiewicz  
**Projekt:** Weather System  
**Grudzień 2025**