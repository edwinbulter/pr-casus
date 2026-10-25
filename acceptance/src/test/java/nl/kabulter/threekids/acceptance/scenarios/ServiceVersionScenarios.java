package nl.kabulter.threekids.acceptance.scenarios;

import nl.kabulter.threekids.acceptance.dsl.Build;
import nl.kabulter.threekids.acceptance.dsl.Operator;
import org.junit.jupiter.api.Test;

class ServiceVersionScenarios {

    private final Operator operator = new Operator();

    @Test
    void theServiceReportsTheVersionThatWasBuilt() {
        // Given the service has been built from the current commit
        String builtVersion = Build.currentVersion();

        // When an operator asks the service for its version
        operator.asksForTheVersion();

        // Then the service reports that commit as its version
        operator.seesVersion(builtVersion);
    }
}
