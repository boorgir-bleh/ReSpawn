import { useEffect, useState } from 'react'
import './neon3.css'
import './flow2.css'
import './polish.css'
import './apple-flow.css'
import './home-login.css'
import './fit-home.css'
import './center-home.css'
import './neon-tunnel.css'
import './provided-background.css'
import './dynamic-background.css'
import './exact-background.css'
import './plain-background.css'
import { api, loadSession, saveSession } from './api'

const hours = ['09:00', '10:00', '11:00', '12:00', '13:00', '14:00', '15:00', '16:00', '17:00', '18:00', '19:00', '20:00']
const CARD_COLORS = ['pink', 'cyan', 'blue', 'violet']
const CARD_NICKNAMES = ['Moonlight', 'Comet', 'Orbit', 'Nova', 'Halo', 'Ember', 'Drift', 'Solace']

// Every timestamp in this app is anchored to UTC (matches how the backend interprets the
// `date` query param) rather than the browser's local timezone - simplest thing that stays
// consistent end-to-end without building real per-cafe timezone support.
function todayUTC() {
  return new Date().toISOString().slice(0, 10)
}

function addDaysUTC(isoDate, days) {
  const d = new Date(`${isoDate}T00:00:00.000Z`)
  d.setUTCDate(d.getUTCDate() + days)
  return d.toISOString().slice(0, 10)
}

function hourToInstant(isoDate, hourLabel) {
  return new Date(`${isoDate}T${hourLabel}:00.000Z`)
}

// Picks "today" unless any part of the 09:00-20:00 window has already passed, in which case
// "tomorrow" - keeps every displayed/bookable hour safely in the future for the backend's
// @Future validation, regardless of what time the app happens to be opened.
function nextBookableDate() {
  const today = todayUTC()
  const windowStart = hourToInstant(today, hours[0])
  return windowStart.getTime() > Date.now() ? today : addDaysUTC(today, 1)
}

function isContiguousSelection(selected) {
  if (selected.length === 0) return false
  const indices = selected.map((h) => hours.indexOf(h)).sort((a, b) => a - b)
  for (let i = 1; i < indices.length; i++) {
    if (indices[i] !== indices[i - 1] + 1) return false
  }
  return true
}

function formatDateLabel(isoDate) {
  return new Date(`${isoDate}T00:00:00.000Z`).toLocaleDateString(undefined, {
    weekday: 'long', day: 'numeric', month: 'long', year: 'numeric', timeZone: 'UTC',
  })
}

function toCard(pc, index) {
  return {
    id: pc.id,
    number: pc.label,
    name: CARD_NICKNAMES[index % CARD_NICKNAMES.length],
    type: `${pc.gpu} · ${pc.ram}`,
    rate: `Rs. ${pc.hourlyRate} / hour`,
    color: CARD_COLORS[index % CARD_COLORS.length],
    status: pc.status,
    active: pc.active,
    tag: pc.status === 'AVAILABLE' ? 'OPEN' : pc.status,
  }
}

function App() {
  const [session, setSession] = useState(() => loadSession())
  const [screen, setScreen] = useState(() => (loadSession() ? (loadSession().role === 'ADMIN' ? 'admin' : 'home') : 'login'))
  const [selectedPC, setSelectedPC] = useState(null)

  const [pcs, setPcs] = useState([])
  const [pcsError, setPcsError] = useState('')

  useEffect(() => {
    api.listPcStations()
      .then((list) => setPcs(list.map(toCard)))
      .catch((err) => setPcsError(err.message))
  }, [])

  const handleLogin = (nextSession) => {
    setSession(nextSession)
    saveSession(nextSession)
    setScreen(nextSession.role === 'ADMIN' ? 'admin' : 'home')
  }

  const handleLogout = () => {
    setSession(null)
    saveSession(null)
    setScreen('login')
  }

  const openPC = (pc) => setSelectedPC(pc)

  if (screen === 'login' || !session) return <Login onLogin={handleLogin} />

  const role = session.role === 'ADMIN' ? 'admin' : 'player'

  return <main className="app-shell">
    <div className="ambient ambient-one" /><div className="ambient ambient-two" />
    <header className="topbar"><button className="brand" onClick={() => setScreen(role === 'player' ? 'home' : 'admin')}><span className="brand-mark">+</span><span>playroom</span></button><span className="header-label">{role === 'player' ? 'PLAYER SPACE' : 'ADMIN SPACE'}</span><button className="logout-button" onClick={handleLogout}>Log out ↗</button></header>
    {pcsError && <p className="form-error" style={{ margin: '1rem 2rem' }}>Could not load PCs: {pcsError}</p>}
    {role === 'player' && screen === 'home' && <PlayerHome pcs={pcs} onBrowse={() => setScreen('pcs')} onOpen={openPC} />}
    {role === 'player' && screen === 'pcs' && <PCSchedule pcs={pcs} onBack={() => setScreen('home')} onOpen={openPC} />}
    {role === 'admin' && <Admin session={session} pcs={pcs} />}
    {selectedPC && <BookingMenu pc={selectedPC} session={session} onClose={() => setSelectedPC(null)} />}
  </main>
}

