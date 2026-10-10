import React,{useEffect,useState} from 'react'

const api=async(path,opts={})=>{
  let r
  try{r=await fetch('/api'+path,{headers:{'Content-Type':'application/json'},...opts})}
  catch{throw new Error('Cannot reach the server. Is the backend running on port 8080?')}
  const text=await r.text()
  if(!r.ok){
    let msg=''
    try{msg=JSON.parse(text).message}catch{}
    const err=new Error(msg||(r.status>=500?'Server error. Please try again.':'Request failed'))
    err.status=r.status
    throw err
  }
  return text?JSON.parse(text):null
}

const storedUser=()=>{try{return JSON.parse(localStorage.getItem('jobhubUser')||'null')}catch{return null}}
const JOB_TYPES=['Full Time','Part Time','Internship','Contract']
const emptyForm={title:'',company:'',location:'',type:'Full Time',salary:'',skills:'',description:''}

function App(){
 const [user,setUser]=useState(storedUser)
 const [page,setPage]=useState('home')
 const [jobs,setJobs]=useState([])
 const [query,setQuery]=useState('')
 const [message,setMessage]=useState('')

 const clearUser=()=>{setUser(null);localStorage.removeItem('jobhubUser')}
 // the server keeps the session; drop a stale "logged in" state (server restarted, session expired)
 useEffect(()=>{if(user)api('/users/me').then(u=>{setUser(u);localStorage.setItem('jobhubUser',JSON.stringify(u))}).catch(e=>{if(e.status===401){clearUser();setPage('home')}})},[])

 const search=()=>api('/jobs?q='+encodeURIComponent(query)).then(setJobs).catch(e=>setMessage(e.message))
 // reload every time the Jobs page is opened so newly approved jobs show up
 useEffect(()=>{if(page==='jobs')search()},[page])

 const signIn=u=>{setUser(u);localStorage.setItem('jobhubUser',JSON.stringify(u));setMessage('');setPage('dashboard')}
 const login=async(e)=>{
   e.preventDefault()
   try{signIn(await api('/users/login',{method:'POST',body:JSON.stringify(Object.fromEntries(new FormData(e.target)))}))}
   catch(err){setMessage(err.message)}
 }
 const register=async(e)=>{
   e.preventDefault()
   try{signIn(await api('/users/register',{method:'POST',body:JSON.stringify(Object.fromEntries(new FormData(e.target)))}))}
   catch(err){setMessage(err.message)}
 }
 const logout=()=>{api('/users/logout',{method:'POST',body:'{}'}).catch(()=>{});clearUser();setMessage('');setPage('home')}
 const applyTo=async(j)=>{
   if(!user){setPage('login');return}
   try{await api('/applications',{method:'POST',body:JSON.stringify({jobId:j.id,seekerId:user.id})});setMessage('Application submitted!')}
   catch(err){setMessage(err.message)}
 }
 const onEnter=e=>{if(e.key==='Enter')search()}

 return <div className="app">
  <nav><div className="brand" onClick={()=>setPage('home')}>Job<span>Hub</span></div>
   <div className="navlinks"><button onClick={()=>setPage('home')}>Home</button><button onClick={()=>setPage('jobs')}>Jobs</button>
   {user?<><button onClick={()=>setPage('dashboard')}>Dashboard</button><button className="logout" onClick={logout}>Logout</button></>
   :<><button onClick={()=>setPage('login')}>Login</button><button className="primary" onClick={()=>setPage('register')}>Sign Up</button></>}</div>
  </nav>
  {message&&<div className="alert">{message}<button onClick={()=>setMessage('')}>×</button></div>}

  {page==='home'&&<><section className="hero"><div><p className="eyebrow">YOUR NEXT OPPORTUNITY</p><h1>Find a job you<br/><span>actually love.</span></h1><p className="sub">Connect with great companies, discover meaningful opportunities, and take the next step in your career.</p>
  <div className="search"><input value={query} onChange={e=>setQuery(e.target.value)} onKeyDown={e=>{if(e.key==='Enter')setPage('jobs')}} placeholder="Job title, skill, or company"/><button onClick={()=>setPage('jobs')}>Search Jobs</button></div></div></section>
  <section className="stats"><div><b>10K+</b><span>Active Jobs</span></div><div><b>2K+</b><span>Companies</span></div><div><b>25K+</b><span>Job Seekers</span></div><div><b>95%</b><span>Success Rate</span></div></section>
  <section className="features"><h2>Everything you need to get hired</h2><div className="cards"><Card icon="🔎" title="Smart Job Search" text="Find relevant jobs quickly with powerful search."/><Card icon="🏢" title="Top Companies" text="Discover opportunities from growing companies."/><Card icon="📊" title="Track Applications" text="Keep an eye on every application in one place."/></div></section></>}

  {page==='jobs'&&<section className="content"><h1>Explore Jobs</h1><div className="search"><input value={query} onChange={e=>setQuery(e.target.value)} onKeyDown={onEnter} placeholder="Search jobs..."/><button onClick={search}>Search</button></div><div className="jobgrid">{jobs.map(j=><JobCard key={j.id} job={j} user={user} apply={()=>applyTo(j)}/>)}</div>{jobs.length===0&&<p className="empty">No approved jobs found.</p>}</section>}

  {page==='login'&&<Auth title="Welcome back" submit="Login" onSubmit={login} fields={['email','password']}/>}
  {page==='register'&&<Auth title="Create your account" submit="Create Account" onSubmit={register} fields={['name','email','password']} withRole/>}
  {page==='dashboard'&&user&&<Dashboard user={user} setPage={setPage} setMessage={setMessage}/>}
 </div>
}

