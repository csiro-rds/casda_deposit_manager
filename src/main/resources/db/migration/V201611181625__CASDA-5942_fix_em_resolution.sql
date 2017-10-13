-- CASDA-5942 - CASDA - Data Deposit - em_resolution not being calculated during deposit 
-- Populate the em_resolution field for existing image, spectrum and moment_map records  

update casda.image_cube set em_resolution = (em_max-em_min)/num_chan 
  where em_max is not null and em_resolution is null and num_chan > 0;

update casda.spectrum set em_resolution = (em_max-em_min)/num_chan 
  where em_max is not null and em_resolution is null and num_chan > 0;

update casda.moment_map set em_resolution = (em_max-em_min)/num_chan 
  where em_max is not null and em_resolution is null and num_chan > 0;
