/*
 * Created on May 15, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package middleware.almeida;

import java.util.StringTokenizer;

/**
 *
 * @author  Luis Cruz & Sara Ribeiro
 */
public class LoadDisciplinas extends LoadDataFile {

	private static LoadDisciplinas loader = null;

	private LoadDisciplinas() {
	}

	public static void main(String[] args) {
		loader = new LoadDisciplinas();
		loader.load();
	}

	protected void processLine(String line) {
		StringTokenizer stringTokenizer =
			new StringTokenizer(line, getFieldSeperator());

		int numberTokens = stringTokenizer.countTokens(); 

		String codigoCurso = stringTokenizer.nextToken();
		String codigoRamo = stringTokenizer.nextToken();
		String ano = stringTokenizer.nextToken();
		String semestre = stringTokenizer.nextToken();
		String codigoDisciplina = stringTokenizer.nextToken();
		stringTokenizer.nextToken();   // ???
		String teorica = stringTokenizer.nextToken().replace(',','.');
		String pratica = stringTokenizer.nextToken().replace(',','.');
		String laboratorio = stringTokenizer.nextToken().replace(',','.');
		String teoricopratica = stringTokenizer.nextToken().replace(',','.');
		String nomeDisciplina = stringTokenizer.nextToken();

		Almeida_disc almeida_disc = new Almeida_disc();
		almeida_disc.setCodint(loader.numberElementsWritten + 1);
		almeida_disc.setCodcur((new Integer(codigoCurso)).longValue());
		almeida_disc.setCodram((new Integer(codigoRamo)).longValue());
		almeida_disc.setAnodis((new Integer(ano)).longValue());
		almeida_disc.setSemdis((new Integer(semestre)).longValue());
		almeida_disc.setCoddis(codigoDisciplina);
		almeida_disc.setTeo((new Double(teorica)).doubleValue());
		almeida_disc.setTeopra((new Double(teoricopratica)).doubleValue());
		almeida_disc.setPra((new Double(pratica)).doubleValue());
		almeida_disc.setLab((new Double(laboratorio)).doubleValue());
		almeida_disc.setNomedis(nomeDisciplina);

		writeElement(almeida_disc);
	}

	protected String getFilename() {
		return "etc/migration/DISCIPLINAS.TXT";
	}

	protected String getFieldSeperator() {
		return "\t";
	}

}