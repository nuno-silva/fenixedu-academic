package net.sourceforge.fenixedu.presentationTier.Action.directiveCouncil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sourceforge.fenixedu.applicationTier.Servico.cardGeneration.DeleteCardGenerationEntryService;
import net.sourceforge.fenixedu.domain.Degree;
import net.sourceforge.fenixedu.domain.ExecutionYear;
import net.sourceforge.fenixedu.domain.Person;
import net.sourceforge.fenixedu.domain.cardGeneration.CardGenerationBatch;
import net.sourceforge.fenixedu.domain.cardGeneration.CardGenerationEntry;
import net.sourceforge.fenixedu.domain.cardGeneration.CardGenerationProblem;
import net.sourceforge.fenixedu.domain.cardGeneration.CardGenerationRegister;
import net.sourceforge.fenixedu.domain.cardGeneration.CardGenerationServices;
import net.sourceforge.fenixedu.domain.cardGeneration.Category;
import net.sourceforge.fenixedu.domain.cardGeneration.CardGenerationBatch.CardGenerationBatchCreator;
import net.sourceforge.fenixedu.domain.cardGeneration.CardGenerationBatch.CardGenerationBatchDeleter;
import net.sourceforge.fenixedu.domain.cardGeneration.CardGenerationEntry.CardGenerationEntryDeleter;
import net.sourceforge.fenixedu.domain.cardGeneration.CardGenerationProblem.CardGenerationProblemDeleter;
import net.sourceforge.fenixedu.domain.degree.DegreeType;
import net.sourceforge.fenixedu.domain.student.Student;
import net.sourceforge.fenixedu.presentationTier.Action.base.FenixDispatchAction;
import net.sourceforge.fenixedu.presentationTier.Action.exceptions.FenixActionException;
import net.sourceforge.fenixedu.presentationTier.Action.manager.payments.SIBSPaymentsDA.UploadBean;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import pt.ist.fenixWebFramework.renderers.utils.RenderUtils;
import pt.ist.fenixWebFramework.services.Service;
import pt.utl.ist.fenix.tools.util.FileUtils;
import pt.utl.ist.fenix.tools.util.StringNormalizer;

public class ManageCardGenerationDA extends FenixDispatchAction {
    
    static public class UploadBean implements Serializable {

	private static final long serialVersionUID = 82359323827088875L;

	private transient InputStream inputStream;

	private String filename;

	public InputStream getInputStream() {
	    return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
	    this.inputStream = inputStream;
	}

	public String getFilename() {
	    return filename;
	}

	public void setFilename(String filename) {
	    this.filename = StringNormalizer.normalize(filename);
	}
    }

    public ActionForward firstPage(final ActionMapping mapping, final ActionForm actionForm, final HttpServletRequest request,
	    final HttpServletResponse response) {
	checkForProblemasInCategoryCodes(request);
	setContext(request);
	return mapping.findForward("firstPage");
    }
    
    public ActionForward uploadCardInfo(final ActionMapping mapping, final ActionForm actionForm, final HttpServletRequest request,
	    final HttpServletResponse response) {
	request.setAttribute("uploadBean", new UploadBean());
	return mapping.findForward("uploadCardInfo");
    }

    public ActionForward showCategoryCodes(final ActionMapping mapping, final ActionForm actionForm,
	    final HttpServletRequest request, final HttpServletResponse response) {
	request.setAttribute("categories", Category.values());
	checkForProblemasInDegrees(request);
	return mapping.findForward("showCategoryCodes");
    }

    public ActionForward showDegreeCodesAndLabels(final ActionMapping mapping, final ActionForm actionForm,
	    final HttpServletRequest request, final HttpServletResponse response) {
	final DegreeType degreeType = getDegreeType(request);
	final Set<Degree> degrees = getDegrees(degreeType);
	request.setAttribute("degrees", degrees);
	return mapping.findForward("showDegreeCodesAndLabels");
    }

    public ActionForward editDegree(final ActionMapping mapping, final ActionForm actionForm, final HttpServletRequest request,
	    final HttpServletResponse response) {
	final Degree degree = getDegree(request);
	request.setAttribute("degree", degree);
	return mapping.findForward("editDegree");
    }

    public ActionForward createCardGenerationBatch(final ActionMapping mapping, final ActionForm actionForm,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
	final ExecutionYear executionYear = getExecutionYear(request);
	final CardGenerationBatchCreator cardGenerationBatchCreator = new CardGenerationBatchCreator(executionYear, false);
	executeFactoryMethod(cardGenerationBatchCreator);
	return firstPage(mapping, actionForm, request, response);
    }

