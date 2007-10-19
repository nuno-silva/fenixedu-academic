package net.sourceforge.fenixedu.domain.time.calendarStructure;

import net.sourceforge.fenixedu.domain.exceptions.DomainException;
import net.sourceforge.fenixedu.util.MultiLanguageString;

import org.joda.time.DateTime;

public class GradeSubmissionCE extends GradeSubmissionCE_Base {
    
    public GradeSubmissionCE(AcademicCalendarEntry academicCalendarEntry, MultiLanguageString title, 
	    MultiLanguageString description, DateTime begin, DateTime end, SeasonType seasonType) {
	
	super();
	super.initEntry(academicCalendarEntry, title, description, begin, end);	
	setSeasonType(seasonType);
    }
    
    private GradeSubmissionCE(AcademicCalendarEntry academicCalendarEntry, GradeSubmissionCE gradeSubmissionCE) {	
	super();	
	super.initVirtualEntry(academicCalendarEntry, gradeSubmissionCE);		
    }

    @Override
    public boolean isGradeSubmissionPeriod() {
	return true;
    }
    
    @Override
    public void setSeasonType(SeasonType seasonType) {
        if(seasonType == null) {
            throw new DomainException("error.GradeSubmissionCE.empty.season.type");
        }
        super.setSeasonType(seasonType);
    }
    
    @Override
    public SeasonType getSeasonType() {
        if(isVirtual()) {
            return ((GradeSubmissionCE)getTemplateEntry()).getSeasonType();
        }
        return super.getSeasonType();
    }
    
    @Override
    protected boolean isParentEntryInvalid(AcademicCalendarEntry parentEntry) {
	return !parentEntry.isAcademicSemester() && !parentEntry.isAcademicTrimester();	
    } 
    
    @Override
    protected boolean areIntersectionsPossible() {	
	return false;
    }

    @Override
    protected boolean areOutOfBoundsPossible() {
	return false;
    }
    
    @Override
    protected boolean exceededNumberOfChildEntries(AcademicCalendarEntry childEntry) {	
	return false;
    }

    @Override
    protected AcademicCalendarEntry makeAnEntryCopyInDifferentCalendar(AcademicCalendarEntry parentEntry, boolean virtual) {
	if(virtual) {
	    return new GradeSubmissionCE(parentEntry, this);
	} else {
	    return new GradeSubmissionCE(parentEntry, getTitle(), getDescription(), getBegin(), getEnd(), getSeasonType());
	}
    }   
}
