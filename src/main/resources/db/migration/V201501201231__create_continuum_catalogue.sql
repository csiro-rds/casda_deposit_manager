-- CASDA-3353

DROP TABLE IF EXISTS casda.continuum_component;

CREATE TABLE casda.continuum_component (
    id                  BIGSERIAL PRIMARY KEY,
    catalogue_id        BIGINT references casda.catalogue(id),
    sbid                INTEGER references casda.observation(sbid),
    project_id          BIGINT references casda.project(id),
    island_id           VARCHAR(25),
    component_id        VARCHAR(30),
    component_name      VARCHAR(15),
    ra_hms_cont         VARCHAR(16),
    dec_dms_cont        VARCHAR(16),
    ra_deg_cont         double precision,
    dec_deg_cont        double precision,
    ra_err              REAL,
    dec_err             REAL,
    freq                REAL,
    flux_peak           REAL,
    flux_peak_err       REAL,
    flux_int            REAL,
    flux_int_err        REAL,
    maj_axis            REAL,
    min_axis            REAL,
    pos_ang             REAL,
    maj_axis_err        REAL,
    min_axis_err        REAL,
    pos_ang_err         REAL,
    maj_axis_deconv     REAL,
    min_axis_deconv     REAL,
    pos_ang_deconv      REAL,
    chi_squared_fit     REAL,
    rms_fit_gauss       REAL,
    spectral_index      REAL,
    spectral_curvature  REAL,
    rms_image           REAL,
    flag_c1             SMALLINT,
    flag_c2             SMALLINT,
    flag_c3             SMALLINT,
    flag_c4             SMALLINT,
    comment             VARCHAR(1000),
    last_modified       TIMESTAMP DEFAULT now()
);

ALTER TABLE casda.continuum_component ADD CONSTRAINT unique_continuum_component_name UNIQUE (component_name);

CREATE INDEX idx_polcomp_foreign_catalogue_id ON casda.continuum_component(catalogue_id);
CREATE INDEX idx_polcomp_foreign_sbid ON casda.continuum_component(sbid);
CREATE INDEX idx_polcomp_foreign_project_id ON casda.continuum_component(project_id);
CREATE INDEX idx_polcomp_component_id ON casda.continuum_component(component_id);
CREATE INDEX idx_polcomp_component_name ON casda.continuum_component(component_name);
CREATE INDEX idx_polcomp_ra_hms_cont ON casda.continuum_component(ra_hms_cont);
CREATE INDEX idx_polcomp_dec_dms_cont ON casda.continuum_component(dec_dms_cont);
CREATE INDEX idx_polcomp_ra_deg_cont ON casda.continuum_component(ra_deg_cont);
CREATE INDEX idx_polcomp_dec_deg_cont ON casda.continuum_component(dec_deg_cont);
CREATE INDEX idx_polcomp_freq ON casda.continuum_component(freq);
CREATE INDEX idx_polcomp_flux_peak ON casda.continuum_component(flux_peak);
CREATE INDEX idx_polcomp_flux_int ON casda.continuum_component(flux_int);
CREATE INDEX idx_polcomp_spectral_index ON casda.continuum_component(spectral_index);
CREATE INDEX idx_polcomp_flag_c1 ON casda.continuum_component(flag_c1);
CREATE INDEX idx_polcomp_flag_c2 ON casda.continuum_component(flag_c2);
CREATE INDEX idx_polcomp_flag_c3 ON casda.continuum_component(flag_c3);
CREATE INDEX idx_polcomp_flag_c4 ON casda.continuum_component(flag_c4);