    public ActionForward createEmptyCardGenerationBatch(final ActionMapping mapping, final ActionForm actionForm,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
	final ExecutionYear executionYear = getExecutionYear(request);
	final CardGenerationBatchCreator cardGenerationBatchCreator = new CardGenerationBatchCreator(executionYear, true);
	executeFactoryMethod(cardGenerationBatchCreator);
	return firstPage(mapping, actionForm, request, response);
    }

    public ActionForward deleteCardGenerationBatch(final ActionMapping mapping, final ActionForm actionForm,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
	final CardGenerationBatch cardGenerationBatch = getCardGenerationBatch(request);
	final CardGenerationBatchDeleter cardGenerationBatchDeleter = new CardGenerationBatchDeleter(cardGenerationBatch);
	executeFactoryMethod(cardGenerationBatchDeleter);
	return firstPage(mapping, actionForm, request, response);
    }

    public ActionForward manageCardGenerationBatch(final ActionMapping mapping, final ActionForm actionForm,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
	final CardGenerationBatch cardGenerationBatch = getCardGenerationBatch(request);
	request.setAttribute("cardGenerationBatch", cardGenerationBatch);
	return mapping.findForward("manageCardGenerationBatch");
    }

    public ActionForward setCardDate(final ActionMapping mapping, final ActionForm actionForm, final HttpServletRequest request,
	    final HttpServletResponse response) throws Exception {
	final CardGenerationBatch cardGenerationBatch = getCardGenerationBatch(request);
	cardGenerationBatch.setSent(new DateTime());
	request.setAttribute("cardGenerationBatch", cardGenerationBatch);
	return mapping.findForward("manageCardGenerationBatch");
    }

    public ActionForward manageCardGenerationBatchProblems(final ActionMapping mapping, final ActionForm actionForm,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
	final CardGenerationBatch cardGenerationBatch = getCardGenerationBatch(request);
	request.setAttribute("cardGenerationBatch", cardGenerationBatch);
	return mapping.findForward("manageCardGenerationBatchProblems");
    }

    public ActionForward showCardGenerationProblem(final ActionMapping mapping, final ActionForm actionForm,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
	final CardGenerationProblem cardGenerationProblem = getCardGenerationProblem(request);
	request.setAttribute("cardGenerationProblem", cardGenerationProblem);
	final CardGenerationBatch cardGenerationBatch = cardGenerationProblem.getCardGenerationBatch();
	final Person person = cardGenerationProblem.getPerson();
	if (person != null) {
	    request.setAttribute("cardGenerationProblems", person.getCardGenerationProblems(cardGenerationBatch));
	} else {
	    request.setAttribute("cardGenerationProblems", null);
	}
	return mapping.findForward("showCardGenerationProblem");
    }

    public ActionForward deleteCardGenerationEntry(final ActionMapping mapping, final ActionForm actionForm,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
	final CardGenerationEntry cardGenerationEntry = getCardGenerationEntry(request);
	final CardGenerationEntryDeleter cardGenerationEntryDeleter = new CardGenerationEntryDeleter(cardGenerationEntry);
	executeFactoryMethod(cardGenerationEntryDeleter);
	return showCardGenerationProblem(mapping, actionForm, request, response);
    }

    public ActionForward deleteCardGenerationProblem(final ActionMapping mapping, final ActionForm actionForm,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
	final CardGenerationProblem cardGenerationProblem = getCardGenerationProblem(request);
	final CardGenerationBatch cardGenerationBatch = cardGenerationProblem.getCardGenerationBatch();
	final CardGenerationProblemDeleter cardGenerationProblemDeleter = new CardGenerationProblemDeleter(cardGenerationProblem);
	executeFactoryMethod(cardGenerationProblemDeleter);
	request.setAttribute("cardGenerationBatch", cardGenerationBatch);
	return mapping.findForward("manageCardGenerationBatchProblems");
    }

    public ActionForward downloadCardGenerationBatch(final ActionMapping mapping, final ActionForm actionForm,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
	final CardGenerationBatch cardGenerationBatch = getCardGenerationBatch(request);
	try {
	    final ServletOutputStream writer = response.getOutputStream();
	    response.setHeader("Content-disposition", "attachment; filename=identificationCardsIST"
		    + cardGenerationBatch.getCreated().toString("yyyyMMddHHmmss") + ".txt");
	    response.setContentType("text/plain");
	    writeFile(cardGenerationBatch, writer);
	    writer.flush();
	    response.flushBuffer();
	} catch (IOException e1) {
	    throw new FenixActionException();
	}
	return null;
    }

