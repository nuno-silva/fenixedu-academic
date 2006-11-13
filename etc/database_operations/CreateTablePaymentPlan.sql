DROP TABLE IF EXISTS PAYMENT_PLAN;
CREATE TABLE PAYMENT_PLAN (
  ID_INTERNAL int(11) unsigned NOT NULL auto_increment,
  KEY_EXECUTION_YEAR int(11) NOT NULL,
  KEY_SERVICE_AGREEMENT_TEMPLATE int(11) NOT NULL,
  WHEN_CREATED datetime NOT NULL,
  OJB_CONCRETE_CLASS VARCHAR(255) NOT NULL,
  KEY_ROOT_DOMAIN_OBJECT int(11) NOT NULL default '1',
  PRIMARY KEY  (ID_INTERNAL),
  KEY (KEY_ROOT_DOMAIN_OBJECT),
  KEY (KEY_EXECUTION_YEAR),
  KEY (KEY_SERVICE_AGREEMENT_TEMPLATE)
) ENGINE=InnoDB;