function Card({icon,title,text}){return <div className="feature"><div className="icon">{icon}</div><h3>{title}</h3><p>{text}</p></div>}
function JobCard({job,user,apply}){
 const canApply=!user||user.role==='JOB_SEEKER'
 return <article className="job"><div className="companyIcon">💼</div><div className="jobbody"><h3>{job.title}</h3><p className="company">{job.company}</p><div className="tags"><span>{job.location}</span><span>{job.type}</span><span>{job.salary||'Salary not specified'}</span></div><p>{job.description}</p>{canApply&&<button className="primary small" onClick={apply}>{user?'Apply Now':'Login to Apply'}</button>}</div></article>
}

function Auth({title,submit,onSubmit,fields,withRole}){
 return <section className="auth"><form onSubmit={onSubmit}><h1>{title}</h1><p>Join JobHub and find your next opportunity.</p>
  {fields.map(f=><input key={f} name={f} type={f==='password'?'password':f==='email'?'email':'text'} placeholder={f[0].toUpperCase()+f.slice(1)} minLength={withRole&&f==='password'?6:undefined} required/>)}
  {withRole&&<select name="role" defaultValue="JOB_SEEKER"><option value="JOB_SEEKER">I'm looking for a job</option><option value="EMPLOYER">I'm hiring</option></select>}
  <button className="primary wide">{submit}</button></form></section>
}

