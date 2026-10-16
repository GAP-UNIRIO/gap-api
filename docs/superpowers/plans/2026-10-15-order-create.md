# Order Create Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. The user requested parallelization where possible; the isolated worktree is outside the agent write boundary, so execution proceeds natively to protect the original checkout. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement order creation with individually pre-signed temporary S3 uploads, final attachment storage, and persisted approval stages.

**Architecture:** Keep S3 operations behind `IStorageService`; use an AWS S3 implementation for temporary POST policies and object transfer. `OrderService` validates the user and temporary keys, persists the order within a transaction to obtain its ID, moves files, associates annexes through the existing `Order.annexes` cascade, instantiates the existing workflow engine, and compensates completed S3 transfers when creation fails.

**Tech Stack:** Java 21, Spring Boot 4.1, Spring MVC, Spring Data JPA, Jakarta Validation, AWS SDK for Java v2, JUnit 5, Mockito, MockMvc.

**Spec:** `docs/superpowers/specs/2026-10-15-order-create-design.md`

## Global Constraints

- The production storage provider is undecided; the first implementation targets AWS S3 behind a replaceable interface.
- The frontend uploads file contents directly to external storage; file bytes do not pass through the API.
- Each upload authorization handles one attachment and returns a short-lived POST policy bound to a reserved temporary key and content type.
- Order creation accepts only temporary keys issued by the API.
- User existence is checked before any S3 move.
- Failure must not leave persisted order, annex, or approval stage data; completed S3 transfers are compensated.
- Preserve existing API authentication and response envelope conventions.
- Write and run all feature tests before implementing functional production behavior.

## Review Focus

- A signed policy must bind the exact key and content type; tampered or malformed temporary keys must be rejected before any S3 or database interaction.
- A missing user must return 404 before S3 is called and before any entity is persisted.
- If a later attachment copy fails, all earlier final objects must be deleted and no order-related data must be persisted.
- Course-specific workflow must win over the default template; stages must be ordered, pending, and assigned to the correct course unit.
- A failure after S3 movement, including missing workflow or persistence failure, must roll back database writes and compensate final objects.

---

## File Map

**Tests created before functional implementation:**

- `src/test/java/com/gap/api/Controller/OrderControllerTest.java` — endpoint paths, request validation, response envelope/status, service delegation, and service errors.
- `src/test/java/com/gap/api/Service/AWSStorageServiceTest.java` — POST policy, issued-key validation, copy/delete behavior, and S3 failure compensation.
- `src/test/java/com/gap/api/Service/OrderServiceTest.java` — order, annex, workflow orchestration and failure compensation.
- `src/test/java/com/gap/api/Integration/OrderServiceIntegrationTest.java` — PostgreSQL-backed transaction rollback after storage movement.
- `src/test/java/com/gap/api/Service/WorkflowEngineServiceTest.java` — course-specific/default template selection and exact approval stage contents.

**Production implementation after all feature tests exist:**

- `src/main/java/com/gap/api/Controller/OrderController.java` — upload authorization and order creation routes under `/orders`.
- `src/main/java/com/gap/api/Model/DTO/AnnexUploadRequest.java`, `AnnexUploadResponse.java`, `AnnexRequest.java`, `OrderRequest.java`, `OrderResponse.java` — validated API contracts. `OrderRequest` carries `userId`, `orderType`, optional `extraData`, and a list of annexes; each annex carries `name`, `contentType`, and `temporaryKey`.
- `src/main/java/com/gap/api/Service/Interface/IStorageService.java`, `IOrderService.java` — storage and order service contracts.
- `src/main/java/com/gap/api/Service/AWSStorageService.java` — AWS S3 POST policy signing, temporary key authentication, final copy, and temporary deletion.
- `src/main/java/com/gap/api/Service/OrderService.java` — transactional orchestration and S3 compensation.
- `src/main/java/com/gap/api/Repository/OrderRepository.java` — order and cascaded annex persistence.
- `src/main/java/com/gap/api/Config/S3StorageConfig.java`, `src/main/resources/application.yaml`, `application-dev.yaml`, `build.gradle` — AWS client, externalized storage settings, and AWS SDK dependency.
- `src/main/java/com/gap/api/Service/WorkflowEngineService.java` — only adjust if tests reveal required integration fixes; preserve the existing template selection behavior.
- `src/main/java/com/gap/api/Exception/GlobalExceptionHandler.java` — map missing-user and invalid-request service exceptions to the agreed HTTP response shape if needed.

