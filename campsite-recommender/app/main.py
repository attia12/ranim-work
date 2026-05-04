from contextlib import asynccontextmanager
from typing import AsyncGenerator

import httpx
import structlog
from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse

from app.config import settings
from app.routers import recommendations

structlog.configure(
    wrapper_class=structlog.make_filtering_bound_logger(20),
    processors=[
        structlog.processors.TimeStamper(fmt="iso"),
        structlog.processors.add_log_level,
        structlog.processors.JSONRenderer(),
    ],
)
logger = structlog.get_logger()


@asynccontextmanager
async def lifespan(app: FastAPI) -> AsyncGenerator[None, None]:
    app.state.http_client = httpx.AsyncClient(
        timeout=httpx.Timeout(connect=3.0, read=5.0, write=5.0, pool=10.0)
    )
    logger.info("startup", service="campsite-recommender")
    yield
    await app.state.http_client.aclose()
    logger.info("shutdown", service="campsite-recommender")


app = FastAPI(
    title="Campway Recommender",
    description="Deterministic scoring engine for campsite recommendations.",
    version="1.0.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=[settings.cors_origin],
    allow_methods=["GET", "POST"],
    allow_headers=["Authorization", "Content-Type"],
    allow_credentials=True,
)


@app.get("/health", tags=["ops"])
async def health() -> dict[str, str]:
    return {"status": "ok", "service": "campsite-recommender"}


@app.exception_handler(Exception)
async def generic_exception_handler(request: Request, exc: Exception) -> JSONResponse:
    logger.error("unhandled_error", path=str(request.url), error=str(exc))
    return JSONResponse(
        status_code=500,
        content={
            "type": "https://campway.dev/problems/internal",
            "title": "Internal Server Error",
            "detail": str(exc),
            "instance": str(request.url),
        },
    )


app.include_router(recommendations.router, prefix="/api/v1/campsites")
