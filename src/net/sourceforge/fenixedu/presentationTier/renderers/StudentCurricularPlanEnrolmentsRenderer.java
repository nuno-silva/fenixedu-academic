package net.sourceforge.fenixedu.presentationTier.renderers;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import net.sourceforge.fenixedu.dataTransferObject.administrativeOffice.studentEnrolment.StudentEnrolmentBean;
import net.sourceforge.fenixedu.domain.CurricularCourse;
import net.sourceforge.fenixedu.domain.Enrolment;
import net.sourceforge.fenixedu.domain.ExecutionPeriod;
import net.sourceforge.fenixedu.domain.StudentCurricularPlan;
import net.sourceforge.fenixedu.domain.degreeStructure.Context;
import net.sourceforge.fenixedu.domain.enrolment.DegreeModuleToEnrol;
import net.sourceforge.fenixedu.domain.studentCurriculum.CurriculumGroup;
import net.sourceforge.fenixedu.domain.studentCurriculum.CurriculumLine;
import net.sourceforge.fenixedu.domain.studentCurriculum.CurriculumModule;
import net.sourceforge.fenixedu.presentationTier.renderers.converters.DomainObjectKeyArrayConverter;
import net.sourceforge.fenixedu.presentationTier.renderers.converters.DomainObjectKeyConverter;
import net.sourceforge.fenixedu.renderers.InputRenderer;
import net.sourceforge.fenixedu.renderers.components.HtmlBlockContainer;
import net.sourceforge.fenixedu.renderers.components.HtmlCheckBox;
import net.sourceforge.fenixedu.renderers.components.HtmlComponent;
import net.sourceforge.fenixedu.renderers.components.HtmlMultipleHiddenField;
import net.sourceforge.fenixedu.renderers.components.HtmlMultipleValueComponent;
import net.sourceforge.fenixedu.renderers.components.HtmlTable;
import net.sourceforge.fenixedu.renderers.components.HtmlTableCell;
import net.sourceforge.fenixedu.renderers.components.HtmlTableRow;
import net.sourceforge.fenixedu.renderers.components.HtmlText;
import net.sourceforge.fenixedu.renderers.components.controllers.HtmlController;
import net.sourceforge.fenixedu.renderers.components.converters.ConversionException;
import net.sourceforge.fenixedu.renderers.components.converters.Converter;
import net.sourceforge.fenixedu.renderers.components.state.IViewState;
import net.sourceforge.fenixedu.renderers.layouts.Layout;
import net.sourceforge.fenixedu.renderers.model.MetaObject;
import net.sourceforge.fenixedu.renderers.model.MetaObjectFactory;
import net.sourceforge.fenixedu.renderers.schemas.Schema;
import net.sourceforge.fenixedu.util.LanguageUtils;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.comparators.ComparatorChain;

public class StudentCurricularPlanEnrolmentsRenderer extends InputRenderer {
    
    private static Integer initialWidth = 70;

    private static Integer widthDecreasePerLevel = 3;


    public StudentCurricularPlanEnrolmentsRenderer() {
	super();
    }

    @Override
    protected Layout getLayout(Object object, Class type) {
	return new StudentCurricularPlanEnrolmentLayout();
    }

    private class StudentCurricularPlanEnrolmentLayout extends Layout {

	private final CopyCheckBoxValuesController enrollmentsController = new CopyCheckBoxValuesController();
	
	private final CopyCheckBoxValuesController degreeModulesToEnrolController = new CopyCheckBoxValuesController(); 
	
	private final List<CurriculumModule> initialCurriculumModules = new ArrayList<CurriculumModule>();
	
	private final ResourceBundle enumerationResources = ResourceBundle.getBundle(
	    	"resources.EnumerationResources", LanguageUtils.getLocale());

