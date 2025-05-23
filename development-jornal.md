# Hall Management System Development Journal

## 2023-07-10 - Project Initialization

Starting work on the Hall Booking Management System according to the requirements specification. The project will be implemented using:

- JavaFX for the GUI
- JFoenix for enhanced UI components
- Text files for data storage
- Maven for dependency management

Initial project structure has been set up with a basic Maven configuration. The pom.xml already includes JavaFX dependencies, but we need to add JFoenix.

### Tasks Completed:

- Initial review of project requirements
- Assessment of existing project structure

### Next Steps:

- Complete Task 1.1: Add JFoenix dependency
- Complete Task 1.2: Set up the required file structure

## 2023-07-11 - Core Implementation

Implemented the core components of the Hall Booking Management System:

### Tasks Completed:

1. **Project Setup**

   - Added JFoenix and BCrypt dependencies to pom.xml
   - Updated module-info.java to include required modules
   - Created the directory structure for MVC pattern

2. **Core Data Models**

   - Created User model with all required fields
   - Created Hall model with HallType enum
   - Created Booking model with BookingStatus enum
   - Created Issue model with IssueStatus enum
   - Implemented toDelimitedString() and fromDelimitedString() methods for all models

3. **File Handling**

   - Created FileConstants class for file path definitions
   - Implemented FileHandler utility with proper file locking using FileChannel
   - Added Task-based asynchronous file operations to prevent UI freezing

4. **Authentication Module**
   - Created SessionManager singleton for tracking the current user
   - Implemented AuthenticationService for login and registration

### Next Steps:

- Implement UI components for login and registration
- Create hall management functionality
- Develop booking workflow

## 2023-07-12 - Service Layer Implementation

Continued implementation of the Hall Booking Management System with a focus on the service layer:

### Tasks Completed:

1. **Service Layer**

   - Created HallService for managing halls (add, update, delete, search)
   - Implemented BookingService with business logic for hall availability and booking operations
   - Added TaskUtils class to handle asynchronous operations properly

2. **Performance Optimizations**
   - Implemented file locking with FileChannel to ensure data consistency
   - Created Task-based operations to prevent UI freezing
   - Added utility methods for executing Tasks synchronously when needed

### Next Steps:

- Implement Issue management functionality
- Create UI components for hall management
- Develop user management features for administrators

## 2023-07-13 - Testing and Validation

Completed the implementation of all service classes and performed testing:

### Tasks Completed:

1. **Service Layer Completion**

   - Implemented IssueService for managing issues (create, update, assign, search)
   - Fixed issues with the TaskUtils class for proper asynchronous execution

2. **Testing**

   - Created a simple console application to test the implementation
   - Successfully tested hall creation and retrieval
   - Verified file operations with proper locking

3. **Validation**
   - Confirmed that the data models work correctly with the delimited string format
   - Verified that the file handling utilities properly create and manage data files
   - Tested the service layer with sample data

### Next Steps:

- Implement the UI components using JavaFX and JFoenix
- Create controllers for connecting the UI to the services
- Develop the navigation system for different user roles

## 2023-07-14 - UI Development

Implemented UI components for user authentication:

### Tasks Completed:

1. **Authentication UI**

   - Created Login.fxml with JFoenix components for user login
   - Implemented Registration.fxml for new user registration
   - Added controllers for both screens with proper validation and error handling
   - Connected UI to the authentication service layer

2. **Dashboard Scaffolding**

   - Created basic dashboard screens for different user roles (Admin, Manager, Scheduler, Customer)
   - Set up navigation between screens based on user role

3. **Security Enhancements**

   - Added input validation for login and registration forms
   - Implemented email format validation using regex
   - Added password strength requirements

4. **Application Entry Point**

   - Updated the main App class to start with the login screen
   - Updated module-info.java to properly expose controllers to JavaFX

### Next Steps:

- Implement Hall management UI for Scheduler role
- Develop Booking workflow UI for Customer role
- Create Admin features for user management
- Build Manager dashboard with reporting features

## 2023-07-15 - Hall Management Implementation

Implemented hall management functionality for the Scheduler role:

### Tasks Completed:

1. **Hall Management UI**

   - Created HallManagement.fxml with a form for adding/editing halls and a table for displaying existing halls
   - Implemented CRUD operations for halls (Create, Read, Update, Delete)
   - Added validation for hall data input
   - Connected UI to the HallService

2. **Scheduler Dashboard**

   - Created SchedulerDashboardController to handle navigation
   - Added button to navigate to the Hall Management screen
   - Implemented logout functionality to return to login screen

