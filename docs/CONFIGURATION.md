# Configuration Guide

## Azure OpenAI Configuration

The application requires Azure OpenAI configuration for LLM integration.

### Setup

1. **Copy the example configuration**:
   ```bash
   cp src/test/resources/application-test.yml.example src/test/resources/application-test.yml
   ```

2. **Edit `application-test.yml`** with your actual values:
   ```yaml
   azure:
     openai:
       base-url: https://your-gateway.example.com/openai/deployments/gpt-4o/
       api-version: 2024-05-01-preview
       api-token: your-token-or-dummy
   ```

3. **Verify gitignore**: 
   - `src/test/resources/application-test.yml` is already in `.gitignore`
   - This file will NOT be committed to git

### Environment Variables (Alternative)

You can also configure via environment variables:

```bash
export AZURE_OPENAI_BASE_URL="https://your-gateway.example.com/..."
export AZURE_OPENAI_API_VERSION="2024-05-01-preview"
export AZURE_OPENAI_API_TOKEN="your-token"
```

### Spring Profiles

For local development, you can create `application-local.yml` in `src/main/resources/`:

```yaml
azure:
  openai:
    base-url: https://your-gateway.example.com/...
    api-version: 2024-05-01-preview
    api-token: dummy
```

Then activate with:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## Security Notes

⚠️ **NEVER commit**:
- `application-local.yml`
- `src/test/resources/application-test.yml`

These files contain sensitive URLs and credentials that should not be public.

✅ **Safe to commit**:
- `application.yml` (uses environment variable placeholders)
- `application-test.yml.example` (template with placeholder values)
