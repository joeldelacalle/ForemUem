# Email Header and Signature Verification

This Java application connects to the Gmail server, retrieves emails from the inbox, and verifies the email headers and digital signatures. The application uses OAuth2 for authentication and the Google Gmail API for retrieving emails.

## Features

- Connect to Gmail using OAuth2 authentication.
- Retrieve emails from the Gmail inbox.
- Verify email headers for common standards and potential issues.
- Validate digital signatures and the certificates used to generate these signatures.

## Prerequisites

- Java Development Kit (JDK) 8 or higher.
- Apache Maven 3.6.0 or higher.
- A Google Cloud project with the Gmail API enabled.
- OAuth2 credentials (client ID and client secret) from Google Cloud Console.
- Eclipse IDE (optional, but recommended for development).

## Setup

### 1. Clone the Repository
git clone https://github.com/joeldelacalle/ForemUem.git
cd email-verification

### 2. Configure Google Cloud Project
Go to the Google Cloud Console.
Create a new project or select an existing project.
Enable the Gmail API for your project.
Create OAuth 2.0 credentials (Client ID and Client Secret).
Download the credentials.json file and place it in the src/main/resources directory of your project.

### 3. Build the Project
Navigate to the root directory of the project and run the following command to build the project:
-    mvn clean install

Running the Application
Ensure you have placed your credentials.json file in src/main/resources.
Run the Main class in your IDE or use the following command to run it from the command line:
-    mvn exec:java -Dexec.mainClass="com.yourpackage.Main"

### 4. Usage
The application will prompt you to authenticate with your Google account the first time it runs.
After authentication, the application will retrieve emails from your Gmail inbox.
It will then verify the headers and the digital signatures of the emails.
Contributing
Fork the repository.
Create a new branch (git checkout -b feature-branch).
Make your changes.
Commit your changes (git commit -am 'Add new feature').
Push to the branch (git push origin feature-branch).
Create a new Pull Request.

### 5. License
This project is licensed under the MIT License - see the LICENSE file for details.

### 6. Acknowledgments
Google API Client Library for Java
JavaMail API
Bouncy Castle

### 7. Explicación de las Secciones del README:

- **Título y Descripción**: Proporciona una breve descripción del propósito de la aplicación.
- **Características**: Lista las funcionalidades principales de la aplicación.
- **Prerrequisitos**: Detalla las herramientas y configuraciones necesarias antes de configurar el proyecto.
- **Configuración**: Incluye pasos detallados sobre cómo configurar y ejecutar el proyecto.
- **Ejecución de la Aplicación**: Instrucciones sobre cómo ejecutar la aplicación.
- **Uso**: Explicación sobre cómo utilizar la aplicación después de la configuración inicial.
- **Contribución**: Instrucciones sobre cómo contribuir al proyecto.
- **Licencia**: Información sobre la licencia del proyecto.
- **Agradecimientos**: Reconocimientos a las bibliotecas y recursos utilizados.

Este README proporciona toda la información necesaria para que otros desarrolladores puedan configurar, ejecutar y contribuir a tu proyecto.

