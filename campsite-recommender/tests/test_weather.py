import pytest
import httpx

from app.services.weather_service import WeatherService, _compute_score

SUNNY_PAYLOAD = {
    "current": {
        "weathercode": 0,
        "temperature_2m": 22.0,
        "windspeed_10m": 10.0,
    }
}

STORM_PAYLOAD = {
    "current": {
        "weathercode": 95,
        "temperature_2m": 18.0,
        "windspeed_10m": 55.0,
    }
}

RAIN_PAYLOAD = {
    "current": {
        "weathercode": 65,
        "temperature_2m": 14.0,
        "windspeed_10m": 20.0,
    }
}


def make_mock_client(payload: dict) -> httpx.AsyncClient:
    def handler(request: httpx.Request) -> httpx.Response:
        return httpx.Response(200, json=payload)

    return httpx.AsyncClient(transport=httpx.MockTransport(handler))


def make_timeout_client() -> httpx.AsyncClient:
    def handler(request: httpx.Request) -> httpx.Response:
        raise httpx.TimeoutException("timeout", request=request)

    return httpx.AsyncClient(transport=httpx.MockTransport(handler))


def make_error_client() -> httpx.AsyncClient:
    def handler(request: httpx.Request) -> httpx.Response:
        return httpx.Response(500)

    return httpx.AsyncClient(transport=httpx.MockTransport(handler))


class TestComputeScore:
    def test_clear_sky_warm_is_max(self) -> None:
        score = _compute_score(0, 22.0)
        assert score == pytest.approx(1.0)

    def test_thunderstorm_is_zero(self) -> None:
        score = _compute_score(95, 18.0)
        assert score == pytest.approx(0.0)

    def test_cold_penalty(self) -> None:
        # Clear sky (1.0) but very cold (penalty -0.15) → 0.85
        score = _compute_score(0, 2.0)
        assert score == pytest.approx(0.85)

    def test_very_hot_penalty(self) -> None:
        score = _compute_score(0, 40.0)
        assert score == pytest.approx(0.85)

    def test_unknown_code_defaults_to_half(self) -> None:
        score = _compute_score(999, 22.0)
        assert score == pytest.approx(0.6)  # 0.5 + 0.1 bonus (temp 22)

    def test_score_never_exceeds_one(self) -> None:
        score = _compute_score(0, 22.0)
        assert score <= 1.0

    def test_score_never_below_zero(self) -> None:
        score = _compute_score(95, 2.0)
        assert score >= 0.0


@pytest.mark.asyncio
async def test_sunny_conditions_score_high() -> None:
    client = make_mock_client(SUNNY_PAYLOAD)
    svc = WeatherService(client)
    result = await svc.get_weather_for_campsite(1, 48.85, 2.35)
    assert result.score >= 0.9
    assert result.weathercode == 0
    assert "22" in result.description
    await client.aclose()


@pytest.mark.asyncio
async def test_thunderstorm_score_is_zero() -> None:
    client = make_mock_client(STORM_PAYLOAD)
    svc = WeatherService(client)
    result = await svc.get_weather_for_campsite(1, 48.85, 2.35)
    assert result.score == pytest.approx(0.0, abs=0.15)
    await client.aclose()


@pytest.mark.asyncio
async def test_api_timeout_returns_safe_default() -> None:
    client = make_timeout_client()
    svc = WeatherService(client)
    result = await svc.get_weather_for_campsite(1, 0.0, 0.0)
    assert result.score == pytest.approx(0.5)
    assert result.description == "Inconnu"
    assert result.campsite_id == 1
    await client.aclose()


@pytest.mark.asyncio
async def test_api_error_returns_safe_default() -> None:
    client = make_error_client()
    svc = WeatherService(client)
    result = await svc.get_weather_for_campsite(2, 0.0, 0.0)
    assert result.score == pytest.approx(0.5)
    await client.aclose()


@pytest.mark.asyncio
async def test_concurrent_fetch_returns_all_results() -> None:
    client = make_mock_client(SUNNY_PAYLOAD)
    svc = WeatherService(client)
    campsites = [(i, float(i), float(i)) for i in range(10)]
    results = await svc.get_weather_for_campsites(campsites)
    assert len(results) == 10
    for i in range(10):
        assert i in results
        assert results[i].campsite_id == i
    await client.aclose()


@pytest.mark.asyncio
async def test_cache_returns_same_result() -> None:
    client = make_mock_client(SUNNY_PAYLOAD)
    svc = WeatherService(client)
    r1 = await svc.get_weather_for_campsite(5, 10.0, 10.0)
    r2 = await svc.get_weather_for_campsite(5, 10.0, 10.0)
    assert r1 is r2  # same object from cache
    await client.aclose()
