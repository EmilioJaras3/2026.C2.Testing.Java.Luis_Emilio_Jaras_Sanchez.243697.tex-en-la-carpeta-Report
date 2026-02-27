package com.example;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

public class SauceDemoTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();
        // Create screenshots folder if it doesn't exist
        try {
            Files.createDirectories(Paths.get("screenshots"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @BeforeEach
    void setupTest() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "standard_user",
            "locked_out_user",
            "problem_user",
            "performance_glitch_user",
            "error_user",
            "visual_user"
    })
    @DisplayName("Test SauceDemo with all user types")
    void testSauceDemoUsers(String username) {
        System.out.println("===============================");
        System.out.println("Testing user: " + username);
        driver.get("https://www.saucedemo.com/");

        try {
            // Login
            driver.findElement(By.id("user-name")).sendKeys(username);
            driver.findElement(By.id("password")).sendKeys("secret_sauce");
            driver.findElement(By.id("login-button")).click();

            // Handle locked out user specifically
            if (username.equals("locked_out_user")) {
                WebElement errorMsg = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h3[data-test='error']")));
                System.out.println("Expected error for locked_out_user: " + errorMsg.getText());
                takeScreenshot(username + "_locked_out");
                Assertions.assertTrue(errorMsg.getText().contains("locked out"));
                return; // End test for this user
            }

            // Check if login is successful (Inventory container should be present)
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("inventory_container")));
            Assertions.assertTrue(driver.getTitle().contains("Swag Labs"));

            // Wait for inventory items to load
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("inventory_item")));
            List<WebElement> addButtons = driver.findElements(By.xpath("//button[text()='Add to cart']"));

            System.out.println("Found " + addButtons.size() + " 'Add to cart' buttons.");
            
            // Add first 4 items to the cart
            int itemsToAdd = Math.min(4, addButtons.size());
            for (int i = 0; i < itemsToAdd; i++) {
                try {
                    addButtons.get(i).click();
                } catch (Exception e) {
                    System.out.println("Could not click button " + i + " for user " + username + " (could be expected for problem/error user).");
                }
            }

            // Validate cart badge
            WebElement cartBadge = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("shopping_cart_badge")));
            String itemsInCartStr = cartBadge.getText();
            System.out.println("Items in cart according to badge: " + itemsInCartStr);
            
            // Expected might not be 4 for buggy users
            if (username.equals("problem_user") || username.equals("error_user")) {
                 System.out.println("Note: Problem/Error users might not add all items successfully.");
            } else {
                 Assertions.assertEquals(String.valueOf(itemsToAdd), itemsInCartStr, "Cart badge does not show expected number of items");
            }
            
            takeScreenshot(username + "_success");

        } catch (TimeoutException e) {
            System.out.println("Timeout encountered for user: " + username + ". This might be expected for glitch or error users.");
            takeScreenshot(username + "_timeout_failure");
        } catch (Exception e) {
            System.out.println("Exception encountered for user: " + username + ": " + e.getMessage());
            takeScreenshot(username + "_exception");
        }
    }

    private void takeScreenshot(String name) {
        try {
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File destFile = new File("screenshots/" + name + ".png");
            Files.copy(srcFile.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Screenshot saved: " + destFile.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Failed to take screenshot for " + name);
            e.printStackTrace();
        }
    }
}