    public ActionForward editCardGenerationBatch(final ActionMapping mapping, final ActionForm actionForm,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
	final CardGenerationBatch cardGenerationBatch = getCardGenerationBatch(request);
	request.setAttribute("cardGenerationBatch", cardGenerationBatch);
	return mapping.findForward("editCardGenerationBatch");
    }

    public ActionForward clearConstructionFlag(final ActionMapping mapping, final ActionForm actionForm,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
	final CardGenerationBatch cardGenerationBatch = getCardGenerationBatch(request);
	CardGenerationServices.clearConstructionFlag(cardGenerationBatch);
	request.setAttribute("cardGenerationBatch", cardGenerationBatch);
	return mapping.findForward("manageCardGenerationBatch");
    }

    private void writeFile(final CardGenerationBatch cardGenerationBatch, final ServletOutputStream writer) throws IOException {
	for (final CardGenerationEntry cardGenerationEntry : cardGenerationBatch.getCardGenerationEntriesSet()) {
	    writer.print(cardGenerationEntry.getLine());
	    // writer.print("\r\n");
	}
	// writer.print(fillLeftString("", ' ', 262));
	// writer.print("\r\n");
    }

    private String fillLeftString(final String uppered, final char c, final int fillTo) {
	final StringBuilder stringBuilder = new StringBuilder();
	for (int i = uppered.length(); i < fillTo; i++) {
	    stringBuilder.append(c);
	}
	stringBuilder.append(uppered);
	return stringBuilder.toString();
    }

    protected Degree getDegree(final HttpServletRequest request) {
	final String degreeIdParam = request.getParameter("degreeID");
	final Integer degreeID = degreeIdParam == null || degreeIdParam.length() == 0 ? null : Integer.valueOf(degreeIdParam);
	return degreeIdParam == null ? null : rootDomainObject.readDegreeByOID(degreeID);
    }

    protected Set<Degree> getDegrees(final DegreeType degreeType) {
	final Set<Degree> degrees = new TreeSet<Degree>();
	for (final Degree degree : rootDomainObject.getDegreesSet()) {
	    if (degreeType == null || degree.getDegreeType() == degreeType) {
		degrees.add(degree);
	    }
	}
	return degrees;
    }

    protected DegreeType getDegreeType(final HttpServletRequest request) {
	final String degreeTypeParam = request.getParameter("degreeType");
	return degreeTypeParam == null || degreeTypeParam.length() == 0 ? null : DegreeType.valueOf(degreeTypeParam);
    }

    protected void checkForProblemasInDegrees(final HttpServletRequest request) {
	final Map<DegreeType, Boolean> problemsMap = new HashMap<DegreeType, Boolean>();
	for (final DegreeType degreeType : DegreeType.values()) {
	    problemsMap.put(degreeType, checkHasProblem(degreeType));
	}
	request.setAttribute("problemsMap", problemsMap);
    }

    protected void checkForProblemasInCategoryCodes(final HttpServletRequest request) {
	request.setAttribute("categoryCondesProblem", anyDegreeHasAnyProblem());
    }

    protected Boolean anyDegreeHasAnyProblem() {
	for (final Degree degree : Degree.readNotEmptyDegrees()) {
	    if (checkHasProblem(degree)) {
		return Boolean.TRUE;
	    }
	}
	return Boolean.FALSE;
    }

    protected Boolean checkHasProblem(final DegreeType degreeType) {
	for (final Degree degree : Degree.readNotEmptyDegrees()) {
	    if (degree.getDegreeType() == degreeType && checkHasProblem(degree)) {
		return Boolean.TRUE;
	    }
	}
	return Boolean.FALSE;
    }

    protected boolean checkHasProblem(final Degree degree) {
	final DegreeType degreeType = degree.getDegreeType();
	if (degreeType == DegreeType.DEGREE || degreeType == DegreeType.BOLONHA_DEGREE
		|| degreeType == DegreeType.BOLONHA_INTEGRATED_MASTER_DEGREE) {
	    if (degree.getMinistryCode() == null) {
		return true;
	    }
	}
	if (degree.getIdCardName().length() > 42) {
	    return true;
	}
	return false;
    }

    protected void setContext(final HttpServletRequest request) {
	CardGenerationContext cardGenerationContext = (CardGenerationContext) getRenderedObject("cardGenerationContext");
	if (cardGenerationContext == null) {
	    final ExecutionYear executionYear = getExecutionYear(request);
	    cardGenerationContext = executionYear == null ? new CardGenerationContext()
		    : new CardGenerationContext(executionYear);
	}
	request.setAttribute("cardGenerationContext", cardGenerationContext);
    }

