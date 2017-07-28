-- Inserting 7 new jobs
INSERT INTO `jobs` 
(`JOB_ID`, `JOB_NAME`, `STATUS`)
VALUES 
(1, 'Job 1', 1),
(2, 'Job 2', 1),
(3, 'Job 3', 1),
(4, 'Job 4', 1),
(5, 'Job 5', 1),
(6, 'Job 6', 1),
(7, 'Job 7', 1);

-- All the above 7 jobs will have same job watcher
INSERT INTO `job_watchers`
(`JOB_ID`, `NAME`, `EMAIL`)
VALUES
(1, 'Test User', 'test@gmail.com'),
(2, 'Test User', 'test@gmail.com'),
(3, 'Test User', 'test@gmail.com'),
(4, 'Test User', 'test@gmail.com'),
(5, 'Test User', 'test@gmail.com'),
(6, 'Test User', 'test@gmail.com'),
(7, 'Test User', 'test@gmail.com');

-- All the 7 job will have a simple shell command to echo their name
INSERT INTO `executables`
(`SCRIPT`,`JOB_ID`)
VALUES
('echo I am Job 1', 1),
('echo I am Job 2', 2),
('echo I am Job 3', 3),
('echo I am Job 4', 4),
('echo I am Job 5', 5),
('echo I am Job 6', 6),
('echo I am Job 7', 7);

-- Inserting dependencies
-- Job 1 => Job 3
-- Job 2 => Job 3
-- Job 3 => Job 4
-- Job 5 => Job 1
-- Job 6 => Job 7
INSERT INTO `job_dependencies`
(`JOB_ID`, `DEPENDANT_JOB_ID`)
VALUES 
(1, 3),
(2, 3),
(3, 4),
(5, 1),
(6, 7);
