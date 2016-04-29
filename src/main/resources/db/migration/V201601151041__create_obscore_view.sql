-- This file contains the obscore view 
--
-- To update this view:
-- * rename this file with a new timestamp so flyway will re-run it on deploy
-- * make required changes to this file.


-- ObsCore v1.0 compliant view of the CASDA observations and their data products

-- Note: This currently only runs on the data provided in the RTC metadata file 
-- It will need expansion as we include each new type of data product.
DROP VIEW IF EXISTS casda.obscore;


CREATE VIEW casda.obscore (
    dataproduct_type,
	calib_level,
	obs_collection,
	obs_id,
	obs_publisher_did,
	access_url,
	access_format,
	access_estsize,
	target_name,
	s_ra,
	s_dec,
	s_fov,
	s_region,
	s_resolution,
	t_min,
	t_max,
	t_exptime,
	t_resolution,
	em_min,
	em_max,
	em_res_power,
	o_ucd,
	pol_states,
	facility_name,
	instrument_name,
	
	dataproduct_subtype,
	em_ucd,
	em_unit,
	em_resolution,
	s_resolution_min,
	s_resolution_max,
	s_ucd,
	s_unit,
	released_date,
	quality_level) AS

	-- Advertise image cubes via obs_core
	select 'cube', 2, p.short_name, obs.sbid::text, 'cube-' || ic.id::text, 
	'#{baseUrl}datalink/links?ID=cube-'||ic.id, 'application/x-votable+xml;content=datalink', ic.filesize, 
	ic.target_name, ic.ra_deg, ic.dec_deg, ic.s_fov, ic.s_region_poly, ic.s_resolution, 
	ic.t_min, ic.t_max, ic.t_exptime, null::double precision, 
	ic.em_min, ic.em_max, ic.em_res_power, 
	concat('phot.flux.density', case stokes_parameters when '//' then '' else ';phys.polarisation' end), 
	ic.stokes_parameters, obs.telescope, null,
	null, 'em.wl', 'm', ic.em_resolution, ic.s_resolution_min, ic.s_resolution_max, 'pos.eq', 'deg', ic.released_date,
	ic.quality_level
	from casda.image_cube ic, casda.observation obs, casda.project p
	where obs.id = ic.observation_id and p.id = ic.project_id and ic.deposit_state = 'DEPOSITED' and obs.deposit_state = 'DEPOSITED'

	union all

	-- Advertise measurement sets (visibilities) via obs_core
	select 'visibility', 1, p.short_name, obs.sbid::text, 'visibility-' || ms.id::text, 
	'#{baseUrl}datalink/links?ID=visibility-'||ms.id, 'application/x-votable+xml;content=datalink', ms.filesize, 
	null, null, null, null::double precision, null, null::double precision, 
	obs.obs_start_mjd, obs.obs_end_mjd, (obs.obs_end_mjd - obs.obs_start_mjd)*24*60*60, null::double precision, 
	ms.em_min, ms.em_max, null::double precision, 
	null, null, obs.telescope, null,
	null, 'em.wl', 'm', null::double precision, null::double precision, null::double precision, 'pos.eq', 'deg', 
	ms.released_date, ms.quality_level 
	from casda.measurement_set ms, casda.observation obs, casda.project p 
	where obs.id = ms.observation_id and p.id = ms.project_id and ms.deposit_state = 'DEPOSITED' and obs.deposit_state = 'DEPOSITED'

	union all

	-- Advertise non-level 7 catalogues via obs_core
	select null, 2, p.short_name, obs.sbid::text, 'catalogue-' || c.id::text, 
	'#{baseUrl}tap/', 'application/x-votable+xml', CAST(0 AS BIGINT), 
	null, null, null, null::double precision, null, null::double precision, 
	obs.obs_start_mjd, obs.obs_end_mjd, (obs.obs_end_mjd - obs.obs_start_mjd)*24*60*60, null::double precision, 
	c.em_min, c.em_max, null::double precision, 
	'phot.flux.density', null, obs.telescope, null,
	'catalogue.'||lower(replace(c.catalogue_type,'_', '.')),
	'em.wl', 'm', null::double precision, null::double precision, null::double precision, 'pos.eq', 'deg', 
	c.released_date, c.quality_level
	from casda.catalogue c, casda.observation obs, casda.project p 
	where c.catalogue_type != 'LEVEL7' and obs.id = c.observation_id and p.id = c.project_id and c.deposit_state = 'DEPOSITED' and obs.deposit_state = 'DEPOSITED'

    union all

    -- Advertise level 7 catalogues via obs_core
    select null, 3, p.short_name, p.opal_code, 'catalogue-' || c.id::text, 
    '#{baseUrl}tap/', 'application/x-votable+xml', CAST(0 AS BIGINT), 
    null, null, null, null::double precision, null, null::double precision, 
    null, null, null, null::double precision, 
    null, null, null::double precision, 
    'phot.flux.density', null, 'ASKAP', null,
    'catalogue.derived',
    'em.wl', 'm', null::double precision, null::double precision, null::double precision, 'pos.eq', 'deg', 
	c.released_date, null
    from casda.catalogue c, casda.project p 
    where c.catalogue_type = 'LEVEL7' and p.id = c.project_id and c.deposit_state = 'DEPOSITED'
    
    ORDER BY 4, 5;
  
COMMENT ON VIEW casda.obscore is 'An implementation of the Observation Data Model Core Components \
    to allow generic querying of the availability of CASDA data products.';
  
  