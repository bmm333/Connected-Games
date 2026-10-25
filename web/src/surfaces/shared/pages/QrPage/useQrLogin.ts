import { useMutation } from '@tanstack/react-query'
import type { AuthenticateWithQrCode } from '@use-cases/auth/AuthenticateWithQrCodeUseCase'
import type { PairDevice } from '@use-cases/auth/PairDeviceUseCase'

export function useQrLogin(
  authenticateWithQrCode: AuthenticateWithQrCode,
  pairDevice: PairDevice,
) {
  const mutation = useMutation({
    mutationFn: async (input: {
      mode: 'authenticate' | 'pair'
      code: string
      credentials?: Parameters<PairDevice>[1]
    }) => {
      if (input.mode === 'authenticate') {
        return authenticateWithQrCode(input.code)
      }
      if (input.credentials === undefined) {
        throw new Error('Device credentials are required for pairing')
      }
      return pairDevice(input.code, input.credentials)
    },
  })

  return {
    ...mutation,
    isLoading: mutation.isPending,
    isSuccess: mutation.isSuccess,
    error: mutation.error,
  }
}
