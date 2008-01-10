insert into NODE(KEY_PARENT,KEY_CHILD,NODE_ORDER,VISIBLE,OJB_CONCRETE_CLASS,ASCENDING,CONTENT_ID) 
select F.KEY_ITEM, A.ID_INTERNAL,FIO.ITEM_ORDER,F.VISIBLE,'net.sourceforge.fenixedu.domain.contents.ExplicitOrderNode','1', UUID() 
from FILE F, CONTENT A, FILE_ITEM_ORDER FIO WHERE A.OJB_CONCRETE_CLASS='net.sourceforge.fenixedu.domain.contents.Attachment' 
AND A.KEY_FILE_ITEM  = F.ID_INTERNAL AND F.ID_INTERNAL = FIO.KEY_FILE_ITEM;

DROP TABLE FILE_ITEM_ORDER;