# Task 1 Brief: Enrich ChartUiResponseDTO, update ChartOrchestrationService, and remove /comprehensive endpoint

**Goal:** Consolidate backend DTOs so `/api/v1/astrology/calculate` returns all horoscope data with explicit named chart properties (`d1Chart`, `d9Chart`, `bhavaChart`) and remove the redundant `/comprehensive` endpoint.

### Files to Modify
- `src/main/java/org/vedic/astro/dto/ChartUiResponseDTO.java`
- `src/main/java/org/vedic/astro/service/ChartOrchestrationService.java`
- `src/main/java/org/vedic/astro/controller/ChartController.java`

### Step 1: Update `ChartUiResponseDTO.java`
Add fields:
- `latitude` (double)
- `longitude` (double)
- `resolvedTimezone` (String)
- `d1Chart` (`List<ChartResponseDTO.PositionDetail>`)
- `d9Chart` (`List<ChartResponseDTO.PositionDetail>`)
- `bhavaChart` (`List<ChartResponseDTO.PositionDetail>`)
- `shadbalaStrengths` (`ShadbalaDTO`)
- `structuralDiagnostics` (`DiagnosticsDTO`)

### Step 2: Update `ChartOrchestrationService.java`
In `convertToUiDashboardResponse(ChartResult res, BirthDetailsDTO pay)`:
Populate `latitude`, `longitude`, `resolvedTimezone`, `d1Chart` (compiled with dNo=1), `d9Chart` (compiled with dNo=9), `bhavaChart` (compiled with dNo=-1), `currentDasaTimeline`, `shadbalaStrengths`, and `structuralDiagnostics`.

### Step 3: Remove `/comprehensive` endpoint from `ChartController.java`
Delete the `@PostMapping(path = "/comprehensive" ...)` method from `ChartController.java`.

### Step 4: Verification
Run `mvn compile` in `d:\Intellij_WS\java-astro` and ensure it succeeds.

### Step 5: Commit
Commit changes with message: `refactor: enrich ChartUiResponseDTO and remove redundant /comprehensive endpoint`
