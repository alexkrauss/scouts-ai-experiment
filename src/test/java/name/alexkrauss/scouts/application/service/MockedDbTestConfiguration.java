package name.alexkrauss.scouts.application.service;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

@Configuration
@ComponentScan(basePackages = {
        "name.alexkrauss.scouts.domain",
        "name.alexkrauss.scouts.application",
        "name.alexkrauss.scouts.infrastructure.dbmock"
})
public class MockedDbTestConfiguration {


    public static class DbMockResetTestExecutionListener implements TestExecutionListener {

        @Override
        public void beforeTestMethod(TestContext testContext) {
            testContext.getApplicationContext().getBeansOfType(MockResetAware.class).values()
                    .forEach(MockResetAware::reset);
        }
    }
}