## Phase 1: Write every feature test before functional implementation

### Task 1: Define and write the complete test suite

**Files:**
- Create: `src/test/java/com/gap/api/Controller/OrderControllerTest.java`
- Create: `src/test/java/com/gap/api/Service/AWSStorageServiceTest.java`
- Create: `src/test/java/com/gap/api/Service/OrderServiceTest.java`
- Create: `src/test/java/com/gap/api/Integration/OrderServiceIntegrationTest.java`
- Modify: `src/test/java/com/gap/api/Service/WorkflowEngineServiceTest.java`

**Interfaces:** Tests pin the API and service contracts described in the file map. Where a production type does not yet exist, keep tests compiling only after the contract declarations are added; do not add behavior to production code in this phase.

- [x] Add controller tests `upload_returnsSignedPostFields`, `upload_rejectsInvalidMetadata`, `upload_returns503WhenStorageUnavailable`, `create_returns201WithPersistedOrder`, `create_rejectsInvalidBody`, `create_returns404WhenUserDoesNotExist`, and `create_returns400ForInvalidTemporaryKey`.
- [x] Add storage tests `generateUploadUrl_bindsPolicyToExactKeyAndContentType`, `generateUploadUrl_includesSessionToken`, `generateUploadUrl_failsWhenConfigurationIsMissing`, `isTemporaryKeyIssued_rejectsMalformedAndTamperedKeys`, `moveToOrder_copiesToFinalKeyAndDeletesTemporaryKey`, `moveToOrder_rejectsInvalidKeyBeforeS3Calls`, `moveToOrder_doesNotDeleteSourceWhenCopyFails`, and `moveToOrder_deletesDestinationWhenTemporaryCleanupFails`.
- [x] Add order service tests `generateAnnexUploadUrl_returnsStorageAuthorization`, `create_savesOrderWithoutAnnexes`, `create_movesAndPersistsSingleAnnexAtFinalKey`, `create_movesAndPersistsEveryAnnex`, and `create_checksUserAndKeysBeforeMovingFiles`.
- [x] Add order service failure tests `create_missingUserDoesNotCallStorageOrRepositories`, `create_invalidKeyDoesNotMoveOrPersist`, `create_firstCopyFailureRollsBackAndLeavesNoFinalObject`, `create_laterCopyFailureDeletesEarlierFinalObject`, `create_temporaryCleanupFailureDoesNotPersistOrder`, `create_missingWorkflowDeletesFinalObjectsAndRollsBack`, `create_orderSaveFailureDoesNotMoveFiles`, `create_annexCascadeSaveFailureDeletesFinalObjects`, and `create_stageSaveFailureDeletesFinalObjectsAndRollsBack`.
- [x] Add PostgreSQL-backed integration test `create_workflowFailureRollsBackOrderAndAnnexesAndCleansFinalObjects` to prove that order and cascaded annex rows are absent after the transactional service fails.
- [x] Strengthen workflow tests `instantiateWorkflow_prefersCourseSpecificTemplate`, `instantiateWorkflow_fallsBackToDefaultTemplate`, `instantiateWorkflow_savesSortedPendingStagesWithResponsibleUnits`, and `instantiateWorkflow_missingTemplateDoesNotSaveStages`.
- [x] Run focused feature Gradle tests; expected red result occurred at test compilation because feature contracts and AWS SDK dependency are not yet defined. No functional production behavior was added before all tests existed.

### Task 2: Add the minimum compile-time contracts required by the tests

**Files:**
- Create: DTOs, service interfaces, repository interface, and compile-only class shells listed in the file map.
- Test: the four feature test files from Task 1.

**Interfaces:**
- `IStorageService.generateUploadUrl(String name, String contentType) -> UploadUrlResponse`
- `IStorageService.isTemporaryKeyIssued(String key) -> boolean`
- `IStorageService.moveToOrder(String temporaryKey, Long orderId, String name) -> String finalKey`
- `IStorageService.deleteObject(String key) -> void`
- `IOrderService.create(OrderRequest request) -> OrderResponse`
- `IOrderService.generateAnnexUploadUrl(AnnexUploadRequest request) -> AnnexUploadResponse`
- `OrderRequest(Long userId, Order.OrderType orderType, Map<String,Object> extraData, List<AnnexRequest> annexes)`
- `AnnexRequest(String name, String contentType, String temporaryKey)`
- `AnnexUploadRequest(String name, String contentType)`
- `AnnexUploadResponse(String uploadUrl, String fileKey, Map<String,String> fields)`
- `AnnexResponse(Long id, String name, String contentType, String storageKey)`
- `OrderResponse(Long id, Long userId, Order.OrderType orderType, ZonedDateTime createdAt, List<AnnexResponse> annexes)`

