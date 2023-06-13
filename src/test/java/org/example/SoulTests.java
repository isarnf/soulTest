package org.example;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.github.javafaker.Faker;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.openqa.selenium.By.xpath;

public class SoulTests {
    private WebDriver driver;
    private Faker faker = new Faker();

    @BeforeEach
    void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        driver = new ChromeDriver(options);
    }

    @AfterEach
    void tearDown() {
        driver.quit();
    }

    @Nested
    @DisplayName("When adding a soul")
    class AddNewSoul {
        @Test
        @DisplayName("Should show added soul in the table")
        void shouldShowAddedSoulInTheTable() throws InterruptedException {
            driver.get("http://localhost:3000/");
            driver.findElement((By.id("soul-name"))).sendKeys("Pessoa A");
            driver.findElement((By.id("soul-owner"))).sendKeys("Pessoa B");

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
            Thread.sleep(20);
            final List<WebElement> tableRowsAfterInsertion = tableBody.findElements((By.tagName("tr")));
            final int numberOfRowsAfterInsertion = tableRowsAfterInsertion.size();
            softly.assertThat(numberOfRowsAfterInsertion).isEqualTo(numberOfRows + 1);
            softly.assertAll();
        }

        @Test
        @DisplayName("Should not add soul when fields are empty.")
        void shouldntAddSoulWhenFormIsEmpty() throws InterruptedException {
            driver.get("http://localhost:3000/");

            // Leave text inputs empty
            driver.findElement((By.id("soul-name"))).sendKeys("");
            driver.findElement((By.id("soul-owner"))).sendKeys("");

            // Leave selection box unchanged
            final WebElement location = driver.findElement(By.id("soul-location"));
            final Select select = new Select(location);
            select.selectByIndex(0);

            // Count the number of table rows before save button click
            final SoftAssertions softly = new SoftAssertions();
            final WebElement table = driver.findElement((By.xpath("/html/body/div/div/table")));
            final WebElement tableBody = table.findElement((By.xpath("/html/body/div/div/table/tbody")));
            final List<WebElement> tableRows = tableBody.findElements((By.tagName("tr")));
            final int numberOfRows = tableRows.size();

            // Click the button
            driver.findElement((By.id("save-btn"))).click();
            Thread.sleep(20);

            // Count number of table rows after save button click
            final List<WebElement> tableRowsAfterSaveButtonClick = tableBody.findElements((By.tagName("tr")));
            final int numberOfRowsAfterSaveButtonClick = tableRowsAfterSaveButtonClick.size();
            softly.assertThat(numberOfRowsAfterSaveButtonClick).isEqualTo(numberOfRows);
            softly.assertAll();
        }

        @Test
        @DisplayName("Should not have duplicates.")
        void shouldntHaveDuplicates() {
            driver.get("http://localhost:3000/");

            // Make a list of all names, owners and locations
            final WebElement table = driver.findElement((By.xpath("/html/body/div/div/table")));
            final WebElement tableBody = table.findElement((By.xpath("/html/body/div/div/table/tbody")));
            final List<WebElement> tableRows = tableBody.findElements((By.tagName("tr")));
            final List<String> names = new ArrayList<>();
            final List<String> owners = new ArrayList<>();
            final List<String> locations = new ArrayList<>();

            // Add all the elements of the table to the lists
            for (WebElement tableRow : tableRows) {
                names.add(tableRow.findElements((By.tagName("td"))).get(1).getText());
                owners.add(tableRow.findElements((By.tagName("td"))).get(2).getText());
                locations.add(tableRow.findElements((By.tagName("td"))).get(3).getText());
            }

            // Check if there are duplicates in the table
            boolean foundDuplicate = false;
            for (int i = 0; i < names.size() - 1; i++) {
                if (names.get(i).equals(names.get(i + 1))) {
                    for (int j = 0; j < owners.size() - 1; j++) {
                        if (owners.get(j).equals(owners.get(j + 1))) {
                            for (int k = 0; k < locations.size() - 1; k++) {
                                if (locations.get(k).equals(locations.get(k + 1))) {
                                    foundDuplicate = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            assertThat(foundDuplicate).isFalse();
        }

    }

    @Nested
    @DisplayName("When editing a soul")
    class EditSoul {

        private String luckyTheLocationOfTheSoul() {
            final List<String> locations = Arrays.asList("sky", "hell", "purgatory");
            return locations.get(new Random().nextInt(locations.size()));
        }


        @Test
        @DisplayName("Should edit the first soul of the table")
        void shouldEditTheFirstSoulOfTheTable() throws InterruptedException {
            driver.get("http://localhost:3000/");

            final List<WebElement> rowsTable = driver.findElements(By.xpath("/html/body/div/div/table/tbody/tr"));
            if (rowsTable.size() == 0)
                throw new NoSuchFieldError("Not found rows in table");

            // Values to update soul
            final String changeNameTo = faker.name().fullName();
            final String changeOwnerTo = faker.name().fullName();
            final String changeLocationTo = luckyTheLocationOfTheSoul();

            // Clicking the edit button on the first soul of the table
            driver.findElement(xpath("/html/body/div/div/table/tbody/tr[1]/td[5]/button[1]")).click();

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
            selectLocalication.selectByValue(changeLocationTo);


            // Clicking in save button
            driver.findElement(xpath("//*[@id=\"save-btn\"]")).click();

            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(driver -> driver.findElement(By.xpath("/html/body/div/div/table/tbody/tr[1]")));

            // Picking up new soul values
            final String elementOneNameAfter = driver.findElement(xpath("/html/body/div/div/table/tbody/tr[1]/td[2]")).getText();
            final String elementOneOwnerAfter = driver.findElement(xpath("/html/body/div/div/table/tbody/tr[1]/td[3]")).getText();
            final String elementOneLocationAfter = driver.findElement(xpath("/html/body/div/div/table/tbody/tr[1]/td[4]")).getText();

            // Checking values
            final var softly = new SoftAssertions();
            softly.assertThat(elementOneNameAfter).isEqualTo(changeNameTo);
            softly.assertThat(elementOneOwnerAfter).isEqualTo(changeOwnerTo);
            softly.assertThat(elementOneLocationAfter).isEqualTo(changeLocationTo);
            softly.assertAll();
        }
    }


    @Nested
    @DisplayName("When removing a soul")
    class RemoveSoul {

        @Test
        @DisplayName("Should remove the first soul of the table")
        void shouldRemoveTheFirstSoulOfTheTable() throws InterruptedException {
            driver.get("http://localhost:3000/");

            final int firstElementIdBeforeExclusion = Integer.parseInt(
                    new WebDriverWait(driver, Duration.ofSeconds(5))
                            .until(driver -> driver.findElement
                                    (By.xpath("/html/body/div/div/table/tbody/tr[1]/td[1]"))
                            )
                            .getText()
            );

            final WebElement button = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.elementToBeClickable
                            (By.xpath("/html/body/div/div/table/tbody/tr[1]/td[5]/button[2]"))
                    );
            button.click();

            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.alertIsPresent())
                    .accept();

            Thread.sleep(10);

            final int firstElementIdAfterExclusion = Integer.parseInt(
                    new WebDriverWait(driver, Duration.ofSeconds(5))
                            .until(driver -> driver.findElement
                                    (By.xpath("/html/body/div/div/table/tbody/tr[1]/td[1]"))
                            )
                            .getText()
            );
            assertThat(firstElementIdAfterExclusion).isEqualTo(firstElementIdBeforeExclusion + 1);
        }
    }
}

