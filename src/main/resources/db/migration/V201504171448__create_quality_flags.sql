-- CASDA-866
-- add a table for quality flags that indicate conditions present that can contribute to data quality issues

CREATE TABLE casda.quality_flag (  

id                  BIGSERIAL PRIMARY KEY,
display_order       INTEGER NOT NULL,
code                VARCHAR(30) NOT NULL,
label               VARCHAR(50) NOT NULL,
active              BOOLEAN NOT NULL default TRUE,
last_modified       TIMESTAMP WITH TIME ZONE DEFAULT now()
);
  
COMMENT ON TABLE casda.quality_flag is 'Quality flags to describe conditions present that can contribute to data quality issues';

COMMENT ON COLUMN casda.quality_flag.id is 'The primary key';
COMMENT ON COLUMN casda.quality_flag.display_order is 'Number representing the order to display these flags on the UI';
COMMENT ON COLUMN casda.quality_flag.code is 'A code representing the quality flag';
COMMENT ON COLUMN casda.quality_flag.label is 'The label of the quality flag, used for UI display';
COMMENT ON COLUMN casda.quality_flag.active is 'Whether the quality flag is currently active';
COMMENT ON COLUMN casda.quality_flag.last_modified is 'When the row was last modified';


-- initial values
INSERT INTO casda.quality_flag (code, label, display_order) 
values   
('RFI', 'Radio Frequency Interference', 1),
('LS', 'Low Sensitivity', 2),
('PSB', 'Poor Spectral Baselines', 3),
('CSR', 'Continuum subtraction residuals', 4),
('BN', 'Beam noise', 5),
('IF', 'Instrument failure', 6),
('OE', 'Observer error', 7),
('PIQ', 'Poor image quality', 8);
