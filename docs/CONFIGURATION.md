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

## Security Notes

⚠️ **NEVER commit**:
- `src/main/resources/application-local.yml`

This file contains sensitive URLs and credentials that should not be public.

✅ **Safe to commit**:
- `application.yml` (uses environment variable placeholders)
- `application-local.yml.example` (template with placeholder values)
