# FIBA Backend API Documentation for Frontend Developers

## Base URL
```
https://api-domain.com/api
```

## CORS Configuration

The backend is configured to allow requests from specific origins. If you're experiencing CORS errors like:

```
Access to XMLHttpRequest at 'https://timurbatrshin-fiba-backend-fc1f.twc1.net/api/tournaments?status=UPCOMING' from origin 'http://localhost:8099' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
```

You need to ensure your frontend domain is included in the allowed origins list on the backend.

Currently allowed origins:
- `http://localhost:8099`
- `https://dev.bro-js.ru`

If you're using a different frontend URL, you need to add it to the CORS configuration on the backend.

### Solution for Frontend Developers

1. **Option 1**: Make sure you're running your frontend on one of the allowed origins (preferred)
   - Use `http://localhost:8099` for local development

2. **Option 2**: If you need to use a different frontend URL, ask the backend developer to add your domain to the CORS configuration in:
   - `src/main/java/com/fiba/api/config/CorsConfig.java`
   - `src/main/java/com/fiba/api/config/WebConfig.java`
   - `src/main/resources/application.properties` (under `spring.web.cors.allowed-origins`)

3. **Option 3**: For testing purposes, you can use a CORS proxy like:
   - [CORS Anywhere](https://github.com/Rob--W/cors-anywhere)
   - Browser extensions that disable CORS (for development only)

## Authentication

### Endpoints

#### Register
- **URL**: `/auth/register`
- **Method**: `POST`
- **Request Body**:
```json
{
  "name": "User Name",
  "email": "user@example.com",
  "password": "password123",
  "role": "user" // Optional, defaults to "user"
}
```
- **Response**:
```json
{
  "token": "jwt_token_string",
  "userId": 1,
  "email": "user@example.com",
  "name": "User Name",
  "role": "user"
}
```

#### Login
- **URL**: `/auth/login`
- **Method**: `POST`
- **Request Body**:
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```
- **Response**:
```json
{
  "token": "jwt_token_string",
  "userId": 1,
  "email": "user@example.com",
  "name": "User Name",
  "role": "user"
}
```

#### Refresh Token
- **URL**: `/auth/refresh-token`
- **Method**: `POST`
- **Headers**: `Authorization: Bearer {token}`
- **Response**: Same as login response

## User Profile

### Endpoints

#### Get Profile
- **URL**: `/profile`
- **Method**: `GET`
- **Headers**: `Authorization: Bearer {token}`
- **Response**:
```json
{
  "id": 1,
  "user_id": 1,
  "name": "User Name",
  "email": "user@example.com",
  "photo_url": "url/to/photo.jpg",
  "tournaments_played": 5,
  "total_points": 120,
  "rating": 78
}
```

#### Update Profile
- **URL**: `/profile`
- **Method**: `PUT`
- **Headers**: `Authorization: Bearer {token}`
- **Request Body**:
```json
{
  "photo_url": "url/to/photo.jpg",
  "tournaments_played": 5,
  "total_points": 120,
  "rating": 78
}
```
- **Response**: Updated profile data

#### Upload Profile Photo
- **URL**: `/profile/photo`
- **Method**: `POST`
- **Headers**: `Authorization: Bearer {token}`
- **Content-Type**: `multipart/form-data`
- **Form Data**:
  - `photo`: File
  - `tournaments_played`: Integer (optional)
  - `total_points`: Integer (optional)
  - `rating`: Integer (optional)
- **Response**: Updated profile data

## Tournaments

### Endpoints

#### Get All Tournaments
- **URL**: `/tournaments`
- **Method**: `GET`
- **Query Parameters**:
  - `limit`: Integer (optional) - Limit number of results
  - `sort`: String (optional) - Field to sort by
  - `direction`: String (optional) - Sort direction ("asc" or "desc")
  - `upcoming`: Boolean (optional) - Filter for upcoming tournaments
- **Response**: Array of tournament objects

#### Get Tournament By ID
- **URL**: `/tournaments/{id}`
- **Method**: `GET`
- **Response**: Tournament object

#### Get Tournaments By Status
- **URL**: `/tournaments/status/{status}`
- **Method**: `GET`
- **Response**: Array of tournament objects

#### Get Upcoming Tournaments
- **URL**: `/tournaments/upcoming`
- **Method**: `GET`
- **Response**: Array of tournament objects

#### Get Past Tournaments
- **URL**: `/tournaments/past`
- **Method**: `GET`
- **Response**: Array of tournament objects

#### Search Tournaments
- **URL**: `/tournaments/search`
- **Method**: `GET`
- **Query Parameters**:
  - `query`: String - Search term
- **Response**: Array of tournament objects

#### Create Tournament (Admin only)
- **URL**: `/tournaments`
- **Method**: `POST`
- **Headers**: `Authorization: Bearer {token}`
- **Content-Type**: `multipart/form-data`
- **Form Data**:
  - `title`: String
  - `date`: String (ISO format date)
  - `location`: String
  - `level`: String
  - `prize_pool`: Integer
  - `tournament_image`: File (optional)
- **Response**: Created tournament object

#### Create Business Tournament (Admin or Business)
- **URL**: `/tournaments/business`
- **Method**: `POST`
- **Headers**: `Authorization: Bearer {token}`
- **Content-Type**: `multipart/form-data`
- **Form Data**:
  - `title`: String
  - `date`: String (ISO format date)
  - `location`: String
  - `level`: String
  - `prize_pool`: Integer
  - `sponsor_name`: String (optional)
  - `business_type`: String (optional)
  - `tournament_image`: File (optional)
  - `sponsor_logo`: File (optional)
- **Response**: Created tournament object

#### Register for Tournament
- **URL**: `/tournaments/{id}/register`
- **Method**: `POST`
- **Headers**: `Authorization: Bearer {token}`
- **Request Body**:
```json
{
  "team_name": "Team Name"
}
```
- **Response**: Registration object

## Team Management

### Endpoints

#### Get Team Details
- **URL**: `/teams/{registrationId}`
- **Method**: `GET`
- **Headers**: `Authorization: Bearer {token}` (must be team captain or admin)
- **Response**: Team details with players

## Registrations

### Endpoints

#### Get Registrations By Captain
- **URL**: `/registrations/captain`
- **Method**: `GET`
- **Headers**: `Authorization: Bearer {token}`
- **Response**: Array of registration objects

#### Get Teams By Player
- **URL**: `/registrations/player`
- **Method**: `GET`
- **Headers**: `Authorization: Bearer {token}`
- **Response**: Array of registration objects

#### Get Registrations By Tournament
- **URL**: `/registrations/tournament/{tournamentId}`
- **Method**: `GET`
- **Response**: Array of registration objects

#### Get Registrations By Tournament And Status
- **URL**: `/registrations/tournament/{tournamentId}/status/{status}`
- **Method**: `GET`
- **Response**: Array of registration objects

#### Create Registration
- **URL**: `/registrations`
- **Method**: `POST`
- **Headers**: `Authorization: Bearer {token}`
- **Request Body**:
```json
{
  "tournament_id": 1,
  "team_name": "Team Name"
}
```
- **Response**: Created registration object

#### Update Registration
- **URL**: `/registrations/{id}`
- **Method**: `PUT`
- **Headers**: `Authorization: Bearer {token}` (must be team captain or admin)
- **Request Body**:
```json
{
  "team_name": "Updated Team Name"
}
```
- **Response**: Updated registration object

#### Add Player To Team
- **URL**: `/registrations/{id}/players`
- **Method**: `POST`
- **Headers**: `Authorization: Bearer {token}` (must be team captain)
- **Request Body**:
```json
{
  "player_id": 2
}
```
- **Response**: Updated registration object

#### Remove Player From Team
- **URL**: `/registrations/{id}/players/{playerId}`
- **Method**: `DELETE`
- **Headers**: `Authorization: Bearer {token}` (must be team captain)
- **Response**: Updated registration object

## Players

### Endpoints

#### Get Player By ID
- **URL**: `/players/{id}`
- **Method**: `GET`
- **Response**:
```json
{
  "id": 1,
  "name": "Player Name",
  "email": "player@example.com",
  "points": 120,
  "rating": 78,
  "tournaments_played": 5,
  "photo_url": "url/to/photo.jpg"
}
```

#### Get Player Rankings
- **URL**: `/players/rankings`
- **Method**: `GET`
- **Query Parameters**:
  - `category`: String - Category to rank by (points, rating, tournaments)
  - `limit`: Integer (optional, default=10) - Limit number of results
- **Response**: Array of player objects with rankings

## Data Models

### User
```json
{
  "id": 1,
  "name": "User Name",
  "email": "user@example.com",
  "role": "user" // Can be "user", "admin", or "business"
}
```

### Profile
```json
{
  "id": 1,
  "user": { User object },
  "photoUrl": "url/to/photo.jpg",
  "tournamentsPlayed": 5,
  "totalPoints": 120,
  "rating": 78
}
```

### Tournament
```json
{
  "id": 1,
  "name": "Tournament Name",
  "title": "Tournament Title",
  "date": "2025-05-15T10:00:00",
  "location": "Tournament Location",
  "level": "Professional",
  "prizePool": 1000000,
  "status": "registration", // Can be "registration", "in_progress", "completed"
  "sponsorName": "Sponsor Name",
  "sponsorLogo": "url/to/logo.jpg",
  "imageUrl": "url/to/image.jpg",
  "businessType": "Business Type"
}
```

### Registration
```json
{
  "id": 1,
  "teamName": "Team Name",
  "tournament": { Tournament object },
  "captain": { User object },
  "players": [{ User objects }],
  "status": "pending" // Can be "pending", "approved", "rejected"
}
```

## Authentication and Authorization

1. **JWT Authentication**:
   - The backend uses JWT tokens for authentication
   - Store the JWT token after login and include it in the Authorization header for all protected requests
   - Format: `Authorization: Bearer {token}`

2. **Role-Based Authorization**:
   - Different endpoints require different roles:
     - `user`: Regular user
     - `admin`: Administrator with full access
     - `business`: Business users with special privileges

3. **Token Expiration**:
   - JWT tokens expire after a certain period
   - Use the refresh token endpoint to get a new token before expiration

## API Response Format

1. **Success Response**:
   - HTTP Status: 200 OK
   - Body: Data object or array

2. **Error Response**:
   - HTTP Status: 4xx or 5xx
   - Body: `{ "error": "Error message" }`

## Best Practices for Frontend

1. Always validate user input before sending to the backend
2. Handle authentication token expiration gracefully
3. Implement proper error handling for API responses
4. Use loading states during API calls to improve user experience
5. Cache responses where appropriate to reduce API calls
6. Use optimistic updates for better user experience 