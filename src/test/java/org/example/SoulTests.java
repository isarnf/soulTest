package org.example;

import com.github.javafaker.Faker;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.setAllowComparingPrivateFields;
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
        driver.get("http://localhost:3000/");
    }

    @AfterEach
    void tearDown() {
        driver.quit();
    }

    private List<WebElement> getRows(WebDriver driver) {
        final List<WebElement> rows = driver.findElements(By.xpath("/html/body/div/div/table/tbody/tr"));
        if (rows == null)
            throw new NoSuchElementException("Table is empty");

        return rows;
    }

    private WebElement findSoulById(WebDriver driver, String id) {
        final List<WebElement> rows = getRows(driver);

        for (WebElement e : rows)
            if (e.findElements(By.tagName("td")).get(0).getText().equals(id))
                return e;

        return null;
    }

    private void fillInputs(WebDriver driver, String name, String owner, String location) {
        final WebElement inputName = driver.findElement(xpath("//*[@id=\"soul-name\"]"));
        final WebElement inputOwner = driver.findElement(xpath("//*[@id=\"soul-owner\"]"));
        final Select selectLocalication = new Select(driver.findElement(xpath("//*[@id=\"soul-location\"]")));

        inputName.sendKeys(name);
        inputOwner.sendKeys(owner);
        selectLocalication.selectByValue(location);
    }

    private void clickTheSoulEditButton(WebElement soul) {
        soul.findElements(By.tagName("td")).get(4).findElements(By.tagName("button")).get(0).click();
    }

    private void clickTheSoulDeleteButton(WebElement soul) {
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(
                        soul.findElements(By.tagName("td")).get(4).findElements(By.tagName("button")).get(1))
                )
                .click();
    }

    private void clickSaveButton(WebDriver driver) {
        driver.findElement(xpath("/html/body/div/form/div[3]/input")).click();
    }

    @Nested
    @DisplayName("When adding a soul")
    class AddNewSoul {
        @Test
        @DisplayName("Should show added soul in the table")
        void shouldShowAddedSoulInTheTable() throws InterruptedException {
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

        @Test
        @DisplayName("Should not add duplicates.")
        void shouldntAddDuplicates() throws InterruptedException {
            final SoftAssertions softly = new SoftAssertions();

            // Check if table contain rows
            final WebElement table = driver.findElement((By.xpath("/html/body/div/div/table")));
            final WebElement tableBody = table.findElement((By.xpath("/html/body/div/div/table/tbody")));
            final List<WebElement> tableRows = tableBody.findElements((By.tagName("tr")));
            final int numberOfRows = tableRows.size();
            softly.assertThat(numberOfRows).isGreaterThan(0);


            // Get the values of first row
            final String nameFirstRow = tableRows.get(0).findElements((By.tagName("td"))).get(1).getText();
            final String ownerFirstRow = tableRows.get(0).findElements((By.tagName("td"))).get(2).getText();
            final String locationFirstRow = tableRows.get(0).findElements((By.tagName("td"))).get(3).getText();

            // Fill form inputs with same values of the first row
            driver.findElement((By.id("soul-name"))).sendKeys(nameFirstRow);
            driver.findElement((By.id("soul-owner"))).sendKeys(ownerFirstRow);
            final WebElement location = driver.findElement(By.id("soul-location"));
            final Select select = new Select(location);
            select.selectByValue(locationFirstRow);

            // Click the save button
            driver.findElement((By.id("save-btn"))).click();
            Thread.sleep(20);

            // Count number of table rows after saving
            final List<WebElement> tableRowsAfterSaveButtonClick = tableBody.findElements((By.tagName("tr")));
            final int numberOfRowsAfterSaveButtonClick = tableRowsAfterSaveButtonClick.size();
            softly.assertThat(numberOfRowsAfterSaveButtonClick).isEqualTo(numberOfRows);
            softly.assertAll();
        }

    }

    @Nested
    @DisplayName("When editing a soul")
    class EditSoul {

        private String luckyTheLocationOfTheSoul() {
            final List<String> locations = Arrays.asList("sky", "hell", "purgatory");
            return locations.get(new Random().nextInt(locations.size()));
        }

        private void cleanInputs(WebDriver driver) {
            final WebElement inputName = driver.findElement(xpath("//*[@id=\"soul-name\"]"));
            final WebElement inputOwner = driver.findElement(xpath("//*[@id=\"soul-owner\"]"));
            final Select selectLocalication = new Select(driver.findElement(xpath("//*[@id=\"soul-location\"]")));

            inputName.clear();
            inputOwner.clear();
            selectLocalication.selectByVisibleText("Selecione");
        }

        private WebElement getFirstSoulInTable(WebDriver driver) {
            WebElement soul = driver.findElement(xpath("/html/body/div/div/table/tbody/tr[1]"));
            if (soul == null)
                throw new NoSuchElementException("Soul not found");
            return soul;
        }

        private String getSoulId(WebElement soul) {
            return soul.findElements(By.tagName("td")).get(0).getText();
        }

        @Test
        @DisplayName("Should edit the first soul of the table")
        void shouldEditTheFirstSoulOfTheTable() {
            final String changeNameTo = faker.name().fullName();
            final String changeOwnerTo = faker.name().fullName();
            final String changeLocationTo = luckyTheLocationOfTheSoul();

            final WebElement firstSoulInTable = getFirstSoulInTable(driver);
            final String id = getSoulId(firstSoulInTable);
            clickTheSoulEditButton(firstSoulInTable);

            cleanInputs(driver);
            fillInputs(driver, changeNameTo, changeOwnerTo, changeLocationTo);
            clickSaveButton(driver);


            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(driver -> driver.findElement(By.xpath("/html/body/div/div/table/tbody/tr[1]")));

            final WebElement soul = findSoulById(driver, id);
            final String soulName = soul.findElements(By.tagName("td")).get(1).getText();
            final String soulOwner = soul.findElements(By.tagName("td")).get(2).getText();
            final String soulLocation = soul.findElements(By.tagName("td")).get(3).getText();

            final var softly = new SoftAssertions();
            softly.assertThat(soulName).isEqualTo(changeNameTo);
            softly.assertThat(soulOwner).isEqualTo(changeOwnerTo);
            softly.assertThat(soulLocation).isEqualTo(changeLocationTo);
            softly.assertAll();
        }
    }

    @Nested
    @DisplayName("When removing a soul")
    class RemoveSoul {

        @Test
        @DisplayName("Should remove the first soul of the table")
        void shouldRemoveTheFirstSoulOfTheTable() {

            final WebElement soul = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(driver -> driver.findElement
                            (By.xpath("/html/body/div/div/table/tbody/tr[1]"))
                    );

            final String idOfFirstSoulBeforeExclusion = soul.findElements(By.tagName("td")).get(0).getText();

            clickTheSoulDeleteButton(soul);

            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.alertIsPresent())
                    .accept();

            driver.navigate().refresh();

            boolean elementIsNotPresent = findSoulById(driver, idOfFirstSoulBeforeExclusion) == null;

            assertThat(elementIsNotPresent).isTrue();
        }
    }
}