3. **Validation and Error Handling**

   - Added input validation for hall capacity and rate
   - Implemented error messages for various scenarios (failed operations, invalid inputs)
   - Added proper form clearing and state management

### Next Steps:

- Implement Availability Scheduling for halls
- Develop Maintenance Scheduling functionality
- Build Booking workflow for customers
- Implement User Management for administrators

## 2023-05-22 - Build Configuration Fix

Fixed the Maven build configuration to properly execute the application:

### Tasks Completed:

1. **Maven Configuration Update**

   - Added the exec-maven-plugin to the pom.xml with proper mainClass configuration
   - Set com.group4.App as the main class for execution
   - Fixed the "missing or invalid mainClass parameter" error

2. **JavaFX Compatibility Fix**

   - Updated JavaFX dependencies from version 13 to version 20
   - Added mac-aarch64 classifier to JavaFX dependencies for Apple Silicon compatibility
   - Fixed the architecture compatibility issue with JavaFX libraries

3. **JFoenix Compatibility Fix**
   - Added VM arguments to open the Java reflection API to JFoenix for both unnamed and named modules
   - Updated javafx-maven-plugin and exec-maven-plugin configurations with appropriate VM arguments
   - Added logging configuration for better debugging of JavaFX and JFoenix issues
   - Added commandlineArgs option for direct VM args in exec-maven-plugin

### Next Steps:

- Continue implementing UI components
- Test the application execution using Maven
- Implement remaining features according to requirements

## 2023-05-22 - Testing with ConsoleApp

After resolving build configuration issues, we continued testing the application:

### Tasks Completed:

1. **Build System Updates**

   - Updated to JavaFX version 17.0.6 for better compatibility with Java modules
   - Updated javafx-maven-plugin to version 0.0.8 for improved stability
   - Simplified the Maven configuration for easier maintenance

2. **Testing Outcomes**

   - Successfully ran the ConsoleApp which demonstrated core functionality working correctly
   - Verified that hall creation and retrieval operations are working properly
   - Data persistence to file system was confirmed working

3. **Ongoing Challenges**
   - The main JavaFX GUI application still has compatibility issues with JFoenix on Apple Silicon
   - NSInternalInconsistencyException occurred when trying to run the full GUI application
   - These issues appear to be related to macOS-specific JavaFX rendering on Apple Silicon

### Next Steps:

- Continue to use ConsoleApp for core functionality testing
- Investigate alternative UI component libraries to replace JFoenix
- Consider updating the application to use JavaFX without JFoenix for Silicon Mac compatibility
- Implement remaining business logic features independently of UI issues

## 2023-05-24 - Hall Availability and Maintenance Management

Implemented hall availability and maintenance management features:

### Tasks Completed:

1. **Availability Model and Service**

   - Created Availability model class for representing hall availability periods
   - Implemented AvailabilityService with methods for managing availability and maintenance schedules
   - Added conflict detection to prevent double-booking of halls
   - Ensured all file operations use proper locking and run in background threads

2. **Availability Management UI**

   - Created AvailabilityController for managing hall availability
   - Implemented Availability.fxml with form for adding/editing availability periods
   - Added table view for displaying existing availability entries
   - Implemented CRUD operations with proper validation

3. **Maintenance Management**

   - Created MaintenanceController for scheduling hall maintenance
   - Implemented Maintenance.fxml with specialized UI for maintenance scheduling
   - Added functionality to mark halls as unavailable during maintenance periods
   - Ensured maintenance schedules are properly stored and retrieved

4. **Performance Optimizations**
   - Used Task-based asynchronous operations for all file I/O
   - Implemented proper error handling and user feedback
   - Added validation for date/time inputs to prevent invalid schedules

### Next Steps:

- Implement integration with Booking system to check availability
- Develop Customer booking workflow
- Create Administrator features for user management
- Build Manager dashboard with reporting capabilities

## 2023-05-25 - Customer Booking Workflow Implementation

Implemented the customer booking features according to the requirements:

### Tasks Completed:

1. **Hall Search & Filter**

   - Created HallBookingController for managing hall search and booking
   - Implemented HallBooking.fxml with filters for date, hall type, and capacity
   - Added table view for displaying filtered halls
   - Integrated with the availability system to only show available halls for the selected date

2. **Booking Workflow**

   - Created BookingConfirmationController for booking process
   - Implemented BookingConfirmation.fxml with date/time selection and cost calculation
   - Added validation to ensure bookings are within business hours (8 AM - 6 PM)
   - Integrated with the BookingService to create booking entries

