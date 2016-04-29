DROP TABLE IF EXISTS casda.polarisation_component;

CREATE TABLE casda.polarisation_component (
    id                  BIGSERIAL PRIMARY KEY,
    catalogue_id        BIGINT references casda.catalogue(id),
    sbid                INTEGER references casda.observation(sbid),
    project_id          BIGINT references casda.project(id),
    component_id        VARCHAR(30),
    component_name      VARCHAR(15),
    ra_deg_cont         double precision,
    dec_deg_cont        double precision,
    flux_i_median       double precision,
    flux_q_median       double precision,
    flux_u_median       double precision,
    flux_v_median       double precision,    
    rms_i              	double precision,
    rms_q           	double precision,
    rms_u               double precision,
    rms_v               double precision,
    co_1               	double precision,
    co_2               	double precision,
    co_3               	double precision,
    co_4  				double precision,
    co_5				double precision,
    lambda_ref_sq       double precision,
    rmsf_fwhm           double precision,
    pol_peak            double precision,
    pol_peak_debias     double precision,
    pol_peak_err        double precision,
    pol_peak_fit        double precision,
    pol_peak_fit_debias double precision,
    pol_peak_fit_err    double precision,
    pol_peak_fit_snr    double precision,
    pol_peak_fit_snr_err double precision,
    fd_peak			     double precision,    
    fd_peak_err		    double precision,
    fd_peak_fit		    double precision,
    fd_peak_fit_err     double precision,
    pol_ang_ref		    double precision,
    pol_ang_ref_err     double precision,
    pol_ang_zero	    double precision,
    pol_ang_zero_err    double precision,
    pol_frac		    double precision,
    pol_frac_err 	    double precision,
    complex_1		    double precision,
    complex_2		    double precision,
    flag_p1			    boolean,
    flag_p2			    boolean,
    flag_p3			    VARCHAR(50),
    flag_p4			    VARCHAR(50),
    last_modified       TIMESTAMP DEFAULT now()
    
);

ALTER TABLE casda.polarisation_component ADD CONSTRAINT unique_polarisation_component_id UNIQUE (id);

CREATE INDEX idx_contp_foreign_catalogue_id ON casda.polarisation_component(catalogue_id);
CREATE INDEX idx_contp_foreign_sbid ON casda.polarisation_component(sbid);
CREATE INDEX idx_contp_foreign_project_id ON casda.polarisation_component(project_id);
CREATE INDEX idx_contp_component_id ON casda.polarisation_component(component_id);
CREATE INDEX idx_contp_component_name ON casda.polarisation_component(component_name);
CREATE INDEX idx_contp_ra_deg_cont ON casda.polarisation_component(ra_deg_cont);
CREATE INDEX idx_contp_dec_deg_cont ON casda.polarisation_component(dec_deg_cont);
CREATE INDEX idx_contp_flux_i_median ON casda.polarisation_component(flux_i_median);
CREATE INDEX idx_contp_flux_q_median ON casda.polarisation_component(flux_q_median);
CREATE INDEX idx_contp_flux_u_median ON casda.polarisation_component(flux_u_median);
CREATE INDEX idx_contp_flux_v_median ON casda.polarisation_component(flux_v_median);
CREATE INDEX idx_contp_lambda_ref_sq ON casda.polarisation_component(lambda_ref_sq);
CREATE INDEX idx_contp_pol_peak ON casda.polarisation_component(pol_peak);
CREATE INDEX idx_contp_pol_peak_debias ON casda.polarisation_component(pol_peak_debias);
CREATE INDEX idx_contp_pol_peak_fit ON casda.polarisation_component(pol_peak_fit);
CREATE INDEX idx_contp_pol_peak_fit_debias ON casda.polarisation_component(pol_peak_fit_debias);
CREATE INDEX idx_contp_fd_peak ON casda.polarisation_component(fd_peak);
CREATE INDEX idx_contp_fd_peak_fit ON casda.polarisation_component(fd_peak_fit);
CREATE INDEX idx_contp_pol_frac ON casda.polarisation_component(pol_frac);

