package org.example;

import com.github.javafaker.Faker;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.openqa.selenium.By.xpath;

public class SoulTests {
    private WebDriver driver;
    private final Faker faker = new Faker();

    @BeforeEach
    void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        driver = new ChromeDriver(options);
    }
    @AfterEach
    void tearDown(){
        driver.quit();
    }

    @Nested
    @DisplayName("When editing a soul")
    class EditSoul{

        private String luckyTheLocalicationOfTheSoul(){
            final List<String> locations = Arrays.asList("sky", "hell", "purgatory");
            return locations.get(new Random().nextInt(locations.size()));
        }

        @Test
        @DisplayName("Should edit the first soul of the table")
        void editar() throws InterruptedException {
            driver.get("http://localhost:3000/");

            // Values to update soul
            final String changeNameTo = faker.name().fullName();
            final String changeOwnerTo = faker.name().fullName();
            final String changeLocalicationTo = luckyTheLocalicationOfTheSoul();

            // Clicking the edit button on the first soul of the table
            driver.findElement(xpath("/html/body/div/div/table/tbody/tr[1]/td[5]/button[1]")).click();

            Thread.sleep(500);

            // Get inputs
            final WebElement inputName = driver.findElement(xpath("//*[@id=\"soul-name\"]"));
            final WebElement inputOwner = driver.findElement(xpath("//*[@id=\"soul-owner\"]"));
            final Select selectLocalication = new Select(driver.findElement(xpath("//*[@id=\"soul-location\"]")));

            //Clear inputs
            inputName.clear();
            inputOwner.clear();

            // Changing entries with new soul values
            inputName.sendKeys(changeNameTo);
            inputOwner.sendKeys(changeOwnerTo);
            selectLocalication.selectByValue(changeLocalicationTo);


            // Clicking in save button
            driver.findElement(xpath("//*[@id=\"save-btn\"]")).click();

            Thread.sleep(500);

            // Picking up new soul values
            final String elementOneNameAfter = driver.findElement(xpath("/html/body/div/div/table/tbody/tr[1]/td[2]")).getText();
            final String elementOneOwnerAfter = driver.findElement(xpath("/html/body/div/div/table/tbody/tr[1]/td[3]")).getText();
            final String elementOneLocationAfter = driver.findElement(xpath("/html/body/div/div/table/tbody/tr[1]/td[4]")).getText();

            // Checking values
            final var softly = new SoftAssertions();
            softly.assertThat(elementOneNameAfter).isEqualTo(changeNameTo);
            softly.assertThat(elementOneOwnerAfter).isEqualTo(changeOwnerTo);
            softly.assertThat(elementOneLocationAfter).isEqualTo(changeLocalicationTo);
            softly.assertAll();
        }

    }
}
