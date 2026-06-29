'use strict'
const FCM_URL = 'https://fcm.googleapis.com/fcm/send'
const FCM_SERVER_KEY = process.env.FCM_SERVER_KEY || ''

async function sendFcm(message) {
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

async function sendToDevice(deviceToken, title, body, data) {
  return sendFcm({
    to: deviceToken,
    notification: { title, body },
    data,
    android: { priority: 'high', notification: { channel_id: 'default', sound: 'default' } },
    apns: { payload: { aps: { sound: 'default', badge: 1 } } },
  })
}

async function sendToMultipleDevices(deviceTokens, title, body, data) {
  if (deviceTokens.length === 0) return false
  return sendFcm({
    registration_ids: deviceTokens,
    notification: { title, body },
    data,
    android: { priority: 'high', notification: { channel_id: 'default', sound: 'default' } },
    apns: { payload: { aps: { sound: 'default', badge: 1 } } },
  })
}

async function sendDeliveryNotification(deviceToken, trackingNumber, status) {
  const statusLabels = {
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

module.exports = { sendToDevice, sendToMultipleDevices, sendDeliveryNotification }