- [x] Add DTO declarations, validation annotations, AWS SDK dependency, repository/service contracts, and temporary compile shells. The shells were then replaced with functional implementations after the behavior tests were red.
- [x] Keep compile-only task free of S3 calls, database orchestration, policy signing, or workflow behavior.
- [x] Re-run focused tests after the shells; compile succeeded and behavior assertions failed as expected.

## Phase 2: Implement production behavior after all tests are present

### Task 3: Implement S3 upload authorization and storage operations

**Files:**
- Modify: `build.gradle`, application YAML files, `S3StorageConfig.java`, `AWSStorageService.java`
- Test: `AWSStorageServiceTest.java`

**Interfaces:** Consumes `IStorageService` from Task 2.

- [x] Add AWS SDK v2 S3 dependencies and configuration for region, bucket, temporary/final prefixes, and upload-key secret.
- [x] Implement an expiring SigV4 POST policy bound to bucket, exact temporary key, and content type; include the session token for temporary credentials.
- [x] Implement opaque UUID-based temporary keys authenticated with HMAC and constant-time comparison.
- [x] Implement final-key copy followed by temporary-key deletion; remove the final key if temporary deletion fails.
- [x] Run `./gradlew.bat test --tests com.gap.api.Service.AWSStorageServiceTest`; all storage scenarios pass.

### Task 4: Implement order, annex, and workflow orchestration

**Files:**
- Modify: `OrderService.java`, `WorkflowEngineService.java` only when needed
- Create: `OrderRepository.java`
- Test: `OrderServiceTest.java`, `WorkflowEngineServiceTest.java`

**Interfaces:** `OrderService` consumes `OrderRepository`, `UserRepository`, `IStorageService`, and existing `WorkflowEngineService`.

- [x] Look up the user and validate every temporary key before any storage operation.
- [x] Persist the order inside a transaction to obtain its ID, move each temporary object to its final key, associate annexes through `Order.annexes` cascade, and instantiate approval stages through the existing workflow engine.
- [x] Ensure course-specific template selection takes precedence over the default template and stage records retain sorted step order, pending status, responsible unit, and order association.
- [x] On any failure after an S3 move, delete every final key already created, propagate the original failure, and allow the database transaction to roll back.
- [x] Run the focused service test command; all success and failure cases pass.

### Task 5: Implement validated HTTP endpoints and error mapping

**Files:**
- Create: `OrderController.java`
- Modify: `GlobalExceptionHandler.java` only as needed
- Test: `OrderControllerTest.java`

**Interfaces:** Controller consumes `IOrderService`; requests/responses use the DTOs from Task 2 and `BaseResponse`.

- [x] Implement `POST /orders/annex-upload` for one attachment authorization request and `POST /orders` for creation.
- [x] Apply bean validation to required metadata and request fields; return the established success envelope and HTTP 201 for created orders.
- [x] Map missing user to 404, invalid input/key to 400, and unconfigured storage to 503 using Spring HTTP status handling.
- [x] Run focused controller tests; the complete suite ran with 138 tests passing and the PostgreSQL integration test unable to authenticate with the configured local `testuser` password.

### Task 6: Final verification

**Files:** No production changes unless verification finds a defect.

- [x] Run the focused unit tests after all implementation changes; all pass. Complete Gradle suite attempted (138/139 pass; integration blocked by local DB credentials).
- [x] Run `git diff --check` and inspect the branch diff against `origin/main`.
- [x] Report test evidence, branch name, commit, and local PostgreSQL test profile dependency.

## Self-review

- All ten observable rules in the design are covered by Tasks 1–5; every failure mode in the design has a named test.
- Test-first constraint is explicit: Task 1 writes every feature scenario before any functional production behavior; Task 2 permits only compile-time declarations.
- Contracts are consistent across DTO, storage, and order service tasks.
- Existing workflow engine behavior is exercised directly and reused by order orchestration rather than duplicated.
- Plan remains bounded to the order-creation feature and does not include attachment download or unrelated workflow-template CRUD.
