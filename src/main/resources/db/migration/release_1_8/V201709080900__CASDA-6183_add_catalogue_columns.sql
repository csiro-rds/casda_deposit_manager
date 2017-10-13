-- CASDA-6183 - Additional catalogue columns to accommodate pipleine changes.

-- ### Add new fields to the continuum_component table
ALTER TABLE casda.continuum_component 
ADD COLUMN maj_axis_deconv_err real,
ADD COLUMN min_axis_deconv_err real,
ADD COLUMN pos_ang_deconv_err real,
ADD COLUMN spectral_index_err real,
ADD COLUMN spectral_curvature_err real;
COMMENT ON COLUMN casda.continuum_component.maj_axis_deconv_err IS 'Error in FWHM major axis after deconvolution';
COMMENT ON COLUMN casda.continuum_component.min_axis_deconv_err IS 'Error in FWHM minor axis after deconvolution';
COMMENT ON COLUMN casda.continuum_component.pos_ang_deconv_err IS 'Error in position angle after deconvolution';
COMMENT ON COLUMN casda.continuum_component.spectral_index_err IS 'Error in spectral index (First Taylor term)';
COMMENT ON COLUMN casda.continuum_component.spectral_curvature_err IS 'Error in spectral curvature (Second Taylor term)';
COMMENT ON COLUMN casda.continuum_component.flag_c3 IS 'Spectral index & curvature from fit to Taylor term images';

-- Create gaps in the column order to fit in the new fields in tap output
update casda.tap_columns set column_order = column_order + 2 where table_name = 'casda.continuum_component' and column_order > 31;
update casda.tap_columns set column_order = column_order + 3 where table_name = 'casda.continuum_component' and column_order > 27;

