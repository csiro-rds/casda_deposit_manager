-- This file contains the specdm view.  
--
-- To update this view:
-- * rename this file with a new timestamp so flyway will re-run it on deploy
-- * make required changes to this file.


-- Spectrum Data Model v1.0. compliant view of the CASDA spectrum data products

DROP VIEW IF EXISTS casda.specdm;


CREATE VIEW casda.specdm (
	access_url,
	access_format,
	access_estsize,
	spectrum_type,
	num_chan,
	title,
	obs_collection,
	obs_creator_did,
	data_source,
	creation_type,
	publisher,
	obs_publisher_id,
	s_ra,
	s_dec,
	s_region,
	s_pos,
	s_fov,
	s_resolution,
	em_midpoint,
	em_width,
	em_min,
	em_max,
	em_resolution,
	t_midpoint,
	t_exptime,
	t_min,
	t_max,
	released_date,
	quality_level,
	datamodel,
	creator,
	spaceframe,
	spatialaxis_calibration,
	spectralaxis_ucd,
	spectralaxis_calibration,
	fluxaxis_ucd,
	fluxaxis_calibration) AS

	-- Include the fits spectra
	select '#{baseUrl}ssa/download?ID=spectrum-'||s.id::text, 'application/fits'::text, s.filesize, 
	lower(replace(s.type,'_', '.')), s.num_chan, 
	concat(p.short_name,' - ',obs.sbid,' - ',s.target_name), p.short_name, 
	'spectrum-'||s.id::text, 'survey'::text, 'archival'::text, 'CASDA'::text, 'spectrum-'||s.id::text, 
	s.ra_deg, s.dec_deg, s.s_region_poly, s.ra_deg||' '||s.dec_deg, null::double precision, null::double precision, 
	2.9972e+8/s.centre_freq, s.em_resolution*s.num_chan, s.em_min, s.em_max, s.em_resolution, 
	(s.t_max-s.t_min)*0.5+s.t_min, s.t_exptime, s.t_min, s.t_max,
	s.released_date, s.quality_level,
	'Spectrum-1.0'::text, 'ASKAP'::text, 'ICRS'::text, 'calibrated'::text, 'em.wl'::text, 'calibrated'::text, 
	'phot.flux.density;em.wl'::text, 'calibrated'::text
	from casda.spectrum s, casda.observation obs, casda.project p
	where obs.id = s.observation_id and p.id = s.project_id and s.deposit_state = 'DEPOSITED' and obs.deposit_state = 'DEPOSITED';
  
COMMENT ON VIEW casda.specdm is 'An implementation of the Spectrum Data Model v1.1 \
    to allow CASDA spectrum data products to be searched.';
  
  
-- Create the TAP metadata for the SpecDM view
DELETE FROM casda.tap_columns WHERE table_name = 'ivoa.spectrum_dm';
DELETE FROM casda.tap_tables WHERE table_name = 'ivoa.spectrum_dm';

INSERT INTO casda.tap_tables (schema_name, table_name, table_type, db_schema_name, db_table_name, description, scs_enabled)
  VALUES ('ivoa', 'ivoa.spectrum_dm','view','casda','specdm', 'Implementation of the IVOA Spectrum Data Model v1.1 ', true);
  
