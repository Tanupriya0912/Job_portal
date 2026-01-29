// MongoDB initialization script
// Creates 4 separate databases for each microservice

db = db.getSiblingDB('admin');

// Authenticate as root
db.auth('admin', 'admin123');

// Create auth-db
db = db.getSiblingDB('auth-db');
db.createCollection('users');
db.users.createIndex({ email: 1 }, { unique: true });

// Create user-db
db = db.getSiblingDB('user-db');
db.createCollection('users');
db.users.createIndex({ email: 1 }, { unique: true });

// Create job-db
db = db.getSiblingDB('job-db');
db.createCollection('jobs');
db.jobs.createIndex({ createdBy: 1 });
db.jobs.createIndex({ createdAt: -1 });

// Create application-db
db = db.getSiblingDB('application-db');
db.createCollection('applications');
db.applications.createIndex({ jobId: 1, applicantId: 1 }, { unique: true });
db.applications.createIndex({ recruiterId: 1 });
db.applications.createIndex({ applicantId: 1 });

print('MongoDB initialization complete: Created 4 databases with indexes');