	private final ResourceBundle academicAdminOfficeResources = ResourceBundle.getBundle(
	    	"resources.AcademicAdminOffice", LanguageUtils.getLocale());

	
	@Override
	public HtmlComponent createComponent(Object object, Class type) {
	    StudentEnrolmentBean studentEnrolmentBean = (StudentEnrolmentBean) object;

	    HtmlBlockContainer container = new HtmlBlockContainer();
	    
	    if (studentEnrolmentBean == null) {
		return new HtmlText();
	    } 

	    HtmlMultipleHiddenField hiddenEnrollments = new HtmlMultipleHiddenField();
	    hiddenEnrollments.bind(getInputContext().getMetaObject(), "curriculumModules"); // slot refered by name
	    hiddenEnrollments.setConverter(new DomainObjectKeyArrayConverter());
	    hiddenEnrollments.setController(enrollmentsController);

	    HtmlMultipleHiddenField hiddenDegreeModulesToEnrol = new HtmlMultipleHiddenField();
	    hiddenDegreeModulesToEnrol.bind(getInputContext().getMetaObject(), "degreeModulesToEnrol"); // slot refered by name
	    hiddenDegreeModulesToEnrol.setConverter(new DegreeModuleToEnrolKeyConverter());
	    hiddenDegreeModulesToEnrol.setController(degreeModulesToEnrolController);

	    container.addChild(hiddenEnrollments);
	    container.addChild(hiddenDegreeModulesToEnrol);

	    generateModules(container, studentEnrolmentBean.getStudentCurricularPlan(), studentEnrolmentBean.getStudentCurricularPlan().getRoot(), studentEnrolmentBean.getExecutionPeriod(), 0);
	    studentEnrolmentBean.setInitialCurriculumModules(initialCurriculumModules);
	    return container;
	}

	private void generateModules(HtmlBlockContainer blockContainer, StudentCurricularPlan studentCurricularPlan, CurriculumGroup group, ExecutionPeriod executionPeriod, int depth) {
	    final HtmlTable groupTable = new HtmlTable();
	    blockContainer.addChild(groupTable);
	    groupTable.setClasses("showinfo3 mvert0");
	    groupTable.setStyle("width: " + (initialWidth - depth) + "em; margin-left: " + depth + "em;");
	    
	    HtmlTableRow htmlTableRow = groupTable.createRow();
	    htmlTableRow.setClasses("bgcolor2");
	    
	    
	    if(group.hasAnyCurriculumModules()) {
		htmlTableRow.createCell().setBody(new HtmlText(group.getDegreeModule().getName()));
		generateDegreeModulesEnroled(blockContainer, group, studentCurricularPlan, executionPeriod, depth);
	    } else {
		htmlTableRow.createCell().setBody(new HtmlText(group.getDegreeModule().getName()));
		HtmlTableCell checkBoxCell = htmlTableRow.createCell();
		checkBoxCell.setClasses("aright");
		
		HtmlCheckBox checkBox = new HtmlCheckBox(true);
		MetaObject enrolmentMetaObject = MetaObjectFactory.createObject(group, new Schema(CurriculumGroup.class));
		checkBox.setName("enrolmentCheckBox" + group.getIdInternal());
		checkBox.setUserValue(enrolmentMetaObject.getKey().toString());
		enrollmentsController.addCheckBox(checkBox);
		checkBoxCell.setBody(checkBox);
		initialCurriculumModules.add(group);
	    }
		
	    generateDegreeModulesToEnrol(blockContainer, group, studentCurricularPlan, executionPeriod, depth);
	}

	private void generateDegreeModulesToEnrol(HtmlBlockContainer blockContainer, CurriculumGroup group, StudentCurricularPlan studentCurricularPlan, ExecutionPeriod executionPeriod, int depth) {
	    final HtmlTable groupTable = new HtmlTable();
	    blockContainer.addChild(groupTable);
	    groupTable.setClasses("showinfo3 mvert0");
	    groupTable.setStyle("width: " + (initialWidth - depth - widthDecreasePerLevel) + "em; margin-left: " + (depth + widthDecreasePerLevel) + "em;");
	    
	    generateCurricularCoursesToEnrol(groupTable, group, executionPeriod);
	    generateCourseGroupsToEnrol(groupTable, group, executionPeriod);
	}
	
	

