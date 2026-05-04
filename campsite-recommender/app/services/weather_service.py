import asyncio
import time
from dataclasses import dataclass, field

import httpx
import structlog
from tenacity import AsyncRetrying, stop_after_attempt, wait_exponential, retry_if_exception_type

logger = structlog.get_logger()

WMO_BASE_SCORES: dict[int, float] = {
    0: 1.0,
    1: 0.9,
    2: 0.9,
    3: 0.75,
    45: 0.4,
    48: 0.4,
    51: 0.5,
    53: 0.5,
    55: 0.5,
    56: 0.3,
    57: 0.3,
    61: 0.3,
    63: 0.3,
    65: 0.3,
    66: 0.3,
    67: 0.3,
    71: 0.2,
    73: 0.2,
    75: 0.2,
    77: 0.2,
    80: 0.5,
    81: 0.5,
    82: 0.5,
    95: 0.0,
    96: 0.0,
    99: 0.0,
}

WMO_DESCRIPTIONS: dict[int, str] = {
    0: "Ciel dégagé",
    1: "Principalement clair",
    2: "Partiellement nuageux",
    3: "Couvert",
    45: "Brouillard",
    48: "Brouillard givrant",
    51: "Bruine légère",
    53: "Bruine modérée",
    55: "Bruine intense",
    56: "Bruine verglaçante légère",
    57: "Bruine verglaçante intense",
    61: "Pluie légère",
    63: "Pluie modérée",
    65: "Pluie forte",
    66: "Pluie verglaçante légère",
    67: "Pluie verglaçante forte",
    71: "Neige légère",
    73: "Neige modérée",
    75: "Neige forte",
    77: "Grains de neige",
    80: "Averses légères",
    81: "Averses modérées",
    82: "Averses violentes",
    95: "Orage",
    96: "Orage avec grêle légère",
    99: "Orage avec grêle forte",
}

OPEN_METEO_URL = "https://api.open-meteo.com/v1/forecast"
CACHE_TTL_MINUTES = 10


@dataclass
class WeatherResult:
    campsite_id: int
    score: float
    description: str
    temperature: float
    weathercode: int


@dataclass
class _CacheEntry:
    result: WeatherResult
    timestamp: float = field(default_factory=time.monotonic)

    def is_fresh(self) -> bool:
        age_minutes = (time.monotonic() - self.timestamp) / 60.0
        return age_minutes < CACHE_TTL_MINUTES


def _compute_score(weathercode: int, temperature: float) -> float:
    base = WMO_BASE_SCORES.get(weathercode, 0.5)
    adjustment = 0.0
    if 18.0 <= temperature <= 28.0:
        adjustment += 0.1
    elif temperature < 5.0 or temperature > 35.0:
        adjustment -= 0.15
    return max(0.0, min(1.0, base + adjustment))


def _make_description(weathercode: int, temperature: float) -> str:
    label = WMO_DESCRIPTIONS.get(weathercode, "Inconnu")
    return f"{label}, {temperature:.0f}°C"


class WeatherService:
    def __init__(self, client: httpx.AsyncClient) -> None:
        self._client = client
        self._cache: dict[int, _CacheEntry] = {}

    async def get_weather_for_campsite(
        self, campsite_id: int, lat: float, lon: float
    ) -> WeatherResult:
        cached = self._cache.get(campsite_id)
        if cached is not None and cached.is_fresh():
            return cached.result

        result = await self._fetch_with_retry(campsite_id, lat, lon)
        self._cache[campsite_id] = _CacheEntry(result=result)
        return result

    async def _fetch_with_retry(
        self, campsite_id: int, lat: float, lon: float
    ) -> WeatherResult:
        try:
            async for attempt in AsyncRetrying(
                stop=stop_after_attempt(3),
                wait=wait_exponential(multiplier=0.5, min=0.5, max=4.0),
                retry=retry_if_exception_type((httpx.TransportError, httpx.TimeoutException)),
                reraise=False,
            ):
                with attempt:
                    return await self._do_fetch(campsite_id, lat, lon)
        except Exception as exc:
            logger.warning("weather_fetch_failed", campsite_id=campsite_id, error=str(exc))

        return WeatherResult(
            campsite_id=campsite_id,
            score=0.5,
            description="Inconnu",
            temperature=20.0,
            weathercode=-1,
        )

    async def _do_fetch(self, campsite_id: int, lat: float, lon: float) -> WeatherResult:
        start = time.monotonic()
        params = {
            "latitude": lat,
            "longitude": lon,
            "current": "weathercode,temperature_2m,windspeed_10m",
        }
        response = await self._client.get(OPEN_METEO_URL, params=params)
        duration_ms = int((time.monotonic() - start) * 1000)
        response.raise_for_status()
        data = response.json()

        current = data.get("current", {})
        weathercode = int(current.get("weathercode", -1))
        temperature = float(current.get("temperature_2m", 20.0))

        logger.info(
            "weather_fetched",
            campsite_id=campsite_id,
            url=OPEN_METEO_URL,
            duration_ms=duration_ms,
            weathercode=weathercode,
        )

        score = _compute_score(weathercode, temperature)
        description = _make_description(weathercode, temperature)

        return WeatherResult(
            campsite_id=campsite_id,
            score=score,
            description=description,
            temperature=temperature,
            weathercode=weathercode,
        )

    async def get_weather_for_campsites(
        self, campsites: list[tuple[int, float, float]]
    ) -> dict[int, WeatherResult]:
        tasks = [
            self.get_weather_for_campsite(cid, lat, lon)
            for cid, lat, lon in campsites
        ]
        results = await asyncio.gather(*tasks, return_exceptions=True)

        out: dict[int, WeatherResult] = {}
        for (cid, lat, lon), result in zip(campsites, results):
            if isinstance(result, WeatherResult):
                out[cid] = result
            else:
                out[cid] = WeatherResult(
                    campsite_id=cid,
                    score=0.5,
                    description="Inconnu",
                    temperature=20.0,
                    weathercode=-1,
                )
        return out