function Login({ onLogin }) {
  const [mode, setMode] = useState('login') // 'login' | 'register' | 'otp'
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [fullName, setFullName] = useState('')
  const [phoneNumber, setPhoneNumber] = useState('')
  const [code, setCode] = useState('')
  const [error, setError] = useState('')
  const [info, setInfo] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [cooldown, setCooldown] = useState(0)

  useEffect(() => {
    if (cooldown <= 0) return
    const timer = setTimeout(() => setCooldown((s) => s - 1), 1000)
    return () => clearTimeout(timer)
  }, [cooldown])

  const submitCredentials = async (event) => {
    event.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      if (mode === 'register') {
        // New accounts still need one OTP round-trip to prove the email is real.
        const challenge = await api.register(fullName, email, phoneNumber, password)
        setInfo(challenge.message)
        setCooldown(30)
        setMode('otp')
      } else {
        // Returning users: password is enough, no OTP step.
        const auth = await api.login(email, password)
        onLogin(auth)
      }
    } catch (err) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  const submitOtp = async (event) => {
    event.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      const auth = await api.verifyOtp(email, code)
      onLogin(auth)
    } catch (err) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  const resend = async () => {
    if (cooldown > 0) return
    setError('')
    try {
      const challenge = await api.resendOtp(email)
      setInfo(challenge.message)
      setCooldown(30)
    } catch (err) {
      setError(err.message)
    }
  }

  if (mode === 'otp') {
    return <main className="login-shell"><div className="grid-lines" /><div className="fluid-orb orb-back" /><div className="fluid-orb orb-front" /><div className="fluid-flow flow-pink" /><div className="fluid-flow flow-blue" />
      <section className="login-content reveal">
        <h1>Check your<br />email.</h1>
        <p className="menu-subtitle">{info || `We sent a code to ${email}`}</p>
        <form className="login-form" onSubmit={submitOtp}>
          <input value={code} onChange={(e) => setCode(e.target.value)} placeholder="6-digit code" inputMode="numeric" autoComplete="one-time-code" aria-label="Verification code" required />
          {error && <p className="form-error">{error}</p>}
          <button className="enter-button" type="submit" disabled={submitting}>{submitting ? 'Verifying…' : 'Verify'} <span>↗</span></button>
        </form>
        <button className="text-button" type="button" onClick={resend} disabled={cooldown > 0} style={{ marginTop: '1rem' }}>
          {cooldown > 0 ? `Resend code (${cooldown}s)` : 'Resend code'}
        </button>
        <button className="text-button" type="button" onClick={() => { setMode('login'); setError(''); setCode('') }} style={{ marginTop: '0.5rem' }}>← Back</button>
      </section>
    </main>
  }

  return <main className="login-shell"><div className="grid-lines" /><div className="fluid-orb orb-back" /><div className="fluid-orb orb-front" /><div className="fluid-flow flow-pink" /><div className="fluid-flow flow-blue" />
    <section className="login-content reveal">
      <h1>Game Vault.</h1>
      <form className="login-form" onSubmit={submitCredentials}>
        {mode === 'register' && <input value={fullName} onChange={(e) => setFullName(e.target.value)} placeholder="Full name" autoComplete="name" aria-label="Full name" required />}
        <input value={email} onChange={(e) => setEmail(e.target.value)} placeholder="Email" type="email" autoComplete="email" aria-label="Email" required />
        {mode === 'register' && <input value={phoneNumber} onChange={(e) => setPhoneNumber(e.target.value)} placeholder="Phone number" type="tel" autoComplete="tel" aria-label="Phone number" required />}
        <input value={password} onChange={(e) => setPassword(e.target.value)} placeholder="Password" type="password" autoComplete={mode === 'register' ? 'new-password' : 'current-password'} aria-label="Password" required />
        {error && <p className="form-error">{error}</p>}
        <button className="enter-button" type="submit" disabled={submitting}>{submitting ? 'Please wait…' : mode === 'register' ? 'Create account' : 'Enter'} <span>↗</span></button>
      </form>
      <button className="text-button" type="button" onClick={() => { setMode(mode === 'register' ? 'login' : 'register'); setError('') }} style={{ marginTop: '1rem' }}>
        {mode === 'register' ? 'Already have an account? Log in' : "New here? Create an account"}
      </button>
    </section>
  </main>
}