	private void generateCourseGroupsToEnrol(HtmlTable groupTable, CurriculumGroup group, ExecutionPeriod executionPeriod) {
	    List<Context> curricularCoursesToEnrol = group.getCourseGroupContextsToEnrol(executionPeriod);
	    Collections.sort(curricularCoursesToEnrol, new BeanComparator("childDegreeModule.name", Collator.getInstance()));
	    
	    for (Context context : curricularCoursesToEnrol) {
		HtmlTableRow htmlTableRow = groupTable.createRow();
		htmlTableRow.setClasses("bgcolor2");
		htmlTableRow.createCell().setBody(new HtmlText(context.getChildDegreeModule().getName()));
		HtmlTableCell checkBoxCell = htmlTableRow.createCell();
		checkBoxCell.setClasses("aright");

		DegreeModuleToEnrol degreeModuleToEnrol = new DegreeModuleToEnrol(group, context);
		HtmlCheckBox checkBox = new HtmlCheckBox(false);
		checkBox.setName("degreeModuleToEnrolCheckBox" + degreeModuleToEnrol.getContext().getIdInternal() + ":" + degreeModuleToEnrol.getCurriculumGroup().getIdInternal());
		checkBox.setUserValue(degreeModuleToEnrol.getKey());
		degreeModulesToEnrolController.addCheckBox(checkBox);
		checkBoxCell.setBody(checkBox);
	    }
	}

	private void generateCurricularCoursesToEnrol(HtmlTable groupTable, CurriculumGroup group, ExecutionPeriod executionPeriod) {
	    List<Context> curricularCoursesToEnrol = group.getCurricularCourseContextsToEnrol(executionPeriod);
	    ComparatorChain comparatorChain = new ComparatorChain();
	    comparatorChain.addComparator(new BeanComparator("curricularPeriod"));
	    comparatorChain.addComparator(new BeanComparator("childDegreeModule.name", Collator.getInstance()));
	    Collections.sort(curricularCoursesToEnrol, comparatorChain);
	    
	    for (Context context : curricularCoursesToEnrol) {
		CurricularCourse curricularCourse = (CurricularCourse) context.getChildDegreeModule();

		HtmlTableRow htmlTableRow = groupTable.createRow();
		htmlTableRow.createCell().setBody(new HtmlText(curricularCourse.getName()));

		//Year
		final HtmlTableCell yearCell = htmlTableRow.createCell();
		yearCell.setClasses("smalltxt");
		yearCell.setAlign("rigth");

		final StringBuilder year = new StringBuilder();
		year.append(context.getCurricularPeriod().getFullLabel());
		yearCell.setBody(new HtmlText(year.toString()));

		//Ects
		final HtmlTableCell ectsCell = htmlTableRow.createCell();
		ectsCell.setClasses("smalltxt");
		ectsCell.setAlign("rigth");

		final StringBuilder ects = new StringBuilder();
		ects.append(curricularCourse.getEctsCredits()).append(" ").append(academicAdminOfficeResources.getString("credits.abbreviation"));
		ectsCell.setBody(new HtmlText(ects.toString()));


		HtmlTableCell checkBoxCell = htmlTableRow.createCell();
		checkBoxCell.setClasses("aright");

		DegreeModuleToEnrol degreeModuleToEnrol = new DegreeModuleToEnrol(group, context);
		HtmlCheckBox checkBox = new HtmlCheckBox(false);
		checkBox.setName("degreeModuleToEnrolCheckBox" + degreeModuleToEnrol.getContext().getIdInternal() + ":" + degreeModuleToEnrol.getCurriculumGroup().getIdInternal());
		checkBox.setUserValue(degreeModuleToEnrol.getKey());
		degreeModulesToEnrolController.addCheckBox(checkBox);
		checkBoxCell.setBody(checkBox);
	    }
	}

	private void generateDegreeModulesEnroled(HtmlBlockContainer blockContainer, CurriculumGroup group, StudentCurricularPlan studentCurricularPlan, ExecutionPeriod executionPeriod, int depth) {
	    final HtmlTable groupTable = new HtmlTable();
	    blockContainer.addChild(groupTable);
	    groupTable.setClasses("showinfo3 mvert0");
	    groupTable.setStyle("width: " + (initialWidth - depth - widthDecreasePerLevel) + "em; margin-left: " + (depth + widthDecreasePerLevel) + "em;");
	    
	    for (CurriculumLine curriculumLine : group.getCurriculumLines()) {
		if(((CurriculumLine) curriculumLine).isEnrolment()) {
		    Enrolment enrolment = (Enrolment) curriculumLine;
		    if(enrolment.getExecutionPeriod().equals(executionPeriod) && enrolment.isEnroled()) {
			generateEnrolment(groupTable, enrolment);
		    }
		}
	    }

	    for (CurriculumGroup curriculumGroup : group.getCurriculumGroups()) {
		generateModules(blockContainer, studentCurricularPlan, curriculumGroup, executionPeriod, depth + 3);
	    }
	}

