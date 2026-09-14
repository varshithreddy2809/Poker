# Phase 1 database setup

Run `phase-1-schema.sql` once against MySQL 8.0+ to create the
`card_game_platform` database and its Phase 1 tables.

Spring Boot reads the database connection from environment variables:

| Variable | Default | Description |
| --- | --- | --- |
| `CARD_GAME_DB_URL` | `jdbc:mysql://localhost:3306/card_game_platform?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata` | JDBC connection URL |
| `CARD_GAME_DB_USERNAME` | `root` | MySQL username |
| `CARD_GAME_DB_PASSWORD` | none | MySQL password; required for a password-protected account |

For the current PowerShell session, set the password before starting the app:

```powershell
$env:CARD_GAME_DB_PASSWORD = 'your-mysql-password'
.\mvnw.cmd spring-boot:run
```

Expected successful startup output includes a completed Spring Boot start and
no Hibernate schema-validation errors. Do not add database credentials to this
repository or `application.properties`.
