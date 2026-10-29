package com.connectedgames.architecture;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.base.DescribedPredicate.alwaysTrue;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.onionArchitecture;

/**
 * Rules of Clean Architecture, same per each service.
 * Each service executes with ArchTests.in(CleanARchitecture.class)
 */
public final class CleanArchitectureRules {

    private CleanArchitectureRules() {
    }

    /** Domain dose not depend on nothing from outside, Adapters do not call eachother */
    @ArchTest
    static final ArchRule cleanArchitecture = onionArchitecture()
            .domainModels("..domain..")
            .applicationServices("..application..")
            .adapter("web", "..adapters.in.web..")
            .adapter("mqtt", "..adapters.in.mqtt..")
            .adapter("persistence", "..adapters.out.persistence..")
            // infrastructure is the composition root: it creates the use cases, so it may see every layer
            .ignoreDependency(resideInAPackage("..infrastructure.."), alwaysTrue())
            .withOptionalLayers(true);

    /** Nobody depends on the composition root, otherwise the wiring would leak into the layers */
    @ArchTest
    static final ArchRule nothingDependsOnInfrastructure = noClasses()
            .that().resideOutsideOfPackage("..infrastructure..")
            .should().dependOnClassesThat()
            .resideInAPackage("..infrastructure..")
            .allowEmptyShould(true);

    /** Domain is Pure Java*/
    @ArchTest
    static final ArchRule domainIsPureJava = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("org.springframework..", "jakarta.persistence..")
            .allowEmptyShould(true);
}
