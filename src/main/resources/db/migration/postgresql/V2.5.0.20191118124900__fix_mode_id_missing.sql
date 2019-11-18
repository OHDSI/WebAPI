-- Add mode_id to cohort_inclusion_result table
ALTER TABLE ${ohdsiSchema}.cohort_inclusion_result ADD COLUMN IF NOT EXISTS mode_id INT NOT NULL DEFAULT 0;

-- Add mode_id to cohort_inclusion_stats table
ALTER TABLE ${ohdsiSchema}.cohort_inclusion_stats ADD COLUMN IF NOT EXISTS mode_id INT NOT NULL DEFAULT 0;

-- Add mode_id to cohort_summary_stats table
ALTER TABLE ${ohdsiSchema}.cohort_summary_stats ADD COLUMN IF NOT EXISTS mode_id INT NOT NULL DEFAULT 0;