function PlayerHome({ pcs, onBrowse, onOpen }) {
  return <><section className="player-view" id="home"><div className="hero-copy reveal"><p className="eyebrow"><span className="live-dot" /> PLAYROOM IS OPEN UNTIL 11 PM</p><h1>Your next<br /><em>good time</em> is here.</h1><p className="hero-description">Pick a PC, settle in, and play your way. We will save your spot.</p><div className="hero-actions"><button className="primary-button" onClick={onBrowse}>Browse all PCs <span>↗</span></button><span className="trust-line">Easy booking · Pay now or later</span></div></div><div className="hero-art reveal delay-one"><div className="sun-disc" /><div className="squiggle">〰</div><div className="desk"><div className="monitor"><span>PLAY</span></div></div><div className="plant" /><div className="art-caption">SELECT YOUR<br /><strong>perfect setup.</strong></div></div></section><section className="quick-section"><div className="section-heading"><div><p className="eyebrow">START HERE</p><h2>Pick a PC. Pick a time.</h2></div><button className="text-button" onClick={onBrowse}>View all PCs ↗</button></div><div className="pc-grid">{pcs.slice(0, 3).map((pc) => <PCCard key={pc.id} pc={pc} onOpen={onOpen} />)}</div></section></>
}

function PCSchedule({ pcs: allPCs, onBack, onOpen }) {
  return <section className="schedule-page reveal"><button className="back-button" onClick={onBack}>← Back to home</button><div className="section-heading schedule-heading"><div><p className="eyebrow">{formatDateLabel(nextBookableDate()).toUpperCase()}</p><h1>Find your<br /><em>play space.</em></h1></div><p className="schedule-note">Choose a PC to view its hours.</p></div><div className="schedule-grid">{allPCs.map((pc) => <PCCard key={pc.id} pc={pc} onOpen={onOpen} />)}</div></section>
}

function PCCard({ pc, onOpen }) {
  const bookable = pc.active && pc.status === 'AVAILABLE'
  return <article className={`pc-card neon-${pc.color} detailed-card`} onClick={() => bookable && onOpen(pc)} style={!bookable ? { opacity: 0.55, cursor: 'not-allowed' } : undefined}><div className="pc-visual"><span className="pc-number">{pc.number}</span><div className="pc-screen">{pc.name[0]}</div><span className="pc-tag">{pc.tag}</span></div><div className="pc-info"><div><h3>{pc.name}</h3><p>{pc.type}</p></div><strong>{pc.rate}</strong></div></article>
}