INSERT INTO casda.tap_columns (column_order, table_name, column_name, db_column_name, unit, utype, ucd, description, datatype, size, principal, indexed, std, scs_verbosity ) 
values 
(1, 'ivoa.spectrum_dm', 'access_url',      'access_url',      NULL,    'Access.Reference', 'meta.ref.url', 'URL used to access dataset', 'CLOB', 2000, 1, 0, 0, 1),
(2, 'ivoa.spectrum_dm', 'access_format',   'access_format',   NULL,     'Access.Format', NULL, 'Content or MIME type of dataset', 'VARCHAR', 255, 1, 0, 0, 2),
(3, 'ivoa.spectrum_dm', 'access_estsize',  'access_estsize',  'kbyte',     'Access.Size', NULL, 'Estimated dataset size', 'BIGINT', 15, 1, 0, 0, 1),
(4, 'ivoa.spectrum_dm', 'spectrum_type',   'spectrum_type',   NULL,     NULL, 'meta.id', 'Further description of the type of spectrum', 'VARCHAR', 50, 1, 0, 0, 1),
(5, 'ivoa.spectrum_dm', 'num_chan',        'num_chan',        NULL,     'Dataset.Length', 'meta.number', 'Number of points', 'BIGINT', 15, 0, 0, 0, 3),
(6, 'ivoa.spectrum_dm', 'title',           'title',           NULL,    'DataID.Title', 'meta.title;meta.dataset', 'Dataset Title', 'VARCHAR', 255, 1, 0, 0, 1),
(7, 'ivoa.spectrum_dm', 'obs_collection',  'obs_collection',  NULL,    'DataID.Collection', NULL, 'Data collection to which dataset belongs', 'VARCHAR', 255, 0, 0, 0, 3),
(8, 'ivoa.spectrum_dm', 'obs_creator_did', 'obs_creator_did', NULL,    'DataID.CreatorDID', 'meta.id', 'Creator''s ID for the dataset', 'VARCHAR', 255, 1, 0, 0, 2),
(9, 'ivoa.spectrum_dm', 'data_source',     'data_source',     NULL,    'DataID.DataSource', NULL, 'Original source of the data', 'VARCHAR', 255, 0, 0, 0, 3),
(10,'ivoa.spectrum_dm', 'creation_type',   'creation_type',   NULL,    'DataID.CreationType', NULL, 'Dataset creation type', 'VARCHAR', 255, 0, 0, 0, 3),
(11, 'ivoa.spectrum_dm', 'publisher',       'publisher',      NULL,     'Curation.Publisher', 'meta.curation', 'Dataset publisher', 'VARCHAR', 255, 0, 0, 0, 3),
(12, 'ivoa.spectrum_dm', 'obs_publisher_id', 'obs_publisher_id', NULL,    'Curation.PublisherDID', 'meta.ref.url;meta.curation', 'Publisher''s ID for the dataset ID', 'VARCHAR', 255, 0, 0, 0, 2),
(13, 'ivoa.spectrum_dm', 's_ra',            's_ra',           'deg',     NULL, 'pos.eq.ra;meta.main', 'J2000 right ascension in decimal degrees', 'DOUBLE', 15, 1, 1, 0, 1),
(14, 'ivoa.spectrum_dm', 's_dec',           's_dec',          'deg',     NULL, 'pos.eq.dec;meta.main', 'J2000 declination in decimal degrees', 'DOUBLE', 15, 1, 1, 0, 1),
(15, 'ivoa.spectrum_dm', 's_pos',           's_pos',          'deg',     'Char.SpatialAxis.Coverage.Location.Value', 'pos.eq', 'Spatial Position', 'VARCHAR', 255, 0, 0, 0, 3),
(16, 'ivoa.spectrum_dm', 's_fov',           's_fov',          'deg',     'Char.SpatialAxis.Coverage.Bounds.Extent', 'instr.fov', 'Aperture angular size', 'DOUBLE', 15, 0, 0, 0, 3),
(17, 'ivoa.spectrum_dm', 's_resolution',    's_resolution',   'deg',     'Char.SpatialAxis.Resolution', 'pos.angResolution', 'Spatial resolution of data', 'DOUBLE', 15, 0, 0, 0, 3),
(18, 'ivoa.spectrum_dm', 'em_midpoint',     'em_midpoint',    'm',     'Char.SpectralAxis.Coverage.Location.Value', 'em.wl;instr.bandpass', 'Spectral coord value', 'DOUBLE', 15, 1, 0, 0, 1),
(19, 'ivoa.spectrum_dm', 'em_width',        'em_width',       'm',     'Char.SpectralAxis.Coverage.Bounds.Extent', 'em.wl;instr.bandwidth', 'Width of spectrum', 'DOUBLE', 15, 1, 0, 0, 2),
(20, 'ivoa.spectrum_dm', 'em_min',          'em_min',         'm',     'Char.SpectralAxis.Coverage.Bounds.Start', 'em.wl;stat.min', 'Start in spectral coordinate', 'DOUBLE', 15, 1, 1, 0, 1),
(21, 'ivoa.spectrum_dm', 'em_max',          'em_max',         'm',     'Char.SpectralAxis.Coverage.Bounds.Stop', 'em.wl;stat.max', 'Stop in spectral coordinate', 'DOUBLE', 15, 1, 1, 0, 1),
(22, 'ivoa.spectrum_dm', 'em_resolution',   'em_resolution',  'm',     'Char.SpectralAxis.Resolution', 'spect.resolution;em', 'Spectral resolution FWHM', 'DOUBLE', 15, 1, 0, 0, 2),
(23, 'ivoa.spectrum_dm', 't_midpoint',      't_midpoint',     'd',     'Char.TimeAxis.Coverage.Location.Value', 'time.epoch', 'Midpoint of exposure on MJD scale', 'DOUBLE', 15, 1, 0, 0, 1),
(24, 'ivoa.spectrum_dm', 't_exptime',       't_exptime',      'd',     'Char.TimeAxis.Coverage.Bounds.Extent', 'time.duration', 'Total exposure time', 'DOUBLE', 15, 0, 0, 0, 2),
(25, 'ivoa.spectrum_dm', 't_min',           't_min',          'd',     'Char.TimeAxis.Coverage.Bounds.Start', 'time.start;obs.exposure', 'Observation start time', 'DOUBLE', 15, 0, 0, 0, 2),
(26, 'ivoa.spectrum_dm', 't_max',           't_max',          'd',     'Char.TimeAxis.Coverage.Bounds.Stop', 'time.end;obs.exposure', 'Observation end time', 'DOUBLE', 15, 0, 0, 0, 2),
(27, 'ivoa.spectrum_dm', 'released_date',   'released_date',  NULL,     NULL, NULL, 'The date that this data product was released', 'TIMESTAMP', 24, 0, 0, 0, 3),
(28, 'ivoa.spectrum_dm', 'quality_level',   'quality_level',  NULL,     NULL, NULL, 'Indicator of quality level, updated by validators', 'VARCHAR', 15, 0, 0, 0, 2),
(29, 'ivoa.spectrum_dm', 's_region',        's_region',       NULL,     NULL, 'phys.angArea;obs', 'Region covered as specified in STC or ADQL', 'REGION', 200, 1, 0, 1, 3),

