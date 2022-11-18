package de.elbosso.tools.rfc3161timestampingserver;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.CompositeArchRule;
import org.slf4j.Logger;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;

@AnalyzeClasses(packages = "de.elbosso.tools.rfc3161timestampingserver")
public class CodingRulesTest {

    @ArchTest
    private final ArchRule no_access_to_standard_streams = NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;

    @ArchTest
    private final ArchRule no_generic_exceptions = NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;

    @ArchTest
    private final ArchRule no_java_util_logging = NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;

    @ArchTest
    private final ArchRule no_inline_java_util_logging=
            noClasses().
                    should().callMethod("java.util.logging.Logger","getLogger","java.lang.String").
                    orShould().callMethod("java.util.logging.Logger","getLogger","java.lang.String","java.lang.String");

    @ArchTest
    private final ArchRule loggers_should_be_private_static_final =
//            fields().that().haveRawType(Logger.class)
            fields().that().haveName("CLASSS_LOGGER")
                    .and().haveRawType(org.slf4j.Logger.class)
                    .or().haveName("EXCEPTION_LOGGER")
                    .and().haveRawType(Logger.class)
                    .should().bePrivate()
                    .andShould().beStatic()
                    .andShould().beFinal()
                    .because("we agreed on this convention");

    @ArchTest
    static final ArchRule no_classes_should_access_standard_streams_or_throw_generic_exceptions =
            CompositeArchRule.of(NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS)
                    .and(NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS);

    @ArchTest
    private final ArchRule no_system_currenttimemillis=
            noClasses().
                    should().callMethod("java.lang.System","currentTimeMillis");
    @ArchTest
    private final ArchRule no_new_javautildate=
            noClasses().
                    should().callConstructor("java.util.Date");
}