function BookingMenu({ pc, session, onClose }) {
  const bookingDate = nextBookableDate()
  const [busySlots, setBusySlots] = useState([])
  const [slotsLoading, setSlotsLoading] = useState(true)
  const [slotsError, setSlotsError] = useState('')
  const [selectedHours, setSelectedHours] = useState([])
  const [paymentMode, setPaymentMode] = useState('POSTPAID')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [result, setResult] = useState(null)

  useEffect(() => {
    setSlotsLoading(true)
    api.getBusySlots(pc.id, bookingDate)
      .then(setBusySlots)
      .catch((err) => setSlotsError(err.message))
      .finally(() => setSlotsLoading(false))
  }, [pc.id, bookingDate])

  const isHourBusy = (hourLabel) => {
    const t = hourToInstant(bookingDate, hourLabel).getTime()
    return busySlots.some((slot) => t >= new Date(slot.startTime).getTime() && t < new Date(slot.endTime).getTime())
  }

  const toggleHour = (hour) => {
    if (isHourBusy(hour)) return
    setSelectedHours((current) => current.includes(hour) ? current.filter((h) => h !== hour) : [...current, hour].sort())
  }

  const confirmBooking = async () => {
    if (!isContiguousSelection(selectedHours)) {
      setError('Please select a continuous block of hours.')
      return
    }
    setSubmitting(true)
    setError('')
    try {
      const startTime = hourToInstant(bookingDate, selectedHours[0]).toISOString()
      const durationMinutes = selectedHours.length * 60
      const response = await api.createBooking(session.token, { pcStationId: pc.id, startTime, durationMinutes, paymentMode })
      setResult(response)
    } catch (err) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  return <div className="modal-backdrop" role="presentation" onClick={onClose}><div className="booking-menu" role="dialog" aria-modal="true" onClick={(event) => event.stopPropagation()}><button className="close-button" onClick={onClose} aria-label="Close schedule">×</button>
    {result ? <div className="success-state">
      <div className="success-mark">✓</div>
      <p className="eyebrow">{result.booking.status === 'CONFIRMED' ? 'SPOT SAVED' : 'AWAITING PAYMENT'}</p>
      <h2>{pc.number} is yours.</h2>
      {result.qrCodeBase64PNG
        ? <>
          <p>Scan to pay Rs. {result.booking.amount} via UPI. Your spot is held while payment is confirmed at the counter.</p>
          <img src={`data:image/png;base64,${result.qrCodeBase64PNG}`} alt="UPI payment QR code" style={{ width: 200, height: 200, margin: '1rem auto', display: 'block' }} />
        </>
        : <p>Rs. {result.booking.amount} due at the counter when you arrive.</p>}
      <button className="primary-button" onClick={onClose}>Done <span>↗</span></button>
    </div> : <>
      <p className="eyebrow">{pc.number} / {pc.name}</p>
      <h2>Choose your hours.</h2>
      <p className="menu-subtitle">{formatDateLabel(bookingDate)} · tap a continuous block of hours.</p>
      {slotsLoading ? <p className="menu-subtitle">Loading availability…</p> : slotsError ? <p className="form-error">{slotsError}</p> : <div className="hour-list">{hours.map((hour) => {
        const busy = isHourBusy(hour)
        return <button key={hour} disabled={busy} className={`${busy ? 'is-booked' : ''} ${selectedHours.includes(hour) ? 'is-selected' : ''}`} onClick={() => toggleHour(hour)}><strong>{hour}</strong><small>{busy ? 'Booked' : selectedHours.includes(hour) ? 'Your hour' : 'Available'}</small></button>
      })}</div>}
      <div className="menu-footer" style={{ flexDirection: 'column', alignItems: 'stretch', gap: '0.75rem' }}>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <button type="button" className={paymentMode === 'POSTPAID' ? 'primary-button small' : 'text-button'} onClick={() => setPaymentMode('POSTPAID')}>Pay at counter</button>
          <button type="button" className={paymentMode === 'PREPAID_QR' ? 'primary-button small' : 'text-button'} onClick={() => setPaymentMode('PREPAID_QR')}>Pay now (UPI)</button>
        </div>
        {error && <p className="form-error">{error}</p>}
        <div className="menu-footer">
          <span>{selectedHours.length ? `${selectedHours.length} hour${selectedHours.length > 1 ? 's' : ''} selected` : 'Select a time to continue'}</span>
          <button className="primary-button" disabled={!selectedHours.length || submitting} onClick={confirmBooking}>{submitting ? 'Booking…' : 'Continue'} <span>↗</span></button>
        </div>
      </div>
    </>}
  </div></div>
}

function Admin({ session, pcs }) {
  const [bookings, setBookings] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.adminListBookings(session.token)
      .then(setBookings)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [session.token])

  const inUse = pcs.filter((pc) => pc.status === 'IN_USE').length

  return <section className="admin-view reveal"><div className="section-heading"><div><p className="eyebrow">{formatDateLabel(todayUTC()).toUpperCase()}</p><h1>Welcome back, <em>{session.fullName}.</em></h1></div></div>
    <div className="metric-grid">
      <article className="metric-card peach"><span>PCs in use</span><strong>{inUse} <small>/ {pcs.length}</small></strong><small>{pcs.length - inUse} PCs are free right now</small></article>
      <article className="metric-card mint-card"><span>Total bookings</span><strong>{bookings.length}</strong><small>Across all statuses</small></article>
      <article className="metric-card lavender"><span>Pending payment</span><strong>{bookings.filter((b) => b.status === 'PENDING_PAYMENT').length}</strong><small>QR bookings awaiting confirmation</small></article>
    </div>
    <div className="admin-columns">
      <div className="panel">
        <div className="panel-heading"><div><p className="eyebrow">YOUR FLOOR</p><h2>All bookings</h2></div></div>
        {loading && <p className="menu-subtitle">Loading…</p>}
        {error && <p className="form-error">{error}</p>}
        {!loading && !error && bookings.length === 0 && <p className="menu-subtitle">No bookings yet.</p>}
        {bookings.map((booking) => <div className="booking-row" key={booking.id}>
          <time>{new Date(booking.startTime).toLocaleString(undefined, { timeZone: 'UTC', hour: '2-digit', minute: '2-digit', day: 'numeric', month: 'short' })}</time>
          <div className="avatar">{booking.userFullName.split(' ').map((part) => part[0]).join('')}</div>
          <div className="booking-person"><strong>{booking.userFullName}</strong><span>{booking.pcStationLabel}</span></div>
          <b>Rs. {booking.amount}</b>
          <span className="status-pill">{booking.status}</span>
        </div>)}
      </div>
    </div>
  </section>
}

export default App
