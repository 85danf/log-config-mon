# CloudWatch log exfiltration config monitor 



---
### REST API
`POST api/v1/config/flowlogs` --> creates a config object and persists it to db

`POST api/v1/validate`        --> runs a validation cycle by looking up all 'stale' config objects and validating them

`GET api/v1/health`           --> healthcheck

---

### Running Examples

##### Running Locally
` mvn spring-boot:run`

##### Running Container
- A pre-built container is also available at docker hub under `feldan/configmon:0.9`
- `docker run -d --rm --name configmon -p8080:8080 feldan/configmon:0.9`

---

#### Building
- Clone this repo
- Run `mvn clean install` (also runs tests)

#### Running Tests
- Run `mvn test`

#### Building a Docker Container
- `mvn clean install`
- `docker build -t configmon:0.9 .` (or any other image/tag combo)
