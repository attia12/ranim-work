package tn.esprit.projetpidev.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import tn.esprit.projetpidev.domain.Campsite;
import tn.esprit.projetpidev.domain.CampsiteBooking;
import tn.esprit.projetpidev.domain.User;
import tn.esprit.projetpidev.domain.enums.CampsiteBookingStatus;
import tn.esprit.projetpidev.domain.enums.CampsiteStatus;
import tn.esprit.projetpidev.domain.enums.CampsiteType;
import tn.esprit.projetpidev.domain.enums.Role;
import tn.esprit.projetpidev.repositories.CampsiteBookingRepository;
import tn.esprit.projetpidev.repositories.CampsiteRepository;
import tn.esprit.projetpidev.repositories.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository       userRepo;
    private final CampsiteRepository   campsiteRepo;
    private final CampsiteBookingRepository bookingRepo;
    private final PasswordEncoder      passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        String password = passwordEncoder.encode(
                System.getenv().getOrDefault("SEED_PASSWORD", "Dev@12345!"));

        seedUsers(password);
        seedCampsitesAndBookings();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // USERS
    // ─────────────────────────────────────────────────────────────────────────
    private void seedUsers(String password) {
        List<User> toSave = new ArrayList<>();

        // 10 Campers
        for (int i = 1; i <= 10; i++) {
            String email = "camper" + i + "@campconnect.tn";
            if (userRepo.findByEmail(email).isEmpty()) {
                toSave.add(User.builder()
                        .firstName("Camper").lastName("User" + i).email(email)
                        .password(password).role(Role.COMPERS).enabled(true)
                        .phoneNumber("5500000" + (i < 10 ? "0" + i : i))
                        .build());
            }
        }

        // 5 Campsite Owners
        for (int i = 1; i <= 5; i++) {
            String email = "owner" + i + "@campconnect.tn";
            if (userRepo.findByEmail(email).isEmpty()) {
                toSave.add(User.builder()
                        .firstName("Owner").lastName("User" + i).email(email)
                        .password(password).role(Role.COMPSITEOWNERS).enabled(true)
                        .phoneNumber("5400000" + i)
                        .build());
            }
        }

        // 10 Providers
        for (int i = 1; i <= 10; i++) {
            String email = "provider" + i + "@campconnect.tn";
            if (userRepo.findByEmail(email).isEmpty()) {
                toSave.add(User.builder()
                        .firstName("Provider").lastName("User" + i).email(email)
                        .password(password).role(Role.EQUIPEMENTPROVIEDERS).enabled(true)
                        .phoneNumber("5600000" + (i < 10 ? "0" + i : i))
                        .build());
            }
        }

        // 10 Delivery agents
        for (int i = 1; i <= 10; i++) {
            String email = "delivery" + i + "@campconnect.tn";
            if (userRepo.findByEmail(email).isEmpty()) {
                toSave.add(User.builder()
                        .firstName("Delivery").lastName("Agent" + i).email(email)
                        .password(password).role(Role.DELIVERYAGENT).enabled(true)
                        .phoneNumber("5700000" + (i < 10 ? "0" + i : i))
                        .build());
            }
        }

        // 2 Admins
        for (int i = 1; i <= 2; i++) {
            String email = "admin" + i + "@campconnect.tn";
            if (userRepo.findByEmail(email).isEmpty()) {
                toSave.add(User.builder()
                        .firstName("Admin").lastName("User" + i).email(email)
                        .password(password).role(Role.ADMIN).enabled(true)
                        .phoneNumber("58000000" + i)
                        .build());
            }
        }

        if (!toSave.isEmpty()) {
            userRepo.saveAll(toSave);
            log.info("[Seed] Created {} users", toSave.size());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CAMPSITES + BOOKINGS
    // Runs only if no campsites exist yet (idempotent guard)
    // ─────────────────────────────────────────────────────────────────────────
    private void seedCampsitesAndBookings() {
        if (campsiteRepo.count() > 0) {
            log.info("[Seed] Campsites already seeded — skipping");
            return;
        }

        // Fetch owners (must exist after seedUsers)
        User owner1 = userRepo.findByEmail("owner1@campconnect.tn").orElse(null);
        User owner2 = userRepo.findByEmail("owner2@campconnect.tn").orElse(null);
        User owner3 = userRepo.findByEmail("owner3@campconnect.tn").orElse(null);

        if (owner1 == null || owner2 == null || owner3 == null) {
            log.warn("[Seed] Owners not found — skipping campsite seed");
            return;
        }

        // ── 10 Campsites ─────────────────────────────────────────────────────
        //   naturalFeatures: comma-separated from NaturalFeature enum
        //   FOREST | LAKE | MOUNTAIN | BEACH | RIVER | PLAIN
        //   Real European coordinates for Open-Meteo weather accuracy

        Campsite c1 = campsiteRepo.save(Campsite.builder()
                .name("Camp des Pins — Fontainebleau")
                .description("Forêt dense, lac naturel, idéal pour les amoureux de nature. Bungalows en bois et emplacements tente.")
                .country("France").city("Fontainebleau").address("Route des Pins, 77300")
                .latitude(48.40).longitude(2.70)
                .capacity(40).type(CampsiteType.OFFICIAL)
                .pricePerNight(BigDecimal.valueOf(45))
                .naturalFeatures("FOREST,LAKE")
                .amenities("electricity,showers,toilets,wifi")
                .pictures("https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?w=800")
                .status(CampsiteStatus.ACTIVE)
                .startDate(LocalDate.of(2024, 4, 1))
                .endDate(LocalDate.of(2026, 10, 31))
                .owner(owner1).build());

        Campsite c2 = campsiteRepo.save(Campsite.builder()
                .name("Camp Alpin — Chamonix")
                .description("Au pied du Mont-Blanc. Randonnées glaciaires, rivières de montagne, vues panoramiques.")
                .country("France").city("Chamonix").address("Chemin des Alpages, 74400")
                .latitude(45.92).longitude(6.87)
                .capacity(30).type(CampsiteType.OFFICIAL)
                .pricePerNight(BigDecimal.valueOf(75))
                .naturalFeatures("MOUNTAIN,RIVER")
                .amenities("electricity,showers,toilets,parking")
                .pictures("https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800")
                .status(CampsiteStatus.ACTIVE)
                .startDate(LocalDate.of(2024, 5, 1))
                .endDate(LocalDate.of(2026, 9, 30))
                .owner(owner1).build());

        Campsite c3 = campsiteRepo.save(Campsite.builder()
                .name("Camp Méditerranée — Nice")
                .description("Plage privée, eaux turquoise, ambiance Côte d'Azur. Accès direct mer.")
                .country("France").city("Nice").address("Promenade des Plages, 06200")
                .latitude(43.70).longitude(7.26)
                .capacity(60).type(CampsiteType.OFFICIAL)
                .pricePerNight(BigDecimal.valueOf(95))
                .naturalFeatures("BEACH")
                .amenities("electricity,showers,toilets,wifi,restaurant")
                .pictures("https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800")
                .status(CampsiteStatus.ACTIVE)
                .startDate(LocalDate.of(2024, 6, 1))
                .endDate(LocalDate.of(2026, 9, 15))
                .owner(owner2).build());

        Campsite c4 = campsiteRepo.save(Campsite.builder()
                .name("Camp Ardennes — La Roche")
                .description("Forêt primaire, rivière Ourthe, accrobranche et kayak. Ambiance sauvage garantie.")
                .country("Belgique").city("La Roche-en-Ardenne").address("Route de la Forêt, 6980")
                .latitude(50.18).longitude(5.57)
                .capacity(35).type(CampsiteType.OFFICIAL)
                .pricePerNight(BigDecimal.valueOf(35))
                .naturalFeatures("FOREST,RIVER")
                .amenities("showers,toilets,fire_pit")
                .pictures("https://images.unsplash.com/photo-1533873984035-25970ab07461?w=800")
                .status(CampsiteStatus.ACTIVE)
                .startDate(LocalDate.of(2024, 3, 1))
                .endDate(LocalDate.of(2026, 11, 30))
                .owner(owner2).build());

        Campsite c5 = campsiteRepo.save(Campsite.builder()
                .name("Camp Bretagne — Quiberon")
                .description("Péninsule sauvage entre océan et dunes. Vagues, vent et couchers de soleil spectaculaires.")
                .country("France").city("Quiberon").address("Côte Sauvage, 56170")
                .latitude(47.48).longitude(-3.11)
                .capacity(50).type(CampsiteType.OFFICIAL)
                .pricePerNight(BigDecimal.valueOf(55))
                .naturalFeatures("BEACH,PLAIN")
                .amenities("electricity,showers,toilets,surf_rental")
                .pictures("https://images.unsplash.com/photo-1505118380757-91f5f5632de0?w=800")
                .status(CampsiteStatus.ACTIVE)
                .startDate(LocalDate.of(2024, 5, 15))
                .endDate(LocalDate.of(2026, 9, 30))
                .owner(owner3).build());

        Campsite c6 = campsiteRepo.save(Campsite.builder()
                .name("Camp Pyrénées — Gavarnie")
                .description("Cirque de Gavarnie à 100m. Lac de montagne, cascade géante, flore unique.")
                .country("France").city("Gavarnie").address("Route du Cirque, 65120")
                .latitude(42.73).longitude(-0.01)
                .capacity(25).type(CampsiteType.OFFICIAL)
                .pricePerNight(BigDecimal.valueOf(65))
                .naturalFeatures("MOUNTAIN,LAKE")
                .amenities("showers,toilets,guide_service")
                .pictures("https://images.unsplash.com/photo-1501854140801-50d01698950b?w=800")
                .status(CampsiteStatus.ACTIVE)
                .startDate(LocalDate.of(2024, 6, 1))
                .endDate(LocalDate.of(2026, 9, 15))
                .owner(owner3).build());

        Campsite c7 = campsiteRepo.save(Campsite.builder()
                .name("Camp Loire — Val de Loire")
                .description("Entre châteaux et vignobles. Bords de Loire, piste cyclable, calme absolu.")
                .country("France").city("Amboise").address("Route des Châteaux, 37400")
                .latitude(47.39).longitude(0.98)
                .capacity(45).type(CampsiteType.OFFICIAL)
                .pricePerNight(BigDecimal.valueOf(40))
                .naturalFeatures("RIVER,PLAIN")
                .amenities("electricity,showers,toilets,bike_rental")
                .pictures("https://images.unsplash.com/photo-1476514525535-07fb3b4ae5f1?w=800")
                .status(CampsiteStatus.ACTIVE)
                .startDate(LocalDate.of(2024, 4, 1))
                .endDate(LocalDate.of(2026, 10, 31))
                .owner(owner1).build());

        Campsite c8 = campsiteRepo.save(Campsite.builder()
                .name("Camp Vosges — Route des Crêtes")
                .description("Forêt vosgienne dense, sommets à 1400m, chalets traditionnels alsaciens.")
                .country("France").city("Munster").address("Route des Crêtes, 68140")
                .latitude(48.04).longitude(7.14)
                .capacity(30).type(CampsiteType.OFFICIAL)
                .pricePerNight(BigDecimal.valueOf(50))
                .naturalFeatures("FOREST,MOUNTAIN")
                .amenities("electricity,showers,toilets,sauna")
                .pictures("https://images.unsplash.com/photo-1448375240586-882707db888b?w=800")
                .status(CampsiteStatus.ACTIVE)
                .startDate(LocalDate.of(2024, 4, 1))
                .endDate(LocalDate.of(2026, 11, 15))
                .owner(owner2).build());

        Campsite c9 = campsiteRepo.save(Campsite.builder()
                .name("Camp Luberon — Provence")
                .description("Lavandes à perte de vue, lac de Sainte-Croix, villages perchés. Dolce vita provençale.")
                .country("France").city("Manosque").address("Route de Valensole, 04100")
                .latitude(43.83).longitude(5.78)
                .capacity(35).type(CampsiteType.OFFICIAL)
                .pricePerNight(BigDecimal.valueOf(70))
                .naturalFeatures("PLAIN,LAKE")
                .amenities("electricity,showers,toilets,pool,wifi")
                .pictures("https://images.unsplash.com/photo-1499002238440-d264edd596ec?w=800")
                .status(CampsiteStatus.ACTIVE)
                .startDate(LocalDate.of(2024, 5, 1))
                .endDate(LocalDate.of(2026, 9, 30))
                .owner(owner3).build());

        Campsite c10 = campsiteRepo.save(Campsite.builder()
                .name("Camp Normandie — Côte Fleurie")
                .description("Plage de Deauville, falaises d'Étretat à proximité. Iode et coquillages.")
                .country("France").city("Deauville").address("Boulevard de la Mer, 14800")
                .latitude(49.36).longitude(0.07)
                .capacity(55).type(CampsiteType.OFFICIAL)
                .pricePerNight(BigDecimal.valueOf(60))
                .naturalFeatures("BEACH,PLAIN")
                .amenities("electricity,showers,toilets,wifi,restaurant")
                .pictures("https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800")
                .status(CampsiteStatus.ACTIVE)
                .startDate(LocalDate.of(2024, 4, 1))
                .endDate(LocalDate.of(2026, 10, 15))
                .owner(owner1).build());

        log.info("[Seed] Created 10 campsites");

        // ── Bookings ─────────────────────────────────────────────────────────
        // Fetch campers
        User camper1 = userRepo.findByEmail("camper1@campconnect.tn").orElse(null);
        User camper2 = userRepo.findByEmail("camper2@campconnect.tn").orElse(null);
        User camper3 = userRepo.findByEmail("camper3@campconnect.tn").orElse(null);
        User camper4 = userRepo.findByEmail("camper4@campconnect.tn").orElse(null);
        // camper5 intentionally left with no bookings → tests neutral profile

        if (camper1 == null || camper2 == null || camper3 == null || camper4 == null) {
            log.warn("[Seed] Campers not found — skipping booking seed");
            return;
        }

        List<CampsiteBooking> bookings = new ArrayList<>();

        // ── camper1: FOREST lover, summer, budget ~45€ ───────────────────────
        // Profile: preferred_features={FOREST:1.0, LAKE:0.75, RIVER:0.5}, avg_price=43€, season=SUMMER
        bookings.add(booking(c1, camper1, "2024-07-05", "2024-07-10", 2, 225.00));  // FOREST,LAKE — July
        bookings.add(booking(c4, camper1, "2024-08-12", "2024-08-17", 2, 175.00));  // FOREST,RIVER — Aug
        bookings.add(booking(c8, camper1, "2024-06-20", "2024-06-25", 3, 250.00));  // FOREST,MOUNTAIN — June
        bookings.add(booking(c1, camper1, "2024-07-25", "2024-07-30", 2, 225.00));  // FOREST,LAKE — July again
        bookings.add(booking(c4, camper1, "2025-07-01", "2025-07-06", 2, 175.00));  // FOREST,RIVER — July 2025
        bookings.add(booking(c8, camper1, "2025-08-10", "2025-08-15", 2, 250.00));  // FOREST,MOUNTAIN — Aug 2025
        // Expected recommendation: C1 & C4 & C8 already visited → recommender shows c2(MOUNTAIN) c7(RIVER)...

        // ── camper2: MOUNTAIN lover, spring, budget ~70€ ─────────────────────
        // Profile: preferred_features={MOUNTAIN:1.0, RIVER:0.67, LAKE:0.67}, avg_price=70€, season=SPRING
        bookings.add(booking(c2, camper2, "2024-04-08", "2024-04-14", 2, 450.00));  // MOUNTAIN,RIVER — April
        bookings.add(booking(c6, camper2, "2024-05-15", "2024-05-20", 2, 325.00));  // MOUNTAIN,LAKE — May
        bookings.add(booking(c2, camper2, "2024-03-20", "2024-03-25", 3, 375.00));  // MOUNTAIN,RIVER — March
        bookings.add(booking(c6, camper2, "2025-04-10", "2025-04-15", 2, 325.00));  // MOUNTAIN,LAKE — April 2025
        bookings.add(booking(c8, camper2, "2025-05-03", "2025-05-08", 2, 250.00));  // FOREST,MOUNTAIN — May 2025
        // Expected: C2 & C6 & C8 visited → recommends C1 (FOREST,LAKE) close to profile

        // ── camper3: BEACH lover, summer, budget ~75€ ────────────────────────
        // Profile: preferred_features={BEACH:1.0, PLAIN:0.67}, avg_price=70€, season=SUMMER
        bookings.add(booking(c3, camper3, "2024-07-01", "2024-07-07", 4, 665.00));  // BEACH — July
        bookings.add(booking(c5, camper3, "2024-08-05", "2024-08-10", 3, 275.00));  // BEACH,PLAIN — Aug
        bookings.add(booking(c10, camper3, "2024-07-14", "2024-07-19", 2, 300.00)); // BEACH,PLAIN — July
        bookings.add(booking(c3, camper3, "2025-06-20", "2025-06-25", 2, 475.00));  // BEACH — June 2025
        bookings.add(booking(c5, camper3, "2025-08-01", "2025-08-06", 3, 275.00));  // BEACH,PLAIN — Aug 2025
        // Expected: C3 & C5 & C10 visited → recommends C9 (PLAIN,LAKE) and others

        // ── camper4: mixed LAKE + FOREST, autumn, budget ~50€ ───────────────
        // Profile: preferred_features={FOREST:1.0, LAKE:0.75, PLAIN:0.5}, avg_price=52€, season=AUTUMN
        bookings.add(booking(c1, camper4, "2024-10-05", "2024-10-09", 2, 180.00));  // FOREST,LAKE — Oct
        bookings.add(booking(c9, camper4, "2024-09-20", "2024-09-24", 2, 280.00));  // PLAIN,LAKE — Sept
        bookings.add(booking(c4, camper4, "2024-11-02", "2024-11-06", 2, 140.00));  // FOREST,RIVER — Nov
        bookings.add(booking(c7, camper4, "2025-10-10", "2025-10-14", 3, 160.00));  // RIVER,PLAIN — Oct 2025
        bookings.add(booking(c8, camper4, "2025-09-15", "2025-09-19", 2, 200.00));  // FOREST,MOUNTAIN — Sept 2025
        // Expected: C1 & C9 & C4 & C7 & C8 visited → recommends C6 (MOUNTAIN,LAKE) etc

        bookingRepo.saveAll(bookings);
        log.info("[Seed] Created {} confirmed bookings", bookings.size());
        log.info("[Seed] ─────────────────────────────────────────────────────");
        log.info("[Seed] Accounts ready (password: Dev@12345!)");
        log.info("[Seed]   Campers : camper1..5@campconnect.tn");
        log.info("[Seed]   Owners  : owner1..5@campconnect.tn");
        log.info("[Seed]   Admins  : admin1..2@campconnect.tn");
        log.info("[Seed]   camper5 has NO bookings → tests neutral recommendations");
        log.info("[Seed] ─────────────────────────────────────────────────────");
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private CampsiteBooking booking(
            Campsite campsite, User camper,
            String checkIn, String checkOut,
            int guests, double total) {

        return CampsiteBooking.builder()
                .campsite(campsite)
                .camper(camper)
                .checkInDate(LocalDate.parse(checkIn))
                .checkOutDate(LocalDate.parse(checkOut))
                .numberOfGuests(guests)
                .totalPrice(BigDecimal.valueOf(total))
                .status(CampsiteBookingStatus.CONFIRMED)
                .build();
    }
}
