/*
 * Created on 19/Ago/2003
 */

package net.sourceforge.fenixedu.applicationTier.Servico.teacher;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.sourceforge.fenixedu.applicationTier.Servico.exceptions.FenixServiceException;
import net.sourceforge.fenixedu.applicationTier.Servico.exceptions.InvalidArgumentsServiceException;
import net.sourceforge.fenixedu.dataTransferObject.InfoStudent;
import net.sourceforge.fenixedu.domain.Advisory;
import net.sourceforge.fenixedu.domain.DistributedTest;
import net.sourceforge.fenixedu.domain.DistributedTestAdvisory;
import net.sourceforge.fenixedu.domain.ExecutionCourse;
import net.sourceforge.fenixedu.domain.IAdvisory;
import net.sourceforge.fenixedu.domain.IDistributedTest;
import net.sourceforge.fenixedu.domain.IDistributedTestAdvisory;
import net.sourceforge.fenixedu.domain.IExecutionCourse;
import net.sourceforge.fenixedu.domain.IOnlineTest;
import net.sourceforge.fenixedu.domain.IQuestion;
import net.sourceforge.fenixedu.domain.IStudent;
import net.sourceforge.fenixedu.domain.IStudentTestQuestion;
import net.sourceforge.fenixedu.domain.ITest;
import net.sourceforge.fenixedu.domain.ITestQuestion;
import net.sourceforge.fenixedu.domain.ITestScope;
import net.sourceforge.fenixedu.domain.OnlineTest;
import net.sourceforge.fenixedu.domain.Student;
import net.sourceforge.fenixedu.domain.StudentTestQuestion;
import net.sourceforge.fenixedu.domain.Test;
import net.sourceforge.fenixedu.domain.TestScope;
import net.sourceforge.fenixedu.persistenceTier.ExcepcaoPersistencia;
import net.sourceforge.fenixedu.persistenceTier.IPersistentDistributedTest;
import net.sourceforge.fenixedu.persistenceTier.IPersistentExecutionCourse;
import net.sourceforge.fenixedu.persistenceTier.IPersistentStudentTestQuestion;
import net.sourceforge.fenixedu.persistenceTier.IPersistentTest;
import net.sourceforge.fenixedu.persistenceTier.IPersistentTestQuestion;
import net.sourceforge.fenixedu.persistenceTier.ISuportePersistente;
import net.sourceforge.fenixedu.persistenceTier.PersistenceSupportFactory;
import net.sourceforge.fenixedu.util.tests.CorrectionAvailability;
import net.sourceforge.fenixedu.util.tests.TestType;
import pt.utl.ist.berserk.logic.serviceManager.IService;

/**
 * @author Susana Fernandes
 */
public class InsertDistributedTest implements IService {

    private String contextPath = new String();

    public InsertDistributedTest() {
    }

