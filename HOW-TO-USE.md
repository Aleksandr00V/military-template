# Тестування етапів

## Запуск

```bash
docker-compose up -d
mvn spring-boot:run
```

---

## Етап 1: Liquibase

При старті додатку в консолі має бути:
```
liquibase.util - Previously run: 13
```

---

## Етап 2: VehicleAssignment + Транзакції

1. Залогінитись:
```
POST http://localhost:8080/api/auth/login
{
  "username": "admin",
  "password": "admin123"
}
```

2. Призначити водія (з токеном):
```
POST http://localhost:8080/api/assignments/assign
Authorization: Bearer <token>
{
  "vehicleId": 1,
  "driverId": 1,
  "notes": "Тест"
}
```

3. Перевірити історію:
```
GET http://localhost:8080/api/assignments/vehicle/1/history
Authorization: Bearer <token>
```

---

## Етап 3: JWT Security

1. Login - отримати токен:
```
POST http://localhost:8080/api/auth/login
{
  "username": "admin",
  "password": "admin123"
}
```

Відповідь містить токен:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGVzIjpbIlJPTEVfQURNSU4iXX0...",
  "username": "admin",
  "email": "admin@military.ua",
  "roles": ["ROLE_ADMIN", "ROLE_OPERATOR", "ROLE_VIEWER"]
}
```

**В Authorization header вставляти тільки значення поля `token`:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGVzIjpbIlJPTEVfQURNSU4iXX0...
```

2. Без токена - має бути 403:
```
GET http://localhost:8080/api/vehicles
```

3. З токеном - має бути 200:
```
GET http://localhost:8080/api/vehicles
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

4. DELETE тільки для ADMIN (operator отримає 403):
```
DELETE http://localhost:8080/api/vehicles/1
Authorization: Bearer <token>
```

---

## Етап 4: MapStruct

Перевірити що відповідь містить вкладені об'єкти:
```
GET http://localhost:8080/api/vehicles/1
Authorization: Bearer <token>
```

Має повернути category як об'єкт і driverName як рядок.

---

## Етап 5: Redis Caching

1. Перший запит - повільний (з БД):
```
GET http://localhost:8080/api/vehicle-categories
```

2. Другий запит - швидкий (з кешу)

3. Після створення/оновлення кеш очищається автоматично

---

## Етап 6: Spring Events

1. Зробити призначення водія:
```
POST http://localhost:8080/api/assignments/assign
Authorization: Bearer <token>
{
  "vehicleId": 1,
  "driverId": 1,
  "notes": "Тест події"
}
```

2. В консолі Spring Boot з'явиться:
```
========== VEHICLE ASSIGNMENT EVENT ==========
Operation: Призначення водія Іван Петренко на АА1234ВВ
Assignment ID: 1
Vehicle: АА1234ВВ (КрАЗ-255)
Driver ID: 1, Name: Іван Петренко
Type: ASSIGN_DRIVER
Performed by: admin
Notes: Тест події
Event time: 2025-12-12T21:30:00.123
Thread: task-1
==============================================
```

3. При відправці на ТО додатково буде VehicleStatusChangedEvent:
```
POST http://localhost:8080/api/assignments/maintenance
{
  "vehicleId": 1,
  "notes": "Планове ТО"
}
```

В консолі:
```
========== VEHICLE STATUS CHANGE ==========
Vehicle: АА1234ВВ (ID: 1)
Status change: ACTIVE -> IN_MAINTENANCE
Changed by: admin
Critical: false
Thread: task-2
=============================================
```

4. При списанні - критична подія з WARN:
```
POST http://localhost:8080/api/assignments/decommission
{
  "vehicleId": 2,
  "notes": "Списання"
}
```

В консолі:
```
========== CRITICAL STATUS CHANGE ==========
Vehicle: ВВ5678АА (ID: 2)
Status change: IN_POOL -> DECOMMISSIONED
Critical: true
WARN - Vehicle ВВ5678АА has been DECOMMISSIONED
=============================================
```

**Важливо:** Thread має бути `task-1`, `task-2` (асинхронний пул), а НЕ `http-nio-8080-exec-1` (основний потік). Це доказує що @Async працює.

---

## Етап 7: Metrics + Logging

1. Метрики:
```
http://localhost:8080/actuator/prometheus
```

Шукати:
```
military_auth_login_total
military_vehicle_assigned_total
```

2. Correlation ID в логах:
```
[http-nio-8080-exec-1] [a1b2c3d4] INFO ...
```

3. У відповіді header:
```
X-Correlation-ID: a1b2c3d4
```