COMMENT ON COLUMN casda.continuum_component.id is 'The primary key'; 
COMMENT ON COLUMN casda.continuum_component.catalogue_id is 'The foreign key into the Catalogue table';
COMMENT ON COLUMN casda.continuum_component.sbid is 'The foreign key into the Observation table (using the sbid column)';
COMMENT ON COLUMN casda.continuum_component.project_id is 'The foreign key into the Project table';
COMMENT ON COLUMN casda.continuum_component.island_id is 'The identifier for the island from which this component was extracted';
COMMENT ON COLUMN casda.continuum_component.component_id is 'Identifier of the component, this identifier is specific to this image';
COMMENT ON COLUMN casda.continuum_component.component_name is 'Name to identify component on the sky';
COMMENT ON COLUMN casda.continuum_component.ra_hms_cont is 'Right Ascension .  RA in hours, mins, secs format';
COMMENT ON COLUMN casda.continuum_component.dec_dms_cont is 'Declination. Dec in degrees, arcmin, arcsec format';
COMMENT ON COLUMN casda.continuum_component.ra_deg_cont is 'Right Ascension.  RA in decimal degrees';
COMMENT ON COLUMN casda.continuum_component.dec_deg_cont is 'Declination. Dec in decimal degrees';
COMMENT ON COLUMN casda.continuum_component.ra_err is 'Error in RA';
COMMENT ON COLUMN casda.continuum_component.dec_err is 'Error in Declination';
COMMENT ON COLUMN casda.continuum_component.freq is 'Frequency at which the image was made';
COMMENT ON COLUMN casda.continuum_component.flux_peak is 'Peak Flux Density';
COMMENT ON COLUMN casda.continuum_component.flux_peak_err is 'Error in peak flux density (FLUX_PEAK)';
COMMENT ON COLUMN casda.continuum_component.flux_int is 'Integrated flux density';
COMMENT ON COLUMN casda.continuum_component.flux_int_err is 'Error in integrated flux density (FLUX_INT)';
COMMENT ON COLUMN casda.continuum_component.maj_axis is 'FWHM major axis before deconvolution';
COMMENT ON COLUMN casda.continuum_component.min_axis is 'FWHM minor axis before deconvolution';
COMMENT ON COLUMN casda.continuum_component.pos_ang is 'Position Angle before deconvolution';
COMMENT ON COLUMN casda.continuum_component.maj_axis_err is 'Error in major axis before deconvolution (MAJ_AXIS)';
COMMENT ON COLUMN casda.continuum_component.min_axis_err is 'Error in minor axis before deconvolution (MIN_AXIS)';
COMMENT ON COLUMN casda.continuum_component.pos_ang_err is 'Error in position angle before deconvolution (POS_ANG)';
COMMENT ON COLUMN casda.continuum_component.maj_axis_deconv is 'FWHM major axis after deconvolution';
COMMENT ON COLUMN casda.continuum_component.min_axis_deconv is 'FWHM minor axis after deconvolution';
COMMENT ON COLUMN casda.continuum_component.pos_ang_deconv is 'Position angle after deconvolution';
COMMENT ON COLUMN casda.continuum_component.chi_squared_fit is 'Chi-squared value of Gaussian fit';
COMMENT ON COLUMN casda.continuum_component.rms_fit_gauss is 'RMS residual of Gaussian fit';
COMMENT ON COLUMN casda.continuum_component.spectral_index is 'Spectral index (First Taylor term)';
COMMENT ON COLUMN casda.continuum_component.spectral_curvature is 'Spectral curvature (Second Taylor term)';
COMMENT ON COLUMN casda.continuum_component.rms_image is 'RMS noise level in image';
COMMENT ON COLUMN casda.continuum_component.flag_c1 is 'Placeholder field for continuum flag (1)';
COMMENT ON COLUMN casda.continuum_component.flag_c2 is 'Placeholder field for continuum flag (2)';
COMMENT ON COLUMN casda.continuum_component.flag_c3 is 'Placeholder field for continuum flag (3)';
COMMENT ON COLUMN casda.continuum_component.flag_c4 is 'Placeholder field for continuum flag (4)';
COMMENT ON COLUMN casda.continuum_component.comment is 'Comment';
COMMENT ON COLUMN casda.continuum_component.last_modified is 'When the row was last modified (usually via an insert)';

