import { useEffect, useState } from 'react'
import axios from 'axios'

export default function JobsPage() {
  const [jobs, setJobs] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    axios.get('http://localhost:8080/api/jobs')
      .then(r => setJobs(r.data.content || []))
      .catch(console.error)
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <p style={{padding:'2rem'}}>Loading jobs...</p>

  return (
    <div style={{padding:'2rem', maxWidth:'800px', margin:'0 auto'}}>
      <h1 style={{marginBottom:'1.5rem'}}>Available Jobs ({jobs.length})</h1>
      {jobs.map(job => (
        <div key={job.id} style={{border:'1px solid #e0e0e0', borderRadius:'10px',
          padding:'1.25rem', marginBottom:'1rem', boxShadow:'0 1px 3px rgba(0,0,0,0.06)'}}>
          <h3 style={{margin:'0 0 4px'}}>{job.title}</h3>
          <p style={{margin:'0 0 4px', color:'#555'}}>{job.company} · {job.location} · {job.jobType}</p>
          {job.salary && <p style={{margin:'0 0 4px', color:'#2a7a2a', fontWeight:500}}>${job.salary.toLocaleString()}/yr</p>}
          <p style={{margin:'0', color:'#777', fontSize:'13px'}}>{job.description}</p>
        </div>
      ))}
    </div>
  )
}