    public Integer run(Integer executionCourseId, Integer testId, String testInformation,
            Calendar beginDate, Calendar beginHour, Calendar endDate, Calendar endHour,
            TestType testType, CorrectionAvailability correctionAvaiability, Boolean imsFeedback,
            List infoStudentList, String contextPath) throws FenixServiceException {
        this.contextPath = contextPath.replace('\\', '/');
        try {

            ISuportePersistente persistentSuport = PersistenceSupportFactory.getDefaultPersistenceSupport();
            IPersistentExecutionCourse persistentExecutionCourse = persistentSuport
                    .getIPersistentExecutionCourse();
            IExecutionCourse executionCourse = (IExecutionCourse) persistentExecutionCourse.readByOID(
                    ExecutionCourse.class, executionCourseId);
            if (executionCourse == null)
                throw new InvalidArgumentsServiceException();

            IPersistentDistributedTest persistentDistributedTest = persistentSuport
                    .getIPersistentDistributedTest();
            IDistributedTest distributedTest = new DistributedTest();

            IPersistentTest persistentTest = persistentSuport.getIPersistentTest();
            ITest test = (ITest) persistentTest.readByOID(Test.class, testId);
            if (test == null)
                throw new InvalidArgumentsServiceException();

            distributedTest.setTitle(test.getTitle());
            distributedTest.setTestInformation(testInformation);
            distributedTest.setBeginDate(beginDate);
            distributedTest.setBeginHour(beginHour);
            distributedTest.setEndDate(endDate);
            distributedTest.setEndHour(endHour);
            distributedTest.setTestType(testType);
            distributedTest.setCorrectionAvailability(correctionAvaiability);
            distributedTest.setImsFeedback(imsFeedback);
            distributedTest.setNumberOfQuestions(test.getNumberOfQuestions());

            ITestScope testScope = persistentSuport.getIPersistentTestScope().readByDomainObject(
                    executionCourse);

            if (testScope == null) {
                testScope = new TestScope(persistentExecutionCourse.materialize(executionCourse));
                persistentSuport.getIPersistentTestScope().simpleLockWrite(testScope);
            }
            distributedTest.setTestScope(testScope);
            persistentDistributedTest.simpleLockWrite(distributedTest);

            IPersistentStudentTestQuestion persistentStudentTestQuestion = persistentSuport
                    .getIPersistentStudentTestQuestion();
            IPersistentTestQuestion persistentTestQuestion = persistentSuport
                    .getIPersistentTestQuestion();

            List testQuestionList = persistentTestQuestion.readByTest(test);
            Iterator testQuestionIt = testQuestionList.iterator();

            while (testQuestionIt.hasNext()) {
                ITestQuestion testQuestion = (ITestQuestion) testQuestionIt.next();
                List questionList = new ArrayList();
                questionList.addAll(testQuestion.getQuestion().getMetadata().getVisibleQuestions());

                for (int i = 0; i < infoStudentList.size(); i++) {
                    InfoStudent infoStudent = (InfoStudent) infoStudentList.get(i);
                    IStudent student = (IStudent) persistentSuport.getIPersistentStudent().readByOID(
                            Student.class, infoStudent.getIdInternal());
                    IStudentTestQuestion studentTestQuestion = new StudentTestQuestion();
                    persistentStudentTestQuestion.lockWrite(studentTestQuestion);
                    studentTestQuestion.setStudent(student);
                    studentTestQuestion.setDistributedTest(distributedTest);
                    studentTestQuestion.setTestQuestionOrder(testQuestion.getTestQuestionOrder());
                    studentTestQuestion.setTestQuestionValue(testQuestion.getTestQuestionValue());
                    studentTestQuestion.setCorrectionFormula(testQuestion.getCorrectionFormula());
                    studentTestQuestion.setTestQuestionMark(new Double(0));
                    studentTestQuestion.setOldResponse(new Integer(0));
                    studentTestQuestion.setResponse(null);

                    if (questionList.size() == 0)
                        questionList.addAll(testQuestion.getQuestion().getMetadata()
                                .getVisibleQuestions());
                    IQuestion question = getStudentQuestion(questionList);
                    if (question == null) {
                        throw new InvalidArgumentsServiceException();
                    }
                    studentTestQuestion.setQuestion(question);
                    questionList.remove(question);
                }
            }
            //Create Evaluation - OnlineTest and Marks
            if (distributedTest.getTestType().equals(new TestType(TestType.EVALUATION))) {
                IOnlineTest onlineTest = new OnlineTest();
                persistentSuport.getIPersistentEvaluation().simpleLockWrite(onlineTest);
                onlineTest.setDistributedTest(distributedTest);
                List executionCourseList = new ArrayList();
                executionCourseList.add(executionCourse);
                onlineTest.setAssociatedExecutionCourses(executionCourseList);
            }

            //Create Advisory

            IAdvisory advisory = new Advisory();
            advisory.setCreated(Calendar.getInstance().getTime());
            advisory.setExpires(endDate.getTime());
            advisory.setSender("Docente da disciplina " + executionCourse.getNome());
            advisory.setSubject(distributedTest.getTitle());
            String msgBeginning;

            if (distributedTest.getTestType().equals(new TestType(TestType.INQUIRY)))
                msgBeginning = new String("Tem um <a href='" + this.contextPath
                        + "/student/studentTests.do?method=prepareToDoTest&testCode="
                        + distributedTest.getIdInternal() + "'>questionário</a> para responder entre ");
            else
                msgBeginning = new String("Tem uma <a href='" + this.contextPath
                        + "/student/studentTests.do?method=prepareToDoTest&testCode="
                        + distributedTest.getIdInternal() + "'>Ficha de Trabalho</a> a realizar entre ");
            advisory.setMessage(msgBeginning + " as " + getHourFormatted(beginHour) + " de "
                    + getDateFormatted(beginDate) + " e as " + getHourFormatted(endHour) + " de "
                    + getDateFormatted(endDate));
            advisory.setOnlyShowOnce(new Boolean(false));
            persistentSuport.getIPersistentAdvisory().simpleLockWrite(advisory);

            //Create DistributedTestAdvisory
            IDistributedTestAdvisory distributedTestAdvisory = new DistributedTestAdvisory();
            persistentSuport.getIPersistentDistributedTestAdvisory().simpleLockWrite(
                    distributedTestAdvisory);
            distributedTestAdvisory.setAdvisory(advisory);
            distributedTestAdvisory.setDistributedTest(distributedTest);

            return advisory.getIdInternal();
        } catch (ExcepcaoPersistencia e) {
            throw new FenixServiceException(e);
        } catch (Exception e) {
            throw new FenixServiceException(e);
        }
    }

    private IQuestion getStudentQuestion(List questions) {
        IQuestion question = null;
        if (questions.size() != 0) {
            Random r = new Random();
            int questionIndex = r.nextInt(questions.size());
            question = (IQuestion) questions.get(questionIndex);
        }
        return question;
    }

    private String getDateFormatted(Calendar date) {
        String result = new String();
        result += date.get(Calendar.DAY_OF_MONTH);
        result += "/";
        result += date.get(Calendar.MONTH) + 1;
        result += "/";
        result += date.get(Calendar.YEAR);
        return result;
    }

    private String getHourFormatted(Calendar hour) {
        String result = new String();
        result += hour.get(Calendar.HOUR_OF_DAY);
        result += ":";
        if (hour.get(Calendar.MINUTE) < 10)
            result += "0";
        result += hour.get(Calendar.MINUTE);
        return result;
    }
}