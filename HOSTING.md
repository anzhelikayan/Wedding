# Hosting

## What is already configured

- Guest answers are saved to the database.
- Admin page: `/admin`
- CSV export: `/admin/responses.csv`
- Health check: `/health`
- Docker deploy is supported through `Dockerfile`.
- Local development uses H2 at `./data/wedding-rsvp`.
- Production should use PostgreSQL.

## Admin password

Local default:

```text
admin123
```

On hosting, set:

```text
ADMIN_PASSWORD=your-strong-password
```

Then open:

```text
https://your-site/admin
```

or:

```text
https://your-site/admin?key=your-strong-password
```

## Production environment variables

Minimum:

```text
ADMIN_PASSWORD=your-strong-password
H2_CONSOLE_ENABLED=false
```

Database, option A:

```text
DATABASE_URL=postgres://user:password@host:5432/database
```

Database, option B:

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/database
SPRING_DATASOURCE_USERNAME=user
SPRING_DATASOURCE_PASSWORD=password
```

## Recommended host

Use Railway or Render with PostgreSQL.

## Railway database

Do not rely on the local H2 file database in production. It can disappear after a Railway restart, redeploy, or container move.

On Railway:

1. Add a PostgreSQL service to the project.
2. Open the Wedding web service.
3. Go to `Variables`.
4. Add this variable:

```text
DATABASE_URL=${{Postgres.DATABASE_URL}}
```

Keep:

```text
ADMIN_PASSWORD=your-strong-password
H2_CONSOLE_ENABLED=false
```

After redeploy, guest answers will be stored in PostgreSQL and will survive future deploys.

High-level steps:

1. Push this project to GitHub.
2. Create a PostgreSQL database on the hosting platform.
3. Create a Web Service from the GitHub repo.
4. Use Dockerfile deploy if asked.
5. Set the environment variables above.
6. Set health check path to `/health`.
7. Open the public URL and test the RSVP form.
8. Open `/admin` and check that the answer appeared.
