const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081'
const SESSION_KEY = 'cafe_erp_session'

export function loadSession() {
  try {
    const raw = localStorage.getItem(SESSION_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export function saveSession(session) {
  try {
    if (session) localStorage.setItem(SESSION_KEY, JSON.stringify(session))
    else localStorage.removeItem(SESSION_KEY)
  } catch {
    // localStorage unavailable (private mode, etc.) - session just won't survive a reload
  }
}

async function request(path, { method = 'GET', body, token } = {}) {
  const headers = { 'Content-Type': 'application/json' }
  if (token) headers.Authorization = `Bearer ${token}`

  let response
  try {
    response = await fetch(`${BASE_URL}${path}`, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    })
  } catch {
    throw new ApiError('Could not reach the server. Is the backend running?', 0)
  }

  const text = await response.text()
  const data = text ? JSON.parse(text) : null

  if (!response.ok) {
    const message = data?.details?.length ? data.details.join(', ') : (data?.message || `Request failed (${response.status})`)
    throw new ApiError(message, response.status, data?.details)
  }

  return data
}

export class ApiError extends Error {
  constructor(message, status, details) {
    super(message)
    this.status = status
    this.details = details
  }
}

export const api = {
  register: (fullName, email, phoneNumber, password) =>
    request('/api/auth/register', { method: 'POST', body: { fullName, email, phoneNumber, password } }),

  login: (email, password) =>
    request('/api/auth/login', { method: 'POST', body: { email, password } }),

  verifyOtp: (email, code) =>
    request('/api/auth/otp/verify', { method: 'POST', body: { email, code } }),

  resendOtp: (email) =>
    request('/api/auth/otp/resend', { method: 'POST', body: { email } }),

  listPcStations: () =>
    request('/api/pc-stations'),

  getBusySlots: (pcStationId, isoDate) =>
    request(`/api/pc-stations/${pcStationId}/busy-slots?date=${isoDate}`),

  createBooking: (token, { pcStationId, startTime, durationMinutes, paymentMode }) =>
    request('/api/bookings', { method: 'POST', token, body: { pcStationId, startTime, durationMinutes, paymentMode } }),

  listMyBookings: (token) =>
    request('/api/bookings/me', { token }),

  cancelBooking: (token, bookingId) =>
    request(`/api/bookings/${bookingId}/cancel`, { method: 'POST', token }),

  adminListBookings: (token, status) =>
    request(`/api/admin/bookings${status ? `?status=${status}` : ''}`, { token }),
}
