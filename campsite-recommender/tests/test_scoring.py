import pytest
from datetime import date

from app.models.campsite import CampsiteDTO, NaturalFeature
from app.models.booking import BookingHistory
from app.models.review import ReviewHistory
from app.services.scoring_engine import ScoringEngine
from app.services.profile_builder import Season

engine = ScoringEngine()


def make_booking(
    features: list[NaturalFeature],
    price: float,
    month: int,
    campsite_id: int = 1,
) -> BookingHistory:
    return BookingHistory(
        campsite_id=campsite_id,
        campsite_name="Test Campsite",
        natural_features=features,
        price_per_night=price,
        check_in=date(2025, month, 10),
        check_out=date(2025, month, 15),
        latitude=48.85,
        longitude=2.35,
    )


def make_campsite(
    cid: int,
    features: list[NaturalFeature],
    price: float,
    rating: float = 0.0,
) -> CampsiteDTO:
    return CampsiteDTO(
        id=cid,
        name=f"Campsite {cid}",
        natural_features=features,
        price_per_night=price,
        latitude=48.0 + cid * 0.1,
        longitude=2.0 + cid * 0.1,
        global_avg_rating=rating,
        image_url=None,
    )


class TestTerrainScore:
    def test_forest_preference_beats_beach(self) -> None:
        bookings = [make_booking([NaturalFeature.FOREST], 50.0, 6) for _ in range(5)]
        profile = engine.build_user_profile(bookings, [])
        forest_campsite = make_campsite(1, [NaturalFeature.FOREST], 50.0)
        beach_campsite = make_campsite(2, [NaturalFeature.BEACH], 50.0)
        forest_bd = engine.score_campsite(forest_campsite, profile, 0.5, 6, {})
        beach_bd = engine.score_campsite(beach_campsite, profile, 0.5, 6, {})
        assert forest_bd.terrain > beach_bd.terrain

    def test_no_history_returns_neutral(self) -> None:
        profile = engine.build_user_profile([], [])
        campsite = make_campsite(1, [NaturalFeature.MOUNTAIN], 60.0)
        bd = engine.score_campsite(campsite, profile, 0.5, 6, {})
        assert bd.terrain == pytest.approx(0.3)

    def test_campsite_no_features_returns_neutral(self) -> None:
        bookings = [make_booking([NaturalFeature.LAKE], 50.0, 7)]
        profile = engine.build_user_profile(bookings, [])
        campsite = make_campsite(1, [], 50.0)
        bd = engine.score_campsite(campsite, profile, 0.5, 7, {})
        assert bd.terrain == pytest.approx(0.3)


class TestBudgetScore:
    def test_exact_match_gives_one(self) -> None:
        bookings = [make_booking([NaturalFeature.LAKE], 80.0, 7)]
        profile = engine.build_user_profile(bookings, [])
        campsite = make_campsite(1, [NaturalFeature.LAKE], 80.0)
        bd = engine.score_campsite(campsite, profile, 0.5, 7, {})
        assert bd.budget == pytest.approx(1.0)

    def test_double_price_gives_zero(self) -> None:
        bookings = [make_booking([NaturalFeature.LAKE], 50.0, 7)]
        profile = engine.build_user_profile(bookings, [])
        campsite = make_campsite(1, [NaturalFeature.LAKE], 100.0)
        bd = engine.score_campsite(campsite, profile, 0.5, 7, {})
        assert bd.budget == pytest.approx(0.0)

    def test_no_history_gives_neutral(self) -> None:
        profile = engine.build_user_profile([], [])
        campsite = make_campsite(1, [], 100.0)
        bd = engine.score_campsite(campsite, profile, 0.5, 7, {})
        assert bd.budget == pytest.approx(0.5)


class TestSeasonScore:
    def test_summer_preferred_current_summer_gives_one(self) -> None:
        bookings = [make_booking([NaturalFeature.BEACH], 40.0, 7) for _ in range(3)]
        profile = engine.build_user_profile(bookings, [])
        assert profile.preferred_season == Season.SUMMER
        campsite = make_campsite(1, [], 40.0)
        bd = engine.score_campsite(campsite, profile, 0.5, 7, {})
        assert bd.season == pytest.approx(1.0)

    def test_summer_preferred_current_winter_gives_point_two(self) -> None:
        bookings = [make_booking([NaturalFeature.BEACH], 40.0, 7) for _ in range(3)]
        profile = engine.build_user_profile(bookings, [])
        campsite = make_campsite(1, [], 40.0)
        bd = engine.score_campsite(campsite, profile, 0.5, 1, {})  # January = WINTER
        assert bd.season == pytest.approx(0.2)

    def test_adjacent_season_gives_half(self) -> None:
        bookings = [make_booking([NaturalFeature.FOREST], 50.0, 7) for _ in range(3)]
        profile = engine.build_user_profile(bookings, [])
        assert profile.preferred_season == Season.SUMMER
        campsite = make_campsite(1, [], 50.0)
        # Spring (April) is adjacent to Summer
        bd = engine.score_campsite(campsite, profile, 0.5, 4, {})
        assert bd.season == pytest.approx(0.5)

    def test_no_bookings_defaults_to_summer_preference(self) -> None:
        profile = engine.build_user_profile([], [])
        assert profile.preferred_season == Season.SUMMER


class TestEmptyHistory:
    def test_no_crash_all_neutral(self) -> None:
        profile = engine.build_user_profile([], [])
        campsite = make_campsite(1, [NaturalFeature.MOUNTAIN], 60.0)
        bd = engine.score_campsite(campsite, profile, 0.5, 6, {})
        assert 0.0 <= bd.total <= 100.0
        assert bd.total == bd.total  # not NaN
        assert bd.terrain == pytest.approx(0.3)
        assert bd.budget == pytest.approx(0.5)
        assert bd.rating == pytest.approx(0.5)


class TestRankCampsites:
    def test_top_5_from_20_are_highest(self) -> None:
        bookings = [make_booking([NaturalFeature.FOREST], 50.0, 7)]
        profile = engine.build_user_profile(bookings, [])
        campsites = [
            make_campsite(i, [NaturalFeature.FOREST], 50.0 + i * 2.0)
            for i in range(20)
        ]
        ranked = engine.rank_campsites(campsites, profile, {}, 7, {})
        assert len(ranked) == 5
        scores = [bd.total for _, bd in ranked]
        assert scores == sorted(scores, reverse=True)

    def test_less_than_5_campsites_returns_all(self) -> None:
        profile = engine.build_user_profile([], [])
        campsites = [make_campsite(i, [], 50.0) for i in range(3)]
        ranked = engine.rank_campsites(campsites, profile, {}, 7, {})
        assert len(ranked) == 3

    def test_scores_never_out_of_range(self) -> None:
        bookings = [make_booking([NaturalFeature.LAKE], 100.0, 12)]
        profile = engine.build_user_profile(bookings, [])
        campsites = [make_campsite(i, [NaturalFeature.LAKE], i * 10.0) for i in range(1, 21)]
        ranked = engine.rank_campsites(campsites, profile, {}, 12, {})
        for _, bd in ranked:
            assert 0.0 <= bd.total <= 100.0
