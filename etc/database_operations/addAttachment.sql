UPDATE CONTENT CI, FILE FI, ACCESSIBLE_ITEM I SET FI.KEY_ITEM = CI.ID_INTERNAL 
WHERE FI.OJB_CONCRETE_CLASS = 'net.sourceforge.fenixedu.domain.FileItem' AND I.OJB_CONCRETE_CLASS LIKE "%.Item"
AND FI.KEY_ITEM = I.ID_INTERNAL AND I.ID_INTERNAL = CI.OLD_ID_INTERNAL AND CI.OJB_CONCRETE_CLASS LIKE "%.Item";

ALTER TABLE CONTENT ADD COLUMN KEY_FILE_ITEM INT(11) DEFAULT NULL;
ALTER TABLE CONTENT ADD INDEX (KEY_FILE_ITEM);

ALTER TABLE FILE ADD COLUMN KEY_ATTACHMENT INT(11) DEFAULT NULL;
ALTER TABLE FILE ADD INDEX (KEY_ATTACHMENT);

insert into CONTENT(OJB_CONCRETE_CLASS, CONTENT_ID, KEY_FILE_ITEM) 
select 'net.sourceforge.fenixedu.domain.contents.Attachment', UUID(), F.ID_INTERNAL
FROM FILE F, CONTENT C WHERE F.OJB_CONCRETE_CLASS='net.sourceforge.fenixedu.domain.FileItem' AND F.KEY_ITEM = C.ID_INTERNAL;

UPDATE FILE, CONTENT SET FILE.KEY_ATTACHMENT = CONTENT.ID_INTERNAL WHERE CONTENT.OJB_CONCRETE_CLASS = 'net.sourceforge.fenixedu.domain.contents.Attachment' AND CONTENT.KEY_FILE_ITEM = FILE.ID_INTERNAL;

CREATE TABLE FILE_ITEM_ORDER (KEY_PARENT INTEGER, KEY_FILE_ITEM INTEGER, ITEM_ORDER INTEGER, INDEX(KEY_FILE_ITEM));
