# SOA Shared Expense Application

Spring Boot aplikacija za upravljanje skupnih stroškov z uporabo Google Cloud Firestore.

## 📋 Zahteve

- Java 21
- Maven 3.9+
- Docker (opcijsko)
- Google Cloud Firestore credentials

## 🚀 Vzpostavitev projekta

### 1. Kloniraj repozitorij

```bash
git clone <repository-url>
cd soa-shared-expense
```

### 2. Nastavi Firestore credentials

**Pomembno**: Ustvari folder `src/main/resources/` če še ne obstaja.

```bash
mkdir -p src/main/resources
```

Dodaj svoj `firestore.json` file v `src/main/resources/` direktorij. Ta datoteka vsebuje Google Cloud Firestore credentials in je potrebna za delovanje aplikacije.

**Struktura firestore.json:**

```json
{
  "type": "service_account",
  "project_id": "your-project-id",
  "private_key_id": "...",
  "private_key": "-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----\n",
  "client_email": "...",
  "client_id": "...",
  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
  "token_uri": "https://oauth2.googleapis.com/token",
  ...
}
```

**Opomba**: `firestore.json` je v `.gitignore` in se ne bo commit-al v git repozitorij.

### 3. Zgradi projekt

```bash
mvn clean install
```

### 4. Zagni aplikacijo

```bash
mvn spring-boot:run
```

Aplikacija bo zagnana na `http://localhost:8080`

## 🐳 Docker

### Zgradi Docker image

```bash
docker build -t soa-shared-expense:latest .
```

### Zagni Docker container

```bash
docker run -p 8080:8080 soa-shared-expense:latest
```

Aplikacija bo dostopna na `http://localhost:8080`

**Opomba**: `firestore.json` je že vključen v JAR file med build procesom, zato ni potrebno dodatno kopirati datoteke v container.

## 📡 API Metode

### Groups API (`/groups`)

#### Ustvari skupino

```http
POST /groups/create
Content-Type: application/json

{
  "groupTitle": "Ime skupine",
  "groupMembers": ["user1", "user2", "user3"]
}
```

#### Pridobi vse skupine uporabnika

```http
GET /groups/user-groups/{userId}
```

#### Pridobi skupino po ID

```http
GET /groups/{id}
```

#### Pridobi člane skupine

```http
GET /groups/group-members/{id}
```

#### Dodaj člana v skupino

```http
POST /groups/add-member/{id}
Content-Type: application/json

{
  "memberId": "user123"
}
```

#### Posodobi naslov skupine

```http
PUT /groups/update-title/{id}
Content-Type: application/json

{
  "title": "Novi naslov skupine"
}
```

#### Izbriši skupino

```http
DELETE /groups/delete-group/{id}
```

#### Odstrani člana iz skupine

```http
DELETE /groups/remove-member/{id}
Content-Type: application/json

{
  "memberId": "user123"
}
```

### Expenses API (`/group-expenses`)

#### Dodaj expense v skupino

```http
POST /group-expenses/add/{groupId}
Content-Type: application/json

{
  "description": "Večerja v restavraciji",
  "payments": {
    "user1": 25.50,
    "user2": 15.75,
    "user3": 10.00
  }
}
```

**Opomba**: `totalAmount` se preračuna avtomatično iz `payments`.

#### Pridobi vse expense-e skupine

```http
GET /group-expenses/get-all/{groupId}
```

#### Posodobi expense

```http
PUT /group-expenses/update/{groupId}/{expenseId}
Content-Type: application/json

{
  "description": "Posodobljen opis",
  "payments": {
    "user1": 30.00,
    "user2": 20.00
  }
}
```

#### Izbriši expense

```http
DELETE /group-expenses/delete/{groupId}/{expenseId}
```

#### Pridobi skupne stroške skupine

```http
GET /group-expenses/total/{groupId}
```

#### Pridobi stroške po članih

```http
GET /group-expenses/each-spent/{groupId}
```

**Response:**

```json
{
  "user1": 45.5,
  "user2": 30.25,
  "user3": 20.0
}
```

#### Razdeli dolg med člani

```http
GET /group-expenses/split/{groupId}
```

**Response:**

```json
{
  "user1": [
    {
      "user2": 15.25
    },
    {
      "user3": 5.0
    }
  ]
}
```

## 🔧 Tehnologije

- **Spring Boot** 3.5.8
- **Java** 21
- **Google Cloud Firestore** 3.13.0
- **Firebase Admin SDK** 9.3.0
- **Lombok**
- **Maven**

## 📝 Opombe

- Vsi endpointi vračajo HTTP status kode (200 OK, 201 Created, 400 Bad Request, 404 Not Found)
- `firestore.json` mora biti v `src/main/resources/` direktoriju
- Aplikacija uporablja Firestore za shranjevanje podatkov
- Port 8080 je privzeti Spring Boot port
