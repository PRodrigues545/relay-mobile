//Codigo exemplo, para apagar. É so para nao ficar um ficheiro vazio.


const { remote } = require('webdriverio');

async function main() {
    const driver = await remote({
        hostname: '127.0.0.1',
        port: 4723,
        path: '/',
        capabilities: {
            platformName: 'Android',
            'appium:automationName': 'UiAutomator2',
            'appium:deviceName': 'Android Emulator',
            'appium:app': 'C:\\caminho\\para\\a\\tua\\app.apk',
            'appium:appWaitActivity': '*'
        }
    });

    try {
        // Encontrar os campos de login
        const emailInput = await driver.$('id:ipca.api.relay:id/email');
        const passwordInput = await driver.$('id:ipca.api.relay:id/password');
        const loginButton = await driver.$('id:ipca.api.relay:id/loginButton');

        // Preencher os dados
        await emailInput.setValue('teste@email.com');
        await passwordInput.setValue('Password123!');

        // Clicar no botão de login
        await loginButton.click();

        // Verificar o resultado esperado
        const homeScreen = await driver.$('id:ipca.api.relay:id/homeScreen');
        await homeScreen.waitForDisplayed({ timeout: 10000 });

        console.log('Teste de login concluído com sucesso!');
    } catch (error) {
        console.error('O teste falhou:', error.message);
        throw error;
    } finally {
        await driver.deleteSession();
    }
}

main().catch(console.error);
