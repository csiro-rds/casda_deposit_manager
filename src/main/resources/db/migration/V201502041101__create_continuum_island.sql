-- CASDA-3432

ALTER TABLE casda.catalogue ADD COLUMN catalogue_type VARCHAR(25);
COMMENT ON COLUMN casda.catalogue.catalogue_type is 'The catalogue type, eg continuum_component, continuum_island, etc'; 


DROP TABLE IF EXISTS casda.continuum_island;

CREATE TABLE casda.continuum_island (
    id                  BIGSERIAL PRIMARY KEY,
    catalogue_id        BIGINT references casda.catalogue(id),
    sbid                INTEGER references casda.observation(sbid),
    project_id          BIGINT references casda.project(id),
    island_id           VARCHAR(25),
    island_name         VARCHAR(15),
    n_components        INTEGER,
    ra_hms_cont         VARCHAR(16),
    dec_dms_cont        VARCHAR(16),
    ra_deg_cont         double precision,
    dec_deg_cont        double precision,
    freq                REAL,
    maj_axis            REAL,
    min_axis            REAL,
    pos_ang             REAL,
    flux_int            REAL,
    flux_peak           REAL,
    x_min               INTEGER,
    x_max               INTEGER,
    y_min               INTEGER,
    y_max               INTEGER,
    n_pix               INTEGER,
    x_ave               REAL,
    y_ave               REAL,
    x_cen               REAL,
    y_cen               REAL,
    x_peak              INTEGER,
    y_peak              INTEGER,
    flag_c1             SMALLINT,
    flag_c2             SMALLINT,
    flag_c3             SMALLINT,
    flag_c4             SMALLINT,
    comment             VARCHAR(1000),
    last_modified       TIMESTAMP WITH TIME ZONE DEFAULT now()
);

ALTER TABLE casda.continuum_island ADD CONSTRAINT unique_continuum_island_id UNIQUE (island_id);

CREATE INDEX idx_conti_foreign_catalogue_id ON casda.continuum_island(catalogue_id);
CREATE INDEX idx_conti_foreign_sbid ON casda.continuum_island(sbid);
CREATE INDEX idx_conti_foreign_project_id ON casda.continuum_island(project_id);
CREATE INDEX idx_conti_island_id ON casda.continuum_island(island_id);
CREATE INDEX idx_conti_island_name ON casda.continuum_island(island_name);
CREATE INDEX idx_conti_ra_hms_cont ON casda.continuum_island(ra_hms_cont);
CREATE INDEX idx_conti_dec_dms_cont ON casda.continuum_island(dec_dms_cont);
CREATE INDEX idx_conti_ra_deg_cont ON casda.continuum_island(ra_deg_cont);
CREATE INDEX idx_conti_dec_deg_cont ON casda.continuum_island(dec_deg_cont);
CREATE INDEX idx_conti_freq ON casda.continuum_island(freq);
CREATE INDEX idx_conti_flux_peak ON casda.continuum_island(flux_peak);
CREATE INDEX idx_conti_flux_int ON casda.continuum_island(flux_int);
CREATE INDEX idx_conti_flag_c1 ON casda.continuum_island(flag_c1);
CREATE INDEX idx_conti_flag_c2 ON casda.continuum_island(flag_c2);
CREATE INDEX idx_conti_flag_c3 ON casda.continuum_island(flag_c3);
CREATE INDEX idx_conti_flag_c4 ON casda.continuum_island(flag_c4);



COMMENT ON COLUMN casda.continuum_island.id is 'The primary key'; 
COMMENT ON COLUMN casda.continuum_island.catalogue_id is 'The foreign key into the Catalogue table';
COMMENT ON COLUMN casda.continuum_island.sbid is 'The foreign key into the Observation table (using the sbid column)';
COMMENT ON COLUMN casda.continuum_island.project_id is 'The foreign key into the Project table';
COMMENT ON COLUMN casda.continuum_island.island_id is 'The unique identifier for the island';
COMMENT ON COLUMN casda.continuum_island.island_name is 'The island name';
COMMENT ON COLUMN casda.continuum_island.n_components is 'The number of discrete components extracted from the island';
COMMENT ON COLUMN casda.continuum_island.ra_hms_cont is 'J2000 Right Ascension in hours, mins, secs (hh:mm:ss.SSS) format';
COMMENT ON COLUMN casda.continuum_island.dec_dms_cont is 'J2000 Declination in degrees, arcmin, arcsec (dd:mm:ss.SSS) format';
COMMENT ON COLUMN casda.continuum_island.ra_deg_cont is 'J2000 Right Ascension in decimal degrees';
COMMENT ON COLUMN casda.continuum_island.dec_deg_cont is 'J2000 Declination in decimal degrees';
COMMENT ON COLUMN casda.continuum_island.freq is 'Frequency at which the image was made';
COMMENT ON COLUMN casda.continuum_island.maj_axis is 'Major axis determined from detected pixels';
COMMENT ON COLUMN casda.continuum_island.min_axis is 'Minor axis determined from detected pixels';
COMMENT ON COLUMN casda.continuum_island.pos_ang is 'Position Angle of major axis (East of North)';
COMMENT ON COLUMN casda.continuum_island.flux_int is 'Integrated flux density';
COMMENT ON COLUMN casda.continuum_island.flux_peak is 'Peak Flux Density';
COMMENT ON COLUMN casda.continuum_island.x_min is 'Minimum x pixel';
COMMENT ON COLUMN casda.continuum_island.x_max is 'Maximum x pixel';
COMMENT ON COLUMN casda.continuum_island.y_min is 'Minimum y pixel';
COMMENT ON COLUMN casda.continuum_island.y_max is 'Maximum y pixel';
COMMENT ON COLUMN casda.continuum_island.n_pix is 'Number of pixels above threshold';
COMMENT ON COLUMN casda.continuum_island.x_ave is 'Average x pixel';
COMMENT ON COLUMN casda.continuum_island.y_ave is 'Average y pixel';
COMMENT ON COLUMN casda.continuum_island.x_cen is 'Flux weighted centroid x pixel';
COMMENT ON COLUMN casda.continuum_island.y_cen is 'Flux weighted centroid y pixel';
COMMENT ON COLUMN casda.continuum_island.x_peak is 'x pixel corresponding to peak flux density';
COMMENT ON COLUMN casda.continuum_island.y_peak is 'y pixel corresponding to peak flux density';
COMMENT ON COLUMN casda.continuum_island.flag_c1 is 'Placeholder field for continuum flag (1)';
COMMENT ON COLUMN casda.continuum_island.flag_c2 is 'Placeholder field for continuum flag (2)';
COMMENT ON COLUMN casda.continuum_island.flag_c3 is 'Placeholder field for continuum flag (3)';
COMMENT ON COLUMN casda.continuum_island.flag_c4 is 'Placeholder field for continuum flag (4)';
COMMENT ON COLUMN casda.continuum_island.comment is 'Comment added during validation';
COMMENT ON COLUMN casda.continuum_island.last_modified is 'When the row was last modified (usually via an insert)';

