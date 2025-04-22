-- First add the column as nullable
ALTER TABLE tournaments ADD COLUMN IF NOT EXISTS name VARCHAR(255);

-- Update existing records
UPDATE tournaments SET name = 'Unnamed Tournament' WHERE name IS NULL;

-- Then make the column not nullable
ALTER TABLE tournaments ALTER COLUMN name SET NOT NULL; 