3. **Receipt Generation**

   - Created ReceiptController for displaying booking receipts
   - Implemented Receipt.fxml with a detailed view of the booking information
   - Added print functionality for generating physical receipts
   - Ensured proper formatting of dates and costs

4. **Integration with Existing Services**
   - Connected availability checking to the booking process
   - Used Task-based operations for all database interactions
   - Added proper error handling and user feedback
   - Implemented validation to prevent invalid bookings

### Next Steps:

- Implement booking cancellation feature
- Create Administrator features for user management
- Build Manager dashboard with reporting capabilities
- Add issue management functionality

## 2023-05-26 - System Integration and Usability Improvements

Enhanced system integration and usability across the customer booking workflow:

### Tasks Completed:

1. **Booking System Integration Finalization**

   - Connected all components of the booking workflow (search, confirmation, receipt)
   - Implemented proper state management between screens
   - Enhanced error handling for booking conflicts and validation issues
   - Added confirmation dialogs before finalizing bookings

2. **Usability Enhancements**

   - Improved UI responsiveness with background processing for long operations
   - Added progress indicators during file operations
   - Enhanced search filters with dynamic updating
   - Implemented clearer validation error messages

3. **Data Consistency Checks**

   - Added cross-validation between availability and bookings
   - Implemented transaction-like patterns for booking operations
   - Added data recovery mechanisms for interrupted operations
   - Enhanced atomic operations using FileChannel

4. **Testing and Validation**
   - Conducted user acceptance testing on the booking workflow
   - Fixed edge cases in date/time selection
   - Ensured proper handling of concurrent booking attempts
   - Verified consistency of booking data with hall availability

### Next Steps:

- Implement booking cancellation functionality
- Develop administrator panel for user account management
- Create manager reporting dashboard with analytics
- Build issue reporting and tracking system

## 2023-05-27 - User Dashboard Implementation and System Integration

Implemented user dashboards and completed system integration:

### Tasks Completed:

1. **Customer Dashboard Implementation**

   - Created CustomerDashboardController for managing customer bookings
   - Implemented booking cancellation feature with 3-day policy enforcement
   - Added TableView for displaying customer's bookings with status
   - Connected to BookingService for retrieving and canceling bookings

2. **Admin Dashboard Implementation**

   - Created AdminDashboardController for user management
   - Implemented AdminService for CRUD operations on users
   - Added user filtering by role (Admin, Manager, Scheduler, Customer)
   - Implemented user blocking/unblocking functionality

3. **Manager Dashboard Implementation**

   - Created ManagerDashboardController with tabbed interface
   - Implemented sales reporting with BarChart visualization
   - Added period filtering (Weekly, Monthly, Yearly) for sales data
   - Implemented issue management with assignment to staff

4. **Data Integration**

   - Created sample data for testing all features
   - Ensured proper file structure for data persistence
   - Connected all controllers to appropriate service classes
   - Implemented proper navigation between screens

### Next Steps:

- Implement additional validation for edge cases
- Add more detailed reporting features
- Enhance UI with responsive design
- Implement comprehensive error handling

## 2023-05-28 - Security Enhancements and Testing Framework

Implemented several important security and quality improvements to the Hall Management System:

### Tasks Completed:

1. **Password Security Implementation**

   - Updated the AuthenticationService to use BCrypt for password hashing and verification
   - Modified the registration process to properly hash passwords before storing
   - Added BCrypt dependency in the JUnit classpath for testing
   - Updated the login process to verify passwords using BCrypt.checkpw()

2. **Custom Exception Classes**

   - Created BookingConflictException class for handling booking conflicts
   - Implemented InvalidInputException for better validation error handling
   - Added proper exception documentation with javadoc comments

3. **File Permission Controls**

   - Created FilePermissionManager class to handle role-based file access
   - Implemented methods to check read/write permissions based on user roles
   - Updated FileHandler to use the FilePermissionManager for access control
   - Added proper file permission checks before read/write operations

4. **Test Framework**

   - Set up JUnit testing infrastructure with Maven configuration
   - Created UserAuthenticationTest for testing login and registration
   - Implemented BookingCalculatorTest for validating cost calculations
   - Added test data handling with proper setup and teardown

### Next Steps:

- Integrate the custom exceptions into existing controllers
- Complete comprehensive test coverage for all service classes
- Implement UI feedback for permission denied scenarios
- Add logging for security-related events and file operations
