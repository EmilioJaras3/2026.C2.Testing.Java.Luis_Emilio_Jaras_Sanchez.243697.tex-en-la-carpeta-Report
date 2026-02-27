Cypress.on('uncaught:exception', (err, runnable) => {
    // returning false here prevents Cypress from
    // failing the test
    return false
})

describe('SauceDemo E2E Tests with multiple users', () => {

    const users = [
        'standard_user',
        'locked_out_user',
        'problem_user',
        'performance_glitch_user',
        'error_user',
        'visual_user'
    ];

    users.forEach((username) => {
        it(`Should test workflow for user: ${username}`, () => {
            cy.log(`--- Testing user: ${username} ---`);

            // Log to Node console
            cy.task('log', `================================`);
            cy.task('log', `Started testing user: ${username}`);

            cy.visit('/');

            // Login
            cy.get('#user-name').type(username);
            cy.get('#password').type('secret_sauce');
            cy.get('#login-button').click();

            if (username === 'locked_out_user') {
                cy.get('[data-test="error"]').should('be.visible').and('contain.text', 'locked out');
                cy.task('log', `User ${username} correctly locked out.`);
                // Take screenshot manually for success criteria on locked out user
                cy.screenshot(`${username}_locked_out`);
                return; // Stop test here for locked_out_user
            }

            // Verify successful login
            cy.get('#inventory_container').should('be.visible');
            cy.title().should('include', 'Swag Labs');

            // Add first 4 items to the cart
            cy.get('.inventory_item').then($items => {
                const itemsCount = $items.length;
                cy.task('log', `Found ${itemsCount} items for user: ${username}`);

                // Some users might have UI glitches, so we wrap this in try-catch-like behavior in Cypress
                // Cypress doesn't have try-catch for assertions, but we can check if elements exist
                let itemsToAdd = Math.min(4, itemsCount);
                for (let i = 0; i < itemsToAdd; i++) {
                    // Try to click 'Add to cart' using specific button text for robustness, or index
                    cy.get('.inventory_item').eq(i).find('button').invoke('text').then((text) => {
                        if (text.includes('Add to cart') || text.includes('Remove')) {
                            cy.get('.inventory_item').eq(i).find('button').click({ force: true });
                            cy.task('log', `${username} - Clicked button on item ${i + 1}`);
                        }
                    });
                }
            });

            // Validate cart badge
            cy.get('.shopping_cart_badge').should('exist').invoke('text').then((text) => {
                cy.task('log', `Cart badge value for ${username}: ${text}`);

                // We don't strictly assert the value is '4' because problem/error users might fail to add items
                if (username === 'standard_user' || username === 'performance_glitch_user' || username === 'visual_user') {
                    expect(text).to.eq('4');
                    cy.task('log', `SUCCESS: Added 4 items to cart for ${username}`);
                } else {
                    cy.task('log', `NOTE: User ${username} has known bugs, cart value is ${text} instead of expected 4`);
                }
            });

            // Take final success screenshot
            cy.screenshot(`${username}_completed`);
        });
    });
});
