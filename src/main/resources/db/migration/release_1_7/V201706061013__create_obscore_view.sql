-- This file contains the obscore view 
--
-- To update this view:
-- * rename this file with a new timestamp so flyway will re-run it on deploy
-- * make required changes to this file.


-- ObsCore v1.0 compliant view of the CASDA observations and their data products

-- Note: This currently only runs on the data provided in the RTC metadata file 
-- It will need expansion as we include each new type of data product.
CREATE OR REPLACE FUNCTION casda.mergeValues(pol1 text, pol2 text) 
RETURNS text AS $$
DECLARE
  instance text;
  final text;
BEGIN
	final = pol1;
	FOREACH instance IN ARRAY regexp_split_to_array(pol2, '/') 
	LOOP
	    IF
		instance != '' and pol1 not like('%' || instance || '%') THEN
		final = final || instance || '/';
	    END IF;
	END LOOP;	

	RETURN final;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION casda.mergePolarisations(mes_id bigint) 
RETURNS text AS $$
DECLARE
  final text = '/';
  row text;
BEGIN
	FOR row IN select polarisations from casda.scan where measurement_set_id = mes_id
	LOOP
		final = casda.mergeValues(final, row);
	END LOOP;	

	RETURN final;
END;
$$ LANGUAGE plpgsql;

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
	concat('phot.flux.density', case stokes_parameters when '//' then '' when '/I/' then '' else ';phys.polarisation' end), 
	ic.stokes_parameters, obs.telescope, null,
	lower(replace(ic.type,'_', '.')),
	'em.wl', 'm', ic.em_resolution, ic.s_resolution_min, ic.s_resolution_max, 'pos.eq', 'deg', ic.released_date,
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
	'phys.polarisation',
	casda.mergePolarisations(ms.id),
	obs.telescope, null,
	null, 'em.wl', 'm', null::double precision, null::double precision, null::double precision, 'pos.eq', 'deg', 
	ms.released_date, ms.quality_level 
	from casda.measurement_set ms, casda.observation obs, casda.project p
	where obs.id = ms.observation_id and p.id = ms.project_id and ms.deposit_state = 'DEPOSITED' and obs.deposit_state = 'DEPOSITED'
  

	union all

	-- Advertise non-level 7 catalogues via obs_core
	select null, 2, p.short_name, obs.sbid::text, 'catalogue-' || c.id::text, 
	'#{baseUrl}tap/', 'application/x-votable+xml', CAST(0 AS BIGINT), 
	null, c.ra_deg, c.dec_deg, null, c.s_region_poly, null::double precision, 
	obs.obs_start_mjd, obs.obs_end_mjd, (obs.obs_end_mjd - obs.obs_start_mjd)*24*60*60, null::double precision, 
	c.em_min, c.em_max, null::double precision, 
	'phot.flux.density', null, obs.telescope, null,
	'catalogue.'||lower(replace(c.catalogue_type,'_', '.')),
	'em.wl', 'm', null::double precision, null::double precision, null::double precision, 'pos.eq', 'deg', 
	c.released_date, c.quality_level
	from casda.catalogue c, casda.observation obs, casda.project p 
	where c.catalogue_type != 'DERIVED_CATALOGUE' and obs.id = c.observation_id and p.id = c.project_id and c.deposit_state = 'DEPOSITED' and obs.deposit_state = 'DEPOSITED'

    union all

    -- Advertise level 7 catalogues via obs_core
    select null, 3, p.short_name, p.opal_code, 'catalogue-' || c.id::text, 
    '#{baseUrl}tap/', 'application/x-votable+xml', CAST(0 AS BIGINT), 
	null, c.ra_deg, c.dec_deg, null, c.s_region_poly, null::double precision,
    null, null, null, null::double precision, 
    null, null, null::double precision, 
    'phot.flux.density', null, 'ASKAP', null,
    'catalogue.derived',
    'em.wl', 'm', null::double precision, null::double precision, null::double precision, 'pos.eq', 'deg', 
	c.released_date, null
	FROM casda.catalogue c, casda.project p
	WHERE c.catalogue_type::text = 'DERIVED_CATALOGUE'::text AND p.id = c.project_id AND c.deposit_state::text = 'DEPOSITED'::text and c.level7_active = 't'

    union all

	-- Advertise spectra via obs_core
	select 'spectrum', 2, p.short_name, obs.sbid::text, 'spectrum-' || s.id::text, 
	'#{baseUrl}datalink/links?ID=spectrum-'||s.id, 'application/x-votable+xml;content=datalink', s.filesize, 
	s.target_name, s.ra_deg, s.dec_deg, null::double precision, s.s_region_poly, null::double precision, 
	s.t_min, s.t_max, s.t_exptime, null::double precision, 
	s.em_min, s.em_max, null::double precision, 
	concat('phot.flux.density', case stokes_parameters when '//' then '' when '/I/' then '' else ';phys.polarisation' end), 
	s.stokes_parameters, obs.telescope, null,
	lower(replace(s.type,'_', '.')), 
	'em.wl', 'm', s.em_resolution, null::double precision, null::double precision, 'pos.eq', 'deg', s.released_date,
	s.quality_level
	from casda.spectrum s, casda.observation obs, casda.project p
	where obs.id = s.observation_id and p.id = s.project_id and s.deposit_state = 'DEPOSITED' and obs.deposit_state = 'DEPOSITED'

    union all

	-- Advertise moment_maps via obs_core
	select 'image', 2, p.short_name, obs.sbid::text, 'moment_map-' || mm.id::text, 
	'#{baseUrl}datalink/links?ID=moment_map-'||mm.id, 'application/x-votable+xml;content=datalink', mm.filesize, 
	mm.target_name, mm.ra_deg, mm.dec_deg, null::double precision, mm.s_region_poly, null::double precision, 
	mm.t_min, mm.t_max, mm.t_exptime, null::double precision, 
	mm.em_min, mm.em_max, null::double precision, 
	concat(case "type" when 'spectral_restored_mom0' then 'phot.flux.density;askap:arith.integrated;em.radio' 
			when 'spectral_restored_mom1' then 'spect.dopplerVeloc.opt' 
			when 'spectral_restored_mom2' then 'phys.veloc' 
			else 'phot.flux.density' end, 
		case stokes_parameters when '//' then '' when '/I/' then '' else ';phys.polarisation' end),	
	mm.stokes_parameters, obs.telescope, null,
	lower(replace(mm.type,'_', '.')), 
	'em.wl', 'm', mm.em_resolution, null::double precision, null::double precision, 'pos.eq', 'deg', mm.released_date,
	mm.quality_level
	from casda.moment_map mm, casda.observation obs, casda.project p
	where obs.id = mm.observation_id and p.id = mm.project_id and mm.deposit_state = 'DEPOSITED' and obs.deposit_state = 'DEPOSITED'
	
	union all

	-- Advertise cubelets via obs_core
	select 'image', 2, p.short_name, obs.sbid::text, 'cubelet-' || cube.id::text, 
	'#{baseUrl}datalink/links?ID=cubelet-'||cube.id, 'application/x-votable+xml;content=datalink', cube.filesize, 
	cube.target_name, cube.ra_deg, cube.dec_deg, null::double precision, cube.s_region_poly, null::double precision, 
	cube.t_min, cube.t_max, cube.t_exptime, null::double precision, 
	cube.em_min, cube.em_max, null::double precision, 
	concat('phot.flux.density', case stokes_parameters when '//' then '' when '/I/' then '' else ';phys.polarisation' end),
	cube.stokes_parameters, obs.telescope, null,
	lower(replace(cube.type,'_', '.')), 
	'em.wl', 'm', cube.em_resolution, null::double precision, null::double precision, 'pos.eq', 'deg', cube.released_date,
	cube.quality_level
	from casda.cubelet cube, casda.observation obs, casda.project p
	where obs.id = cube.observation_id and p.id = cube.project_id and cube.deposit_state = 'DEPOSITED' and obs.deposit_state = 'DEPOSITED'
	
    ORDER BY 4, 5;
  
COMMENT ON VIEW casda.obscore is 'An implementation of the Observation Data Model Core Components \
    to allow generic querying of the availability of CASDA data products.';
  
  