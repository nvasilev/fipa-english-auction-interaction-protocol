package fipa;

import java.util.logging.Level;

public class ProdConsConfiguration {

    // --- Class Variables -----------------------------------------------------

    private static ProdConsConfiguration instance;

    // --- Instance Variables --------------------------------------------------

    private Level loggingLevel;

    // --- Constructors --------------------------------------------------------

    private ProdConsConfiguration() {
	loggingLevel = Level.INFO;
    }

    // --- Methods -------------------------------------------------------------

    public static ProdConsConfiguration instance() {
	if (instance == null) {
	    instance = new ProdConsConfiguration();
	}
	return instance;
    }

    public Level getLogLevel() {
	return Level.INFO;
    }
}
