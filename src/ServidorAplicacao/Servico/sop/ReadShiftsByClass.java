/*
 * ReadShiftsByExecutionDegreeAndCurricularYear.java
 *
 * Created on 2003/08/12
 */

package ServidorAplicacao.Servico.sop;

/**
 * @author Luis Cruz & Sara Ribeiro
 * 
 */
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;

import pt.utl.ist.berserk.logic.serviceManager.IService;
import DataBeans.InfoClass;
import DataBeans.InfoExecutionCourse;
import DataBeans.InfoLesson;
import DataBeans.InfoShift;
import DataBeans.util.Cloner;
import Dominio.IAula;
import Dominio.ITurma;
import Dominio.ITurno;
import Dominio.Turma;
import ServidorPersistente.ExcepcaoPersistencia;
import ServidorPersistente.ISuportePersistente;
import ServidorPersistente.OJB.SuportePersistenteOJB;

public class ReadShiftsByClass implements IService {

    public Object run(InfoClass infoClass) throws ExcepcaoPersistencia {

        ISuportePersistente sp = SuportePersistenteOJB.getInstance();

        ITurma shcoolClass = (ITurma) sp.getITurmaPersistente().readByOID(Turma.class,
                infoClass.getIdInternal());

        List shifts = sp.getITurmaTurnoPersistente().readByClass(shcoolClass);

        return CollectionUtils.collect(shifts, new Transformer() {
            public Object transform(Object arg0) {
                ITurno shift = (ITurno) arg0;
                InfoShift infoShift = InfoShift.newInfoFromDomain(shift);
                infoShift.setInfoLessons((List) CollectionUtils.collect(shift.getAssociatedLessons(),
                        new Transformer() {
                            public Object transform(Object arg0) {
                                InfoLesson infoLesson = Cloner.copyILesson2InfoLesson((IAula) arg0);
                                ITurno shift = ((IAula) arg0).getShift();
                                InfoShift infoShift = InfoShift.newInfoFromDomain(shift);
                                infoLesson.setInfoShift(infoShift);

                                return infoLesson;
                            }
                        }));
                InfoExecutionCourse infoExecutionCourse = InfoExecutionCourse.newInfoFromDomain(shift
                        .getDisciplinaExecucao());
                infoShift.setInfoDisciplinaExecucao(infoExecutionCourse);
                return infoShift;
            }
        });
    }

}