-- Add new columns to tap output
INSERT INTO casda.tap_columns (column_order, table_name, column_name, db_column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
VALUES
(26, 'casda.continuum_component', 'maj_axis_deconv_err', 'maj_axis_deconv_err','Error in FWHM major axis after deconvolution', 'REAL', 13, 'arcsec', NULL, 'stat.error;phys.angSize.smajAxis;em.radio;askap:meta.deconvolved', 0, 0, 1, 3),    
(28, 'casda.continuum_component', 'min_axis_deconv_err', 'min_axis_deconv_err','Error in FWHM minor axis after deconvolution', 'REAL', 13, 'arcsec', NULL, 'stat.error;phys.angSize.sminAxis;em.radio;askap:meta.deconvolved', 0, 0, 1, 3),    
(30, 'casda.continuum_component', 'pos_ang_deconv_err', 'pos_ang_deconv_err','Error in position angle after deconvolution', 'REAL', 13, 'deg', NULL, 'stat.error;phys.angSize;pos.posAng;em.radio;askap:meta.deconvolved', 0, 0, 1, 3),    
(34, 'casda.continuum_component', 'spectral_index_err', 'spectral_index_err','Error in spectral index (First Taylor term)', 'REAL', 13, NULL, NULL, 'stat.error;spect.index;em.radio', 0, 1, 1, 3),    
(36, 'casda.continuum_component', 'spectral_curvature_err', 'spectral_curvature_err','Error in spectral curvature (Second Taylor term)', 'REAL', 13, NULL, NULL, 'stat.error;askap:spect.curvature;em.radio', 0, 0, 1, 3);    


-- Update the flag column in tap output
UPDATE casda.tap_columns SET column_name = 'spectral_index_from_tt', description = 'Spectral index &amp; curvature from fit to Taylor term images' 
WHERE table_name = 'casda.continuum_component' and column_name = 'flag_c3';




-- ### Add new fields to the continuum_island table
ALTER TABLE casda.continuum_island 
ADD COLUMN flux_int_err real,
ADD COLUMN mean_background real,
ADD COLUMN background_noise real,
ADD COLUMN max_residual real,
ADD COLUMN min_residual real,
ADD COLUMN mean_residual real,
ADD COLUMN rms_residual real,
ADD COLUMN stddev_residual real,
ADD COLUMN solid_angle real,
ADD COLUMN beam_area real;
COMMENT ON COLUMN casda.continuum_island.flux_int_err IS 'Error on the integrated flux density';
COMMENT ON COLUMN casda.continuum_island.mean_background IS 'Mean value of the background level over the island';
COMMENT ON COLUMN casda.continuum_island.background_noise IS 'Average noise value in the background over the island extent';
COMMENT ON COLUMN casda.continuum_island.max_residual IS 'Maximum value of the residual after subtracting fitted components';
COMMENT ON COLUMN casda.continuum_island.min_residual IS 'Minimum value of the residual after subtracting fitted components';
COMMENT ON COLUMN casda.continuum_island.mean_residual IS 'Mean value of the residual after subtracting fitted components';
COMMENT ON COLUMN casda.continuum_island.rms_residual IS 'RMS of the residual after subtracting fitted components';
COMMENT ON COLUMN casda.continuum_island.stddev_residual IS 'Standard deviation of the residual after subtracting fitted components';
COMMENT ON COLUMN casda.continuum_island.solid_angle IS 'Area of the island on the sky';
COMMENT ON COLUMN casda.continuum_island.beam_area IS 'Solid angle of the restoring beam';


-- Rearrange columns in tap output
update casda.tap_columns set column_order = column_order + 2 where table_name = 'casda.continuum_island' and column_order > 22;
update casda.tap_columns set column_order = column_order + 7 where table_name = 'casda.continuum_island' and column_order > 17;
update casda.tap_columns set column_order = column_order + 1 where table_name = 'casda.continuum_island' and column_order > 16;

-- Add new columns to tap output
INSERT INTO casda.tap_columns (column_order, table_name, column_name, db_column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
VALUES
(17, 'casda.continuum_island', 'flux_int_err', 'flux_int_err','Error on the integrated flux density', 'REAL', 13, 'mJy', NULL, 'stat.error;phot.flux.density.integrated;em.radio', 0, 0, 1, 3),    
(19, 'casda.continuum_island', 'mean_background', 'mean_background','Mean value of the background level over the island', 'REAL', 13, 'mJy/beam', NULL, 'askap:phot.flux.density.voxel;instr.skyLevel;stat.mean;em.radio', 0, 0, 1, 3),    
(20, 'casda.continuum_island', 'background_noise', 'background_noise','Average noise value in the background over the island extent', 'REAL', 13, 'mJy/beam', NULL, 'askap:phot.flux.density.voxel;instr.skyLevel;askap:stat.rms;em.radio', 0, 0, 1, 3),    
(21, 'casda.continuum_island', 'max_residual', 'max_residual','Maximum value of the residual after subtracting fitted components', 'REAL', 13, 'mJy/beam', NULL, 'askap:phot.flux.density.voxel;stat.max;src.net;em.radio', 0, 0, 1, 3),    
(22, 'casda.continuum_island', 'min_residual', 'min_residual','Minimum value of the residual after subtracting fitted components', 'REAL', 13, 'mJy/beam', NULL, 'askap:phot.flux.density.voxel;stat.min;src.net;em.radio', 0, 0, 1, 3),    
(23, 'casda.continuum_island', 'mean_residual', 'mean_residual','Mean value of the residual after subtracting fitted components', 'REAL', 13, 'mJy/beam', NULL, 'askap:phot.flux.density.voxel;stat.mean;src.net;em.radio', 0, 0, 1, 3),    
(24, 'casda.continuum_island', 'rms_residual', 'rms_residual','RMS of the residual after subtracting fitted components', 'REAL', 13, 'mJy/beam', NULL, 'askap:phot.flux.density.voxel;askap:stat.rms;src.net;em.radio', 0, 0, 1, 3),    
(25, 'casda.continuum_island', 'stddev_residual', 'stddev_residual','Standard deviation of the residual after subtracting fitted components', 'REAL', 13, 'mJy/beam', NULL, 'askap:phot.flux.density.voxel;stat.stdev;src.net;em.radio', 0, 0, 1, 3),    
(31, 'casda.continuum_island', 'solid_angle', 'solid_angle','Area of the island on the sky', 'REAL', 13, 'arcmin2', NULL, 'phys.angArea', 0, 0, 1, 3),    
(31, 'casda.continuum_island', 'beam_area', 'beam_area','Solid angle of the restoring beam', 'REAL', 13, 'arcmin2', NULL, 'phys.angArea;instr.beam', 0, 0, 1, 3);    



