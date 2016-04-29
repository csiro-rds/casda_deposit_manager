--CASDA-2042--

CREATE table casda.spectral_line_emission (
	id BIGSERIAL PRIMARY KEY,
	catalogue_id  BIGINT references casda.catalogue(id),
	sbid    INTEGER references casda.observation(sbid),
	project_id    BIGINT references casda.project(id),
    object_id VARCHAR(27),
    object_name VARCHAR(15),
    ra_hms_w VARCHAR(11),
    dec_dms_w VARCHAR(11),
    ra_deg_w DOUBLE PRECISION,
    ra_deg_w_err REAL,
    dec_deg_w DOUBLE PRECISION,
    dec_deg_w_err REAL,
    ra_deg_uw DOUBLE PRECISION,
    ra_deg_uw_err REAL,
    dec_deg_uw DOUBLE PRECISION,
    dec_deg_uw_err REAL,
    glong_w DOUBLE PRECISION,
    glong_w_err REAL,
    glat_w DOUBLE PRECISION,
    glat_w_err REAL,
    glong_uw DOUBLE PRECISION,
    glong_uw_err REAL,
    glat_uw DOUBLE PRECISION,
    glat_uw_err REAL,
    maj_axis REAL,
    min_axis REAL,
    pos_ang REAL,
    maj_axis_fit REAL,
    maj_axis_fit_err REAL,
    min_axis_fit REAL,
    min_axis_fit_err REAL,
    pos_ang_fit REAL,
    pos_ang_fit_err REAL,
    size_x INTEGER,
    size_y INTEGER,
    size_z INTEGER,
    n_vox INTEGER,
    asymmetry_2d REAL,
    asymmetry_2d_err REAL,
    asymmetry_3d REAL,
    asymmetry_3d_err REAL,
    freq_uw DOUBLE PRECISION,
    freq_uw_err DOUBLE PRECISION,
    freq_w DOUBLE PRECISION,
    freq_w_err DOUBLE PRECISION,
    freq_peak DOUBLE PRECISION,
    vel_uw REAL,
    vel_uw_err REAL,
    vel_w REAL,
    vel_w_err REAL,
    vel_peak REAL,
    integ_flux REAL,
    integ_flux_err REAL,
    flux_voxel_max REAL,
    flux_voxel_min REAL,
    flux_voxel_mean REAL,
    flux_voxel_stdev REAL,
    flux_voxel_rms REAL,
    rms_imagecube REAL,
    w50_freq REAL,
    w50_freq_err REAL,
    cw50_freq REAL,
    cw50_freq_err REAL,
    w20_freq REAL,
    w20_freq_err REAL,
    cw20_freq REAL,
    cw20_freq_err REAL,
    w50_vel REAL,
    w50_vel_err REAL,
    cw50_vel REAL,
    cw50_vel_err REAL,
    w20_vel REAL,
    w20_vel_err REAL,
    cw20_vel REAL,
    cw20_vel_err REAL,
    freq_w50_clip_uw DOUBLE PRECISION,
    freq_w50_clip_uw_err DOUBLE PRECISION,
    freq_cw50_clip_uw DOUBLE PRECISION,
    freq_cw50_clip_uw_err DOUBLE PRECISION,
    freq_w20_clip_uw DOUBLE PRECISION,
    freq_w20_clip_uw_err DOUBLE PRECISION,
    freq_cw20_clip_uw DOUBLE PRECISION,
    freq_cw20_clip_uw_err DOUBLE PRECISION,
    vel_w50_clip_uw REAL,
    vel_w50_clip_uw_err REAL,
    vel_cw50_clip_uw REAL,
    vel_cw50_clip_uw_err REAL,
    vel_w20_clip_uw REAL,
    vel_w20_clip_uw_err REAL,
    vel_cw20_clip_uw REAL,
    vel_cw20_clip_uw_err REAL,
    freq_w50_clip_w DOUBLE PRECISION,
    freq_w50_clip_w_err DOUBLE PRECISION,
    freq_cw50_clip_w DOUBLE PRECISION,
    freq_cw50_clip_w_err DOUBLE PRECISION,
    freq_w20_clip_w DOUBLE PRECISION,
    freq_w20_clip_w_err DOUBLE PRECISION,
    freq_cw20_clip_w DOUBLE PRECISION,
    freq_cw20_clip_w_err DOUBLE PRECISION,
    vel_w50_clip_w REAL,
    vel_w50_clip_w_err REAL,
    vel_cw50_clip_w REAL,
    vel_cw50_clip_w_err REAL,
    vel_w20_clip_w REAL,
    vel_w20_clip_w_err REAL,
    vel_cw20_clip_w REAL,
    vel_cw20_clip_w_err REAL,
    integ_flux_w50_clip REAL,
    integ_flux_w50_clip_err REAL,
    integ_flux_cw50_clip REAL,
    integ_flux_cw50_clip_err REAL,
    integ_flux_w20_clip REAL,
    integ_flux_w20_clip_err REAL,
    integ_flux_cw20_clip REAL,
    integ_flux_cw20_clip_err REAL,
    bf_a REAL,
    bf_a_err REAL,
    bf_w DOUBLE PRECISION,
    bf_w_err DOUBLE PRECISION,
    bf_b1 REAL,
    bf_b1_err REAL,
    bf_b2 REAL,
    bf_b2_err REAL,
    bf_xe DOUBLE PRECISION,
    bf_xe_err DOUBLE PRECISION,
    bf_xp DOUBLE PRECISION,
    bf_xp_err DOUBLE PRECISION,
    bf_c REAL,
    bf_c_err REAL,
    bf_n REAL,
    bf_n_err REAL,
    flag_s1 INTEGER,
    flag_s2 INTEGER,
    flag_s3 INTEGER,
    quality_level character varying(15) DEFAULT 'NOT_VALIDATED'::character varying,
 	released_date timestamp with time zone,
    last_modified       TIMESTAMP WITH TIME ZONE DEFAULT now()
);


CREATE INDEX ON casda.spectral_line_emission(id);
CREATE INDEX ON casda.spectral_line_emission(catalogue_id); 
CREATE INDEX ON casda.spectral_line_emission(sbid); 
CREATE INDEX ON casda.spectral_line_emission(project_id); 
CREATE INDEX ON casda.spectral_line_emission(object_id); 
CREATE INDEX ON casda.spectral_line_emission(object_name);
CREATE INDEX ON casda.spectral_line_emission(ra_hms_w);
CREATE INDEX ON casda.spectral_line_emission(dec_dms_w);
CREATE INDEX ON casda.spectral_line_emission(ra_deg_w);
CREATE INDEX ON casda.spectral_line_emission(dec_deg_w);
CREATE INDEX ON casda.spectral_line_emission(ra_deg_uw);
CREATE INDEX ON casda.spectral_line_emission(dec_deg_uw);
CREATE INDEX ON casda.spectral_line_emission(glong_w);
CREATE INDEX ON casda.spectral_line_emission(glat_w);
CREATE INDEX ON casda.spectral_line_emission(glong_uw);
CREATE INDEX ON casda.spectral_line_emission(glat_uw);
CREATE INDEX ON casda.spectral_line_emission(freq_uw);
CREATE INDEX ON casda.spectral_line_emission(freq_w);
CREATE INDEX ON casda.spectral_line_emission(freq_peak);
CREATE INDEX ON casda.spectral_line_emission(vel_uw);
CREATE INDEX ON casda.spectral_line_emission(vel_w);
CREATE INDEX ON casda.spectral_line_emission(vel_peak);
CREATE INDEX ON casda.spectral_line_emission(integ_flux);
CREATE INDEX ON casda.spectral_line_emission(flux_voxel_max);
CREATE INDEX ON casda.spectral_line_emission(rms_imagecube);
CREATE INDEX ON casda.spectral_line_emission(flag_s1);
CREATE INDEX ON casda.spectral_line_emission(flag_s2);
CREATE INDEX ON casda.spectral_line_emission(flag_s3);


