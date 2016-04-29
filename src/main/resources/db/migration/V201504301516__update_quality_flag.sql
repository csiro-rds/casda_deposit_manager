-- CASDA-4304 fix case for quality flags

UPDATE casda.quality_flag SET label='Radio frequency interference', last_modified=now() WHERE code='RFI';
UPDATE casda.quality_flag SET label='Low sensitivity', last_modified=now() WHERE code='LS';
UPDATE casda.quality_flag SET label='Poor spectral baselines', last_modified=now() WHERE code='PSB';
