-- Create Quality Grades Table
CREATE TABLE IF NOT EXISTS quality_grades (
    id BIGSERIAL PRIMARY KEY,
    commodity_id BIGINT NOT NULL,
    grade_name VARCHAR(100) NOT NULL,
    grade_code VARCHAR(50) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (commodity_id) REFERENCES commodities(id) ON DELETE CASCADE,
    UNIQUE (commodity_id, grade_code)
);

-- Create indexes for quality grades
CREATE INDEX IF NOT EXISTS idx_quality_grades_commodity_id ON quality_grades(commodity_id);
CREATE INDEX IF NOT EXISTS idx_quality_grades_grade_code ON quality_grades(grade_code);
CREATE INDEX IF NOT EXISTS idx_quality_grades_active ON quality_grades(active);
