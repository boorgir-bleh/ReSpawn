import { useState } from 'react'
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

const pcs = [
  { number: 'PC 01', name: 'Moonlight', type: 'RTX 4070 · 32 GB RAM', rate: 'Rs. 120 / hour', color: 'pink', booked: ['10:00', '13:00', '14:00'] },
  { number: 'PC 02', name: 'Comet', type: 'RTX 4060 · 16 GB RAM', rate: 'Rs. 90 / hour', color: 'cyan', booked: ['11:00', '12:00', '17:00'] },
  { number: 'PC 03', name: 'Orbit', type: 'RTX 4060 · 16 GB RAM', rate: 'Rs. 90 / hour', color: 'blue', booked: ['09:00', '15:00', '16:00'] },
  { number: 'PC 04', name: 'Nova', type: 'RTX 4090 · 64 GB RAM', rate: 'Rs. 150 / hour', color: 'violet', booked: ['12:00', '18:00'] },
]

const hours = ['09:00', '10:00', '11:00', '12:00', '13:00', '14:00', '15:00', '16:00', '17:00', '18:00', '19:00', '20:00']
const bookings = [
  { time: '10:00 AM', name: 'Aarav Shah', pc: 'Moonlight', amount: 'Rs. 360', state: 'Playing now' },
  { time: '11:30 AM', name: 'Meera Joshi', pc: 'Comet', amount: 'Rs. 180', state: 'Coming up' },
  { time: '01:00 PM', name: 'Kabir Rao', pc: 'Orbit', amount: 'Rs. 270', state: 'Coming up' },
]

function App() {
  const [screen, setScreen] = useState('login')
  const [role, setRole] = useState('player')
  const [selectedPC, setSelectedPC] = useState(null)
  const [selectedHours, setSelectedHours] = useState([])
  const [booked, setBooked] = useState(false)

  const openPC = (pc) => { setSelectedPC(pc); setSelectedHours([]); setBooked(false) }
  const toggleHour = (hour) => setSelectedHours((current) => current.includes(hour) ? current.filter((item) => item !== hour) : [...current, hour].sort())

  if (screen === 'login') return <Login onLogin={(nextRole) => { setRole(nextRole); setScreen(nextRole === 'player' ? 'home' : 'admin') }} />

  return <main className="app-shell">
    <div className="ambient ambient-one" /><div className="ambient ambient-two" />
    <header className="topbar"><button className="brand" onClick={() => setScreen(role === 'player' ? 'home' : 'admin')}><span className="brand-mark">+</span><span>playroom</span></button><span className="header-label">{role === 'player' ? 'PLAYER SPACE' : 'ADMIN SPACE'}</span><button className="logout-button" onClick={() => setScreen('login')}>Log out ↗</button></header>
    {role === 'player' && screen === 'home' && <PlayerHome onBrowse={() => setScreen('pcs')} onOpen={openPC} />}
    {role === 'player' && screen === 'pcs' && <PCSchedule pcs={pcs} onBack={() => setScreen('home')} onOpen={openPC} />}
    {role === 'admin' && <Admin />}
    {selectedPC && <BookingMenu pc={selectedPC} selectedHours={selectedHours} toggleHour={toggleHour} booked={booked} onClose={() => setSelectedPC(null)} onConfirm={() => setBooked(true)} />}
  </main>
}

function Login({ onLogin }) {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')

  const enterSpace = (event) => {
    event.preventDefault()
    if (username.trim() && password.trim()) onLogin('player')
  }

  return <main className="login-shell"><div className="grid-lines" /><div className="fluid-orb orb-back" /><div className="fluid-orb orb-front" /><div className="fluid-flow flow-pink" /><div className="fluid-flow flow-blue" /><section className="login-content reveal"><h1>Game Vault.</h1><form className="login-form" onSubmit={enterSpace}><input value={username} onChange={(event) => setUsername(event.target.value)} placeholder="Username" autoComplete="username" aria-label="Username" required /><input value={password} onChange={(event) => setPassword(event.target.value)} placeholder="Password" type="password" autoComplete="current-password" aria-label="Password" required /><button className="enter-button" type="submit">Enter <span>↗</span></button></form></section></main>
}

function PlayerHome({ onBrowse, onOpen }) {
  return <><section className="player-view" id="home"><div className="hero-copy reveal"><p className="eyebrow"><span className="live-dot" /> PLAYROOM IS OPEN UNTIL 11 PM</p><h1>Your next<br /><em>good time</em> is here.</h1><p className="hero-description">Pick a PC, settle in, and play your way. We will save your spot.</p><div className="hero-actions"><button className="primary-button" onClick={onBrowse}>Browse all PCs <span>↗</span></button><span className="trust-line">Easy booking · Pay now or later</span></div></div><div className="hero-art reveal delay-one"><div className="sun-disc" /><div className="squiggle">〰</div><div className="desk"><div className="monitor"><span>PLAY</span></div></div><div className="plant" /><div className="art-caption">SELECT YOUR<br /><strong>perfect setup.</strong></div></div></section><section className="quick-section"><div className="section-heading"><div><p className="eyebrow">START HERE</p><h2>Pick a PC. Pick a time.</h2></div><button className="text-button" onClick={onBrowse}>View all PCs ↗</button></div><div className="pc-grid">{pcs.slice(0, 3).map((pc) => <PCCard key={pc.number} pc={pc} onOpen={onOpen} />)}</div></section></>
}

