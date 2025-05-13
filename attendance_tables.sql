-- Add attendance related tables

-- Table for storing employee face data
CREATE TABLE IF NOT EXISTS `employee_face_data` (
  `face_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `emp_id` int(10) unsigned NOT NULL,
  `face_encoding` LONGBLOB NOT NULL,
  `date_added` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`face_id`),
  FOREIGN KEY (`emp_id`) REFERENCES `employee`(`emp_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table for storing attendance records
CREATE TABLE IF NOT EXISTS `attendance` (
  `attendance_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `emp_id` int(10) unsigned NOT NULL,
  `date` DATE NOT NULL,
  `time_in` TIME,
  `time_out` TIME,
  `status` ENUM('Present', 'Absent', 'Late') NOT NULL,
  `verification_method` ENUM('Face', 'Manual') NOT NULL DEFAULT 'Face',
  PRIMARY KEY (`attendance_id`),
  FOREIGN KEY (`emp_id`) REFERENCES `employee`(`emp_id`),
  UNIQUE KEY `unique_daily_attendance` (`emp_id`, `date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table for attendance settings
CREATE TABLE IF NOT EXISTS `attendance_settings` (
  `setting_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `work_start_time` TIME NOT NULL DEFAULT '09:00:00',
  `work_end_time` TIME NOT NULL DEFAULT '17:00:00',
  `late_threshold_minutes` int NOT NULL DEFAULT 15,
  `absent_deduction_amount` DECIMAL(10,2) NOT NULL DEFAULT 500.00,
  `late_deduction_amount` DECIMAL(10,2) NOT NULL DEFAULT 100.00,
  PRIMARY KEY (`setting_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert default attendance settings
INSERT INTO `attendance_settings` 
(`work_start_time`, `work_end_time`, `late_threshold_minutes`, `absent_deduction_amount`, `late_deduction_amount`)
VALUES 
('09:00:00', '17:00:00', 15, 500.00, 100.00);
