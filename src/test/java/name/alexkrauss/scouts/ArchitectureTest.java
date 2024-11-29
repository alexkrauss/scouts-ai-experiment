package name.alexkrauss.scouts;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.Architectures;
import org.springframework.stereotype.Repository;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "name.alexkrauss.scouts")
public class ArchitectureTest {
    @ArchTest
    public static final ArchRule hexagonalArchitecture =
            Architectures.onionArchitecture()
                    .domainModels("..domain.model..")
                    .domainServices("..domain.service..")
                    .applicationServices("..application..")
                    .adapter("rest", "..infrastructure.rest..")
                    .adapter("db", "..infrastructure.db..")
                    .adapter("dbmock", "..infrastructure.dbmock..")
                    .withOptionalLayers(true);

    @ArchTest
    public static final ArchRule repositoryInterfaces =
            classes().that().resideInAPackage("..application.ports.persistence..")
            .should().haveSimpleNameEndingWith("Repository")
            .andShould().beInterfaces();

    @ArchTest
    public static final ArchRule repositoryImplementations =
            classes().that().resideInAPackage("..infrastructure.db..")
                    .and().haveSimpleNameEndingWith("Repository")
                    .should().haveSimpleNameStartingWith("Db")
                    .andShould().beAnnotatedWith(Repository.class);

    @ArchTest
    public static final ArchRule jooqOnlyInDb =
            noClasses().that().resideOutsideOfPackage("..infrastructure.db..")
                    .should().dependOnClassesThat().resideInAnyPackage("org.jooq..");
}