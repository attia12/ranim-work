from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        case_sensitive=False,
    )

    cors_origin: str = "http://localhost:9099"
    host: str = "0.0.0.0"
    port: int = 8001
    log_level: str = "INFO"


settings = Settings()
