import { BrowserRouter, Routes, Route, Link } from 'react-router-dom'
import JobsPage from './pages/JobsPage'
import AiMatchPage from './pages/AiMatchPage'

export default function App() {
  return (
    <BrowserRouter>
      <nav style={{padding:'1rem', borderBottom:'1px solid #eee',
                   display:'flex', gap:'1.5rem', alignItems:'center'}}>
        <strong style={{fontSize:'18px'}}>JobBoard</strong>
        <Link to="/" style={{textDecoration:'none', color:'#0066cc'}}>Browse Jobs</Link>
        <Link to="/ai-match" style={{textDecoration:'none', color:'#0066cc'}}>AI Match</Link>
      </nav>
      <Routes>
        <Route path="/" element={<JobsPage />} />
        <Route path="/ai-match" element={<AiMatchPage />} />
      </Routes>
    </BrowserRouter>
  )
}