Cypress.on('uncaught:exception', (err, runnable) => {
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
            cy.task('log', `--- Testing user: ${username} ---`);
            cy.visit('/');

            cy.get('#user-name').type(username);
            cy.get('#password').type('secret_sauce');
            cy.get('#login-button').click();

            if (username === 'locked_out_user') {
                cy.get('[data-test="error"]').should('be.visible').and('contain.text', 'locked out');
                cy.screenshot(`${username}_locked_out`);
                return;
            }

            cy.get('#inventory_container').should('be.visible');
            cy.title().should('include', 'Swag Labs');

            cy.get('.inventory_item').then($items => {
                const itemsCount = $items.length;
                let itemsToAdd = Math.min(4, itemsCount);
                for (let i = 0; i < itemsToAdd; i++) {
                    cy.get('.inventory_item').eq(i).find('button').invoke('text').then((text) => {
                        if (text.includes('Add to cart') || text.includes('Remove')) {
                            cy.get('.inventory_item').eq(i).find('button').click({ force: true });
                        }
                    });
                }
            });

            cy.get('.shopping_cart_badge').should('exist').invoke('text').then((text) => {
                if (username === 'standard_user' || username === 'performance_glitch_user' || username === 'visual_user') {
                    expect(text).to.eq('4');
                }
            });

            cy.screenshot(`${username}_completed`);
        });
    });
});