    private ExecutionYear getExecutionYear(final HttpServletRequest request) {
	final String executionYearParam = request.getParameter("executionYearID");
	final Integer executionYearID = executionYearParam == null || executionYearParam.length() == 0 ? null : Integer
		.valueOf(executionYearParam);
	return executionYearID == null ? null : rootDomainObject.readExecutionYearByOID(executionYearID);
    }

    protected CardGenerationBatch getCardGenerationBatch(final HttpServletRequest request) {
	final String cardGenerationBatchParam = request.getParameter("cardGenerationBatchID");
	final Integer cardGenerationBatchID = cardGenerationBatchParam == null || cardGenerationBatchParam.length() == 0 ? null
		: Integer.valueOf(cardGenerationBatchParam);
	return cardGenerationBatchID == null ? null : rootDomainObject.readCardGenerationBatchByOID(cardGenerationBatchID);
    }

    private CardGenerationEntry getCardGenerationEntry(HttpServletRequest request) {
	final String cardGenerationEntryParam = request.getParameter("cardGenerationEntryID");
	final Integer cardGenerationEntryID = cardGenerationEntryParam == null || cardGenerationEntryParam.length() == 0 ? null
		: Integer.valueOf(cardGenerationEntryParam);
	return cardGenerationEntryID == null ? null : rootDomainObject.readCardGenerationEntryByOID(cardGenerationEntryID);
    }

    protected CardGenerationProblem getCardGenerationProblem(final HttpServletRequest request) {
	final String cardGenerationProblemParam = request.getParameter("cardGenerationProblemID");
	final Integer cardGenerationProblemID = cardGenerationProblemParam == null || cardGenerationProblemParam.length() == 0 ? null
		: Integer.valueOf(cardGenerationProblemParam);
	return cardGenerationProblemID == null ? null : rootDomainObject.readCardGenerationProblemByOID(cardGenerationProblemID);
    }

    public ActionForward createNewEntry(final ActionMapping mapping, final ActionForm actionForm,
	    final HttpServletRequest request, final HttpServletResponse response) {
	final Student student = getDomainObject(request, "studentID");
	final CardGenerationBatch cardGenerationBatch = getDomainObject(request, "cardGenerationBatchID");
	cardGenerationBatch.createCardGenerationEntry(student);
	return firstPage(mapping, actionForm, request, response);
    }

    public ActionForward deletePersonCard(final ActionMapping mapping, final ActionForm actionForm,
	    final HttpServletRequest request, final HttpServletResponse response) {
	final CardGenerationEntry cardGenerationEntry = getDomainObject(request, "cardGenerationEntryId");
	if (cardGenerationEntry != null) {
	    DeleteCardGenerationEntryService.run(cardGenerationEntry);
	}
	return firstPage(mapping, actionForm, request, response);
    }
    
