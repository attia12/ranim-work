# Campsite Recommendation System — How It Works

## Overview

When a logged-in user visits the home page, the app automatically suggests up to 5 campsites that best match their profile. The system uses no machine learning training — it is a deterministic scoring engine that runs every time a recommendation is requested.

---

## The Three Layers

```
Angular (front)  -->  Spring Boot (back)  -->  FastAPI (AI microservice)
     shows              fetches user                scores campsites
   the cards            data & calls               against user profile
```

---

## Step-by-Step Flow

### Step 1 — User opens the home page

The Angular carousel component loads and calls:

```
GET /api/v1/campsites/recommended
Authorization: Bearer <JWT token>
```

### Step 2 — Spring Boot prepares the data

Spring Boot does three things:

1. **Reads the user's booking history** — last 24 months of confirmed bookings (max 50).
2. **Reads all ACTIVE campsites** — filters out ones the user already booked, takes up to 200.
3. **Builds a request** and sends it to the FastAPI microservice.

The request looks like this:

```json
{
  "user_id": 3,
  "bookings": [
    {
      "campsite_id": 1,
      "campsite_name": "Forest Camp",
      "natural_features": ["FOREST", "LAKE"],
      "price_per_night": 30.0,
      "check_in": "2024-07-10",
      "check_out": "2024-07-15"
    }
  ],
  "all_campsites": [
    {
      "id": 5,
      "name": "Camp Bretagne",
      "natural_features": ["BEACH", "PLAIN"],
      "price_per_night": 55.0,
      "latitude": 47.48,
      "longitude": -3.1
    }
  ]
}
```

### Step 3 — FastAPI builds the user profile

FastAPI looks at the user's past bookings and extracts:

- **Preferred terrain** — which natural features appear most (FOREST, LAKE, MOUNTAIN, BEACH, RIVER, PLAIN).
- **Preferred budget** — average price per night across past bookings.
- **Preferred season** — which season the user tends to book in (Spring, Summer, Autumn, Winter).

Example profile for a user who always books forest campsites at ~30€/night in summer:

```
preferred_features = [FOREST, LAKE]
avg_budget        = 30€
preferred_season  = SUMMER
```

### Step 4 — FastAPI scores every candidate campsite

Each campsite gets a score out of 100, built from 5 components:

| Component | Weight | What it measures |
|-----------|--------|-----------------|
| Terrain   | 35%    | How well the campsite's features match the user's preferred features |
| Budget    | 25%    | How close the price is to what the user usually pays |
| Season    | 20%    | Whether today's season matches the user's booking season |
| Rating    | 10%    | Global average rating of the campsite (0 to 5 stars) |
| Weather   | 10%    | Current weather quality at the campsite's location |

**Terrain score** — if the user likes FOREST and LAKE, and the campsite has FOREST, it scores high. No match = 0.

**Budget score** — uses a bell curve. If the campsite costs exactly what the user usually pays, score = 1.0. The further away the price, the lower the score.

**Season score** — if the user always books in summer and it is currently summer, score = 1.0. Adjacent seasons (spring/autumn next to summer) get 0.5. Opposite season gets 0.

**Rating score** — normalized from 0–5 stars to 0–1.

**Weather score** — FastAPI calls the free Open-Meteo API with the campsite's GPS coordinates and gets the current WMO weather code:
- Clear sky = 1.0 (perfect)
- Partly cloudy = 0.8
- Overcast = 0.6
- Light rain = 0.4
- Heavy rain / storm = 0.0

**Final score formula:**

```
total = (terrain × 0.35) + (budget × 0.25) + (season × 0.20) + (rating × 0.10) + (weather × 0.10)
total = total × 100   --> expressed as a score out of 100
```

### Step 5 — FastAPI returns the top 5

The campsites are sorted by total score descending. Only the top 5 are returned.

### Step 6 — Spring Boot caches and returns

Spring Boot caches the result in Redis for 30 minutes (so repeated page loads do not call FastAPI again). It then returns the list to Angular.

### Step 7 — Angular displays the carousel

The carousel shows cards with:
- The campsite photo
- Name and price per night
- Weather description and icon
- Natural feature chips
- Score ring (green = 70+, amber = 45–69, red = below 45)

Clicking a card navigates to the campsite detail page.

---

## What Happens When There Is No History

If the user has no confirmed bookings:
- The terrain score defaults to 0 for all campsites (no preference known).
- Budget and season scores still work based on today's season and a neutral budget.
- The system still returns 5 campsites, ranked mostly by weather and rating.

---

## What Happens When FastAPI Is Down

Spring Boot catches the error and returns HTTP 503. Angular detects this and shows a fallback banner:

> "Make your first bookings to receive personalised recommendations."

---

## Key Design Decisions

- **No training needed** — the scoring is pure math, not a trained model. It works immediately with even one booking.
- **Deterministic** — same inputs always produce the same output. Easy to debug.
- **Campsites already booked are excluded** — the user will not be recommended a campsite they already visited.
- **Weather is fetched in parallel** — FastAPI fetches weather for all candidate campsites concurrently using asyncio, so latency stays low even with many campsites.
- **Redis cache** — avoids hammering FastAPI and Open-Meteo on every page load. Cache is per user and expires after 30 minutes.
- **Dates sent as strings** — Java's LocalDate would serialize as `[2024, 7, 5]` (an array) which Python's Pydantic rejects. The fix was to send dates as ISO strings `"2024-07-05"`.
