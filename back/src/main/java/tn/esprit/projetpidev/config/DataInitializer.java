package tn.esprit.projetpidev.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import tn.esprit.projetpidev.domain.Campsite;
import tn.esprit.projetpidev.domain.CampsiteBooking;
import tn.esprit.projetpidev.domain.CampsitePayment;
import tn.esprit.projetpidev.domain.User;
import tn.esprit.projetpidev.domain.enums.CampsiteBookingStatus;
import tn.esprit.projetpidev.domain.enums.CampsitePaymentMethod;
import tn.esprit.projetpidev.domain.enums.CampsitePaymentStatus;
import tn.esprit.projetpidev.domain.enums.CampsiteStatus;
import tn.esprit.projetpidev.domain.enums.CampsiteType;
import tn.esprit.projetpidev.domain.enums.Role;
import tn.esprit.projetpidev.repositories.CampsiteBookingRepository;
import tn.esprit.projetpidev.repositories.CampsitePaymentRepository;
import tn.esprit.projetpidev.repositories.CampsiteRepository;
import tn.esprit.projetpidev.repositories.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository            userRepo;
    private final CampsiteRepository        campsiteRepo;
    private final CampsiteBookingRepository bookingRepo;
    private final CampsitePaymentRepository paymentRepo;
    private final PasswordEncoder           passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        String password = passwordEncoder.encode(
                System.getenv().getOrDefault("SEED_PASSWORD", "Dev@12345!"));

        seedUsers(password);
        seedCampsitesAndBookings();
        seedAnalyticsData();
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

    // ─────────────────────────────────────────────────────────────────────────
    // ANALYTICS SEED — payments, cancellations, fraud suspects
    // Runs only once (guard: paymentRepo.count() == 0)
    // ─────────────────────────────────────────────────────────────────────────
    private void seedAnalyticsData() {
        if (paymentRepo.count() > 0) {
            log.info("[Seed] Analytics data already seeded — skipping");
            return;
        }

        // ── Resolve campsites (must already exist) ────────────────────────────
        List<Campsite> cs = campsiteRepo.findAll();
        if (cs.size() < 10) { log.warn("[Seed] Not enough campsites for analytics seed"); return; }
        // Sort by id so indices are deterministic
        cs.sort((a, b) -> (int)(a.getId() - b.getId()));
        Campsite c1 = cs.get(0), c2 = cs.get(1), c3 = cs.get(2), c4 = cs.get(3),
                 c5 = cs.get(4), c6 = cs.get(5), c7 = cs.get(6), c8 = cs.get(7),
                 c9 = cs.get(8), c10 = cs.get(9);

        // ── Resolve campers ───────────────────────────────────────────────────
        User p1  = camper(1), p2  = camper(2), p3  = camper(3), p4  = camper(4),
             p5  = camper(5), p6  = camper(6), p7  = camper(7), p8  = camper(8),
             p9  = camper(9), p10 = camper(10);
        if (p1 == null || p10 == null) { log.warn("[Seed] Campers not found — skipping analytics"); return; }

        List<CampsiteBooking> newBookings = new ArrayList<>();
        List<CampsitePayment> payments    = new ArrayList<>();

        // ── Step 1: payments for existing recommendation bookings (2024 history)
        // These were already saved; we just add payments in bulk.
        // paidAt set to mid-2024 so they appear in totalRevenue but NOT in last-12-months chart
        List<CampsiteBooking> existing = bookingRepo.findAll();
        CampsitePaymentMethod[] methods = CampsitePaymentMethod.values();
        int mi = 0;
        for (CampsiteBooking b : existing) {
            if (b.getStatus() == CampsiteBookingStatus.CONFIRMED && b.getTotalPrice() != null) {
                payments.add(payment(b, b.getTotalPrice(), methods[mi++ % 3],
                        LocalDateTime.of(2024, 7, 10, 12, 0)));
            }
        }

        // ── Step 2: CONFIRMED bookings spread over 12 months (Jun 2025 – May 2026)
        // Each month gets 3–7 bookings to produce realistic revenue peaks in summer

        // June 2025  — 4 bookings → ~€1 830
        newBookings.add(confirmed(c3, p1,  "2025-06-05", "2025-06-10", 2, 475.00));
        newBookings.add(confirmed(c5, p2,  "2025-06-12", "2025-06-17", 3, 275.00));
        newBookings.add(confirmed(c9, p3,  "2025-06-18", "2025-06-23", 2, 350.00));
        newBookings.add(confirmed(c2, p4,  "2025-06-25", "2025-06-30", 2, 450.00));

        // July 2025  — 7 bookings → ~€3 310 (peak summer)
        newBookings.add(confirmed(c3, p5,  "2025-07-01", "2025-07-08", 4, 760.00));
        newBookings.add(confirmed(c2, p1,  "2025-07-05", "2025-07-11", 2, 525.00));
        newBookings.add(confirmed(c10,p2,  "2025-07-08", "2025-07-13", 3, 360.00));
        newBookings.add(confirmed(c6, p3,  "2025-07-14", "2025-07-20", 2, 455.00));
        newBookings.add(confirmed(c1, p4,  "2025-07-20", "2025-07-25", 2, 225.00));
        newBookings.add(confirmed(c5, p5,  "2025-07-22", "2025-07-27", 3, 550.00));
        newBookings.add(confirmed(c9, p1,  "2025-07-28", "2025-08-02", 2, 490.00));

        // August 2025 — 6 bookings → ~€2 885
        newBookings.add(confirmed(c3, p2,  "2025-08-02", "2025-08-08", 3, 665.00));
        newBookings.add(confirmed(c10,p3,  "2025-08-05", "2025-08-10", 2, 360.00));
        newBookings.add(confirmed(c2, p4,  "2025-08-10", "2025-08-16", 2, 525.00));
        newBookings.add(confirmed(c8, p5,  "2025-08-15", "2025-08-20", 3, 375.00));
        newBookings.add(confirmed(c5, p1,  "2025-08-18", "2025-08-23", 2, 440.00));
        newBookings.add(confirmed(c6, p2,  "2025-08-24", "2025-08-29", 2, 390.00));

        // September 2025 — 5 bookings → ~€2 125
        newBookings.add(confirmed(c9, p3,  "2025-09-02", "2025-09-07", 2, 490.00));
        newBookings.add(confirmed(c7, p4,  "2025-09-08", "2025-09-13", 3, 300.00));
        newBookings.add(confirmed(c4, p5,  "2025-09-14", "2025-09-19", 2, 210.00));
        newBookings.add(confirmed(c1, p1,  "2025-09-20", "2025-09-25", 2, 700.00));
        newBookings.add(confirmed(c8, p2,  "2025-09-25", "2025-09-30", 2, 250.00));

        // October 2025 — 4 bookings → ~€1 415
        newBookings.add(confirmed(c4, p3,  "2025-10-03", "2025-10-07", 2, 280.00));
        newBookings.add(confirmed(c7, p4,  "2025-10-10", "2025-10-15", 3, 480.00));
        newBookings.add(confirmed(c1, p5,  "2025-10-15", "2025-10-19", 2, 405.00));
        newBookings.add(confirmed(c8, p1,  "2025-10-22", "2025-10-26", 2, 600.00));

        // November 2025 — 3 bookings → ~€820
        newBookings.add(confirmed(c4, p2,  "2025-11-05", "2025-11-09", 2, 280.00));
        newBookings.add(confirmed(c7, p3,  "2025-11-12", "2025-11-16", 2, 240.00));
        newBookings.add(confirmed(c1, p4,  "2025-11-20", "2025-11-24", 2, 300.00));

        // December 2025 — 2 bookings → ~€635
        newBookings.add(confirmed(c4, p5,  "2025-12-05", "2025-12-09", 2, 175.00));
        newBookings.add(confirmed(c7, p1,  "2025-12-20", "2025-12-25", 3, 460.00));

        // January 2026 — 2 bookings → ~€455
        newBookings.add(confirmed(c7, p2,  "2026-01-08", "2026-01-12", 2, 240.00));
        newBookings.add(confirmed(c4, p3,  "2026-01-20", "2026-01-24", 2, 215.00));

        // February 2026 — 2 bookings → ~€525
        newBookings.add(confirmed(c8, p4,  "2026-02-07", "2026-02-12", 2, 375.00));
        newBookings.add(confirmed(c1, p5,  "2026-02-14", "2026-02-18", 2, 300.00));

        // March 2026 — 3 bookings → ~€905
        newBookings.add(confirmed(c2, p1,  "2026-03-05", "2026-03-10", 2, 375.00));
        newBookings.add(confirmed(c6, p2,  "2026-03-12", "2026-03-17", 2, 325.00));
        newBookings.add(confirmed(c9, p3,  "2026-03-20", "2026-03-25", 2, 350.00));

        // April 2026 — 4 bookings → ~€1 540
        newBookings.add(confirmed(c3, p4,  "2026-04-01", "2026-04-07", 2, 570.00));
        newBookings.add(confirmed(c2, p5,  "2026-04-08", "2026-04-14", 2, 450.00));
        newBookings.add(confirmed(c5, p1,  "2026-04-15", "2026-04-20", 3, 275.00));
        newBookings.add(confirmed(c9, p2,  "2026-04-22", "2026-04-27", 2, 490.00));

        // May 2026 — 3 bookings → ~€1 230 (current month, partial)
        newBookings.add(confirmed(c3, p3,  "2026-05-01", "2026-05-06", 2, 475.00));
        newBookings.add(confirmed(c6, p4,  "2026-05-02", "2026-05-07", 2, 390.00));
        newBookings.add(confirmed(c10,p5,  "2026-05-03", "2026-05-08", 3, 540.00));

        bookingRepo.saveAll(newBookings);

        // Build payments for the new confirmed bookings with correct paidAt months
        int idx = 0;
        LocalDateTime[] paidDates = {
            // June 2025 (4)
            ldt(2025,6,6), ldt(2025,6,13), ldt(2025,6,19), ldt(2025,6,26),
            // July 2025 (7)
            ldt(2025,7,2), ldt(2025,7,6), ldt(2025,7,9), ldt(2025,7,15),
            ldt(2025,7,21), ldt(2025,7,23), ldt(2025,7,29),
            // August 2025 (6)
            ldt(2025,8,3), ldt(2025,8,6), ldt(2025,8,11), ldt(2025,8,16),
            ldt(2025,8,19), ldt(2025,8,25),
            // September 2025 (5)
            ldt(2025,9,3), ldt(2025,9,9), ldt(2025,9,15), ldt(2025,9,21), ldt(2025,9,26),
            // October 2025 (4)
            ldt(2025,10,4), ldt(2025,10,11), ldt(2025,10,16), ldt(2025,10,23),
            // November 2025 (3)
            ldt(2025,11,6), ldt(2025,11,13), ldt(2025,11,21),
            // December 2025 (2)
            ldt(2025,12,6), ldt(2025,12,21),
            // January 2026 (2)
            ldt(2026,1,9), ldt(2026,1,21),
            // February 2026 (2)
            ldt(2026,2,8), ldt(2026,2,15),
            // March 2026 (3)
            ldt(2026,3,6), ldt(2026,3,13), ldt(2026,3,21),
            // April 2026 (4)
            ldt(2026,4,2), ldt(2026,4,9), ldt(2026,4,16), ldt(2026,4,23),
            // May 2026 (3)
            ldt(2026,5,2), ldt(2026,5,3), ldt(2026,5,4)
        };
        CampsitePaymentMethod[] pm = { CampsitePaymentMethod.CARD, CampsitePaymentMethod.PAYPAL,
                                       CampsitePaymentMethod.BANK_TRANSFER, CampsitePaymentMethod.CARD,
                                       CampsitePaymentMethod.CARD, CampsitePaymentMethod.PAYPAL };
        for (CampsiteBooking b : newBookings) {
            payments.add(payment(b, b.getTotalPrice(), pm[idx % pm.length], paidDates[idx]));
            idx++;
        }

        // ── Step 3: CANCELLED bookings for fraud suspects ─────────────────────
        // camper6: 7 cancellations
        List<CampsiteBooking> cancelled = new ArrayList<>();
        cancelled.add(cancel(c1, p6,  "2025-06-10", "2025-06-15", "Changed plans"));
        cancelled.add(cancel(c3, p6,  "2025-07-05", "2025-07-10", "Found cheaper option"));
        cancelled.add(cancel(c5, p6,  "2025-08-01", "2025-08-06", "Family emergency"));
        cancelled.add(cancel(c7, p6,  "2025-09-10", "2025-09-15", "Work conflict"));
        cancelled.add(cancel(c2, p6,  "2025-10-05", "2025-10-09", "Changed plans"));
        cancelled.add(cancel(c9, p6,  "2025-11-01", "2025-11-05", "Changed plans"));
        cancelled.add(cancel(c4, p6,  "2026-01-15", "2026-01-19", "No reason given"));

        // camper7: 6 cancellations
        cancelled.add(cancel(c2, p7,  "2025-07-10", "2025-07-15", "Changed plans"));
        cancelled.add(cancel(c6, p7,  "2025-08-05", "2025-08-10", "Price issue"));
        cancelled.add(cancel(c8, p7,  "2025-09-15", "2025-09-20", "Changed plans"));
        cancelled.add(cancel(c10,p7,  "2025-10-10", "2025-10-15", "Work conflict"));
        cancelled.add(cancel(c3, p7,  "2025-12-01", "2025-12-05", "Changed plans"));
        cancelled.add(cancel(c1, p7,  "2026-02-10", "2026-02-14", "Changed plans"));

        // camper8: 6 cancellations
        cancelled.add(cancel(c4, p8,  "2025-06-20", "2025-06-24", "Bad weather forecast"));
        cancelled.add(cancel(c7, p8,  "2025-07-18", "2025-07-22", "Changed plans"));
        cancelled.add(cancel(c9, p8,  "2025-08-20", "2025-08-25", "Illness"));
        cancelled.add(cancel(c1, p8,  "2025-10-20", "2025-10-24", "Changed plans"));
        cancelled.add(cancel(c5, p8,  "2026-01-08", "2026-01-12", "Changed plans"));
        cancelled.add(cancel(c2, p8,  "2026-03-15", "2026-03-20", "Changed plans"));

        // camper9: 5 cancellations
        cancelled.add(cancel(c3, p9,  "2025-07-25", "2025-07-30", "Price issue"));
        cancelled.add(cancel(c6, p9,  "2025-09-05", "2025-09-10", "Changed plans"));
        cancelled.add(cancel(c10,p9,  "2025-11-10", "2025-11-14", "Work conflict"));
        cancelled.add(cancel(c8, p9,  "2026-02-20", "2026-02-24", "Changed plans"));
        cancelled.add(cancel(c4, p9,  "2026-04-10", "2026-04-14", "No reason given"));

        // camper10: 5 cancellations
        cancelled.add(cancel(c5, p10, "2025-08-12", "2025-08-17", "Changed plans"));
        cancelled.add(cancel(c2, p10, "2025-10-01", "2025-10-05", "Found cheaper option"));
        cancelled.add(cancel(c7, p10, "2025-12-10", "2025-12-14", "Family emergency"));
        cancelled.add(cancel(c9, p10, "2026-01-25", "2026-01-29", "Changed plans"));
        cancelled.add(cancel(c1, p10, "2026-03-10", "2026-03-14", "Changed plans"));

        bookingRepo.saveAll(cancelled);

        // ── Step 4: PENDING bookings (waiting for owner confirmation) ──────────
        List<CampsiteBooking> pending = new ArrayList<>();
        pending.add(pending(c3, p1, "2026-05-15", "2026-05-20", 2, 475.00));
        pending.add(pending(c2, p2, "2026-05-18", "2026-05-23", 2, 375.00));
        pending.add(pending(c6, p3, "2026-05-20", "2026-05-25", 3, 325.00));
        pending.add(pending(c9, p4, "2026-05-22", "2026-05-27", 2, 490.00));
        pending.add(pending(c10,p5, "2026-05-25", "2026-05-30", 2, 360.00));
        bookingRepo.saveAll(pending);

        paymentRepo.saveAll(payments);

        log.info("[Seed] Analytics seeded: {} new confirmed bookings, {} payments, {} cancellations, {} pending",
                newBookings.size(), payments.size(), cancelled.size(), pending.size());
        log.info("[Seed] Total seeded revenue spans Jun 2025 – May 2026 (~€17 600)");
        log.info("[Seed] Fraud suspects: camper6-10 (5-7 cancellations each)");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User camper(int i) {
        return userRepo.findByEmail("camper" + i + "@campconnect.tn").orElse(null);
    }

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

    private CampsiteBooking confirmed(Campsite c, User u, String in, String out, int guests, double total) {
        return CampsiteBooking.builder()
                .campsite(c).camper(u)
                .checkInDate(LocalDate.parse(in)).checkOutDate(LocalDate.parse(out))
                .numberOfGuests(guests).totalPrice(BigDecimal.valueOf(total))
                .status(CampsiteBookingStatus.CONFIRMED)
                .build();
    }

    private CampsiteBooking cancel(Campsite c, User u, String in, String out, String reason) {
        return CampsiteBooking.builder()
                .campsite(c).camper(u)
                .checkInDate(LocalDate.parse(in)).checkOutDate(LocalDate.parse(out))
                .numberOfGuests(2).totalPrice(BigDecimal.ZERO)
                .status(CampsiteBookingStatus.CANCELLED)
                .cancellationReason(reason)
                .build();
    }

    private CampsiteBooking pending(Campsite c, User u, String in, String out, int guests, double total) {
        return CampsiteBooking.builder()
                .campsite(c).camper(u)
                .checkInDate(LocalDate.parse(in)).checkOutDate(LocalDate.parse(out))
                .numberOfGuests(guests).totalPrice(BigDecimal.valueOf(total))
                .status(CampsiteBookingStatus.PENDING)
                .build();
    }

    private CampsitePayment payment(CampsiteBooking booking, BigDecimal amount,
                                    CampsitePaymentMethod method, LocalDateTime paidAt) {
        return CampsitePayment.builder()
                .booking(booking)
                .amount(amount)
                .method(method)
                .status(CampsitePaymentStatus.PAID)
                .transactionId("TXN-" + booking.getId() + "-" + paidAt.getYear())
                .referenceCode("REF-CW-" + paidAt.getYear() + paidAt.getMonthValue())
                .paidAt(paidAt)
                .build();
    }

    private LocalDateTime ldt(int year, int month, int day) {
        return LocalDateTime.of(year, month, day, 10, 0);
    }
}
