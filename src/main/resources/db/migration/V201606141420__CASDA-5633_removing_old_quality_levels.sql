update casda.image_cube set quality_level = 'NOT_VALIDATED' where quality_level = 'UNDER_REVIEW' or quality_level = 'NOT_APPLICABLE';

update casda.measurement_set set quality_level = 'NOT_VALIDATED' where quality_level = 'UNDER_REVIEW' or quality_level = 'NOT_APPLICABLE';

update casda.catalogue set quality_level = 'NOT_VALIDATED' where quality_level = 'UNDER_REVIEW' or quality_level = 'NOT_APPLICABLE';