export enum UserRole {
  CAMPER             = 'COMPERS',
  GEAR_PROVIDER      = 'EQUIPEMENTPROVIEDERS',
  CAMPSITE_OWNER     = 'COMPSITEOWNERS',
  CAMPSITE_MANAGER   = 'CAMPSITEMANAGER',
  GUIDE              = 'GUIDE',
  COACH              = 'COACH',
  SPONSOR            = 'SPONSOR',
  PARTNER            = 'PARTENERS',
  DELIVERY_PERSONNEL = 'DELIVERYAGENT',
  FORUM_MODERATOR    = 'FORUM_MODERATOR',
  EVENT_ORGANIZER    = 'EVENT_ORGANIZER',
  ADMIN              = 'ADMIN'
}

export interface User {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  role: UserRole;
  phoneNumber?: string;
  avatar?: string;
  address?: string;
  city?: string;

  // EQUIPEMENTPROVIEDERS
  companyName?: string;
  shopDescription?: string;

  // DELIVERYAGENT
  licenseNumber?: string;
  isVerified?: boolean;

  // COMPERS
  experienceLevel?: string;
  preferredTerrain?: string;
  bio?: string;
  totalTrips?: number;

  // COMPSITEOWNERS
  businessName?: string;
  businessDescription?: string;
  websiteUrl?: string;
  region?: string;

  // CAMPSITEMANAGER
  managedRegion?: string;
  managerNotes?: string;

  // GUIDE
  specialization?: string;
  yearsExperience?: number;
  languages?: string;
  certificationNumber?: string;

  // COACH
  coachingType?: string;
  certifications?: string;

  // SPONSORS
  sponsorshipDescription?: string;
  logoUrl?: string;
  industry?: string;

  // PARTENERS
  organizationName?: string;
  partnershipDescription?: string;
  partnerType?: string;

  // EVENT_ORGANIZER
  phoneContact?: string;
}
