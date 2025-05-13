# Enhanced Employee Payroll Management System

An advanced employee payroll management system with facial recognition attendance tracking and automatic payroll deductions.

## New Features

1. **Facial Recognition Attendance**
   - Automated attendance tracking using facial recognition
   - Real-time face detection and verification
   - Support for multiple face samples per employee
   - Attendance status tracking (Present, Late, Absent)

2. **Automatic Payroll Deductions**
   - Automatic calculation of deductions based on attendance
   - Configurable deduction amounts for absences and late arrivals
   - Integration with existing payroll system

## Requirements

- SQLyog
- Java JDK 8 or higher
- MySQL 5.0 or higher
- OpenCV 4.5.4 or higher
- Python dependencies (see requirements.txt)

## Installation

1. Install required dependencies:
   ```bash
   pip install -r requirements.txt
   ```

2. Import database schema:
   ```bash
   mysql -u your_username -p your_database < employee_payroll.sql
   mysql -u your_username -p your_database < attendance_tables.sql
   ```

3. Configure database connection in `db.java`

## Usage

1. **Employee Face Enrollment**
   - Go to Attendance > Enroll Employee Face
   - Enter employee ID
   - Capture multiple face samples
   - Save and train the model

2. **Daily Attendance**
   - Go to Attendance > Face Recognition Attendance
   - Start recognition
   - System automatically marks attendance when face is recognized

3. **Payroll Processing**
   - System automatically calculates deductions based on attendance
   - View deductions in the Deductions menu
   - Final salary is adjusted based on attendance records

## Configuration

Attendance settings can be configured in the database:
- Work start time
- Work end time
- Late threshold (minutes)
- Absent deduction amount
- Late arrival deduction amount

## Security

- Face data is securely stored in the database
- Attendance records are tamper-proof
- System logs all verification methods