    public ActionForward readCardInfo(final ActionMapping mapping, final ActionForm actionForm,
	    final HttpServletRequest request, final HttpServletResponse response) {
	UploadBean bean = (UploadBean) getRenderedObject("uploadBean");
	RenderUtils.invalidateViewState("uploadBean");
	fixBadData();
	int[] registers = new int[2];
	try {
	    registers = process(FileUtils.copyToTemporaryFile(bean.getInputStream()));
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	request.setAttribute("readingComplete", true);
	request.setAttribute("createdRegisters", registers[0]);
	request.setAttribute("notCreatedRegisters", registers[1]);
	return mapping.findForward("uploadCardInfo");
    }

    private void fixBadData() {
	switchPerson(395374, 82254);
	switchPerson(1728, 8336);
    }

    @Service
    private void switchPerson(final int personId, final int cardGenerationEntryId) {
	final Person person = (Person) rootDomainObject.readPartyByOID(new Integer(personId));
	final CardGenerationEntry cardGenerationEntry = rootDomainObject.readCardGenerationEntryByOID(new Integer(cardGenerationEntryId));
	cardGenerationEntry.setPerson(person);
    }

    @Service
    private int[] process(final File file) throws FileNotFoundException, IOException {
	final String contents = FileUtils.readFile(new FileInputStream(file));
	final String[] lines = contents.split("\n");
	int[] registers = new int[2];
	for (final String line : lines) {
	    CardEmissionEntry cardEmissionEntry = new CardEmissionEntry(line);
	    if(cardEmissionEntry.createdRegister) {
		++registers[0];
	    } else {
		++registers[1];
	    }
	}
	return registers;
    }

    private static class CardEmissionEntry {

	final Set<CardGenerationEntry> cardGenerationEntries = new HashSet<CardGenerationEntry>();

	final String line;
	
	boolean createdRegister = false;

	private CardEmissionEntry(final String line) {
	    this.line = line;
	    final String[] parts = line.split("\t");
	    if (parts.length != 3) {
		System.out.println("Invalid format for line: " + line);
	    } else {
		final String identifier = parts[0].trim();
		final String date = parts[1].trim();
		final String type = parts[2].trim().toLowerCase();

		findCardGenerationEntries(identifier);

		Person person = null;
		for (final CardGenerationEntry cardGenerationEntry : cardGenerationEntries) {
		    if (person == null) {
			person = cardGenerationEntry.getPerson();
		    } else if (person != cardGenerationEntry.getPerson()) {
			System.out.println("Line      : " + line);
			System.out.println("identifier: " + identifier);
			for (final CardGenerationEntry cge : cardGenerationEntries) {
			    final Person p = cge.getPerson();
			    System.out.println("CGE.id: " + cge.getIdInternal() + " " + "P.id: " + (p == null ? "" : p.getIdInternal() + " (" + p.getUsername() + ")"));
			    System.out.println(cge.getLine());
			}
			throw new Error("Card emitted matches multiple people! " + line);
		    }
		}
		if (person != null) {
		    int year;
		    int month;
		    int day;

		    final int hi1 = date.indexOf('-');
		    final int si1 = date.indexOf('/');
		    if (hi1 == -1 && si1 == -1) {
			year = -1;
			month = -1;
			day = -1;
			System.out.println("Unkown date format: " + date);
		    } else if (si1 == -1) {
			final int hi2 = date.indexOf('-', hi1 + 1);
			if (hi1 == 4) {
			    year = Integer.parseInt(date.substring(0, hi1));
			    month = Integer.parseInt(date.substring(hi1 + 1, hi2));
			    day = Integer.parseInt(date.substring(hi2 + 1));
			} else {
			    final int dp1 = Integer.parseInt(date.substring(0, hi1));
			    final int dp2 = Integer.parseInt(date.substring(hi1 + 1, hi2));
			    year = Integer.parseInt(date.substring(hi2 + 1));
			    if (dp2 > 12) {
				day = dp2;
				month = dp1;
			    } else {
				day = dp1;
				month = dp2;				    
			    }
			}
		    } else if (hi1 == -1) {
			final int si2 = date.indexOf('/', si1 + 1);
			if (si1 == 4) {
			    year = Integer.parseInt(date.substring(0, si1));
			    month = Integer.parseInt(date.substring(si1 + 1, si2));
			    day = Integer.parseInt(date.substring(si2 + 1));
			} else {
			    final int dp1 = Integer.parseInt(date.substring(0, si1));
			    final int dp2 = Integer.parseInt(date.substring(si1 + 1, si2));
			    year = Integer.parseInt(date.substring(si2 + 1));
			    if (dp1 > 12) {
				day = dp1;
				month = dp2;				    
			    } else {
				day = dp2;
				month = dp1;
			    }				
			}
		    } else {
			System.out.println("hi1: " + hi1);
			System.out.println("si1: " + si1);
			throw new Error("Unkown date format: " + date);
		    }

		    LocalDate emission = year == -1 ? null : new LocalDate(year, month, day);
		    final boolean withAccountInformation = type.indexOf('n') > type.indexOf('b');
		    // Checking for the possibility of the same file submited twice
		    boolean repetition = false;
		    for(CardGenerationRegister cgr : person.getCardGenerationRegister()) {
			if((cgr.getEmission() != null) && emission.compareTo(cgr.getEmission()) == 0) {
			    repetition = true;
			}
		    }
		    if(!repetition && (emission != null)) {
			createdRegister = true;
			new CardGenerationRegister(person, identifier, emission, withAccountInformation);
		    }
		}
	    }
	}

	private void findCardGenerationEntries(final String identifier) {
	    final String relevantPart = identifier.length() == 15 ? identifier.substring(0, identifier.length() - 2) : identifier;
	    for (final CardGenerationEntry cardGenerationEntry : rootDomainObject.getCardGenerationEntriesSet()) {
		if (cardGenerationEntry.getLine().indexOf(relevantPart) == 8) {
		    cardGenerationEntries.add(cardGenerationEntry);
		}
	    }
	}
    }
}