COMMENT ON TABLE casda.spectral_line_emission is 'Describes each spectral line emission to be archived';
--Identifiers
COMMENT ON COLUMN casda.spectral_line_emission.id is 'Primary key unique identifier'; 
COMMENT ON COLUMN casda.spectral_line_emission.catalogue_id is 'Foreign key to the catalogue table'; 
COMMENT ON COLUMN casda.spectral_line_emission.sbid is 'The foreign key into the Observation table (using the sbid column)';
COMMENT ON COLUMN casda.spectral_line_emission.project_id is 'The foreign key into the Project table';
COMMENT ON COLUMN casda.spectral_line_emission.object_id is 'Object identifier';
COMMENT ON COLUMN casda.spectral_line_emission.object_name is 'Object name';
--position related
COMMENT ON COLUMN casda.spectral_line_emission.ra_hms_w is 'J2000 RA (hh:mm:ss.sss). Weighted mean RA from all voxels that comprise this object. The weight is the voxel flux density. ';
COMMENT ON COLUMN casda.spectral_line_emission.dec_dms_w is 'J2000 Dec (dd:mm:ss.ss). Weighted mean declination from all voxels that comprise this object. The weight is the voxel flux density. ';
COMMENT ON COLUMN casda.spectral_line_emission.ra_deg_w is 'J2000 RA in decimal degrees. Weighted mean RA from all voxels that comprise this object. The weight is the voxel flux density. ';
COMMENT ON COLUMN casda.spectral_line_emission.ra_deg_w_err is 'Error in ra_deg_w';
COMMENT ON COLUMN casda.spectral_line_emission.dec_deg_w is 'J2000 Dec in decimal degrees. Weighted mean Dec from all voxels that comprise this object. The weight is the voxel flux density. ';
COMMENT ON COLUMN casda.spectral_line_emission.dec_deg_w_err is 'Error in dec_deg_w';
COMMENT ON COLUMN casda.spectral_line_emission.ra_deg_uw is 'J2000 RA in decimal degrees. Unweighted mean RA from all voxels that comprise this object.';
COMMENT ON COLUMN casda.spectral_line_emission.ra_deg_uw_err is 'Error in ra_deg_uw';
COMMENT ON COLUMN casda.spectral_line_emission.dec_deg_uw is 'J2000 Dec in decimal degrees Unweighted mean declination from all voxels that comprise this object. ';
COMMENT ON COLUMN casda.spectral_line_emission.dec_deg_uw_err is 'Error in dec_deg_uw';
COMMENT ON COLUMN casda.spectral_line_emission.glong_w is 'Galactic longitude determined from ra_deg_w and dec_deg_w';
COMMENT ON COLUMN casda.spectral_line_emission.glong_w_err is 'Error in glong_w';
COMMENT ON COLUMN casda.spectral_line_emission.glat_w is 'Galactic latitude determined from ra_deg_w and dec_deg_w';
COMMENT ON COLUMN casda.spectral_line_emission.glat_w_err is 'Error in glat_w';
COMMENT ON COLUMN casda.spectral_line_emission.glong_uw is 'Galactic longitude determined from ra_deg_unweighted and dec_deg_unweighted';
COMMENT ON COLUMN casda.spectral_line_emission.glong_uw_err is 'Error in glong_uw';
COMMENT ON COLUMN casda.spectral_line_emission.glat_uw is 'Galactic latitude determined from ra_deg_unweighted and dec_deg_unweighted';
COMMENT ON COLUMN casda.spectral_line_emission.glat_uw_err is 'Error in glat_uw';
--shape-related
COMMENT ON COLUMN casda.spectral_line_emission.maj_axis is 'Major axis determined from moment-0 map of detected pixels';
COMMENT ON COLUMN casda.spectral_line_emission.min_axis is 'Minor axis determined from moment-0 map of detected pixels';
COMMENT ON COLUMN casda.spectral_line_emission.pos_ang is 'Position angle of major axis (East of North)';
COMMENT ON COLUMN casda.spectral_line_emission.maj_axis_fit is 'Major axis determined from Gaussian fit to moment-0 map';
COMMENT ON COLUMN casda.spectral_line_emission.maj_axis_fit_err is 'Error in maj_axis_fit';
COMMENT ON COLUMN casda.spectral_line_emission.min_axis_fit is 'Minor axis determined from Gaussian fit to moment-0 map';
COMMENT ON COLUMN casda.spectral_line_emission.min_axis_fit_err is 'Error in min_axis_fit';
COMMENT ON COLUMN casda.spectral_line_emission.pos_ang_fit is 'Position angle of fitted major axis (East of North) from Gaussian fit to moment-0 map';
COMMENT ON COLUMN casda.spectral_line_emission.pos_ang_fit_err is 'Error in pos_ang_fit';
COMMENT ON COLUMN casda.spectral_line_emission.size_x is 'Size of bounding box of detected voxels in the x-direction';
COMMENT ON COLUMN casda.spectral_line_emission.size_y is 'Size of bounding box of detected voxels in the y-direction';
COMMENT ON COLUMN casda.spectral_line_emission.size_z is 'Size of bounding box of detected voxels in the z-direction (spectral)';
COMMENT ON COLUMN casda.spectral_line_emission.n_vox is 'Total number of detected voxels';
COMMENT ON COLUMN casda.spectral_line_emission.asymmetry_2d is 'Asymetry in flux of the 2D moment-0 map, between 0 (uniform) and 1';
COMMENT ON COLUMN casda.spectral_line_emission.asymmetry_2d_err is 'Error in asymmetry_2d';
COMMENT ON COLUMN casda.spectral_line_emission.asymmetry_3d is 'Asymetry in flux of the 3D distribution of voxels, between 0 (uniform) and 1';
COMMENT ON COLUMN casda.spectral_line_emission.asymmetry_3d_err is 'Error in asymmetry_3d';
--spectral location simple
COMMENT ON COLUMN casda.spectral_line_emission.freq_uw is 'Unweighted mean frequency from all voxels that comprise this object. ';
COMMENT ON COLUMN casda.spectral_line_emission.freq_uw_err is 'Error in freq_uw';
COMMENT ON COLUMN casda.spectral_line_emission.freq_w is 'Weighted mean frequency from all voxels that comprise the object. The weight is the voxel flux density';
COMMENT ON COLUMN casda.spectral_line_emission.freq_w_err is 'Error in freq_w';
COMMENT ON COLUMN casda.spectral_line_emission.freq_peak is 'Frequency of the peak in the integrated spectrum ';
COMMENT ON COLUMN casda.spectral_line_emission.vel_uw is 'Velocity of the object (optical convention and barycentric frame of reference) derived from the unweighted mean frequency from all voxels that comprise this object.';
COMMENT ON COLUMN casda.spectral_line_emission.vel_uw_err is 'Error in vel_uw';
COMMENT ON COLUMN casda.spectral_line_emission.vel_w is 'Velocity of the object (optical convention and barycentric frame of reference) derIved from the weighted mean frequency of all voxels that comprise the object. The weight is the voxel flux density.';
COMMENT ON COLUMN casda.spectral_line_emission.vel_w_err is 'Error in vel_w';
COMMENT ON COLUMN casda.spectral_line_emission.vel_peak is 'Velocity of the object (optical convention and barycentric frame of reference) derived from the frequency of the peak in the integrated spectrum';
--flux related simple
COMMENT ON COLUMN casda.spectral_line_emission.integ_flux is 'Integrated flux density calculated by summing the flux densities for all voxels that comprise the object and correcting for the beam area. ';
COMMENT ON COLUMN casda.spectral_line_emission.integ_flux_err is 'Error in integ_flux';
COMMENT ON COLUMN casda.spectral_line_emission.flux_voxel_min is 'Minimum voxel flux density for voxels that comprise the object';
COMMENT ON COLUMN casda.spectral_line_emission.flux_voxel_max is 'Maximum voxel flux density for voxels that comprise the object';
COMMENT ON COLUMN casda.spectral_line_emission.flux_voxel_mean is 'Mean voxel flux density for voxels that comprise the object';
COMMENT ON COLUMN casda.spectral_line_emission.flux_voxel_stdev is 'Standard deviation flux density for voxels for comprise the object';
COMMENT ON COLUMN casda.spectral_line_emission.flux_voxel_rms is 'rms flux density for voxels for comprise the object';
COMMENT ON COLUMN casda.spectral_line_emission.rms_imagecube is 'Estimate of the rms noise level in the data cube in the vicinity of this object';
--Spectral widths
COMMENT ON COLUMN casda.spectral_line_emission.w50_freq is 'Frequency width using voxels with flux density above 50% of the peak flux density in the integrated spectrum of the object';
COMMENT ON COLUMN casda.spectral_line_emission.w50_freq_err is 'Error in w50_freq';
COMMENT ON COLUMN casda.spectral_line_emission.cw50_freq is 'Frequency width measured from the integrated spectrum''s cumulative flux distribution, using bounds taken from where a Gaussian profile is above 50% of its peak flux density';
COMMENT ON COLUMN casda.spectral_line_emission.cw50_freq_err is 'Error in cw50_freq';
COMMENT ON COLUMN casda.spectral_line_emission.w20_freq is 'Frequency width using voxels with flux density above 20% of the peak flux density in the integrated spectrum of the object';
COMMENT ON COLUMN casda.spectral_line_emission.w20_freq_err is 'Error in w20_freq';
COMMENT ON COLUMN casda.spectral_line_emission.cw20_freq is 'Frequency width measured from the integrated spectrum''s cumulative flux distribution, using bounds taken from where a Gaussian profile is above 20% of its peak flux density';
COMMENT ON COLUMN casda.spectral_line_emission.cw20_freq_err is 'Error in cw20_freq';
COMMENT ON COLUMN casda.spectral_line_emission.w50_vel is 'Velocity width using voxels with flux density above 50% of the peak flux density in the integrated spectrum of the object';
COMMENT ON COLUMN casda.spectral_line_emission.w50_vel_err is 'Error in w50_vel';
COMMENT ON COLUMN casda.spectral_line_emission.cw50_vel is 'Velocity width measured from the integrated spectrum''s cumulative flux distribution, using bounds taken from where a Gaussian profile is above 50% of its peak flux density';
COMMENT ON COLUMN casda.spectral_line_emission.cw50_vel_err is 'Error in cw50_vel';
COMMENT ON COLUMN casda.spectral_line_emission.w20_vel is 'Velocity width using voxels with flux density above 20% of the peak flux density in the integrated spectrum of the object';
COMMENT ON COLUMN casda.spectral_line_emission.w20_vel_err is 'Error in w20_vel';
COMMENT ON COLUMN casda.spectral_line_emission.cw20_vel is 'Velocity width measured from the integrated spectrum''s cumulative flux distribution, using bounds taken from where a Gaussian profile is above 20% of its peak flux density';
COMMENT ON COLUMN casda.spectral_line_emission.cw20_vel_err is 'Error in cw20_vel';
--spectral location complex
COMMENT ON COLUMN casda.spectral_line_emission.freq_w50_clip_uw is 'Frequency determined from unweighted mean frequency of object voxels after rejecting voxels outside of the w50 range';
COMMENT ON COLUMN casda.spectral_line_emission.freq_w50_clip_uw_err is 'Error in freq_w50_clip';
COMMENT ON COLUMN casda.spectral_line_emission.freq_cw50_clip_uw is 'Frequency determined from unweighted mean frequency of object voxels after rejecting voxels outside of the cw50 range';
COMMENT ON COLUMN casda.spectral_line_emission.freq_cw50_clip_uw_err is 'Error in freq_cw50_clip';
COMMENT ON COLUMN casda.spectral_line_emission.freq_w20_clip_uw is 'Frequency determined from unweighted mean frequency of object voxels after rejecting voxels outside of the w20 range';
COMMENT ON COLUMN casda.spectral_line_emission.freq_w20_clip_uw_err is 'Error in freq_w20_clip';
COMMENT ON COLUMN casda.spectral_line_emission.freq_cw20_clip_uw is 'Frequency determined from unweighted mean frequency of object voxels after rejecting voxels outside of the cw50 range.';
COMMENT ON COLUMN casda.spectral_line_emission.freq_cw20_clip_uw_err is 'Error in freq_cw20_clip';
COMMENT ON COLUMN casda.spectral_line_emission.vel_w50_clip_uw is 'Velocity of the object (optical convention and barycentric frame of reference) determined from unweighted mean frequency of object voxels after rejecting voxels outside of the w50 range';
COMMENT ON COLUMN casda.spectral_line_emission.vel_w50_clip_uw_err is 'Error in vel_w50_clip_uw';
COMMENT ON COLUMN casda.spectral_line_emission.vel_cw50_clip_uw is 'Velocity of the object (optical convention and barycentric frame of reference) determined from unweighted mean frequency of object voxels after rejecting voxels outside of the cw50 range';
COMMENT ON COLUMN casda.spectral_line_emission.vel_cw50_clip_uw_err is 'Error in vel_cw50_clip_uw';
COMMENT ON COLUMN casda.spectral_line_emission.vel_w20_clip_uw is 'Velocity of the object (optical convention and barycentric frame of reference) determined from unweighted mean frequency of object voxels after rejecting voxels outside of the w20 range';
COMMENT ON COLUMN casda.spectral_line_emission.vel_w20_clip_uw_err is 'Error in vel_w20_clip_uw';
COMMENT ON COLUMN casda.spectral_line_emission.vel_cw20_clip_uw is 'Velocity of the object (optical convention and barycentric frame of reference) determined from unweighted mean frequency of object voxels after rejecting voxels outside of the cw50 range.';
COMMENT ON COLUMN casda.spectral_line_emission.vel_cw20_clip_uw_err is 'Error in vel_cw20_clip_uw';
COMMENT ON COLUMN casda.spectral_line_emission.freq_w50_clip_w is 'Frequency determined from weighted mean frequency of object voxels after rejecting voxels outside of the w50 range';
COMMENT ON COLUMN casda.spectral_line_emission.freq_w50_clip_w_err is 'Error in freq_w50_clip_w';
COMMENT ON COLUMN casda.spectral_line_emission.freq_cw50_clip_w is 'Frequency determined from weighted mean frequency of object voxels after rejecting voxels outside of the cw50 range';
COMMENT ON COLUMN casda.spectral_line_emission.freq_cw50_clip_w_err is 'Error in freq_cw50_clip_w';
COMMENT ON COLUMN casda.spectral_line_emission.freq_w20_clip_w is 'Frequency determined from weighted mean frequency of object voxels after rejecting voxels outside of the w20 range';
COMMENT ON COLUMN casda.spectral_line_emission.freq_w20_clip_w_err is 'Error in freq_w20_clip_w';
COMMENT ON COLUMN casda.spectral_line_emission.freq_cw20_clip_w is 'Frequency determined from weighted mean frequency of object voxels after rejecting voxels outside of the cw50 range.';
COMMENT ON COLUMN casda.spectral_line_emission.freq_cw20_clip_w_err is 'Error in freq_cw20_clip_w';
COMMENT ON COLUMN casda.spectral_line_emission.vel_w50_clip_w is 'Velocity determined from weighted mean frequency of object voxels after rejecting voxels outside of the w50 range';
COMMENT ON COLUMN casda.spectral_line_emission.vel_w50_clip_w_err is 'Error in vel_w50_clip_w';
COMMENT ON COLUMN casda.spectral_line_emission.vel_cw50_clip_w is 'Velocity determined from weighted mean frequency of object voxels after rejecting voxels outside of the cw50 range';
COMMENT ON COLUMN casda.spectral_line_emission.vel_cw50_clip_w_err is 'Error in vel_cw50_clip_w';
COMMENT ON COLUMN casda.spectral_line_emission.vel_w20_clip_w is 'Velocity determined from weighted mean frequency of object voxels after rejecting voxels outside of the w20 range';
COMMENT ON COLUMN casda.spectral_line_emission.vel_w20_clip_w_err is 'Error in vel_w20_clip_w';
COMMENT ON COLUMN casda.spectral_line_emission.vel_cw20_clip_w is 'Velocity determined from weighted mean frequency of object voxels after rejecting voxels outside of the cw50 range.';
COMMENT ON COLUMN casda.spectral_line_emission.vel_cw20_clip_w_err is 'Error in vel_cw20_clip_w';
--Flux related complex
COMMENT ON COLUMN casda.spectral_line_emission.integ_flux_w50_clip is 'Integrated flux density calculated by summing the flux densities for all voxels that comprise the object after rejecting voxels outside the w50 range';
COMMENT ON COLUMN casda.spectral_line_emission.integ_flux_w50_clip_err is 'Error in integ_flux_w50_clip';
COMMENT ON COLUMN casda.spectral_line_emission.integ_flux_cw50_clip is 'Integrated flux density calculated by summing the flux densities for all voxels that comprise the object after rejecting voxels outside the cw50 range';
COMMENT ON COLUMN casda.spectral_line_emission.integ_flux_cw50_clip_err is 'Error in integ_flux_cw50_clip';
COMMENT ON COLUMN casda.spectral_line_emission.integ_flux_w20_clip is 'Integrated flux density calculated by summing the flux densities for all voxels that comprise the object after rejecting voxels outside the w20 range.';
COMMENT ON COLUMN casda.spectral_line_emission.integ_flux_w20_clip_err is 'Error in integ_flux_w20_clip';
COMMENT ON COLUMN casda.spectral_line_emission.integ_flux_cw20_clip is 'Integrated flux density calculated by summing the flux densities for all voxels that comprise the object after rejecting voxels outside the cw20 range';
COMMENT ON COLUMN casda.spectral_line_emission.integ_flux_cw20_clip_err is 'Error in integ_flux_cw20_clip';
--Busy function parameters
COMMENT ON COLUMN casda.spectral_line_emission.bf_a is 'The amplitude scaling factor a from a ''busy function'' fit';
COMMENT ON COLUMN casda.spectral_line_emission.bf_a_err is 'Error in bf_a';
COMMENT ON COLUMN casda.spectral_line_emission.bf_w is 'The half-width parameter, w, from the ''busy function'' fit';
COMMENT ON COLUMN casda.spectral_line_emission.bf_w_err is 'Error in bf_w';
COMMENT ON COLUMN casda.spectral_line_emission.bf_b1 is 'The slope of the first error function, b1, from the generalised ''busy function'' fit';
COMMENT ON COLUMN casda.spectral_line_emission.bf_b1_err is 'Error in bf_b1';
COMMENT ON COLUMN casda.spectral_line_emission.bf_b2 is 'The slope of the second error function, b2, from the generalised ''busy function'' fit';
COMMENT ON COLUMN casda.spectral_line_emission.bf_b2_err is 'Error in bf_b2';
COMMENT ON COLUMN casda.spectral_line_emission.bf_xe is 'Offset parameter for the error function, xe, from the generalised ''busy function'' fit';
COMMENT ON COLUMN casda.spectral_line_emission.bf_xe_err is 'Error in bf_xe';
COMMENT ON COLUMN casda.spectral_line_emission.bf_xp is 'Offset parameter for the polynomial function, xp, from the generalised ''busy function'' fit';
COMMENT ON COLUMN casda.spectral_line_emission.bf_xp_err is 'Error in bf_xp';
COMMENT ON COLUMN casda.spectral_line_emission.bf_c is 'Parameter c, governing the amplitude of the central trough, from the ''busy function'' fit';
COMMENT ON COLUMN casda.spectral_line_emission.bf_c_err is 'Error in bf_c';
COMMENT ON COLUMN casda.spectral_line_emission.bf_n is 'Degree of the polynomial, n, from the generalised ''busy function'' fit';
COMMENT ON COLUMN casda.spectral_line_emission.bf_n_err is 'Error in bf_n';
--Flags
COMMENT ON COLUMN casda.spectral_line_emission.flag_s1 is 'indicates whether the object is unresolved (1), marginally resolved (2) or resolved (3).';
COMMENT ON COLUMN casda.spectral_line_emission.flag_s2 is 'Placeholder flag';
COMMENT ON COLUMN casda.spectral_line_emission.flag_s3 is 'Placeholder flag';
--our data
COMMENT ON COLUMN casda.spectral_line_emission.quality_level is 'Indicator of quality level, updated by validators';
COMMENT ON COLUMN casda.spectral_line_emission.released_date is 'The date that the spectral line emission data product was released';
COMMENT ON COLUMN casda.spectral_line_emission.last_modified is 'When the row was last modified (usually via an insert)';


