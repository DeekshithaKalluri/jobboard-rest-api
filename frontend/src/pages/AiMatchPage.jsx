import { useState } from 'react'
import axios from 'axios'

export default function AiMatchPage() {
  const [resume, setResume] = useState('')
  const [matches, setMatches] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleMatch = async () => {
    if (resume.length < 50) {
      setError('Please enter at least 50 characters')
      return
    }
    setLoading(true)
    setError('')
    setMatches([])
    try {
      const { data } = await axios.post(
        'http://localhost:8080/api/jobs/ai-match',
        { resumeText: resume }
      )
      const text = typeof data === 'string' ? data : JSON.stringify(data)
      const match = text.match(/\[[\s\S]*\]/)
      if (match) setMatches(JSON.parse(match[0]))
      else setError('AI returned an unexpected response. Try again.')
    } catch (e) {
      setError('Match failed. Is the backend running at localhost:8080?')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{padding:'2rem', maxWidth:'800px', margin:'0 auto'}}>
      <h1 style={{marginBottom:'0.5rem'}}>AI Job Match</h1>
      <p style={{color:'#888', marginBottom:'1.25rem'}}>
        Paste your resume — AI searches real live jobs and ranks your best matches.
      </p>
      <textarea
        rows={8}
        style={{width:'100%', padding:'0.75rem', borderRadius:'8px',
          border:'1px solid #444', fontSize:'14px', boxSizing:'border-box',
          background:'#1a1a1a', color:'white'}}
        placeholder="e.g. Java developer with 3 years Spring Boot, REST APIs, PostgreSQL, Docker..."
        value={resume}
        onChange={e => setResume(e.target.value)}
      />
      <button
        onClick={handleMatch}
        disabled={loading}
        style={{marginTop:'0.75rem', padding:'0.75rem 2rem',
          background: loading ? '#555' : '#0066cc', color:'white',
          border:'none', borderRadius:'8px', cursor:'pointer', fontSize:'15px'}}>
        {loading ? 'Searching live jobs + ranking...' : 'Find My Best Jobs ↗'}
      </button>
      {error && <p style={{color:'#ff6b6b', marginTop:'0.5rem'}}>{error}</p>}

      {matches.length > 0 && (
        <div style={{marginTop:'2rem'}}>
          <h2 style={{marginBottom:'1rem'}}>Your Top Matches</h2>
          {matches.map((m, i) => (
            <div key={i} style={{border:'1px solid #333', borderRadius:'10px',
              padding:'1.25rem', marginBottom:'1rem',
              borderLeft:`4px solid ${m.rank===1 ? '#0066cc' : m.rank===2 ? '#0099cc' : '#666'}`}}>
              <div style={{display:'flex', justifyContent:'space-between', alignItems:'flex-start'}}>
                <div>
                  <h3 style={{margin:'0 0 4px'}}>#{m.rank} {m.title}</h3>
                  <p style={{margin:'0 0 4px', color:'#aaa'}}>{m.company}</p>
                </div>
                {m.match_score && (
                  <span style={{background:'#0066cc', color:'white', padding:'4px 10px',
                    borderRadius:'20px', fontSize:'13px', fontWeight:600, whiteSpace:'nowrap'}}>
                    {m.match_score}% match
                  </span>
                )}
              </div>
              <p style={{margin:'8px 0', color:'#ccc', fontSize:'14px'}}>{m.match_reason}</p>
              {m.apply_url && (
                <a href={m.apply_url} target="_blank" rel="noopener noreferrer"
                  style={{display:'inline-block', marginTop:'8px', padding:'8px 20px',
                    background:'#1a7a1a', color:'white', borderRadius:'6px',
                    textDecoration:'none', fontSize:'14px', fontWeight:500}}>
                  Apply Now ↗
                </a>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}