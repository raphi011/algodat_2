package ads2.ss15.cflp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Diese Klasse enth&auml;lt nur die {@link #main main()}-Methode zum Starten
 * des Programms, sowie {@link #printDebug(String)} und
 * {@link #printDebug(Object)} zum Ausgeben von Debug Meldungen.
 * 
 * <p>
 * <b>WICHTIG:</b> Nehmen Sie keine &Auml;nderungen in dieser Klasse vor. Bei
 * der Abgabe werden diese &Auml;nderungen verworfen und es k&ouml;nnte dadurch
 * passieren, dass Ihr Programm somit nicht mehr korrekt funktioniert.
 * </p>
 */
public class Main {

	/**
	 * Der Name der Datei, aus der die Testinstanz auszulesen ist. Ist <code>
	 * null</code>, wenn von {@link System#in} eingelesen wird.
	 */
	private static String fileName = null;

	/** Der abgeschnittene Pfad */
	private static String choppedFileName;

	/**
	 * Mit diesem flag kann verhindert werden, dass der Thread nach Ablauf der
	 * Zeit beendet wird.
	 */
	private static boolean dontStop = false;

	/** Test flag f&uuml;r Laufzeit Ausgabe */
	private static boolean test = false;

	/** Debug flag f&uuml;r zus&auml;tzliche Debug Ausgaben */
	private static boolean debug = false;
	
	/**
	 * Liest die Daten einer Testinstanz ein und &uuml;bergibt sie an die
	 * entsprechenden Methoden der Implementierung.
	 * 
	 * <p>
	 * Wenn auf der Kommandozeile die Option <code>-d</code> angegeben wird,
	 * werden s&auml;mtliche Strings, die an {@link Main#printDebug(String)}
	 * &uuml;bergeben werden, ausgegeben.
	 * </p>
	 * 
	 * <p>
	 * Der erste String in <code>args</code>, der <em>nicht</em> mit <code>-d
	 * </code>, <code>-t</code>, oder <code>-s</code> beginnt, wird als der Pfad
	 * zur Datei interpretiert, aus der die Testinstanz auszulesen ist. Alle
	 * nachfolgenden Parameter werden ignoriert. Wird kein Dateiname angegeben,
	 * wird die Testinstanz &uuml;ber {@link System#in} eingelesen.
	 * </p>
	 * 
	 * @param args
	 *            Die von der Kommandozeile &uuml;bergebenen Argumente. Die
	 *            Option <code>-d</code> aktiviert debug-Ausgaben &uuml;ber
	 *            {@link #printDebug(String)}, <code>-t</code> gibt
	 *            zus&auml;tzlich Dateiname und Laufzeit aus und <code>-s</code>
	 *            verhindert, dass Ihr Algorithmus nach 30 Sekunden beendet
	 *            wird. Der erste andere String wird als Dateiname
	 *            interpretiert.
	 */
	public static void main(String[] args) {
		processArgs(args);
        if (fileName == null){
            bailOut("Keine Inputdatei angegeben!");
        }
		SecurityManager oldsm = null;
		try {
			oldsm = System.getSecurityManager();
			SecurityManager sm = new ADS1SecurityManager();
			System.setSecurityManager(sm);
		} catch (SecurityException e) {
			bailOut("Error: SecurityManager konnte nicht gesetzt werden: " + e);
		}
		
		CFLPInstanceReader reader = new CFLPInstanceReader(fileName);

		try {
			CFLPInstance instance = reader.readInstance();
			run(instance);
			// Security Manager ruecksetzen
			System.setSecurityManager(oldsm);
		} catch (SecurityException se) {
			bailOut("Unerlaubter Funktionsaufruf: \"" + se.toString() + "\"");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			bailOut("Konnte Instanz nicht einlesen (I/O Fehler)");
		} catch (Exception e) {
			e.printStackTrace();
			bailOut("Ausnahme \"" + e.toString() + "\"");
		}

	}

	/**
	 * Startet Ihre CFLP Implementierung mit einem Testfall und
	 * &uuml;berpr&uuml;ft danach Ihre L&ouml;sung.
	 * 
	 * @param instance Die aktuelle Probleminstanz
	 * 
	 * @throws Exception
	 *             Signalisiert eine Ausnahme
	 */
	protected static void run(CFLPInstance instance) throws Exception {
		CFLPInstance originalInstance = new CFLPInstance(instance);
		
		long start = System.currentTimeMillis();
		long end = System.currentTimeMillis();
		long offs = end - start;
		long timeout = 30000; // 30 Sekunden

		chopFileName();

		AbstractCFLP bnbRunner = new CFLP(instance);
		Thread thread = new Thread(bnbRunner, "CFLP Thread");
		thread.start();

		if (dontStop)
			thread.join(0);
		else {
			// beende thread nach timeout millisecs
			thread.join(timeout);
			if (thread.isAlive())
				thread.stop();
		}

		// speichere Endzeit
		end = System.currentTimeMillis();
		
		// speichere Lösung
		AbstractCFLP.BnBSolution sol = bnbRunner.getBestSolution();
		// checke Lösung
		if (sol == null) {
			bailOut("Keine gueltige Loesung!");
		}

		int upper_bound = sol.getUpperBound();
		
		int[] solution = sol.getBestSolution();
		if(solution.length != originalInstance.getNumCustomers())
			bailOut("Ihre Loesung hat zu wenige/viele Kunden!");
		
		int[] usedBandwidths = new int[originalInstance.getNumFacilities()];
		Arrays.fill(usedBandwidths, 0);
		int[] connectedCustomers = new int[originalInstance.getNumFacilities()];
		Arrays.fill(connectedCustomers, 0);
		
		int fIdx;
		for(int i=0; i < originalInstance.getNumCustomers(); ++i) {
			fIdx = solution[i];
			if(fIdx < 0 || fIdx >= originalInstance.getNumFacilities())
				bailOut("Ungueltiger Facility Index!");
			
			usedBandwidths[fIdx] += originalInstance.bandwidthOf(i);
			if(usedBandwidths[fIdx] > originalInstance.maxBandwidth)
				bailOut("Eine Facility verbraucht zu viel Bandbreite!");
			
			connectedCustomers[fIdx] += 1;
			if(connectedCustomers[fIdx] > originalInstance.maxCustomersFor(fIdx))
				bailOut("Eine Facility hat zu viele Kunden!");
		}
		
		int objectiveValue = originalInstance.calcObjectiveValue(solution);

		if (Math.abs(objectiveValue - upper_bound) > 0) {
			bailOut("Die obere Schranke muss immer gleich der aktuell besten Loesung sein!");
		}

		// Ergebnis ausgeben
		StringBuffer msg = new StringBuffer(test ? choppedFileName + ": " : "");

		long sum = end - start - offs;

		printDebug("Loesung: " + Arrays.toString(solution));
		if (upper_bound > originalInstance.getThreshold())
			bailOut("zu schlechte Loesung: Ihr Ergebnis " + upper_bound
					+ " liegt ueber dem Schwellwert (" + originalInstance.getThreshold() + ")");

		msg.append("Schwellwert = " + originalInstance.getThreshold() + "." + " Ihr Ergebnis ist OK mit " +
				"\n" + upper_bound);

		if (test)
			msg.append(", Zeit: "
					+ (sum > 1000 ? sum / 1000 + "s" : sum + "ms"));

		System.out.println();
		System.out.println(msg.toString());
	}

	/**
	 * &Ouml;ffnet die Eingabedatei und gibt einen {@link Scanner} zur&uuml;ck,
	 * der von ihr liest. Falls kein Dateiname angegeben wurde, wird von
	 * {@link System#in} gelesen.
	 * 
	 * @return Einen {@link Scanner} der von der Eingabedatei liest.
	 */
	private static Scanner openInputFile() {
		if (fileName != null)
			try {
				return new Scanner(new FileInputStream(fileName));
			} catch (FileNotFoundException e) {
				bailOut("Datei \"" + fileName + "\" konnte nicht gefunden werden");
			}

		return new Scanner(System.in);

	}

	/**
	 * Interpretiert die Parameter, die dem Programm &uuml;bergeben wurden und
	 * gibt einen {@link Scanner} zur&uuml;ck, der von der Testinstanz liest.
	 * 
	 * @param args
	 *            Die Eingabeparameter
	 * @return Einen {@link Scanner} der von der Eingabedatei liest.
	 */
	protected static void processArgs(String[] args) {
		for (String a : args) {
			if (a.equals("-s")) {
				dontStop = true;
			} else if (a.equals("-t")) {
				test = true;
			} else if (a.equals("-d")) {
				debug = test = true;
			} else {
				fileName = a;

				break;
			}
		}
	}

	/**
	 * Gibt die Meldung <code>msg</code> aus und beendet das Programm.
	 * 
	 * @param msg
	 *            Die Meldung die ausgegeben werden soll.
	 */
	private static void bailOut(String msg) {
		System.out.println();
		System.err.println((test ? choppedFileName + ": " : "") + "ERR " + msg);
		System.exit(1);
	}

	/**
	 * Abgeschnittenen Pfad erzeugen.
	 */
	private static void chopFileName() {
		if (fileName == null) {
			choppedFileName = "System.in";
			return;
		}

		int i = fileName.lastIndexOf(File.separatorChar);

		if (i > 0)
			i = fileName.lastIndexOf(File.separatorChar, i - 1);
		if (i == -1)
			i = 0;

		choppedFileName = ((i > 0) ? "..." : "") + fileName.substring(i);
	}

	/**
	 * Gibt eine debugging Meldung aus. Wenn das Programm mit <code>-d</code>
	 * gestartet wurde, wird <code>msg</code> zusammen mit dem Dateinamen der
	 * Inputinstanz ausgegeben, ansonsten macht diese Methode nichts.
	 * 
	 * @param msg
	 *            Text der ausgegeben werden soll.
	 */
	public static synchronized void printDebug(String msg) {
		if (!debug)
			return;

		System.out.println(choppedFileName + ": DBG " + msg);
	}

	/**
	 * Gibt eine debugging Meldung aus. Wenn das Programm mit <code>-d</code>
	 * gestartet wurde, wird <code>msg</code> zusammen mit dem Dateinamen der
	 * Inputinstanz ausgegeben, ansonsten macht diese Methode nichts.
	 * 
	 * @param msg
	 *            Object das ausgegeben werden soll.
	 */
	public static void printDebug(Object msg) {
		printDebug(msg.toString());
	}

	/**
	 * Privater Konstruktor.
	 * 
	 */
	private Main() {
	}

}
