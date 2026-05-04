export interface ScoreBreakdown {
  terrain: number;
  budget: number;
  season: number;
  rating: number;
  weather: number;
  total: number;
}

export interface RecommendedCampsite {
  campsiteId: number;
  campsiteName: string;
  score: number;
  scoreBreakdown: ScoreBreakdown;
  weatherDescription: string;
  naturalFeatures: string[];
  pricePerNight: number;
  imageUrl: string | null;
}
