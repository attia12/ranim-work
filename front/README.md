# CampWay Frontend

Angular 18 frontend for the **CampWay** platform — a camping ecosystem connecting campers, gear providers, guides, campsite owners, delivery agents, and administrators.

---

## Tech Stack

| Technology | Version |
|---|---|
| Angular | 18.2 |
| TypeScript | 5.5 |
| Bootstrap | 5.0 |
| AdminLTE | 3.2 |
| Chart.js | 4.5 |
| RxJS | 7.8 |
| Jasmine / Karma | 5.2 / 6.4 |

---

## Prerequisites

- Node.js 18+
- npm 9+
- Angular CLI 18: `npm install -g @angular/cli@18`

---

## Installation

```bash
git clone https://github.com/hafedhhammami6560/CampWay-frontend.git
cd CampWay-frontend
git checkout marketplace-and-delivery-modules
npm install
```

---

## Running the App

```bash
# Development server
ng serve

# Open in browser
http://localhost:4200
```

The app connects to the backend at `http://localhost:9099` by default. To change this, edit `src/environments/environment.ts`:

```ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:9099'
};
```

---

## Project Structure

```
src/
└── app/
    ├── frontoffice/          # Public-facing pages
    │   ├── layout/           # Header, footer, main layout
    │   └── pages/            # Home, shop, cart, login, register...
    ├── dashboards/           # Role-specific dashboards
    │   ├── gear-provider/
    │   ├── delivery/
    │   ├── admin-management/
    │   ├── campsite-owner/
    │   ├── guide/
    │   └── ...
    ├── layout/               # Backoffice layout (sidebar, header)
    ├── services/             # Angular services (auth, cart, product...)
    ├── guards/               # Route guards (auth, admin)
    ├── interceptors/         # HTTP interceptor (JWT token injection)
    ├── models/               # TypeScript interfaces and enums
    └── environments/         # Environment configuration
```

---

## Application Routes

### Public routes
| Route | Component | Description |
|---|---|---|
| `/home` | HomeComponent | Landing page |
| `/shop` | ShopComponent | Equipment marketplace |
| `/shop/product/:id` | ProductDetailComponent | Product details |
| `/cart` | CartComponent | Shopping cart |
| `/campsites` | CampsitesComponent | Browse campsites |
| `/events` | EventsComponent | Upcoming events |
| `/forum` | ForumComponent | Community forum |
| `/messages` | MessagesComponent | User messaging |
| `/delivery` | DeliveryComponent | Delivery tracking |
| `/bookings` | BookingsComponent | My bookings |
| `/payment/:orderId` | PaymentComponent | Order payment |
| `/orders/:orderId` | OrderTrackingComponent | Order tracking |
| `/profile` | ProfileComponent | User profile |
| `/login` | LoginComponent | Login (redirects if logged in) |
| `/register` | RegisterComponent | Register (redirects if logged in) |
| `/forgot-password` | ForgotPasswordComponent | Password reset request |
| `/reset-password` | ResetPasswordComponent | Password reset |

### Admin routes (protected — non-CAMPER roles only)
| Route | Description |
|---|---|
| `/admin/dashboard` | Admin overview |
| `/admin/users` | User management |
| `/admin/gear-provider` | Gear provider management |
| `/admin/deliveries` | Delivery management |
| `/admin/orders` | Order management |
| `/admin/payments` | Payment management |
| `/admin/campsites` | Campsite management |
| `/admin/guide` | Guide management |
| `/admin/sponsor` | Sponsor management |
| `/admin/categories` | Equipment categories |
| `/admin/forum-mod` | Forum moderation |
| `/admin/events` | Event management |

---

## User Roles

| Role | Enum value | Access |
|---|---|---|
| Camper | `COMPERS` | Frontoffice only |
| Gear Provider | `EQUIPEMENTPROVIEDERS` | Frontoffice + gear dashboard |
| Campsite Owner | `COMPSITEOWNERS` | Frontoffice + campsite dashboard |
| Delivery Agent | `DELIVERYAGENT` | Frontoffice + delivery dashboard |
| Guide | `GUIDE` | Frontoffice + guide dashboard |
| Admin | `ADMIN` | Full access |

---

## Services

| Service | Responsibility |
|---|---|
| `AuthService` | Login, logout, register, JWT token management |
| `CartService` | Shopping cart state, localStorage persistence |
| `ProductService` | Equipment CRUD, reviews, validation |
| `NotificationService` | In-app notifications per user session |
| `DeliveryService` | Delivery and vehicle management |
| `CampsiteService` | Campsite data |
| `EventService` | Events data |
| `ForumService` | Forum posts and categories |
| `MessagingService` | User-to-user messages |

---

## Authentication

JWT-based authentication. The `AuthInterceptor` automatically attaches the token to every HTTP request:

```
Authorization: Bearer <token>
```

Token and user data are stored in `localStorage`. On page refresh, the user session is restored automatically.

---

## Running Tests

```bash
# Run all Jasmine tests once
ng test --watch=false --browsers=ChromeHeadless

# Run with live watch
ng test
```

### Test coverage

| File | Tests |
|---|---|
| `auth.service.spec.ts` | 14 tests |
| `cart.service.spec.ts` | 12 tests |
| `product.service.spec.ts` | 14 tests |
| `notification.service.spec.ts` | 12 tests |
| `guards.spec.ts` | 6 tests |
| `auth.interceptor.spec.ts` | 3 tests |
| `login.component.spec.ts` | 7 tests |
| `forgot-password component.spec.ts` | 8 tests |
| **Total** | **76 tests** |

---

## Building for Production

```bash
ng build --configuration production
```

Output is in `dist/pidev/`.

---

## Backend

This frontend connects to the **CampWay Spring Boot backend**:
- Repo: [projet-integration](https://github.com/Ghofran2212/projet-integration)
- Branch: `marketplace-and-delivery-modules`
- Default URL: `http://localhost:9099`

---

## CI/CD

The project uses a Jenkins pipeline for automated testing and build:

```
Checkout → Install → Test (Jasmine/Karma) → Build → Archive
```

Pipeline file: `Jenkinsfile` at the project root.
