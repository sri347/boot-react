# Gemini Deep Research

A comprehensive research automation platform that processes prompts through Google's Gemini AI and sends notifications via email, SMS, and WhatsApp when research reports are ready. The platform features a flexible template system for creating and reusing research prompts with customizable variables.

## Features

- Submit research prompts through a web interface
- Create, edit, and use templates with customizable variables for research prompts
- Import prompts from text files
- Import prompts from Google Sheets
- Process prompts through Google's Gemini AI for deep research
- Receive notifications via email, SMS, or WhatsApp when reports are ready
- Manage all external API configurations through an admin interface
- View and manage research prompts in the admin dashboard

## Technology Stack

- Java 17
- Spring Boot 3.x
- Thymeleaf templates
- PostgreSQL database
- Bootstrap 5 CSS framework

## Getting Started

### Prerequisites

- Java 17 or higher
- PostgreSQL database
- Internet connection for downloading dependencies (the Maven wrapper is included)

### Installation

1. Clone the repository:
   ```
   git clone https://github.com/yourusername/gemini-deep-research.git
   ```

2. Navigate to the project directory:
   ```
   cd gemini-deep-research
   ```

3. Configure your database:
   - The application uses PostgreSQL by default
   - Create a PostgreSQL database for the application
   - Update `src/main/resources/application.properties` with your database details:
     ```
     spring.datasource.url=jdbc:postgresql://localhost:5432/gemini_research
     spring.datasource.username=your_username
     spring.datasource.password=your_password
     ```

4. Build the project using the Maven wrapper (no need to install Maven separately):
   ```
   ./mvnw clean install    # On Linux/Mac
   mvnw.cmd clean install  # On Windows
   ```

5. Run the application:
   ```
   ./mvnw spring-boot:run    # On Linux/Mac
   mvnw.cmd spring-boot:run  # On Windows
   ```

The application will start on http://localhost:8080

Note: On first run, the application will automatically create all required database tables.

### Testing Your Installation

1. Access the application at http://localhost:8080
2. You should see the home page with options to submit research prompts
3. Navigate to the Admin section to configure your API credentials 
4. For initial testing without APIs configured:
   - You can create and manage templates
   - You can submit prompts (they will remain in PENDING status)
   - You can explore the UI and functionalities

Note: Full functionality requires configuring the external APIs as described below.

## Setting Up API Credentials

This application uses database-stored credentials instead of environment variables. Use the admin interface to configure all external services.

### Gemini API

1. Go to https://ai.google.dev/ (Google AI Studio)
2. Sign in or create a Google account
3. Navigate to "API Keys" in the menu
4. Click "Create API Key" and follow the instructions
5. Copy the generated API key

In the application:
1. Navigate to Admin > API Configuration
2. Click "Add New Configuration" or the "Add Gemini API Configuration" button
3. Select "GEMINI" as the Configuration Type
4. Paste your API key (format example: `AIzaSyC...` - typically starts with "AIza")
5. Click "Save Configuration"

### Email Notifications

For Gmail:

1. Create or use an existing Gmail account
2. Enable 2-Step Verification in your Google account
3. Generate an App Password:
   - Go to your Google Account > Security > App Passwords
   - Select "Mail" and "Other" (custom name)
   - Copy the 16-character password generated

In the application:
1. Navigate to Admin > API Configuration
2. Click "Add New Configuration" or the "Add Email Configuration" button
3. Select "EMAIL" as the Configuration Type
4. Enter your Gmail address as Username
5. Enter the 16-character App Password as Password
6. Click "Save Configuration"

### SMS Notifications (SignalWire)

This application uses SignalWire as an open-source alternative to Twilio:

1. Go to https://signalwire.com/ and create an account
2. Create a new project
3. Buy a phone number for sending SMS
4. Collect the following credentials:
   - Project ID (format: UUID like `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`)
   - Space URL (your SignalWire subdomain, e.g., `example.signalwire.com`)
   - API Token (format: long string starting with `PT` followed by alphanumeric characters)
   - Phone Number (format: `+1234567890` with country code)

In the application:
1. Navigate to Admin > API Configuration
2. Click "Add New Configuration" or the "Add SMS Configuration" button
3. Select "SMS" as the Configuration Type
4. Fill in all the required fields with the formats shown above
5. Click "Save Configuration"

### Google Sheets Integration

1. Go to https://console.cloud.google.com/
2. Create a new project
3. Enable the Google Sheets API
4. Create API credentials - you can use either:

**Option 1: API Key (simplest)**
1. Create an API key in Google Cloud Console
2. Restrict the key to Google Sheets API only

**Option 2: OAuth 2.0 (for more complex access)**
1. Create OAuth 2.0 Client ID credentials
2. Configure the OAuth consent screen
3. Generate a refresh token using the OAuth flow

In the application:
1. Navigate to Admin > API Configuration
2. Click "Add New Configuration" or the "Add Google Sheets Configuration" button
3. Select "GOOGLE_SHEETS" as the Configuration Type
4. For API Key option: Enter your key in the API Key field
5. For OAuth option: Enter Client ID, Client Secret, and Refresh Token
6. Click "Save Configuration"

## Usage

### Submitting Research Prompts

1. Go to the home page
2. Enter your research prompt in the form
3. Optionally, enable email, SMS, or WhatsApp notifications
4. Click "Submit"

### Working with Templates

#### Creating Templates
1. Go to the Templates page
2. Click "Create New Template"
3. Fill in the template details:
   - Name: A descriptive name for your template
   - Category: Optional grouping for your templates
   - Description: What the template is used for
   - Template Content: The prompt text with placeholders in the format `{{variable_name}}`
   - Placeholder Format: Optional custom format for placeholders (default is `{{%s}}`)
   - Created By: Your name or identifier
   - Public: Toggle to make the template available to all users
4. Click "Validate Placeholders" to check your template
5. Click "Preview" to see how the template will look with example values
6. Click "Save Template"

#### Using Templates
1. Go to the Templates page
2. Browse templates or use search/filter functionality
3. Click "Use Template" on the desired template
4. Fill in the values for each placeholder
5. Add notification settings if desired
6. Click "Submit Prompt"

#### Example Template
Template content example:
```
Research the impact of {{technology}} on {{industry}} over the last {{timeframe}}. 
Focus on:
1. Major innovations and breakthroughs
2. Economic implications
3. Future trends and predictions
4. Challenges and limitations
5. Case studies of companies that have successfully implemented {{technology}}
```

### Importing Prompts from Files

1. Go to the home page
2. In the "Batch Processing" section, select "Upload Text File"
3. Choose a text file with one prompt per line
4. Optionally, add notification settings
5. Click "Upload"

### Importing Prompts from Google Sheets

1. Go to the home page
2. In the "Batch Processing" section, select "Google Sheets Import"
3. Enter the Spreadsheet ID and range (e.g., "Sheet1!A2:A10")
4. Optionally, add notification settings
5. Click "Import"

### Managing Prompts in Admin Dashboard

1. Go to the Admin Dashboard
2. View all prompts, filter by status, or search
3. Click "Process" to manually process pending prompts
4. Click "View" to see full details of a prompt and its results

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.