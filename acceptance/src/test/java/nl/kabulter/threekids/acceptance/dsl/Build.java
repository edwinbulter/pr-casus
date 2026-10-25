package nl.kabulter.threekids.acceptance.dsl;

/**
 * The build under test. Its version is the short commit hash that the Makefile passes to Maven.
 */
public final class Build {

    private Build() {
    }

    public static String currentVersion() {
        return System.getProperty("app.version", "dev");
    }
}
