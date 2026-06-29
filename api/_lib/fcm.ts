/**
 * Firebase Cloud Messaging service for push notifications.
 * Sends notifications to customer and rider device tokens.
 */

const FCM_URL = 'https://fcm.googleapis.com/fcm/send'
const FCM_SERVER_KEY = process.env.FCM_SERVER_KEY || ''

interface FcmMessage {
  to?: string           // single device token
  registration_ids?: string[]  // multiple device tokens
  notification: {
    title: string
    body: string
    image?: string
  }
  data?: Record<string, string>
  android?: {
    priority: 'normal' | 'high'
    notification?: {
      channel_id?: string
      sound?: string
      click_action?: string
    }
  }
  apns?: {
    payload: {
      aps: {
        sound: string
        badge?: number
      }
    }
  }
}

async function sendFcm(message: FcmMessage): Promise<boolean> {
  if (!FCM_SERVER_KEY) {
    console.warn('[FCM] FCM_SERVER_KEY not set — notification not sent')
    return false
  }
  try {
    const res = await fetch(FCM_URL, {
      method: 'POST',
      headers: {
        Authorization: `key=${FCM_SERVER_KEY}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(message),
    })
    const data = await res.json()
    if (!res.ok) {
      console.error('[FCM] Send failed:', data)
      return false
    }
    return true
  } catch (err) {
    console.error('[FCM] Send error:', err)
    return false
  }
}

export async function sendToDevice(
  deviceToken: string,
  title: string,
  body: string,
  data?: Record<string, string>,
): Promise<boolean> {
  return sendFcm({
    to: deviceToken,
    notification: { title, body },
    data,
    android: { priority: 'high', notification: { channel_id: 'default', sound: 'default' } },
    apns: { payload: { aps: { sound: 'default', badge: 1 } } },
  })
}

export async function sendToMultipleDevices(
  deviceTokens: string[],
  title: string,
  body: string,
  data?: Record<string, string>,
): Promise<boolean> {
  if (deviceTokens.length === 0) return false
  return sendFcm({
    registration_ids: deviceTokens,
    notification: { title, body },
    data,
    android: { priority: 'high', notification: { channel_id: 'default', sound: 'default' } },
    apns: { payload: { aps: { sound: 'default', badge: 1 } } },
  })
}

export async function sendDeliveryNotification(
  deviceToken: string,
  trackingNumber: string,
  status: string,
): Promise<boolean> {
  const statusLabels: Record<string, string> = {
    ASSIGNED: 'Rider Assigned',
    PICKED_UP: 'Package Picked Up',
    OUT_FOR_DELIVERY: 'Out for Delivery',
    DELIVERED: 'Delivered Successfully',
  }
  const label = statusLabels[status] || status
  return sendToDevice(
    deviceToken,
    `Delivery ${label}`,
    `Tracking: ${trackingNumber} — ${label.toLowerCase()}`,
    { trackingNumber, status, type: 'delivery_update' },
  )
}