COMMENT ON COLUMN casda.polarisation_component.id is 'The primary key'; 
COMMENT ON COLUMN casda.polarisation_component.catalogue_id is 'The foreign key into the Catalogue table';
COMMENT ON COLUMN casda.polarisation_component.sbid is 'The foreign key into the Observation table (using the sbid column)';
COMMENT ON COLUMN casda.polarisation_component.project_id is 'The foreign key into the Project table';
COMMENT ON COLUMN casda.polarisation_component.component_id is 'Component identifier'; 
COMMENT ON COLUMN casda.polarisation_component.component_name is 'Component name';
COMMENT ON COLUMN casda.polarisation_component.ra_deg_cont is 'J2000 right ascension in decimal degrees';
COMMENT ON COLUMN casda.polarisation_component.dec_deg_cont is 'J2000 declination in decimal degrees';  
COMMENT ON COLUMN casda.polarisation_component.flux_i_median is 'Band-median value for Stokes I spectrum';
COMMENT ON COLUMN casda.polarisation_component.flux_q_median is 'Band-median value for Stokes Q spectrum';
COMMENT ON COLUMN casda.polarisation_component.flux_u_median is 'Band-median value for Stokes U spectrum';
COMMENT ON COLUMN casda.polarisation_component.flux_v_median is 'Band-median value for Stokes V spectrum';   
COMMENT ON COLUMN casda.polarisation_component.rms_i is 'Band-median sensitivity for Stokes I spectrum';
COMMENT ON COLUMN casda.polarisation_component.rms_q is 'Band-median sensitivity for Stokes Q spectrum';
COMMENT ON COLUMN casda.polarisation_component.rms_u is 'Band-median sensitivity for Stokes U spectrum';
COMMENT ON COLUMN casda.polarisation_component.rms_v is 'Band-median sensitivity for Stokes V spectrum';
COMMENT ON COLUMN casda.polarisation_component.co_1 is 'First order coefficient for polynomial fit to Stokes I spectrum';
COMMENT ON COLUMN casda.polarisation_component.co_2 is 'Second order coefficient for polynomial fit to Stokes I spectrum';
COMMENT ON COLUMN casda.polarisation_component.co_3 is 'Third order coefficient for polynomial fit to Stokes I spectrum';
COMMENT ON COLUMN casda.polarisation_component.co_4 is 'Fourth order coefficient for polynomial fit to Stokes I spectrum ';
COMMENT ON COLUMN casda.polarisation_component.co_5 is 'Fifth order coefficient for polynomial fit to Stokes I spectrum';
COMMENT ON COLUMN casda.polarisation_component.lambda_ref_sq is 'Reference wavelength squared';
COMMENT ON COLUMN casda.polarisation_component.rmsf_fwhm  is 'Full-width at half maximum of the rotation measure spread function';
COMMENT ON COLUMN casda.polarisation_component.pol_peak is 'Peak polarised intensity in the Faraday Dispersion Function';
COMMENT ON COLUMN casda.polarisation_component.pol_peak_debias is 'Effective peak polarised intensity after correction for bias ';
COMMENT ON COLUMN casda.polarisation_component.pol_peak_err is 'Uncertainty in pol_peak';
COMMENT ON COLUMN casda.polarisation_component.pol_peak_fit is 'Peak polarised intensity from a three-point parabolic fit ';
COMMENT ON COLUMN casda.polarisation_component.pol_peak_fit_debias is 'Peak polarised intensity, corrected for bias, from a three-point parabolic fit ';
COMMENT ON COLUMN casda.polarisation_component.pol_peak_fit_err is 'Uncertainty in pol_peak_fit ';
COMMENT ON COLUMN casda.polarisation_component.pol_peak_fit_snr is 'Signal-to-noise ratio of the peak polarisation';
COMMENT ON COLUMN casda.polarisation_component.pol_peak_fit_snr_err is 'Uncertainty in pol_peak_fit_snr';
COMMENT ON COLUMN casda.polarisation_component.fd_peak  is 'Faraday Depth from the channel with the peak of the Faraday Dispersion Function';
COMMENT ON COLUMN casda.polarisation_component.fd_peak_err is 'Uncertainty in far_depth_peak';
COMMENT ON COLUMN casda.polarisation_component.fd_peak_fit is 'Faraday Depth from fit to peak in Faraday Dispersion Function';
COMMENT ON COLUMN casda.polarisation_component.fd_peak_fit_err is 'uncertainty in fd_peak_fit';
COMMENT ON COLUMN casda.polarisation_component.pol_ang_ref is 'Polarisation angle at the reference wavelength';
COMMENT ON COLUMN casda.polarisation_component.pol_ang_ref_err is 'Uncertainty in pol_ang_ref';
COMMENT ON COLUMN casda.polarisation_component.pol_ang_zero is 'Polarisation angle de-rotated to zero wavelength';
COMMENT ON COLUMN casda.polarisation_component.pol_ang_zero_err is 'Uncertainty in pol_ang_zero';
COMMENT ON COLUMN casda.polarisation_component.pol_frac is 'Fractional polarisation';
COMMENT ON COLUMN casda.polarisation_component.pol_frac_err is 'Uncertainty in fractional polarisation';
COMMENT ON COLUMN casda.polarisation_component.complex_1 is 'Statistical measure of polarisation complexity';
COMMENT ON COLUMN casda.polarisation_component.complex_2 is 'Statistical measure of polarisation complexity after removal of a thin-screen model.';
COMMENT ON COLUMN casda.polarisation_component.flag_p1	 is 'True if pol_peak_fit is above a threshold value otherwise pol_peak_fit is an upper limit.';
COMMENT ON COLUMN casda.polarisation_component.flag_p2 is 'True if FDF peak is close to edge';
COMMENT ON COLUMN casda.polarisation_component.flag_p3 is 'placeholder flag 3';
COMMENT ON COLUMN casda.polarisation_component.flag_p4 is 'placeholder flag 4';
COMMENT ON COLUMN casda.polarisation_component.last_modified is 'When the row was last modified (usually via an insert)';