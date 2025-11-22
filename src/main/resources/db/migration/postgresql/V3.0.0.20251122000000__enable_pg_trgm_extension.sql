-- =====================================================================
-- Migration: Enable pg_trgm extension for concept search optimization
-- Feature: 001-concept-search-optimization
-- Date: 2025-11-22
-- OHDSI WebAPI 3.0.0: PostgreSQL pg_trgm extension enablement (transactional)
-- =====================================================================

-- Enable pg_trgm extension (idempotent, transactional)
-- This extension provides trigram-based similarity functions and GIN/GiST index support
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- =====================================================================
-- Extension enabled. Next migration will create GIN trigram indexes.
-- =====================================================================
