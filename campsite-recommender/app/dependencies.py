import httpx
from fastapi import Request
from app.services.weather_service import WeatherService
from app.services.scoring_engine import ScoringEngine


def get_http_client(request: Request) -> httpx.AsyncClient:
    return request.app.state.http_client


def get_weather_service(request: Request) -> WeatherService:
    return WeatherService(get_http_client(request))


def get_scoring_engine() -> ScoringEngine:
    return ScoringEngine()
