ALTER TABLE casda.observation ADD COLUMN redeposit_started timestamp with time zone;

COMMENT ON COLUMN casda.observation.redeposit_started is 'The datetime that the redeposit of this observation started';