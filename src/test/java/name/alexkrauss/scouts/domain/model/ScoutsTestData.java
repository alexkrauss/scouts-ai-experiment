package name.alexkrauss.scouts.domain.model;

import java.time.LocalDate;
import java.util.List;

public class ScoutsTestData {

    public static final Scout JOHN_DOE = Scout.builder()
            .name("John Doe")
            .birthDate(LocalDate.of(2010, 5, 15))
            .address("123 Scout Street, Scout City 12345")
            .phoneNumber("555-0123")
            .healthInsurance("Health Plus")
            .allergyInfo("None")
            .vaccinationInfo("All standard vaccinations complete")
            .contacts(List.of(
                new Contact("Jane Doe", "555-0124", "jane.doe@email.com", "mother"),
                new Contact("Jim Doe", "555-0125", "jim.doe@email.com", "father")
            ))
            .lastUpdated(LocalDate.now())
            .build();

    public static final Scout EMMA_SMITH = Scout.builder()
            .name("Emma Smith")
            .birthDate(LocalDate.of(2011, 8, 21))
            .address("456 Scout Avenue, Scout Town 67890")
            .phoneNumber("555-0126")
            .healthInsurance("MediCare")
            .allergyInfo("Peanuts")
            .vaccinationInfo("Up to date")
            .contacts(List.of(
                new Contact("Sarah Smith", "555-0127", "sarah.smith@email.com", "mother")
            ))
            .lastUpdated(LocalDate.now())
            .build();
}
