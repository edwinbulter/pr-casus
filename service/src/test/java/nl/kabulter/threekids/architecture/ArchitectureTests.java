package nl.kabulter.threekids.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.AccessTarget.CodeUnitCallTarget;
import com.tngtech.archunit.core.domain.JavaCall;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaConstructor;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import java.time.Clock;
import java.util.Date;
import java.util.List;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "nl.kabulter.threekids", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTests {

    /** ADR 0006: today comes from the injected Clock; only ClockConfig reads the system time. */
    @ArchTest
    static final ArchRule onlyClockConfigReadsTheSystemTime = noClasses()
            .that().doNotHaveSimpleName("ClockConfig")
            .should().callCodeUnitWhere(new ReadsTheSystemTime());

    /**
     * A call that reads the system time:
     * <ul>
     *     <li>{@code now()} or {@code now(ZoneId)} of any java.time class; only {@code now(Clock)} is allowed;</li>
     *     <li>a factory of the system clock: {@code Clock.systemDefaultZone()}, {@code systemUTC()}, {@code system(ZoneId)};</li>
     *     <li>{@code System.currentTimeMillis()} and {@code new Date()}.</li>
     * </ul>
     */
    private static final class ReadsTheSystemTime extends DescribedPredicate<JavaCall<?>> {

        ReadsTheSystemTime() {
            super("reads the system time instead of the injected Clock");
        }

        @Override
        public boolean test(JavaCall<?> call) {
            CodeUnitCallTarget target = call.getTarget();
            String owner = target.getOwner().getName();
            String name = target.getName();
            List<String> parameters = target.getRawParameterTypes().stream().map(JavaClass::getName).toList();

            if (owner.startsWith("java.time.") && name.equals("now")) {
                return !parameters.equals(List.of(Clock.class.getName()));
            }
            if (owner.equals(Clock.class.getName())) {
                return name.startsWith("system");
            }
            if (owner.equals(System.class.getName())) {
                return name.equals("currentTimeMillis");
            }
            if (owner.equals(Date.class.getName())) {
                return name.equals(JavaConstructor.CONSTRUCTOR_NAME) && parameters.isEmpty();
            }
            return false;
        }
    }
}