	private void generateEnrolment(final HtmlTable groupTable, Enrolment enrolment) {
	    HtmlTableRow htmlTableRow = groupTable.createRow();
	    HtmlTableCell cellName = htmlTableRow.createCell();
	    cellName.setBody(new HtmlText(enrolment.getCurricularCourse().getName()));
	    
	    // Year
	    final HtmlTableCell yearCell = htmlTableRow.createCell();
	    yearCell.setClasses("smalltxt");
	    yearCell.setAlign("rigth");

	    final StringBuilder year = new StringBuilder();
	    year.append(enrolment.getExecutionPeriod().getExecutionYear().getYear());
	    yearCell.setBody(new HtmlText(year.toString()));

	    // Semester
	    final HtmlTableCell semesterCell = htmlTableRow.createCell();
	    semesterCell.setClasses("smalltxt");
	    semesterCell.setAlign("rigth");

	    final StringBuilder semester = new StringBuilder();
	    semester.append(enrolment.getExecutionPeriod().getSemester().toString());
	    semester.append(" ");
	    semester.append(enumerationResources.getString("SEMESTER.ABBREVIATION"));
	    semesterCell.setBody(new HtmlText(semester.toString()));

	    //Ects
	    final HtmlTableCell ectsCell = htmlTableRow.createCell();
	    ectsCell.setClasses("smalltxt");
	    ectsCell.setAlign("rigth");

	    final StringBuilder ects = new StringBuilder();
	    ects.append(enrolment.getCurricularCourse().getEctsCredits()).append(" ").append(academicAdminOfficeResources.getString("credits.abbreviation"));
	    ectsCell.setBody(new HtmlText(ects.toString()));
	    

	    MetaObject enrolmentMetaObject = MetaObjectFactory.createObject(enrolment, new Schema(Enrolment.class));

	    HtmlCheckBox checkBox = new HtmlCheckBox(true);
	    checkBox.setName("enrolmentCheckBox" + enrolment.getIdInternal());
	    checkBox.setUserValue(enrolmentMetaObject.getKey().toString());
	    enrollmentsController.addCheckBox(checkBox);

	    HtmlTableCell cellCheckBox = htmlTableRow.createCell();
	    cellCheckBox.setClasses("aright");
	    cellCheckBox.setBody(checkBox);

	    initialCurriculumModules.add(enrolment);
	}
	

    }
    
    private static class CopyCheckBoxValuesController extends HtmlController {

	private List<HtmlCheckBox> checkboxes;
	
	public CopyCheckBoxValuesController() {
	    super();

	    this.checkboxes = new ArrayList<HtmlCheckBox>();
	}

	public void addCheckBox(HtmlCheckBox checkBox) {
	    this.checkboxes.add(checkBox);
	}

	@Override
	public void execute(IViewState viewState) {
	    HtmlMultipleValueComponent component = (HtmlMultipleValueComponent) getControlledComponent();
	    
	    List<String> values = new ArrayList<String>();

	    for (HtmlCheckBox checkBox : this.checkboxes) {
		if (checkBox.isChecked()) {
		    values.add(checkBox.getValue());
		}
	    }
	    
	    component.setValues(values.toArray(new String[0]));
	}
    }
    
    public static class DegreeModuleToEnrolKeyConverter extends Converter {

	@Override
	public Object convert(Class type, Object value) {
	    DomainObjectKeyConverter converter = new DomainObjectKeyConverter();
	    List<DegreeModuleToEnrol> result = new ArrayList<DegreeModuleToEnrol>();

	    if (value == null) {
		return null;
	    }

	    String[] values = (String[]) value;
	    for (int i = 0; i < values.length; i++) {
		String key = values[i];

		String[] parts = key.split(",");
		if (parts.length < 2) {
		    throw new ConversionException("invalid key format: " + key);
		}

		Context context = (Context) converter.convert(type, parts[0]);
		CurriculumGroup curriculumGroup = (CurriculumGroup) converter.convert(type, parts[1]);
		DegreeModuleToEnrol degreeModuleToEnrol = new DegreeModuleToEnrol(curriculumGroup, context);
		result.add(degreeModuleToEnrol);
	    }
	    
	    return result;
	}
    }
}
