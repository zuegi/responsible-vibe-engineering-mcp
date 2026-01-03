# Configuration Guide

## LLM Configuration

The application requires LLM configuration for AI integration.
Supports multiple providers: Azure OpenAI, OpenAI, Anthropic, and custom gateways.

### Setup

1. **Copy the example configuration**:
   ```bash
   cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
   ```

2. **Edit `application-local.yml`** with your actual values:
   ```yaml
   llm:
     provider: azure-openai  # or "openai", "anthropic", "custom"
     base-url: https://your-gateway.example.com/openai/deployments/gpt-4o/
     api-version: 2024-05-01-preview
     api-token: your-token-or-dummy
   ```

3. **Verify gitignore**: 
   - `src/main/resources/application-local.yml` is already in `.gitignore`
   - This file will NOT be committed to git

### Environment Variables (Alternative)

You can also configure via environment variables:

```bash
export LLM_PROVIDER="azure-openai"  # or "openai", "anthropic", etc.
export LLM_BASE_URL="https://your-gateway.example.com/..."
export LLM_API_VERSION="2024-05-01-preview"
export LLM_API_TOKEN="your-token"
```

### Spring Profiles

For local development, you can create `application-local.yml` in `src/main/resources/`:

```yaml
llm:
  provider: azure-openai
  base-url: https://your-gateway.example.com/...
  api-version: 2024-05-01-preview
  api-token: dummy
```

Then activate with:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## CI/CD Testing

### GitHub Actions

The CI build excludes LLM integration tests since they require credentials:

```yaml
# .github/workflows/build.yml
- name: Build with Maven
  run: mvn clean verify -Dtest='!KoogIntegrationTest,!SimpleLLMConnectionTest'
```

This ensures:
- ✅ Unit and integration tests run in CI (without LLM)
- ✅ No LLM credentials needed in CI
- ✅ Fast CI builds

### Local Testing

With `application-local.yml` configured:
- All tests run automatically including LLM integration tests
- No profile specification needed (auto-activated via `src/test/resources/application.yml`)

```bash
# Run all tests (including LLM integration)
mvn test

# Run without LLM integration tests
mvn test -Dtest='!KoogIntegrationTest,!SimpleLLMConnectionTest'
```

**Test Files:** 15 test classes covering:
- Domain model tests (ProcessExecution, EngineeringProcess, ExecutionContext, etc.)
- Tool tests (QuestionCatalog, CreateFile, etc.)
- Integration tests (McpProtocol, Koog, SimpleEndToEnd)
- E2E tests (McpClientE2E, InteractionContext)

## Security Notes

⚠️ **NEVER commit**:
- `src/main/resources/application-local.yml`

This file contains sensitive URLs and credentials that should not be public.

✅ **Safe to commit**:
- `application.yml` (uses environment variable placeholders)
- `application-local.yml.example` (template with placeholder values)