(30, 'ivoa.spectrum_dm', 'datamodel',       'datamodel',      NULL,     'Dataset.DataModel', NULL, 'Datamodel name and version', 'VARCHAR', 30, 0, 0, 1, 3),
(31, 'ivoa.spectrum_dm', 'creator',         'creator',        NULL,     'DataID.Creator', NULL, 'Dataset creator', 'VARCHAR', 30, 0, 0, 1, 3),
(32, 'ivoa.spectrum_dm', 'spaceframe',      'spaceframe',     NULL,     'CoordSys.SpaceFrame.Name', NULL, 'Spatial coordinate frame name', 'VARCHAR', 30, 0, 0, 1, 3),
(33, 'ivoa.spectrum_dm', 'spatialaxis_calibration',  'spatialaxis_calibration',  NULL,  'Char.SpatialAxis.Calibration', 'meta.code.qual', 'Type of spatial coord calibration', 'VARCHAR', 30, 0, 0, 1, 3),
(34, 'ivoa.spectrum_dm', 'spectralaxis_ucd', 'spectralaxis_ucd',  NULL,   'Char.SpectralAxis.Ucd', 'meta.ucd', 'UCD for spectral coord', 'VARCHAR', 30, 0, 0, 1, 3),
(35, 'ivoa.spectrum_dm', 'spectralaxis_calibration',  'spectralaxis_calibration',  NULL,  'Char.SpectralAxis.Calibration', 'meta.code.qual', 'Type of spectral coord calibration', 'VARCHAR', 30, 0, 0, 1, 3),
(36, 'ivoa.spectrum_dm', 'fluxaxis_ucd',    'fluxaxis_ucd',   NULL,     'Char.FluxAxis.Ucd', 'meta.ucd', 'UCD for flux', 'VARCHAR', 30, 0, 0, 1, 3),
(37, 'ivoa.spectrum_dm', 'fluxaxis_calibration',  'fluxaxis_calibration',  NULL,  'Char.FluxAxis.Calibration', NULL, 'Type of flux calibration', 'VARCHAR', 30, 0, 0, 1, 3);

