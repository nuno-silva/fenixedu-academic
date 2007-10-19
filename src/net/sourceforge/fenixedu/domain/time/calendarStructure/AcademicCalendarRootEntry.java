package net.sourceforge.fenixedu.domain.time.calendarStructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sourceforge.fenixedu.domain.RootDomainObject;
import net.sourceforge.fenixedu.domain.exceptions.DomainException;
import net.sourceforge.fenixedu.domain.time.chronologies.AcademicChronology;
import net.sourceforge.fenixedu.injectionCode.Checked;
import net.sourceforge.fenixedu.util.MultiLanguageString;

import org.joda.time.DateTime;

public class AcademicCalendarRootEntry extends AcademicCalendarRootEntry_Base {

    private transient AcademicChronology academicChronology;
    
    public AcademicCalendarRootEntry(MultiLanguageString title, MultiLanguageString description, AcademicCalendarEntry templateCalendar) {
	super();	
	setRootDomainObjectForRootEntries(RootDomainObject.getInstance());
	setTitle(title);
	setDescription(description);	
	setTemplateEntry(templateCalendar);
    }
       
    @Checked("AcademicCalendarPredicates.checkPermissionsToManageAcademicCalendarEntry")
    public void edit(MultiLanguageString title, MultiLanguageString description, AcademicCalendarEntry templateCalendar) {	
	setTitle(title);
	setDescription(description);
	setTemplateEntry(templateCalendar);
    }
    
    @Override
    public void delete() {
	super.setRootDomainObjectForRootEntries(null);	
        super.delete();
    }
    
    @Override
    public void setTemplateEntry(AcademicCalendarEntry templateEntry) {
        if(templateEntry != null && getBasedEntries().contains(templateEntry)) {
            throw new DomainException("error.RootEntry.invalid.templateEntry");
        }
        super.setTemplateEntry(templateEntry);
    }
    
    @Override
    public void setBegin(DateTime begin) {        
	throw new DomainException("error.AcademicCalendarRootEntry.impossible.edit.begin.date");
    }
    
    @Override
    public void setEnd(DateTime end) {        
	throw new DomainException("error.AcademicCalendarRootEntry.impossible.edit.end.date");
    }
    
    @Override
    public void setParentEntry(AcademicCalendarEntry parentEntry) {
        throw new DomainException("error.AcademicCalendarRootEntry.impossible.add.parent.unit");
    }
    
    @Override
    protected AcademicCalendarEntry makeAnEntryCopyInDifferentCalendar(AcademicCalendarEntry parentEntry, boolean virtual) {	
	throw new DomainException("error.unsupported.operation");	
    }
    
    @Override
    public void setRootDomainObjectForRootEntries(RootDomainObject rootDomainObjectForRootEntries) {
        if(rootDomainObjectForRootEntries == null) {
            throw new DomainException("error.RootEntry.empty.rootDomainObject.to.academic.calendars");
        }
        super.setRootDomainObjectForRootEntries(rootDomainObjectForRootEntries);
    }
               
    public AcademicChronology getAcademicChronology() {
	if (academicChronology == null) {
	    academicChronology = new AcademicChronology(this);
	}
	return academicChronology;
    }

    @Override
    public DateTime getBegin() {
	SortedSet<AcademicCalendarEntry> result = new TreeSet<AcademicCalendarEntry>(AcademicCalendarEntry.COMPARATOR_BEGIN_DATE);
	result.addAll(getChildEntries());
	return (result.isEmpty()) ? null : result.first().getBegin();
    }
    
    @Override
    public DateTime getEnd() {
        return null;
    }
    
    public List<? extends AcademicCalendarEntry> getAllChildEntriesOrderByDate(Class<? extends AcademicCalendarEntry> entryClass,
	    Class<? extends AcademicCalendarEntry> parentEntryClass) {

	Set<AcademicCalendarEntry> result = new TreeSet<AcademicCalendarEntry>(AcademicCalendarEntry.COMPARATOR_BEGIN_DATE);
	for (AcademicCalendarEntry entry : getChildEntriesSet()) {
	    if (entry.getClass().equals(entryClass) && (parentEntryClass == null || 
		    (parentEntryClass != null && entry.getParentEntry().getClass().equals(parentEntryClass)))) {
		result.add(entry);
	    }
	    result.addAll(entry.getAllChildEntries(entry, entryClass, parentEntryClass));
	}
	return new ArrayList(result);
    }
    
    public AcademicCalendarEntry getEntryByInstant(long instant, Class<? extends AcademicCalendarEntry> entryClass,
	    Class<? extends AcademicCalendarEntry> parentEntryClass) {
	
	List<AcademicCalendarEntry> allEntriesByType = (List<AcademicCalendarEntry>) getAllChildEntriesOrderByDate(entryClass, parentEntryClass);
	for (AcademicCalendarEntry entry : allEntriesByType) {
	    if (entry.containsInstant(instant)) {
		return entry;
	    }
	}
	return null;
    }

    public Integer getEntryValueByInstant(long instant, Class<? extends AcademicCalendarEntry> entryClass, Class<? extends AcademicCalendarEntry> parentEntryClass) {	
	Integer counter = 0;
	List<AcademicCalendarEntry> allEntriesByType = (List<AcademicCalendarEntry>) getAllChildEntriesOrderByDate(entryClass, parentEntryClass);
	for (AcademicCalendarEntry entry : allEntriesByType) {
	    counter++;
	    if (entry.containsInstant(instant)) {
		return counter;
	    }
	}
	return null;
    }

    @Override
    protected boolean areIntersectionsPossible() {	
	return true;
    }

    @Override
    protected boolean areOutOfBoundsPossible() {	
	return true;
    }

    @Override
    protected boolean exceededNumberOfChildEntries(AcademicCalendarEntry childEntry) {	
	return false;
    }

    @Override
    protected boolean isParentEntryInvalid(AcademicCalendarEntry parentEntry) {	
	return true;
    }
    
    @Override
    public boolean isRootEntry() {
        return true;
    }
}
