import { createPlatformKeyValueStore } from '@infrastructure/storage/PlatformKeyValueStore'
import { createPlatformQueryPersister } from '@infrastructure/storage/PlatformQueryPersister'
import {
  createKeyValueStoreUseCases,
  type KeyValueStoreUseCases,
} from '@use-cases/storage/CreateKeyValueStoreUseCases'
import { createAuthDependencies } from '@surfaces/shared/auth-dependencies'
import { createHttpConsoleApiClient } from '@infrastructure/api/console/HttpConsoleApiClient'
import { createOverviewRepository } from '@infrastructure/api/console/OverviewRepositoryImpl'
import { createDataTableRepository } from '@infrastructure/api/console/DataTableRepositoryImpl'
import { createDeviceRepository } from '@infrastructure/api/console/DeviceRepositoryImpl'
import { createGetLocalOverviewUseCase } from '@use-cases/console/GetLocalOverviewUseCase'
import { createFetchTableDataUseCase } from '@use-cases/console/FetchTableDataUseCase'
import { createGetDevicesStatusUseCase } from '@use-cases/console/GetDevicesStatusUseCase'
import { createExecuteDeviceCommandUseCase } from '@use-cases/console/ExecuteDeviceCommandUseCase'
import { createConsoleSessionStore } from '@infrastructure/storage/ConsoleSessionStore'
import { createHttpConsoleAuthApiClient } from '@infrastructure/api/console-auth/HttpConsoleAuthApiClient'
import { createAuthConsoleRepository } from '@infrastructure/api/console-auth/AuthConsoleRepositoryImpl'
import { rolePermissionService } from '@infrastructure/auth/RolePermissionServiceImpl'
import { createLoginWithRoleUseCase } from '@use-cases/console-auth/LoginWithRoleUseCase'
import { createLogoutUseCase } from '@use-cases/console-auth/LogoutUseCase'
import { createGetUserPermissionsUseCase } from '@use-cases/console-auth/GetUserPermissionsUseCase'
import { createValidateRouteAccessUseCase } from '@use-cases/console-auth/ValidateRouteAccessUseCase'
import type { UserSession } from '@domain/console/auth/UserSession'
import { createCatalogRepository } from '@infrastructure/api/CatalogRepository'
import { createMqttClientService } from '@infrastructure/realtime/MqttClientService'
import { createMqttMatchEventPublisher } from '@infrastructure/realtime/MqttMatchEventPublisher'
import { createMatchSimulatorUseCase } from '@use-cases/simulator/MatchSimulatorUseCase'

export interface ConsoleDependencies {
  storage: KeyValueStoreUseCases
  queryPersister: ReturnType<typeof createPlatformQueryPersister>
  auth: ReturnType<typeof createAuthDependencies>
  overview: ReturnType<typeof createGetLocalOverviewUseCase>
  tableData: ReturnType<typeof createFetchTableDataUseCase>
  devices: ReturnType<typeof createGetDevicesStatusUseCase>
  executeDeviceCommand: ReturnType<typeof createExecuteDeviceCommandUseCase>
  simulator: {
    mqtt: ReturnType<typeof createMqttClientService>
    matches: ReturnType<typeof createMatchSimulatorUseCase>
  }
  consoleAuth: {
    sessionStore: ReturnType<typeof createConsoleSessionStore>
    login: ReturnType<typeof createLoginWithRoleUseCase>
    logout: ReturnType<typeof createLogoutUseCase>
    getPermissions: ReturnType<typeof createGetUserPermissionsUseCase>
    validateRouteAccess: ReturnType<typeof createValidateRouteAccessUseCase>
    rolePermissionService: typeof rolePermissionService
    getCurrentSession: () => Promise<UserSession | undefined>
  }
}

export function createConsoleDependencies(): ConsoleDependencies {
  const platformStore = createPlatformKeyValueStore()
  const apiClient = createHttpConsoleApiClient()
  const overviewRepository = createOverviewRepository(apiClient)
  const tableDataRepository = createDataTableRepository(apiClient)
  const deviceRepository = createDeviceRepository(apiClient)
  const consoleSessionStore = createConsoleSessionStore()
  const consoleAuthRepository = createAuthConsoleRepository(
    createHttpConsoleAuthApiClient(),
    consoleSessionStore,
  )
  const mqtt = createMqttClientService()
  const catalogRepository = createCatalogRepository()

  return {
    storage: createKeyValueStoreUseCases(platformStore),
    queryPersister: createPlatformQueryPersister(
      platformStore,
      'PARTITA_QUERY_CACHE_CONSOLE_V1',
    ),
    auth: createAuthDependencies(),
    overview: createGetLocalOverviewUseCase(overviewRepository),
    tableData: createFetchTableDataUseCase(tableDataRepository),
    devices: createGetDevicesStatusUseCase(deviceRepository),
    executeDeviceCommand: createExecuteDeviceCommandUseCase(deviceRepository),
    simulator: {
      mqtt,
      matches: createMatchSimulatorUseCase(
        catalogRepository,
        createMqttMatchEventPublisher(mqtt),
      ),
    },
    consoleAuth: {
      sessionStore: consoleSessionStore,
      login: createLoginWithRoleUseCase(
        consoleAuthRepository,
        consoleSessionStore,
      ),
      logout: createLogoutUseCase(consoleAuthRepository, consoleSessionStore),
      getPermissions: createGetUserPermissionsUseCase(consoleSessionStore),
      validateRouteAccess: createValidateRouteAccessUseCase(
        consoleSessionStore,
        rolePermissionService,
      ),
      rolePermissionService,
      getCurrentSession: () => consoleAuthRepository.getCurrentSession(),
    },
  }
}
