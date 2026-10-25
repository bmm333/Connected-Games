import { Capacitor, registerPlugin } from '@capacitor/core'
import type { QrScannerRepository as DomainQrScannerRepository } from '@domain/qr/QrScannerRepository'

interface NativeBarcodeScanner {
  scan(): Promise<{ barcodes: Array<{ rawValue?: string }> }>
  stop(): Promise<void>
}

const nativeScanner = registerPlugin<NativeBarcodeScanner>('BarcodeScanner')

interface BarcodeDetectorConstructor {
  new (options?: { formats: string[] }): {
    detect(source: ImageBitmapSource): Promise<Array<{ rawValue?: string }>>
  }
}

interface BarcodeDetectorWindow extends Window {
  BarcodeDetector?: BarcodeDetectorConstructor
}

export function createQrScannerRepository(): DomainQrScannerRepository {
  if (Capacitor.isNativePlatform()) {
    return {
      isAvailable: async () => true,
      scan: async () => {
        const result = await nativeScanner.scan()
        const value = result.barcodes[0]?.rawValue?.trim()
        if (value === undefined || value.length === 0) {
          throw new Error('No QR code was detected')
        }
        return value
      },
      stop: () => nativeScanner.stop(),
    }
  }

  let stream: MediaStream | undefined
  let video: HTMLVideoElement | undefined

  return {
    isAvailable: async () =>
      typeof navigator.mediaDevices?.getUserMedia === 'function' &&
      typeof (window as BarcodeDetectorWindow).BarcodeDetector === 'function',
    scan: async () => {
      const Detector = (window as BarcodeDetectorWindow).BarcodeDetector
      if (
        Detector === undefined ||
        typeof navigator.mediaDevices?.getUserMedia !== 'function'
      ) {
        throw new Error('QR scanning is not supported by this browser')
      }

      stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: { ideal: 'environment' } },
      })
      video = document.createElement('video')
      video.srcObject = stream
      video.setAttribute('playsinline', '')
      await video.play()
      const detector = new Detector({ formats: ['qr_code'] })

      return new Promise<string>((resolve, reject) => {
        const startedAt = Date.now()
        const detect = async () => {
          try {
            const barcode = (await detector.detect(video!))[0]?.rawValue
            if (barcode !== undefined && barcode.trim().length > 0) {
              resolve(barcode.trim())
              return
            }
            if (Date.now() - startedAt > 30_000) {
              reject(new Error('QR scan timed out'))
              return
            }
            window.requestAnimationFrame(() => {
              void detect()
            })
          } catch (error) {
            reject(error)
          }
        }
        void detect()
      })
    },
    stop: async () => {
      stream?.getTracks().forEach((track) => track.stop())
      stream = undefined
      video = undefined
    },
  }
}