function PCSchedule({ pcs: allPCs, onBack, onOpen }) {
  return <section className="schedule-page reveal"><button className="back-button" onClick={onBack}>← Back to home</button><div className="section-heading schedule-heading"><div><p className="eyebrow">WEDNESDAY / 26 AUGUST 2026</p><h1>Find your<br /><em>play space.</em></h1></div><p className="schedule-note">Choose a PC to view its hours.</p></div><div className="schedule-grid">{allPCs.map((pc) => <PCCard key={pc.number} pc={pc} onOpen={onOpen} />)}</div></section>
}

function PCCard({ pc, onOpen }) {
  return <article className={`pc-card neon-${pc.color} detailed-card`} onClick={() => onOpen(pc)}><div className="pc-visual"><span className="pc-number">{pc.number}</span><div className="pc-screen">{pc.name[0]}</div><span className="pc-tag">{pc.booked.length < 3 ? 'OPEN' : 'AVAILABLE'}</span></div><div className="pc-info"><div><h3>{pc.name}</h3><p>{pc.type}</p></div><strong>{pc.rate}</strong></div></article>
}

function BookingMenu({ pc, selectedHours, toggleHour, booked, onClose, onConfirm }) {
  return <div className="modal-backdrop" role="presentation" onClick={onClose}><div className="booking-menu" role="dialog" aria-modal="true" onClick={(event) => event.stopPropagation()}><button className="close-button" onClick={onClose} aria-label="Close schedule">×</button>{booked ? <div className="success-state"><div className="success-mark">✓</div><p className="eyebrow">SPOT SAVED</p><h2>{pc.number} is yours.</h2><p>We will remind you 30 minutes before your session.</p><button className="primary-button" onClick={onClose}>Done <span>↗</span></button></div> : <><p className="eyebrow">{pc.number} / {pc.name}</p><h2>Choose your hours.</h2><p className="menu-subtitle">Tap one or more available hours for today.</p><div className="hour-list">{hours.map((hour) => <button key={hour} disabled={pc.booked.includes(hour)} className={`${pc.booked.includes(hour) ? 'is-booked' : ''} ${selectedHours.includes(hour) ? 'is-selected' : ''}`} onClick={() => toggleHour(hour)}><strong>{hour}</strong><small>{pc.booked.includes(hour) ? 'Booked' : selectedHours.includes(hour) ? 'Your hour' : 'Available'}</small></button>)}</div><div className="menu-footer"><span>{selectedHours.length ? `${selectedHours.length} hour${selectedHours.length > 1 ? 's' : ''} selected` : 'Select a time to continue'}</span><button className="primary-button" disabled={!selectedHours.length} onClick={onConfirm}>Continue <span>↗</span></button></div></>}</div></div>
}

function Admin() {
  return <section className="admin-view reveal"><div className="section-heading"><div><p className="eyebrow">WEDNESDAY, 26 AUGUST 2026</p><h1>Good morning, <em>Sam.</em></h1></div><button className="primary-button small">Export revenue <span>↓</span></button></div><div className="metric-grid"><article className="metric-card peach"><span>Today’s revenue</span><strong>Rs. 8,420</strong><small>↑ 18% from last Wednesday</small></article><article className="metric-card mint-card"><span>Players today</span><strong>24</strong><small>7 more expected this evening</small></article><article className="metric-card lavender"><span>PCs in use</span><strong>6 <small>/ 8</small></strong><small>2 PCs are free right now</small></article></div><div className="admin-columns"><div className="panel"><div className="panel-heading"><div><p className="eyebrow">YOUR FLOOR</p><h2>Today’s bookings</h2></div></div>{bookings.map((booking) => <div className="booking-row" key={booking.time}><time>{booking.time}</time><div className="avatar">{booking.name.split(' ').map((part) => part[0]).join('')}</div><div className="booking-person"><strong>{booking.name}</strong><span>{booking.pc}</span></div><b>{booking.amount}</b><span className="status-pill">{booking.state}</span></div>)}</div><div className="panel"><div className="panel-heading"><div><p className="eyebrow">AT A GLANCE</p><h2>Top players</h2></div></div>{['Aarav Shah', 'Meera Joshi', 'Kabir Rao'].map((name, index) => <div className="player-rank" key={name}><span>0{index + 1}</span><div className="avatar">{name.split(' ').map((part) => part[0]).join('')}</div><strong>{name}<small>{12 - index * 2} visits</small></strong><b>Rs. {4860 - index * 810}</b></div>)}<button className="feedback-button">See anonymous feedback <span>↗</span></button></div></div></section>
}

export default App
