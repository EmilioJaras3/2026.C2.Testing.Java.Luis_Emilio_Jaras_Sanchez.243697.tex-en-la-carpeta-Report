const { defineConfig } = require("cypress");

module.exports = defineConfig({
  e2e: {
    setupNodeEvents(on, config) {
      on('task', {
        log(message) {
          console.log(message)
          return null
        },
      })
    },
    baseUrl: 'https://www.saucedemo.com',
    video: false,
    screenshotOnRunFailure: true,
    screenshotsFolder: 'cypress/screenshots',
    // Viewport to mimic standard user screen
    viewportWidth: 1280,
    viewportHeight: 720,
    defaultCommandTimeout: 10000,
  },
});