function Dashboard({user,setPage,setMessage}){
 const [jobs,setJobs]=useState([]),[users,setUsers]=useState([]),[apps,setApps]=useState([])
 const [form,setForm]=useState(emptyForm)
 const fail=e=>setMessage(e.message)
 const refresh=()=>api(user.role==='ADMIN'?'/jobs/all':'/jobs/employer/'+user.id).then(setJobs).catch(fail)
 useEffect(()=>{
  if(user.role==='EMPLOYER') refresh()
  if(user.role==='ADMIN'){refresh();api('/users').then(setUsers).catch(fail)}
  if(user.role==='JOB_SEEKER') api('/applications/seeker/'+user.id).then(setApps).catch(fail)
 },[user.id,user.role])
 const set=k=>e=>setForm({...form,[k]:e.target.value})
 const create=async e=>{e.preventDefault();try{await api('/jobs',{method:'POST',body:JSON.stringify({...form,employerId:user.id})});setMessage('Job submitted for admin approval.');setForm(emptyForm);refresh()}catch(err){fail(err)}}
 const status=async(id,value)=>{try{await api('/jobs/'+id+'/status?value='+value,{method:'PUT'});refresh()}catch(err){fail(err)}}
 const remove=async id=>{if(!window.confirm('Delete this job and all its applications?'))return;try{await api('/jobs/'+id,{method:'DELETE'});refresh()}catch(err){fail(err)}}
 return <section className="content dashboard"><div className="dashhead"><div><p className="eyebrow">DASHBOARD</p><h1>Hello, {user.name}</h1><p>{user.role.replace('_',' ')}</p></div><button onClick={()=>setPage('jobs')}>Browse Jobs</button></div>
 {user.role==='EMPLOYER'&&<><div className="panel"><h2>Post a Job</h2><form className="jobform" onSubmit={create}>
   {['title','company','location','salary','skills'].map(k=><input key={k} placeholder={k} value={form[k]} required={['title','company','location'].includes(k)} maxLength={255} onChange={set(k)}/>)}
   <select value={form.type} onChange={set('type')}>{JOB_TYPES.map(t=><option key={t}>{t}</option>)}</select>
   <textarea placeholder="description" value={form.description} maxLength={2000} onChange={set('description')}/><button className="primary">Post Job</button></form></div>
   <List jobs={jobs} employer remove={remove} setMessage={setMessage}/></>}
 {user.role==='ADMIN'&&<><div className="panel"><h2>Users</h2><p>{users.length} registered users</p>{users.map(u=><p key={u.id}><b>{u.name}</b> · {u.email} · {u.role}</p>)}</div><List jobs={jobs} admin status={status} remove={remove}/></>}
 {user.role==='JOB_SEEKER'&&<div className="panel"><h2>My Applications</h2>{apps.map(a=><p key={a.id}>{a.jobTitle}{a.company&&` @ ${a.company}`} — <b>{a.status}</b></p>)}{apps.length===0&&<p>No applications yet.</p>}</div>}
 </section>
}
function List({jobs,admin,employer,status,remove,setMessage}){
 const [open,setOpen]=useState(null)
 return <div className="panel"><h2>{admin?'Manage Job Listings':'My Job Listings'}</h2>{jobs.map(j=><div key={j.id}><div className="row"><div><b>{j.title}</b><small>{j.company} · {j.location}</small></div><span className={'status '+j.status}>{j.status}</span>{admin&&j.status==='PENDING'&&<div><button onClick={()=>status(j.id,'APPROVED')}>Approve</button><button onClick={()=>status(j.id,'REJECTED')}>Reject</button></div>}{employer&&<button onClick={()=>setOpen(open===j.id?null:j.id)}>{open===j.id?'Hide applicants':'Applicants'}</button>}{(admin||employer)&&<button onClick={()=>remove(j.id)}>Delete</button>}</div>{open===j.id&&<Applicants jobId={j.id} setMessage={setMessage}/>}</div>)}{jobs.length===0&&<p>No jobs yet.</p>}</div>
}

function Applicants({jobId,setMessage}){
 const [apps,setApps]=useState(null)
 const load=()=>api('/applications/job/'+jobId).then(setApps).catch(e=>setMessage(e.message))
 useEffect(()=>{load()},[jobId])
 const set=async(id,v)=>{try{await api('/applications/'+id+'/status?value='+v,{method:'PUT'});load()}catch(e){setMessage(e.message)}}
 if(!apps) return <p>Loading applicants…</p>
 return <div className="applicants">{apps.length===0&&<p>No applicants yet.</p>}{apps.map(a=><div className="row" key={a.id}><div><b>{a.seekerName}</b><small>{a.seekerEmail}{a.resumeUrl&&` · ${a.resumeUrl}`}</small></div><span className="status">{a.status}</span><div><button onClick={()=>set(a.id,'SHORTLISTED')}>Shortlist</button><button onClick={()=>set(a.id,'REJECTED')}>Reject</button><button onClick={()=>set(a.id,'HIRED')}>Hire</button></div></div>)}</div>
}

export default App
