package org.example;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class SoulTests {

    private WebDriver driver;

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

    @Nested @DisplayName("When adding a soul")
    class AddNewSoul{
        @Test @DisplayName("Should show added soul in the table")
        void shouldShowAddedSoulInTheTable() throws InterruptedException {
            driver.get("http://localhost:3000/");
            driver.findElement((By.id("soul-name"))).sendKeys("Cauê");
            driver.findElement((By.id("soul-owner"))).sendKeys("Deus");

            final WebElement location = driver.findElement(By.id("soul-location"));
            final Select select = new Select(location);
            select.selectByIndex(1);
            final SoftAssertions softly = new SoftAssertions();
            softly.assertThat(select.getFirstSelectedOption().getText()).isEqualTo("Céu");

            final WebElement table = driver.findElement((By.xpath("/html/body/div/div/table")));
            final WebElement tableBody = table.findElement((By.xpath("/html/body/div/div/table/tbody")));
            final List<WebElement> tableRows = tableBody.findElements((By.tagName("tr")));
            final int numberOfRows = tableRows.size();

            driver.findElement((By.id("save-btn"))).click();
            Thread.sleep(1);
            final List<WebElement> tableRowsAfterInsertion = tableBody.findElements((By.tagName("tr")));
            final int numberOfRowsAfterInsertion = tableRowsAfterInsertion.size();
            softly.assertThat(numberOfRowsAfterInsertion).isEqualTo(numberOfRows + 1);
            softly.assertAll();
        }
    }
    
    @Nested @DisplayName("When removing a soul")
    class RemoveSoul {

        private boolean isElementPresent(String xpath) {
            try {
                driver.findElement(By.xpath(xpath));
                return true;
            } catch(NoSuchElementException exception) {
                return false;
            }
        }

        @Test @DisplayName("Should remove the first soul of the table")
        void shouldRemoveTheFirstSoulOfTheTable() {
            driver.get("http://localhost:3000/");
            final WebElement button = new WebDriverWait(driver, Duration.ofSeconds(10)) // 10s timeout
                    .until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/div/div/table/tbody/tr[1]/td[5]/button[2]")));
            button.click();
            final int initialSize = driver.findElements(By.xpath("/html/body/div/div/table/tbody/tr/td[1]")).size();
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.alertIsPresent())
                    .accept();
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.numberOfElementsToBeLessThan(By.xpath("/html/body/div/div/table/tbody"), initialSize ));
            assertThat(isElementPresent("/html/body/div/div/table/tbody/tr[1]")).isFalse();
        }
    }


}
