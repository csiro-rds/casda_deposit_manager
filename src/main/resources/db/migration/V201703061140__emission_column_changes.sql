-- CASDA-5973 - Update polarisation and emission catalogue tables to match catalogues coming in from ASKAPsoft

alter table casda.spectral_line_emission rename column flux_voxel_stdev to flux_voxel_stddev;

alter table casda.polarisation_component ALTER column flag_p1 SET DATA TYPE integer using case when flag_p1=true then 1 else 0 end;
alter table casda.polarisation_component ALTER column flag_p2 SET DATA TYPE integer using case when flag_p2=true then 1 else 0 end;
alter table casda.polarisation_component ALTER column flag_p3 SET DATA TYPE integer using case when flag_p3='t' then 1 else 0 end;
alter table casda.polarisation_component ALTER column flag_p4 SET DATA TYPE integer using case when flag_p4='t' then 1 else 0 end;