-- TAP metadata creation script for 
-- Generated by CASDA Data Deposit on 2015-04-07

INSERT INTO casda.tables (schema_name, table_name, table_type, db_schema_name, db_table_name, description, scs_enabled)
VALUES ('casda', 'casda.spectral_line_emission', 'table', 'casda', 'spectral_line_emission', '', true);

INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, unit, scs_verbosity )
VALUES 
(1, 'id', 'casda.spectral_line_emission', 'BIGINT', 19, 1, 1, 1, 'Primary key unique identifier', 'meta.record', null, 1),
(2, 'catalogue_id', 'casda.spectral_line_emission', 'BIGINT', 19, 1, 1, 1, 'Catalogue identifier', 'meta.id', null, 3),
(3, 'sbid', 'casda.spectral_line_emission', 'BIGINT', 15, 1, 1, 1, 'Scheduling Block identifier', 'meta.id', null, 3),
(4, 'project_id', 'casda.spectral_line_emission', 'BIGINT', 19, 1, 1, 1, 'Project identifier', 'meta.id', null, 3),
(5, 'object_id', 'casda.spectral_line_emission', 'VARCHAR', 27, 0, 1, 1, 'Object identifier', 'meta.id;meta.main', null, 3),
(6, 'object_name', 'casda.spectral_line_emission', 'VARCHAR', 15, 0, 1, 1, 'Object Name', 'meta.id', null, 3),
(7, 'ra_hms_w', 'casda.spectral_line_emission', 'VARCHAR', 11, 1, 1, 1, 'J2000 RA (hh:mm:ss.sss). Weighted mean RA from all voxels that comprise this object. The weight is the voxel flux density. ', 'pos.eq.ra', null, 2),
(8, 'dec_dms_w', 'casda.spectral_line_emission', 'VARCHAR', 11, 1, 1, 1, 'J2000 Dec (dd:mm:ss.ss). Weighted mean declination from all voxels that comprise this object. The weight is the voxel flux density. ', 'pos.eq.dec', null, 2),
(9, 'ra_deg_w', 'casda.spectral_line_emission', 'DOUBLE', 19, 1, 1, 1, 'J2000 RA in decimal degrees. Weighted mean RA from all voxels that comprise this object. The weight is the voxel flux density. ', 'pos.eq.ra;meta.main', 'deg', 1),
(10, 'ra_deg_w_err', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Error in ra_deg_w', 'stat.error;pos.eq.ra;meta.main', 'arcsec', 3),
(11, 'dec_deg_w', 'casda.spectral_line_emission', 'DOUBLE', 19, 1, 1, 1, 'J2000 Dec in decimal degrees. Weighted mean Dec from all voxels that comprise this object. The weight is the voxel flux density. ', 'pos.eq.dec;meta.main', 'deg', 1),
(12, 'dec_deg_w_err', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Error in dec_deg_w', 'stat.error;pos.eq.dec;meta.main', 'arcsec', 3),
(13, 'ra_deg_uw', 'casda.spectral_line_emission', 'DOUBLE', 19, 0, 1, 1, 'J2000 RA in decimal degrees Unweighted mean RA from all voxels that comprise this object.', 'pos.eq.ra', 'deg', 3),
(14, 'ra_deg_uw_err', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Error in ra_deg_uw', 'stat.error;pos.eq.ra', 'arcsec', 3),
(15, 'dec_deg_uw', 'casda.spectral_line_emission', 'DOUBLE', 19, 0, 1, 1, 'J2000 Dec in decimal degreesUnweighted mean declination from all voxels that comprise this object. ', 'pos.eq.dec', 'deg', 3),
(16, 'dec_deg_uw_err', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Error in dec_deg_uw', 'stat.error;pos.eq.dec', 'arcsec', 3),
(17, 'glong_w', 'casda.spectral_line_emission', 'DOUBLE', 19, 0, 1, 1, 'Galactic longitude determined from ra_deg_w and dec_deg_w', 'pos.galactic.lon;meta.main', 'deg', 3),
(18, 'glong_w_err', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Error in glong_deg_w', 'stat.error;pos.galactic.lon;meta.main', 'arcsec', 3),
(19, 'glat_w', 'casda.spectral_line_emission', 'DOUBLE', 19, 0, 1, 1, 'Galactic latitude determined from ra_deg_w and dec_deg_w', 'pos.galactic.lat;meta.main', 'deg', 3),
(20, 'glat_w_err', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Error in glat_deg_w', 'stat.error;pos.galactic.lat;meta.main', 'arcsec', 3),
(21, 'glong_uw', 'casda.spectral_line_emission', 'DOUBLE', 19, 0, 1, 1, 'Galactic longitude determined from ra_deg_unweighted and dec_deg_unweighted', 'pos.galactic.lon;meta.main', 'deg', 3),
(22, 'glong_uw_err', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Error in glong_uw', 'stat.error;pos.galactic.lon;meta.main', 'arcsec', 3),
(23, 'glat_uw', 'casda.spectral_line_emission', 'DOUBLE', 19, 0, 1, 1, 'Galactic latitude determined from ra_deg_unweighted and dec_deg_unweighted', 'pos.galactic.lat;meta.main', 'deg', 3),
(24, 'glat_uw_err', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Error in glat_uw', 'stat.error;pos.galactic.lat;meta.main', 'arcsec', 3),
(25, 'maj_axis', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Major axis determined from moment-0 map of detected pixels', 'askap:src.smajAxis;em.radio', 'arcsec', 3),
(26, 'min_axis', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Minor axis determined from moment-0 map of detected pixels', 'askap:src.sminAxis;em.radio', 'arcsec', 3),
(27, 'pos_ang', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Position angle of major axis (East of North)', 'askap:src.posAng;em.radio', 'deg', 3),
(28, 'maj_axis_fit', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Major axis determined from Gaussian fit to moment-0 map', 'askap:src.smajAxis;em.radio;stat.fit', 'arcsec', 3),
(29, 'maj_axis_fit_err', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Error in maj_axis_fit', 'stat.error;askap:src.smajAxis;em.radio;stat.fit', 'arcsec', 3),
(30, 'min_axis_fit', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Minor axis determined from Gaussian fit to moment-0 map', 'askap:src.sminAxis;em.radio;stat.fit', 'arcsec', 3),
(31, 'min_axis_fit_err', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Error in min_axis_fit', 'stat.error;askap:src.sminAxis;em.radio;stat.fit', 'arcsec', 3),
(32, 'pos_ang_fit', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Position angle of fitted major axis (East of North) from Gaussian fit to moment-0 map', 'askap:src.posAng;em.radio;stat.fit', 'deg', 3),
(33, 'pos_ang_fit_err', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Error in pos_ang_fit', 'stat.error;askap:src.posAng;em.radio;stat.fit', 'deg', 3),
(34, 'size_x', 'casda.spectral_line_emission', 'INTEGER', 15, 0, 0, 1, 'Size of bounding box of detected voxels in the x-direction', 'askap:src.size;instr.pixel', null, 3),
(35, 'size_y', 'casda.spectral_line_emission', 'INTEGER', 15, 0, 0, 1, 'Size of bounding box of detected voxels in the y-direction', 'askap:src.size;instr.pixel', null, 3),
(36, 'size_z', 'casda.spectral_line_emission', 'INTEGER', 15, 0, 0, 1, 'Size of bounding box of detected voxels in the z-direction (spectral)', 'askap:src.size;spect.binSize', null, 3),
(37, 'n_vox', 'casda.spectral_line_emission', 'INTEGER', 15, 0, 0, 1, 'Total number of detected voxels', 'askap:src.size;askap:instr.voxel', null, 3),
(38, 'asymmetry_2d', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Asymetry in flux of the 2D moment-0 map, between 0 (uniform) and 1', 'askap:src.asymmetry.2d', null, 3),
(39, 'asymmetry_2d_err', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Error in asymmetry_2d', 'stat.error;askap:src.asymmetry.2d', null, 3),
(40, 'asymmetry_3d', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Asymetry in flux of the 3D distribution of voxels, between 0 (uniform) and 1', 'askap:src.asymmetry.3d', null, 3),
(41, 'asymmetry_3d_err', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Error in asymmetry_3d', 'stat.error;askap:src.asymmetry.3d', null, 3),
(42, 'freq_uw', 'casda.spectral_line_emission', 'DOUBLE', 10, 0, 1, 1, 'Unweighted mean frequency from all voxels that comprise this object. ', 'em.freq', 'MHz', 3),
(43, 'freq_uw_err', 'casda.spectral_line_emission', 'DOUBLE', 10, 0, 0, 1, 'Error in freq_uw', 'stat.error;em.freq', 'MHz', 3),
(44, 'freq_w', 'casda.spectral_line_emission', 'DOUBLE', 10, 0, 1, 1, 'Weighted mean frequency from all voxels that comprise the object. The weight is the voxel flux density', 'em.freq;meta.main', 'MHz', 3),
(45, 'freq_w_err', 'casda.spectral_line_emission', 'DOUBLE', 10, 0, 0, 1, 'Error in freq_w', 'stat.error;em.freq;meta.main', 'MHz', 3),
(46, 'freq_peak', 'casda.spectral_line_emission', 'DOUBLE', 10, 0, 1, 1, 'Frequency of the peak in the integrated spectrum ', 'em.freq;phot.flux.density;stat.max', 'MHz', 3),
(47, 'vel_uw', 'casda.spectral_line_emission', 'REAL', 10, 0, 1, 1, 'Velocity of the object (optical convention and barycentric frame of reference) derived from the unweighted mean frequency from all voxels that comprise this object.', 'spect.dopplerVeloc.opt;em.line.HI', 'MHz', 3),
(48, 'vel_uw_err', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Error in vel_uw', 'stat.error;spect.dopplerVeloc.opt;em.line.HI', 'MHz', 3),
(49, 'vel_w', 'casda.spectral_line_emission', 'REAL', 10, 0, 1, 1, 'Velocity of the object (optical convention and barycentric frame of reference) derIved from the weighted mean frequency of all voxels that comprise the object. The weight is the voxel flux density.', 'spect.dopplerVeloc.opt;em.line.HI;meta.main', 'MHz', 3),
(50, 'vel_w_err', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Error in vel_w', 'stat.error;spect.dopplerVeloc.opt;em.line.HI;meta.main', 'MHz', 3),
(51, 'vel_peak', 'casda.spectral_line_emission', 'REAL', 10, 0, 1, 1, 'Velocity of the object (optical convention and barycentric frame of reference) derived from the frequency of the peak in the integrated spectrum', 'spect.dopplerVeloc.opt;em.line.HI;phot.flux.density;stat.max', 'MHz', 3),
(52, 'integ_flux', 'casda.spectral_line_emission', 'REAL', 10, 0, 1, 1, 'Integrated flux density calculated by summing the flux densities for all voxels that comprise the object and correcting for the beam area. ', 'phot.flux.density;askap:arith.integrated;em.radio', 'Jy km/s', 3),
(53, 'integ_flux_err', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Error in integ_flux', 'stat.error;phot.flux.density;askap:arith.integrated;em.radio', 'Jy km/s', 3),
(54, 'flux_voxel_max', 'casda.spectral_line_emission', 'REAL', 10, 1, 1, 1, 'Maximum voxel flux density for voxels that comprise the object', 'askap:phot.flux.density.voxel;stat.max;em.radio', 'mJy/beam', 1),
(55, 'flux_voxel_min', 'casda.spectral_line_emission', 'REAL', 10, 1, 0, 1, 'Minimum voxel flux density for voxels that comprise object', 'askap:phot.flux.density.voxel;stat.min;em.radio', 'mJy/beam', 1),
(56, 'flux_voxel_mean', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Mean voxel flux density for voxels that comprise the object', 'askap:phot.flux.density.voxel;stat.mean;em.radio', 'mJy/beam', 3),
(57, 'flux_voxel_stdev', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Standard deviation flux density for voxels for comprise the object', 'askap:phot.flux.density.voxel;stat.stdev;em.radio', 'mJy/beam', 3),
(58, 'flux_voxel_rms', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'rms flux density for voxels for comprise the object', 'askap:phot.flux.density.voxel;askap:stat.rms;em.radio', 'mJy/beam', 3),
(59, 'rms_imagecube', 'casda.spectral_line_emission', 'REAL', 10, 0, 1, 1, 'rms_imagecube', 'stat.stdev;phot.flux.density', 'mJy/beam', 3),
(60, 'w50_freq', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Frequency width using voxels with flux density above 50% of the peak flux density in the integrated spectrum of the object', 'askap:em.freq.width', 'kHz', 3),
(61, 'w50_freq_err', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Error in w50_freq', 'stat.error;askap:em.freq.width', 'kHz', 3),
(62, 'cw50_freq', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Frequency width measured from the integrated spectrum''s cumulative flux distribution, using bounds taken from where a Gaussian profile is above 50% of its peak flux density', 'askap:em.freq.width', 'kHz', 3),
(63, 'cw50_freq_err', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Error in cw50_freq', 'stat.error;askap:em.freq.width', 'kHz', 3),
(64, 'w20_freq', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Frequency width using voxels with flux density above 20% of the peak flux density in the integrated spectrum of the object', 'askap:em.freq.width', 'kHz', 3),
(65, 'w20_freq_err', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Error in w20_freq', 'stat.error;askap:em.freq.width', 'kHz', 3),
(66, 'cw20_freq', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Frequency width measured from the integrated spectrum''s cumulative flux distribution, using bounds taken from where a Gaussian profile is above 20% of its peak flux density', 'askap:em.freq.width', 'kHz', 3),
(67, 'cw20_freq_err', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Error in cw20_freq', 'stat.error;askap:em.freq.width', 'kHz', 3),
(68, 'w50_vel', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Velocity width using voxels with flux density above 50% of the peak flux density in the integrated spectrum of the object', 'askap:spect.dopplerVeloc.width', 'km/s', 3),
(69, 'w50_vel_err', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Error in w50_vel', 'stat.error;askap:spect.dopplerVeloc.width', 'km/s', 3),
(70, 'cw50_vel', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Velocity width measured from the integrated spectrum''s cumulative flux distribution, using bounds taken from where a Gaussian profile is above 50% of its peak flux density', 'askap:spect.dopplerVeloc.width', 'km/s', 3),
(71, 'cw50_vel_err', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Error in cw50_vel', 'stat.error;askap:spect.dopplerVeloc.width', 'km/s', 3),
(72, 'w20_vel', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Velocity width using voxels with flux density above 20% of the peak flux density in the integrated spectrum of the object', 'askap:askap:spect.dopplerVeloc.width', 'km/s', 3),
(73, 'w20_vel_err', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Error in w20_vel', 'stat.error;askap:spect.dopplerVeloc.width', 'km/s', 3),
(74, 'cw20_vel', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Velocity width measured from the integrated spectrum''s cumulative flux distribution, using bounds taken from where a Gaussian profile is above 20% of its peak flux density', 'askap:askap:spect.dopplerVeloc.width', 'km/s', 3),
(75, 'cw20_vel_err', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Error in cw20_vel', 'stat.error;askap:spect.dopplerVeloc.width', 'km/s', 3),
(76, 'freq_w50_clip_uw', 'casda.spectral_line_emission', 'DOUBLE', 10, 0, 0, 1, 'Frequency determined from unweighted mean frequency of object voxels after rejecting voxels outside of the w50 range', 'em.freq', 'MHz', 3),
(77, 'freq_w50_clip_uw_err', 'casda.spectral_line_emission', 'DOUBLE', 10, 0, 0, 1, 'Error in freq_w50_clip', 'stat.error;em.freq', 'MHz', 3),
(78, 'freq_cw50_clip_uw', 'casda.spectral_line_emission', 'DOUBLE', 10, 0, 0, 1, 'Frequency determined from unweighted mean frequency of object voxels after rejecting voxels outside of the cw50 range', 'em.freq', 'MHz', 3),
(79, 'freq_cw50_clip_uw_err', 'casda.spectral_line_emission', 'DOUBLE', 10, 0, 0, 1, 'Error in freq_cw50_clip', 'stat.error;em.freq', 'MHz', 3),
(80, 'freq_w20_clip_uw', 'casda.spectral_line_emission', 'DOUBLE', 10, 0, 0, 1, 'Frequency determined from unweighted mean frequency of object voxels after rejecting voxels outside of the w20 range', 'em.freq', 'MHz', 3),
(81, 'freq_w20_clip_uw_err', 'casda.spectral_line_emission', 'DOUBLE', 10, 0, 0, 1, 'Error in freq_w20_clip', 'stat.error;em.freq', 'MHz', 3),
(82, 'freq_cw20_clip_uw', 'casda.spectral_line_emission', 'DOUBLE', 10, 0, 0, 1, 'Frequency determined from unweighted mean frequency of object voxels after rejecting voxels outside of the cw50 range.', 'em.freq', 'MHz', 3),
(83, 'freq_cw20_clip_uw_err', 'casda.spectral_line_emission', 'DOUBLE', 10, 0, 0, 1, 'Error in freq_cw20_clip', 'stat.error;em.freq', 'MHz', 3),
(84, 'vel_w50_clip_uw', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Velocity of the object (optical convention and barycentric frame of reference) determined from unweighted mean frequency of object voxels after rejecting voxels outside of the w50 range', 'spect.dopplerVeloc.opt;em.line.HI', 'km/s', 3),
(85, 'vel_w50_clip_uw_err', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Error in vel_w50_clip_uw', 'stat.error;spect.dopplerVeloc.opt;em.line.HI', 'km/s', 3),
(86, 'vel_cw50_clip_uw', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Velocity of the object (optical convention and barycentric frame of reference) determined from unweighted mean frequency of object voxels after rejecting voxels outside of the cw50 range', 'spect.dopplerVeloc.opt;em.line.HI', 'km/s', 3),
(87, 'vel_cw50_clip_uw_err', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Error in vel_cw50_clip_uw', 'stat.error;spect.dopplerVeloc.opt;em.line.HI', 'km/s', 3),
(88, 'vel_w20_clip_uw', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Velocity of the object (optical convention and barycentric frame of reference) determined from unweighted mean frequency of object voxels after rejecting voxels outside of the w20 range', 'spect.dopplerVeloc.opt;em.line.HI', 'km/s', 3),
(89, 'vel_w20_clip_uw_err', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Error in vel_w20_clip_uw', 'stat.error;spect.dopplerVeloc.opt;em.line.HI', 'km/s', 3),
(90, 'vel_cw20_clip_uw', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Velocity of the object (optical convention and barycentric frame of reference) determined from unweighted mean frequency of object voxels after rejecting voxels outside of the cw50 range.', 'spect.dopplerVeloc.opt;em.line.HI', 'km/s', 3),
(91, 'vel_cw20_clip_uw_err', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Error in vel_cw20_clip_uw', 'stat.error;spect.dopplerVeloc.opt;em.line.HI', 'km/s', 3),
(92, 'freq_w50_clip_w', 'casda.spectral_line_emission', 'DOUBLE', 10, 0, 0, 1, 'Frequency determined from weighted mean frequency of object voxels after rejecting voxels outside of the w50 range', 'em.freq', 'MHz', 3),
(93, 'freq_w50_clip_w_err', 'casda.spectral_line_emission', 'DOUBLE', 10, 0, 0, 1, 'Error in freq_w50_clip_w', 'stat.error;em.freq', 'MHz', 3),
(94, 'freq_cw50_clip_w', 'casda.spectral_line_emission', 'DOUBLE', 10, 0, 0, 1, 'Frequency determined from weighted mean frequency of object voxels after rejecting voxels outside of the cw50 range', 'em.freq', 'MHz', 3),
(95, 'freq_cw50_clip_w_err', 'casda.spectral_line_emission', 'DOUBLE', 10, 0, 0, 1, 'Error in freq_cw50_clip_w', 'stat.error;em.freq', 'MHz', 3),
(96, 'freq_w20_clip_w', 'casda.spectral_line_emission', 'DOUBLE', 10, 0, 0, 1, 'Frequency determined from weighted mean frequency of object voxels after rejecting voxels outside of the w20 range', 'em.freq', 'MHz', 3),
(97, 'freq_w20_clip_w_err', 'casda.spectral_line_emission', 'DOUBLE', 10, 0, 0, 1, 'Error in freq_w20_clip_w', 'stat.error;em.freq', 'MHz', 3),
(98, 'freq_cw20_clip_w', 'casda.spectral_line_emission', 'DOUBLE', 10, 0, 0, 1, 'Frequency determined from weighted mean frequency of object voxels after rejecting voxels outside of the cw50 range.', 'em.freq', 'MHz', 3),
(99, 'freq_cw20_clip_w_err', 'casda.spectral_line_emission', 'DOUBLE', 10, 0, 0, 1, 'Error in freq_cw20_clip_w', 'stat.error;em.freq', 'MHz', 3),
(100, 'vel_w50_clip_w', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Velocity determined from weighted mean frequency of object voxels after rejecting voxels outside of the w50 range', 'spect.dopplerVeloc.opt;em.line.HI', 'km/s', 3),
(101, 'vel_w50_clip_w_err', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Error in vel_w50_clip_w', 'stat.error;spect.dopplerVeloc.opt;em.line.HI', 'km/s', 3),
(102, 'vel_cw50_clip_w', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Velocity determined from weighted mean frequency of object voxels after rejecting voxels outside of the cw50 range', 'spect.dopplerVeloc.opt;em.line.HI', 'km/s', 3),
(103, 'vel_cw50_clip_w_err', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Error in vel_cw50_clip_w', 'stat.error;spect.dopplerVeloc.opt;em.line.HI', 'km/s', 3),
(104, 'vel_w20_clip_w', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Velocity determined from weighted mean frequency of object voxels after rejecting voxels outside of the w20 range', 'spect.dopplerVeloc.opt;em.line.HI', 'km/s', 3),
(105, 'vel_w20_clip_w_err', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Error in vel_w20_clip_w', 'stat.error;spect.dopplerVeloc.opt;em.line.HI', 'km/s', 3),
(106, 'vel_cw20_clip_w', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Velocity determined from weighted mean frequency of object voxels after rejecting voxels outside of the cw50 range.', 'spect.dopplerVeloc.opt;em.line.HI', 'km/s', 3),
(107, 'vel_cw20_clip_w_err', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Error in vel_cw20_clip_w', 'stat.error;spect.dopplerVeloc.opt;em.line.HI', 'km/s', 3),
(108, 'integ_flux_w50_clip', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Integrated flux density calculated by summing the flux densities for all voxels that comprise the object after rejecting voxels outside the w50 range', 'phot.flux.density;askap:arith.integrated;em.radio', 'Jy km/s', 3),
(109, 'integ_flux_w50_clip_err', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Error in integ_flux_w50_clip', 'stat.error;phot.flux.density;askap:arith.integrated;em.radio', 'Jy km/s', 3),
(110, 'integ_flux_cw50_clip', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Integrated flux density calculated by summing the flux densities for all voxels that comprise the object after rejecting voxels outside the cw50 range', 'phot.flux.density;askap:arith.integrated;em.radio', 'Jy km/s', 3),
(111, 'integ_flux_cw50_clip_err', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Error in integ_flux_cw50_clip', 'stat.error;phot.flux.density;askap:arith.integrated;em.radio', 'Jy km/s', 3),
(112, 'integ_flux_w20_clip', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Integrated flux density calculated by summing the flux densities for all voxels that comprise the object after rejecting voxels outside the w20 range.', 'phot.flux.density;askap:arith.integrated;em.radio', 'Jy km/s', 3),
(113, 'integ_flux_w20_clip_err', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Error in integ_flux_w20_clip', 'stat.error;phot.flux.density;askap:arith.integrated;em.radio', 'Jy km/s', 3),
(114, 'integ_flux_cw20_clip', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Integrated flux density calculated by summing the flux densities for all voxels that comprise the object after rejecting voxels outside the cw20 range', 'phot.flux.density;askap:arith.integrated;em.radio', 'Jy km/s', 3),
(115, 'integ_flux_cw20_clip_err', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Error in integ_flux_cw20_clip', 'stat.error;phot.flux.density;askap:arith.integrated;em.radio', 'Jy km/s', 3),
(116, 'bf_a', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'The amplitude scaling factor a from a ''busy function'' fit', 'stat.fit.param', 'Jy km/s', 3),
(117, 'bf_a_err', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Error in bf_a', 'stat.error;stat.fit.param', 'Jy km/s', 3),
(118, 'bf_w', 'casda.spectral_line_emission', 'DOUBLE', 10, 0, 0, 1, 'The half-width parameter, w, from the ''busy function'' fit', 'stat.fit.param', 'MHz', 3),
(119, 'bf_w_err', 'casda.spectral_line_emission', 'DOUBLE', 10, 0, 0, 1, 'Error in bf_w', 'stat.error;stat.fit.param', 'MHz', 3),
(120, 'bf_b1', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'The slope of the first error function, b1, from the generalised ''busy function'' fit', 'stat.fit.param', null, 3),
(121, 'bf_b1_err', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Error in bf_b1', 'stat.error;stat.fit.param', null, 3),
(122, 'bf_b2', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'The slope of the second error function, b2, from the generalised ''busy function'' fit', 'stat.fit.param', null, 3),
(123, 'bf_b2_err', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Error in bf_b2', 'stat.error;stat.fit.param', null, 3),
(124, 'bf_xe', 'casda.spectral_line_emission', 'DOUBLE', 10, 0, 0, 1, 'Offset parameter for the error function, xe, from the generalised ''busy function'' fit', 'stat.fit.param', 'MHz', 3),
(125, 'bf_xe_err', 'casda.spectral_line_emission', 'DOUBLE', 10, 0, 0, 1, 'Error in bf_xe', 'stat.error;stat.fit.param', 'MHz', 3),
(126, 'bf_xp', 'casda.spectral_line_emission', 'DOUBLE', 10, 0, 0, 1, 'Offset parameter for the polynomial function, xp, from the generalised ''busy function'' fit', 'stat.fit.param', 'MHz', 3),
(127, 'bf_xp_err', 'casda.spectral_line_emission', 'DOUBLE', 10, 0, 0, 1, 'Error in bf_xp', 'stat.error;stat.fit.param', 'MHz', 3),
(128, 'bf_c', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Parameter c, governing the amplitude of the central trough, from the ''busy function'' fit', 'stat.fit.param', null, 3),
(129, 'bf_c_err', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Error in bf_c', 'stat.error;stat.fit.param', null, 3),
(130, 'bf_n', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Degree of the polynomial, n, from the generalised ''busy function'' fit', 'stat.fit.param', null, 3),
(131, 'bf_n_err', 'casda.spectral_line_emission', 'REAL', 10, 0, 0, 1, 'Error in bf_n', 'stat.error;stat.fit.param', null, 3),
(132, 'flag_s1', 'casda.spectral_line_emission', 'INTEGER', 15, 0, 1, 1, 'indicates whether the object is unresolved (1), marginally resolved (2) or resolved (3). ', 'meta.code', null, 3),
(133, 'flag_s2', 'casda.spectral_line_emission', 'INTEGER', 15, 0, 1, 1, 'Placeholder flag', 'meta.code', null, 3),
(134, 'flag_s3', 'casda.spectral_line_emission', 'INTEGER', 15, 0, 1, 1, 'Placeholder flag', 'meta.code', null, 3),
(135, 'quality_level', 'casda.spectral_line_emission', 'INTEGER', 1000, 1, 0, 1, 'Indicator of quality level', 'meta.note', null, 3),
(136, 'released_date', 'casda.spectral_line_emission', 'TIMESTAMP', 24, 1, 0, 1, 'Date the catalogue was released', null, null, 3)
;

INSERT INTO casda.keys (key_id, from_table, target_table, description )
VALUES ((SELECT max(cast(numericalkeys.nums[1] as int)) + 1 from (SELECT regexp_matches(key_id, '^\d+$') as nums from casda.keys) as numericalkeys), 'casda.spectral_line_emission', 'casda.catalogue', 'Foreign key from spectral_line_emission to catalogue table');

INSERT INTO casda.key_columns (id, key_id, from_column, target_column, from_table, target_table )
VALUES ((SELECT max(id) + 1 from casda.key_columns), (SELECT key_id FROM casda.keys where from_table = 'casda.spectral_line_emission' and target_table = 'casda.catalogue'), 'catalogue_id', 'id', 'casda.spectral_line_emission', 'casda.catalogue');

