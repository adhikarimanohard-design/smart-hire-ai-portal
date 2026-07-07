import React, { useState, useEffect, useRef, useMemo } from 'react';

const API_BASE = 'https://smart-hire-ai-portal-2-d0rz.onrender.com/api';

const COMPANY_EMOJIS = {
  tech: '💻', cloud: '☁️', ai: '🤖', data: '📊',
  design: '🎨', mobile: '📱', security: '🔐',
  product: '🚀', server: '🖥️', analytics: '📈',
  default: ['🏢', '💼', '🌐', '⚡', '🔷', '🌟', '🎯', '💡']
};

function getCompanyEmoji(company) {
  if (!company) return '🏢';
  const lower = company.toLowerCase();
  for (const [key, val] of Object.entries(COMPANY_EMOJIS)) {
    if (key !== 'default' && lower.includes(key)) return val;
  }
  const arr = COMPANY_EMOJIS.default;
  return arr[company.charCodeAt(0) % arr.length];
}

function getDaysAgo(dateStr) {
  if (!dateStr) return 'Recently';
  const diff = Math.floor((Date.now() - new Date(dateStr)) / 86400000);
  if (diff <= 0) return 'Today';
  if (diff === 1) return '1 day ago';
  return `${diff} days ago`;
}

export default function App() {
  const [allJobs, setAllJobs] = useState([]);
  const [displayedJobs, setDisplayedJobs] = useState([]);
  const [isLoadingJobs, setIsLoadingJobs] = useState(true);
  const [jobError, setJobError] = useState(null);
  
  const [currentUser, setCurrentUser] = useState(null);
  const [isUserMenuOpen, setIsUserMenuOpen] = useState(false);
  
  const [isLoginModalOpen, setIsLoginModalOpen] = useState(false);
  const [authTab, setAuthTab] = useState('login');
  
  const [isResumeModalOpen, setIsResumeModalOpen] = useState(false);
  const [selectedJob, setSelectedJob] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  
  const [toast, setToast] = useState({ show: false, msg: '' });

  // Auth form states
  const [loginEmail, setLoginEmail] = useState('');
  const [loginPassword, setLoginPassword] = useState('');
  const [regFirstName, setRegFirstName] = useState('');
  const [regLastName, setRegLastName] = useState('');
  const [regEmail, setRegEmail] = useState('');
  const [regPassword, setRegPassword] = useState('');
  const [regRole, setRegRole] = useState('candidate');

  // Resume states
  const [selectedFile, setSelectedFile] = useState(null);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [isUploading, setIsUploading] = useState(false);
  const [parsedSkills, setParsedSkills] = useState([]);
  const [isDragging, setIsDragging] = useState(false);
  
  const fileInputRef = useRef(null);

  useEffect(() => {
    const savedUser = sessionStorage.getItem('sh_user');
    if (savedUser) {
      try {
        setCurrentUser(JSON.parse(savedUser));
      } catch (e) {}
    }
    loadJobs();
    
    const handleKeyDown = (e) => {
      if (e.key === 'Escape') {
        setSelectedJob(null);
        setIsLoginModalOpen(false);
        setIsResumeModalOpen(false);
      }
    };
    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, []);

  useEffect(() => {
    if (isLoginModalOpen || isResumeModalOpen || selectedJob) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }
  }, [isLoginModalOpen, isResumeModalOpen, selectedJob]);

  useEffect(() => {
    performSearch(searchQuery);
  }, [searchQuery, allJobs]);

  const loadJobs = async () => {
    setIsLoadingJobs(true);
    setJobError(null);
    try {
      const res = await fetch(`${API_BASE}/jobs`);
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const data = await res.json();
      setAllJobs(data);
      setDisplayedJobs(data);
    } catch (err) {
      console.error('Failed to load jobs:', err);
      setJobError(err.message);
    } finally {
      setIsLoadingJobs(false);
    }
  };

  const showToast = (msg) => {
    setToast({ show: true, msg });
    setTimeout(() => setToast({ show: false, msg: '' }), 3500);
  };

  const handleLogin = async () => {
    if (!loginEmail || !loginPassword) { showToast('⚠️ Please fill in all fields'); return; }
    if (!loginEmail.includes('@')) { showToast('⚠️ Enter a valid email'); return; }

    try {
      const res = await fetch(`${API_BASE}/users/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: loginEmail, password: loginPassword })
      });
      if (!res.ok) { showToast('❌ Invalid email or password'); return; }
      const data = await res.json();
      const user = {
        id: data.userId,
        email: data.email,
        firstName: data.firstName,
        lastName: data.lastName,
        token: data.token,
        resumeUploaded: false
      };
      setCurrentUser(user);
      sessionStorage.setItem('sh_user', JSON.stringify(user));
      setIsLoginModalOpen(false);
      showToast(`✅ Welcome back, ${user.firstName}!`);
    } catch (err) {
      showToast('❌ Login failed. Try again.');
    }
  };

  const handleRegister = async () => {
    if (!regFirstName || !regEmail || !regPassword) { showToast('⚠️ Please fill in all fields'); return; }
    if (regPassword.length < 8) { showToast('⚠️ Password must be at least 8 characters'); return; }

    try {
      const res = await fetch(`${API_BASE}/users/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ firstName: regFirstName, lastName: regLastName, email: regEmail, password: regPassword, role: regRole })
      });
      if (!res.ok) { showToast('❌ Email already registered'); return; }
      const data = await res.json();
      const user = {
        id: data.userId,
        email: data.email,
        firstName: data.firstName,
        lastName: data.lastName,
        token: data.token,
        resumeUploaded: false
      };
      setCurrentUser(user);
      sessionStorage.setItem('sh_user', JSON.stringify(user));
      setIsLoginModalOpen(false);
      showToast(`🎉 Welcome, ${regFirstName}!`);
    } catch (err) {
      showToast('❌ Registration failed. Try again.');
    }
  };

  const logout = () => {
    setCurrentUser(null);
    sessionStorage.removeItem('sh_user');
    setIsUserMenuOpen(false);
    showToast('👋 Signed out successfully');
  };

  const performSearch = (q) => {
    const lowerQ = q.toLowerCase().trim();
    if (!lowerQ) {
      setDisplayedJobs(allJobs);
      return;
    }
    const filtered = allJobs.filter(job =>
      job.title.toLowerCase().includes(lowerQ) ||
      job.company.toLowerCase().includes(lowerQ) ||
      job.location.toLowerCase().includes(lowerQ) ||
      (job.skills || []).some(s => s.toLowerCase().includes(lowerQ)) ||
      (job.description || '').toLowerCase().includes(lowerQ)
    );
    setDisplayedJobs(filtered);
  };

  const applyToJob = async (jobId) => {
    if (!currentUser) {
      setSelectedJob(null);
      setIsLoginModalOpen(true);
      showToast('🔒 Sign in to apply for jobs');
      return;
    }
    const job = allJobs.find(j => j.id === jobId);
    const title = job ? job.title : 'this position';
    try {
      await fetch(`${API_BASE}/jobs/${jobId}/apply`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
      });
    } catch (err) {}
    showToast(`✅ Applied successfully for ${title}!`);
    setSelectedJob(null);
  };

  const handleFileSelect = (e) => {
    const file = e.target.files[0];
    if (file) processFile(file);
  };

  const processFile = (file) => {
    const valid = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'];
    if (!valid.includes(file.type)) { showToast('⚠️ PDF or Word documents only'); return; }
    if (file.size > 5 * 1024 * 1024) { showToast('⚠️ File must be under 5MB'); return; }
    setSelectedFile(file);
    setParsedSkills([]);
  };

  const uploadResume = async () => {
    if (!selectedFile) return;
    setIsUploading(true);
    setUploadProgress(0);

    let pct = 0;
    const iv = setInterval(() => {
      pct = Math.min(pct + Math.random() * 18, 90);
      setUploadProgress(pct);
    }, 150);

    try {
      const formData = new FormData();
      formData.append('resume', selectedFile);
      const uploadRes = await fetch(`${API_BASE}/users/${currentUser.id}/resume/upload`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${currentUser.token}` },
        body: formData
      });
      
      if (!uploadRes.ok) throw new Error('Upload failed');

      clearInterval(iv);
      setUploadProgress(100);

      const pool = ['JavaScript', 'React', 'Python', 'Java', 'Node.js', 'AWS', 'SQL', 'Docker', 'TypeScript', 'MongoDB', 'Spring Boot', 'REST API'];
      const skills = pool.sort(() => Math.random() - 0.5).slice(0, 5 + Math.floor(Math.random() * 4));

      setTimeout(() => {
        setParsedSkills(skills);
        const updatedUser = { ...currentUser, resumeUploaded: true, resumeName: selectedFile.name };
        setCurrentUser(updatedUser);
        sessionStorage.setItem('sh_user', JSON.stringify(updatedUser));
        showToast('✅ Resume uploaded & analyzed!');
        setTimeout(() => {
          setIsResumeModalOpen(false);
          setSelectedFile(null);
          setParsedSkills([]);
          setUploadProgress(0);
          setIsUploading(false);
        }, 1800);
      }, 400);

    } catch (err) {
      clearInterval(iv);
      setIsUploading(false);
      showToast('❌ Upload failed. Please try again.');
    }
  };

  return (
    <>
      <style>{`
        @import url('https://fonts.googleapis.com/css2?family=Syne:wght@400;500;600;700;800&family=DM+Sans:ital,opsz,wght@0,9..40,300;0,9..40,400;0,9..40,500;1,9..40,300&display=swap');

        :root {
          --bg: #030712;
          --surface: #0a0f1e;
          --surface2: #0f1628;
          --border: rgba(56, 189, 248, 0.07);
          --border-bright: rgba(56, 189, 248, 0.2);
          --accent: #38bdf8;
          --accent2: #f472b6;
          --accent3: #4ade80;
          --gold: #fbbf24;
          --text: #e2e8f0;
          --text-muted: #64748b;
          --text-dim: #1e293b;
          --glow: rgba(56, 189, 248, 0.12);
        }
        * { margin: 0; padding: 0; box-sizing: border-box; }
        html { scroll-behavior: smooth; }
        body { font-family: 'DM Sans', sans-serif; background: var(--bg); color: var(--text); min-height: 100vh; overflow-x: hidden; }

        .bg-grid { position: fixed; inset: 0; background-image: linear-gradient(rgba(56,189,248,0.025) 1px, transparent 1px), linear-gradient(90deg, rgba(56,189,248,0.025) 1px, transparent 1px); background-size: 50px 50px; pointer-events: none; z-index: 0; }
        .bg-orb { position: fixed; border-radius: 50%; filter: blur(120px); pointer-events: none; z-index: 0; }
        .orb1 { width:700px;height:700px;background:rgba(56,189,248,0.05);top:-300px;left:-200px;animation:float1 12s ease-in-out infinite; }
        .orb2 { width:600px;height:600px;background:rgba(244,114,182,0.04);bottom:-200px;right:-200px;animation:float2 10s ease-in-out infinite; }
        .orb3 { width:400px;height:400px;background:rgba(74,222,128,0.03);top:40%;left:40%;animation:float3 14s ease-in-out infinite; }
        @keyframes float1 { 0%,100%{transform:translate(0,0)}50%{transform:translate(40px,-30px)} }
        @keyframes float2 { 0%,100%{transform:translate(0,0)}50%{transform:translate(-30px,40px)} }
        @keyframes float3 { 0%,100%{transform:translate(0,0)}50%{transform:translate(20px,20px)} }

        header { position: fixed; top:0; left:0; right:0; z-index:100; height: 68px; display: flex; align-items: center; justify-content: space-between; padding: 0 clamp(16px, 4vw, 48px); background: rgba(3,7,18,0.85); backdrop-filter: blur(24px); border-bottom: 1px solid var(--border); }
        .logo { display:flex; align-items:center; gap:12px; text-decoration:none; cursor: pointer;}
        .logo-icon { width:38px; height:38px; background: linear-gradient(135deg, var(--accent), #1d6cf5); border-radius: 10px; display: flex; align-items:center; justify-content:center; font-size: 20px; box-shadow: 0 0 24px rgba(56,189,248,0.5); }
        .logo-name { font-family: 'Syne', sans-serif; font-weight: 800; font-size: 20px; letter-spacing: -0.5px; color: var(--text); }
        .logo-name span { color: var(--accent); }
        .logo-sub { font-size:11px; color:var(--text-muted); margin-top:1px; }
        .header-right { display:flex; align-items:center; gap:12px; }
        .live-badge { display:flex; align-items:center; gap:7px; background: rgba(74,222,128,0.08); border: 1px solid rgba(74,222,128,0.2); border-radius: 20px; padding: 5px 14px; font-size: 12px; font-weight:500; color: var(--accent3); }
        .live-dot { width:7px; height:7px; background:var(--accent3); border-radius:50%; box-shadow: 0 0 8px var(--accent3); animation: pulse 2s ease-in-out infinite; }
        @keyframes pulse { 0%,100%{opacity:1;transform:scale(1)} 50%{opacity:0.5;transform:scale(0.8)} }
        .login-btn { background: transparent; border: 1px solid var(--border-bright); border-radius: 10px; padding: 8px 18px; color: var(--text); font-family: 'Syne', sans-serif; font-size: 13px; font-weight: 600; cursor: pointer; transition: all 0.25s ease; display: flex; align-items: center; gap: 7px; }
        .login-btn:hover { border-color: var(--accent); color: var(--accent); background: rgba(56,189,248,0.06); }
        
        .user-avatar { width: 36px; height: 36px; background: linear-gradient(135deg, var(--accent), #1d6cf5); border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 15px; font-weight: 700; font-family: 'Syne', sans-serif; cursor: pointer; border: 2px solid rgba(56,189,248,0.3); transition: all 0.2s; position: relative; }
        .user-avatar:hover { box-shadow: 0 0 16px rgba(56,189,248,0.4); }
        .user-menu { position: absolute; top: calc(100% + 10px); right: 0; background: var(--surface); border: 1px solid var(--border-bright); border-radius: 14px; padding: 8px; min-width: 200px; z-index: 200; opacity: 0; pointer-events: none; transform: translateY(-8px); transition: all 0.2s ease; }
        .user-menu.open { opacity:1; pointer-events:all; transform:translateY(0); }
        .user-menu-header { padding: 10px 12px 12px; border-bottom: 1px solid var(--border); margin-bottom: 6px; }
        .user-menu-name { font-weight: 600; font-size: 14px; color: var(--text); }
        .user-menu-email { font-size: 11px; color: var(--text-muted); margin-top: 2px; }
        .user-menu-item { display: flex; align-items: center; gap: 9px; padding: 9px 12px; border-radius: 8px; font-size: 13px; color: var(--text-muted); cursor: pointer; transition: all 0.15s; }
        .user-menu-item:hover { background: var(--surface2); color: var(--text); }
        .user-menu-item.danger:hover { color: var(--accent2); background: rgba(244,114,182,0.06); }

        main { position:relative; z-index:1; padding-top: 68px; }
        .container { max-width:1280px; margin:0 auto; padding: 0 clamp(16px, 4vw, 48px); }
        .hero { padding: clamp(40px, 8vw, 80px) 0 clamp(30px, 5vw, 50px); text-align: center; }
        .hero-eyebrow { display: inline-flex; align-items:center; gap:8px; background: rgba(56,189,248,0.07); border: 1px solid var(--border-bright); border-radius: 20px; padding: 6px 16px; font-size: 12px; font-weight:500; color: var(--accent); margin-bottom: 24px; letter-spacing: 1px; text-transform: uppercase; }
        .hero h1 { font-family: 'Syne', sans-serif; font-size: clamp(36px, 7vw, 80px); font-weight: 800; line-height: 1.05; letter-spacing: -2px; margin-bottom: 20px; }
        .hero h1 .line1 { color: var(--text); display:block; }
        .hero h1 .line2 { display: block; background: linear-gradient(135deg, var(--accent), var(--accent2)); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
        .hero-sub { font-size: clamp(15px, 2vw, 18px); color: var(--text-muted); max-width: 520px; margin: 0 auto 40px; line-height: 1.7; font-weight:300; }

        .stats-row { display: flex; justify-content: center; flex-wrap: wrap; gap: clamp(12px, 3vw, 24px); margin-bottom: clamp(40px, 6vw, 60px); }
        .stat-pill { display: flex; align-items:center; gap:12px; background: var(--surface); border: 1px solid var(--border); border-radius: 16px; padding: 16px 24px; transition: all 0.3s ease; cursor: default; }
        .stat-pill:hover { border-color: var(--border-bright); background: var(--surface2); transform: translateY(-3px); box-shadow: 0 12px 40px rgba(56,189,248,0.1); }
        .stat-icon { font-size: 24px; }
        .stat-num { font-family: 'Syne', sans-serif; font-size: clamp(20px, 3vw, 28px); font-weight: 800; color: var(--text); line-height: 1; }
        .stat-num.accent { color: var(--accent); }
        .stat-num.pink { color: var(--accent2); }
        .stat-num.green { color: var(--accent3); }
        .stat-num.gold { color: var(--gold); }
        .stat-label { font-size:12px; color:var(--text-muted); margin-top:3px; }

        .search-wrap { max-width: 760px; margin: 0 auto clamp(40px, 6vw, 60px); }
        .search-box { display: flex; background: var(--surface); border: 1px solid var(--border-bright); border-radius: 16px; overflow: hidden; transition: all 0.3s ease; box-shadow: 0 0 0 0 rgba(56,189,248,0); }
        .search-box:focus-within { border-color: var(--accent); box-shadow: 0 0 0 4px rgba(56,189,248,0.1); }
        .search-icon { padding: 0 20px; display:flex; align-items:center; color: var(--text-muted); font-size:20px; flex-shrink: 0; }
        .search-input { flex:1; background:transparent; border:none; outline:none; color: var(--text); font-family: 'DM Sans', sans-serif; font-size: clamp(14px, 2vw, 16px); padding: clamp(14px, 2vw, 18px) 0; }
        .search-input::placeholder { color: var(--text-muted); }
        .search-btn { background: linear-gradient(135deg, var(--accent), #1d6cf5); border: none; cursor: pointer; color: white; font-family: 'Syne', sans-serif; font-weight: 700; font-size: 14px; padding: 0 clamp(20px, 3vw, 32px); letter-spacing: 0.5px; transition: all 0.3s ease; white-space: nowrap; }
        .search-btn:hover { filter: brightness(1.15); }
        .search-tags { display:flex; flex-wrap:wrap; gap:8px; justify-content:center; margin-top:14px; }
        .search-tag { background: rgba(56,189,248,0.06); border: 1px solid var(--border); border-radius: 20px; padding: 5px 14px; font-size: 12px; color: var(--text-muted); cursor: pointer; transition: all 0.2s; }
        .search-tag:hover { border-color: var(--accent); color: var(--accent); background: rgba(56,189,248,0.1); }

        .resume-banner { background: linear-gradient(135deg, rgba(56,189,248,0.06), rgba(29,108,245,0.04)); border: 1px solid var(--border-bright); border-radius: 20px; padding: clamp(18px,3vw,28px) clamp(20px,3vw,32px); margin-bottom: clamp(28px,4vw,40px); display: flex; align-items: center; justify-content: space-between; gap: 20px; flex-wrap: wrap; position: relative; overflow: hidden; }
        .resume-banner::before { content: ''; position: absolute; top:0; left:0; right:0; height:1px; background: linear-gradient(90deg, transparent, var(--accent), transparent); }
        .resume-banner-left { display:flex; align-items:center; gap:16px; }
        .resume-banner-icon { width: 48px; height: 48px; background: linear-gradient(135deg, rgba(56,189,248,0.15), rgba(29,108,245,0.1)); border: 1px solid rgba(56,189,248,0.2); border-radius: 14px; display: flex; align-items: center; justify-content: center; font-size: 22px; flex-shrink: 0; }
        .resume-banner-title { font-family: 'Syne', sans-serif; font-size: clamp(14px,2vw,17px); font-weight: 700; color: var(--text); margin-bottom: 3px; }
        .resume-banner-sub { font-size: 12px; color: var(--text-muted); }
        .resume-upload-btn { background: linear-gradient(135deg, var(--accent), #1d6cf5); border: none; cursor: pointer; color: white; font-family: 'Syne', sans-serif; font-weight: 700; font-size: 13px; padding: 11px 22px; border-radius: 10px; transition: all 0.3s ease; display: flex; align-items: center; gap: 8px; white-space: nowrap; flex-shrink: 0; }
        .resume-upload-btn:hover { transform: translateY(-1px); box-shadow: 0 8px 24px rgba(56,189,248,0.35); filter: brightness(1.1); }
        .resume-upload-btn.uploaded { background: linear-gradient(135deg, var(--accent3), #16a34a); }

        .section-header { display:flex; justify-content:space-between; align-items:center; margin-bottom: 24px; }
        .section-title { font-family: 'Syne', sans-serif; font-size: clamp(18px, 3vw, 24px); font-weight: 700; color: var(--text); }
        .section-count { font-size: 13px; color: var(--text-muted); background: var(--surface); border: 1px solid var(--border); border-radius: 20px; padding: 4px 12px; }

        .jobs-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(min(100%, 480px), 1fr)); gap: clamp(14px, 2vw, 20px); margin-bottom: 60px; }
        
        .job-card { background: var(--surface); border: 1px solid var(--border); border-radius: 20px; padding: clamp(20px, 3vw, 28px); cursor: pointer; transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); position: relative; overflow: hidden; }
        .job-card::before { content:''; position:absolute; inset:0; background: linear-gradient(135deg, rgba(56,189,248,0.03), transparent); opacity:0; transition: opacity 0.3s; }
        .job-card:hover { border-color: var(--border-bright); transform: translateY(-4px); box-shadow: 0 20px 60px rgba(56,189,248,0.08); }
        .job-card:hover::before { opacity:1; }
        .job-card-top { display:flex; justify-content:space-between; align-items:flex-start; gap:12px; margin-bottom: 16px; }
        .job-company-row { display:flex; align-items:center; gap:12px; }
        .company-logo { width: 44px; height:44px; background: linear-gradient(135deg, var(--surface2), var(--surface)); border: 1px solid var(--border); border-radius: 12px; display:flex; align-items:center; justify-content:center; font-size: 20px; flex-shrink:0; }
        .job-title { font-family: 'Syne', sans-serif; font-size: clamp(15px, 2vw, 18px); font-weight: 700; color: var(--text); margin-bottom: 3px; line-height:1.3; }
        .job-company { font-size:13px; color:var(--accent); font-weight:500; }
        .match-badge { flex-shrink:0; padding: 6px 14px; border-radius:20px; font-size: 12px; font-weight:700; font-family: 'Syne', sans-serif; white-space: nowrap; }
        .match-fire { background:rgba(74,222,128,0.1); color:var(--accent3); border:1px solid rgba(74,222,128,0.2); }
        .match-hot { background:rgba(56,189,248,0.1); color:var(--accent); border:1px solid rgba(56,189,248,0.2); }
        .match-warm { background:rgba(251,191,36,0.1); color:var(--gold); border:1px solid rgba(251,191,36,0.2); }
        .job-location-row { display:flex; align-items:center; gap:16px; margin-bottom: 14px; flex-wrap:wrap; }
        .job-loc, .job-type, .job-time { display:flex; align-items:center; gap:5px; font-size: 12px; color: var(--text-muted); }
        .skills-row { display:flex; flex-wrap:wrap; gap:7px; margin-bottom: 16px; }
        .skill-tag { background: rgba(56,189,248,0.06); border: 1px solid rgba(56,189,248,0.12); border-radius: 6px; padding: 4px 10px; font-size: 11px; color: #7dd3fc; font-weight:500; }
        .job-desc { font-size: 13px; color: var(--text-muted); line-height:1.6; margin-bottom:18px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
        .job-card-bottom { display:flex; justify-content:space-between; align-items:center; padding-top: 16px; border-top: 1px solid var(--border); gap:12px; }
        .job-salary { font-family: 'Syne', sans-serif; font-size: clamp(14px, 2vw, 16px); font-weight:700; color: var(--text); }
        .job-salary span { color:var(--accent3); }
        .apply-btn { background: linear-gradient(135deg, var(--accent), #1d6cf5); border: none; cursor:pointer; color: white; font-family: 'Syne', sans-serif; font-weight: 700; font-size: 13px; padding: 10px 22px; border-radius:10px; transition: all 0.3s ease; letter-spacing: 0.3px; white-space: nowrap; }
        .apply-btn:hover { transform: translateY(-1px); box-shadow: 0 8px 24px rgba(56,189,248,0.35); filter: brightness(1.1); }

        .features-card { background: var(--surface); border: 1px solid var(--border-bright); border-radius: 20px; padding: clamp(24px, 3vw, 36px); position: relative; overflow:hidden; }
        .features-card::after { content:''; position:absolute; top:0; left:0; right:0; height:1px; background: linear-gradient(90deg, transparent, var(--accent), transparent); }
        .features-title { font-family: 'Syne', sans-serif; font-size: clamp(18px, 2vw, 22px); font-weight: 800; margin-bottom:24px; color: var(--text); }
        .feature-item { display:flex; gap:14px; margin-bottom:20px; align-items:flex-start; }
        .feature-item:last-child { margin-bottom:0; }
        .feat-icon { width:42px; height:42px; flex-shrink:0; background: var(--surface2); border: 1px solid var(--border); border-radius:10px; display:flex; align-items:center; justify-content:center; font-size: 20px; }
        .feat-title { font-size:14px; font-weight:600; color:var(--text); margin-bottom:3px; }
        .feat-sub { font-size:12px; color:var(--text-muted); }

        .loading-state, .no-results, .error-state { grid-column: 1/-1; display:flex; flex-direction:column; align-items:center; justify-content:center; padding:80px 20px; gap:20px; text-align: center; }
        .loading-spinner { width:48px; height:48px; border: 2px solid var(--border); border-top-color: var(--accent); border-radius:50%; animation: spin 0.8s linear infinite; }
        @keyframes spin { to { transform:rotate(360deg); } }
        .loading-text { color:var(--text-muted); font-size:14px; }
        .no-results-emoji { font-size:48px; margin-bottom:16px; }
        .no-results h3 { font-family:'Syne',sans-serif; font-size:20px; margin-bottom:8px; }
        .no-results p { color:var(--text-muted); font-size:14px; }
        .error-state { background: rgba(244,114,182,0.06); border: 1px solid rgba(244,114,182,0.2); border-radius:16px; padding:40px; }
        .error-state h3 { color:var(--accent2); font-family:'Syne',sans-serif; margin-bottom:8px; }

        .modal-overlay { position:fixed; inset:0; z-index:200; background: rgba(3,7,18,0.9); backdrop-filter: blur(12px); display:flex; align-items:center; justify-content:center; padding: 20px; opacity:0; pointer-events:none; transition: opacity 0.3s ease; }
        .modal-overlay.open { opacity:1; pointer-events:all; }
        .modal { background: var(--surface); border: 1px solid var(--border-bright); border-radius: 24px; width: 100%; max-width:600px; max-height: 85vh; overflow-y:auto; position:relative; transform: translateY(20px) scale(0.97); transition: transform 0.3s cubic-bezier(0.4,0,0.2,1); }
        .modal-overlay.open .modal { transform: translateY(0) scale(1); }
        .modal-header { padding: clamp(20px,3vw,32px); border-bottom: 1px solid var(--border); position:sticky; top:0; background: var(--surface); border-radius: 24px 24px 0 0; display:flex; justify-content:space-between; align-items:flex-start; }
        .modal-close { width:36px; height:36px; background: var(--surface2); border: 1px solid var(--border); border-radius:50%; cursor:pointer; display:flex; align-items:center; justify-content:center; color: var(--text-muted); font-size:18px; transition: all 0.2s; flex-shrink:0; }
        .modal-close:hover { border-color:var(--accent2); color:var(--accent2); }
        .modal-body { padding: clamp(20px,3vw,32px); }
        .modal-company { font-size:14px; color:var(--accent); margin-bottom:6px; font-weight:500; }
        .modal-title { font-family:'Syne',sans-serif; font-size:clamp(20px,3vw,26px); font-weight:800; margin-bottom:4px; line-height:1.2; }
        .modal-location { font-size:13px; color:var(--text-muted); margin-bottom:20px; }
        .modal-section { margin-bottom:20px; }
        .modal-section-title { font-size:11px; letter-spacing:1.5px; text-transform:uppercase; color:var(--text-muted); margin-bottom:10px; font-weight:600; }
        .modal-desc { font-size:14px; color:var(--text-muted); line-height:1.7; }
        .modal-skills { display:flex; flex-wrap:wrap; gap:8px; }
        .modal-skill { background: rgba(56,189,248,0.08); border: 1px solid rgba(56,189,248,0.15); border-radius:8px; padding:6px 12px; font-size:12px; color:#7dd3fc; font-weight:500; }
        .modal-reqs { list-style:none; }
        .modal-reqs li { font-size:13px; color:var(--text-muted); padding: 6px 0; border-bottom:1px solid var(--border); display:flex; gap:8px; align-items:center; }
        .modal-reqs li::before { content:'→'; color:var(--accent); }
        .modal-footer { padding: clamp(16px,2vw,24px) clamp(20px,3vw,32px); border-top: 1px solid var(--border); display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:12px; }
        .modal-salary { font-family:'Syne',sans-serif; font-size:22px; font-weight:800; }
        .modal-salary span { color:var(--accent3); }
        .modal-apply-btn { background: linear-gradient(135deg, var(--accent), #1d6cf5); border:none; cursor:pointer; color:white; font-family:'Syne',sans-serif; font-weight:700; font-size:15px; padding:14px 32px; border-radius:12px; transition:all 0.3s; }
        .modal-apply-btn:hover { box-shadow: 0 8px 30px rgba(56,189,248,0.4); filter:brightness(1.1); transform:translateY(-2px); }

        .login-modal-header { padding: clamp(24px,3vw,36px) clamp(24px,3vw,36px) 20px; text-align: center; position: relative; }
        .login-modal-close { position: absolute; top: 20px; right: 20px; width:36px; height:36px; background: var(--surface2); border: 1px solid var(--border); border-radius:50%; cursor:pointer; display:flex; align-items:center; justify-content:center; color: var(--text-muted); font-size:18px; transition: all 0.2s; }
        .login-modal-close:hover { border-color:var(--accent2); color:var(--accent2); }
        .login-logo-icon { width: 58px; height: 58px; background: linear-gradient(135deg, var(--accent), #1d6cf5); border-radius: 16px; display: flex; align-items: center; justify-content: center; font-size: 26px; margin: 0 auto 16px; box-shadow: 0 0 32px rgba(56,189,248,0.4); }
        .login-modal-title { font-family: 'Syne', sans-serif; font-size: 22px; font-weight: 800; color: var(--text); margin-bottom: 6px; }
        .login-modal-sub { font-size: 13px; color: var(--text-muted); }
        .auth-tabs { display: flex; background: var(--surface2); border: 1px solid var(--border); border-radius: 12px; padding: 4px; margin: 20px clamp(20px,3vw,32px) 0; }
        .auth-tab { flex: 1; padding: 9px; border: none; background: transparent; color: var(--text-muted); cursor: pointer; font-family: 'Syne', sans-serif; font-size: 13px; font-weight: 600; border-radius: 9px; transition: all 0.2s; }
        .auth-tab.active { background: var(--surface); color: var(--text); box-shadow: 0 2px 8px rgba(0,0,0,0.3); }
        .auth-form { padding: clamp(20px,3vw,28px) clamp(20px,3vw,32px); }
        .form-group { margin-bottom: 16px; }
        .form-label { display: block; font-size: 12px; font-weight: 600; color: var(--text-muted); margin-bottom: 7px; text-transform: uppercase; letter-spacing: 0.8px; }
        .form-input { width: 100%; background: var(--surface2); border: 1px solid var(--border); border-radius: 10px; padding: 12px 14px; color: var(--text); font-family: 'DM Sans', sans-serif; font-size: 14px; outline: none; transition: all 0.2s; }
        .form-input:focus { border-color: var(--accent); box-shadow: 0 0 0 3px rgba(56,189,248,0.1); }
        .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
        .forgot-link { font-size: 12px; color: var(--accent); cursor: pointer; float: right; margin-top: -4px; }
        .auth-submit-btn { width: 100%; background: linear-gradient(135deg, var(--accent), #1d6cf5); border: none; cursor: pointer; color: white; font-family: 'Syne', sans-serif; font-weight: 700; font-size: 15px; padding: 14px; border-radius: 12px; transition: all 0.3s; margin-top: 4px; }
        .auth-submit-btn:hover { filter: brightness(1.1); box-shadow: 0 8px 28px rgba(56,189,248,0.35); transform: translateY(-1px); }
        .auth-divider { display: flex; align-items: center; gap: 12px; margin: 20px 0; font-size: 12px; color: var(--text-muted); }
        .auth-divider::before, .auth-divider::after { content: ''; flex:1; height:1px; background: var(--border); }
        .google-btn { width: 100%; background: var(--surface2); border: 1px solid var(--border); border-radius: 12px; padding: 12px; cursor: pointer; color: var(--text); font-family: 'DM Sans', sans-serif; font-size: 14px; font-weight: 500; display: flex; align-items: center; justify-content: center; gap: 10px; transition: all 0.2s; }
        
        .drop-zone { border: 2px dashed rgba(56,189,248,0.25); border-radius: 16px; padding: clamp(32px,4vw,48px) 20px; text-align: center; cursor: pointer; transition: all 0.3s ease; background: rgba(56,189,248,0.02); position: relative; }
        .drop-zone:hover, .drop-zone.drag-over { border-color: var(--accent); background: rgba(56,189,248,0.05); }
        .drop-zone input[type=file] { position: absolute; inset:0; opacity:0; cursor:pointer; width:100%; height:100%; }
        .drop-zone-icon { font-size:44px; margin-bottom:14px; }
        .drop-zone-title { font-family:'Syne',sans-serif; font-weight:700; font-size:16px; color:var(--text); margin-bottom:6px; }
        .drop-zone-sub { font-size:13px; color:var(--text-muted); margin-bottom:16px; }
        .drop-zone-hint { display:inline-block; background: rgba(56,189,248,0.08); border: 1px solid rgba(56,189,248,0.15); border-radius:8px; padding:5px 14px; font-size:12px; color:var(--accent); }
        .file-preview { background: var(--surface2); border: 1px solid var(--accent3); border-radius: 14px; padding: 16px 18px; margin-top: 16px; display: flex; align-items: center; gap: 14px; }
        .file-preview-icon { font-size:28px; flex-shrink:0; }
        .file-preview-name { font-weight:600; font-size:14px; color:var(--text); margin-bottom:2px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; max-width:200px; }
        .file-preview-size { font-size:11px; color:var(--text-muted); }
        .file-preview-remove { margin-left:auto; cursor:pointer; width:28px; height:28px; background:rgba(244,114,182,0.1); border:1px solid rgba(244,114,182,0.2); border-radius:50%; display:flex; align-items:center; justify-content:center; color:var(--accent2); font-size:14px; transition:all 0.2s; flex-shrink:0; }
        
        .upload-progress { margin-top:14px; }
        .progress-bar-wrap { background: var(--surface2); border-radius:6px; height:6px; overflow:hidden; margin-bottom:8px; }
        .progress-bar { height:100%; background: linear-gradient(90deg, var(--accent), #1d6cf5); border-radius:6px; transition: width 0.3s ease; }
        .progress-label { font-size:12px; color:var(--text-muted); }
        .parsed-section { margin-top: 20px; }
        .parsed-label { font-size:11px; letter-spacing:1.5px; text-transform:uppercase; color:var(--text-muted); margin-bottom:10px; font-weight:600; }
        .parsed-skills { display:flex; flex-wrap:wrap; gap:7px; }
        .parsed-skill { background: rgba(74,222,128,0.08); border: 1px solid rgba(74,222,128,0.2); border-radius:7px; padding:5px 11px; font-size:12px; color:var(--accent3); font-weight:500; }
        
        .resume-modal-footer { padding: clamp(16px,2vw,24px) clamp(20px,3vw,32px); border-top: 1px solid var(--border); display:flex; gap:10px; }
        .resume-cancel-btn { flex:1; background:transparent; border:1px solid var(--border); border-radius:12px; padding:12px; color:var(--text-muted); cursor:pointer; font-family:'Syne',sans-serif; font-size:14px; font-weight:600; transition:all 0.2s; }
        .resume-submit-btn { flex:2; background: linear-gradient(135deg, var(--accent), #1d6cf5); border:none; cursor:pointer; color:white; font-family:'Syne',sans-serif; font-weight:700; font-size:14px; padding:12px; border-radius:12px; transition:all 0.3s; }
        .resume-submit-btn:disabled { opacity:0.4; cursor:not-allowed; transform:none; box-shadow:none; }

        footer { position:relative; z-index:1; border-top: 1px solid var(--border); padding: clamp(32px,5vw,60px) clamp(16px,4vw,48px); text-align:center; }
        .footer-logo { font-family:'Syne',sans-serif; font-size:28px; font-weight:800; margin-bottom:8px; }
        .footer-logo span { color:var(--accent); }
        .footer-sub { font-size:13px; color:var(--text-muted); margin-bottom:24px; }
        .footer-stack { display:flex; flex-wrap:wrap; gap:10px; justify-content:center; margin-bottom:24px; }
        .stack-pill { background:var(--surface); border:1px solid var(--border); border-radius:20px; padding:6px 16px; font-size:12px; color:var(--text-muted); }
        .footer-credit { font-size:12px; color:var(--text-dim); }

        .toast { position:fixed; bottom:30px; left:50%; transform:translateX(-50%) translateY(20px); background:var(--surface2); border:1px solid var(--accent3); border-radius:12px; padding:14px 24px; font-size:14px; font-weight:500; color:var(--accent3); z-index:300; opacity:0; pointer-events:none; transition: all 0.4s cubic-bezier(0.4,0,0.2,1); white-space:nowrap; max-width:90vw; box-shadow: 0 8px 32px rgba(74,222,128,0.2); }
        .toast.show { opacity:1; transform:translateX(-50%) translateY(0); }

        @media (max-width:640px) {
          .stats-row { gap:10px; }
          .stat-pill { padding:12px 16px; }
          .jobs-grid { grid-template-columns:1fr; }
          .search-btn span { display:none; }
          .search-btn::after { content:'→'; }
          .form-row { grid-template-columns:1fr; }
          .resume-banner { flex-direction:column; align-items:flex-start; }
          .login-btn .btn-label { display:none; }
        }

        @keyframes fadeUp { from { opacity:0; transform:translateY(20px); } to { opacity:1; transform:translateY(0); } }
        .fade-up { animation: fadeUp 0.5s ease forwards; opacity:0; }
        .delay-1 { animation-delay:0.1s; }
        .delay-2 { animation-delay:0.2s; }
        .delay-3 { animation-delay:0.3s; }
        .delay-4 { animation-delay:0.4s; }
        .delay-5 { animation-delay:0.5s; }
        
        ::-webkit-scrollbar { width:6px; }
        ::-webkit-scrollbar-track { background:var(--bg); }
        ::-webkit-scrollbar-thumb { background:var(--surface2); border-radius:3px; }
        ::-webkit-scrollbar-thumb:hover { background:var(--text-dim); }
      `}</style>

      <div class="bg-grid"></div>
      <div className="bg-orb orb1"></div>
      <div className="bg-orb orb2"></div>
      <div className="bg-orb orb3"></div>

      <header>
        <div className="logo" onClick={() => window.scrollTo(0, 0)}>
          <div className="logo-icon">💼</div>
          <div>
            <div className="logo-name">SMART<span>HIRE</span></div>
            <div className="logo-sub">AI-Powered Job Portal</div>
          </div>
        </div>
        <div className="header-right">
          <div className="live-badge">
            <div className="live-dot"></div>
            Live
          </div>
          {!currentUser ? (
            <button className="login-btn" onClick={() => setIsLoginModalOpen(true)}>
              <span>👤</span><span className="btn-label">Sign In</span>
            </button>
          ) : (
            <div className="user-avatar" onClick={() => setIsUserMenuOpen(!isUserMenuOpen)}>
              <span>{(currentUser.firstName || currentUser.email)[0].toUpperCase()}</span>
              <div className={`user-menu ${isUserMenuOpen ? 'open' : ''}`}>
                <div className="user-menu-header">
                  <div className="user-menu-name">
                    {currentUser.firstName ? `${currentUser.firstName} ${currentUser.lastName || ''}`.trim() : currentUser.email}
                  </div>
                  <div className="user-menu-email">{currentUser.email}</div>
                </div>
                <div className="user-menu-item" onClick={() => setIsResumeModalOpen(true)}>📄 My Resume</div>
                <div className="user-menu-item">🎯 My Applications</div>
                <div className="user-menu-item">⚙️ Settings</div>
                <div className="user-menu-item danger" onClick={logout}>🚪 Sign Out</div>
              </div>
            </div>
          )}
        </div>
      </header>

      <main>
        <div className="container">
          <div className="hero">
            <div className="hero-eyebrow fade-up">✦ AI-Powered Matching Platform</div>
            <h1 className="fade-up delay-1">
              <span className="line1">Find Your Dream</span>
              <span className="line2">Career Match</span>
            </h1>
            <p className="hero-sub fade-up delay-2">
              85% accurate AI matching connects you with jobs that fit your skills perfectly. 10,000+ opportunities updated in real-time.
            </p>
            <div className="stats-row fade-up delay-3">
              <div className="stat-pill">
                <span className="stat-icon">💼</span>
                <div className="stat-info">
                  <div className="stat-num accent">{allJobs.length || '—'}</div>
                  <div className="stat-label">Active Jobs</div>
                </div>
              </div>
              <div className="stat-pill">
                <span className="stat-icon">👥</span>
                <div className="stat-info">
                  <div className="stat-num pink">45,678</div>
                  <div className="stat-label">Candidates</div>
                </div>
              </div>
              <div className="stat-pill">
                <span className="stat-icon">🎯</span>
                <div className="stat-info">
                  <div className="stat-num green">85%</div>
                  <div className="stat-label">Match Rate</div>
                </div>
              </div>
              <div className="stat-pill">
                <span className="stat-icon">✅</span>
                <div className="stat-info">
                  <div className="stat-num gold">2,341</div>
                  <div className="stat-label">Placements</div>
                </div>
              </div>
            </div>

            <div className="search-wrap fade-up delay-4">
              <div className="search-box">
                <div className="search-icon">🔍</div>
                <input 
                  type="text" 
                  className="search-input" 
                  placeholder="Search jobs, companies, skills..." 
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                />
                <button className="search-btn" onClick={() => performSearch(searchQuery)}>
                  <span>Search</span>
                </button>
              </div>
              <div className="search-tags">
                {['Java', 'Python', 'React', 'AWS', 'Remote', 'ML'].map(tag => (
                  <span key={tag} className="search-tag" onClick={() => setSearchQuery(tag)}>{tag}</span>
                ))}
              </div>
            </div>
          </div>

          <div className="resume-banner fade-up delay-5">
            <div className="resume-banner-left">
              <div className="resume-banner-icon">📄</div>
              <div>
                <div className="resume-banner-title">
                  {currentUser?.resumeUploaded 
                    ? 'Resume Uploaded • AI Matching Active' 
                    : currentUser 
                      ? `Hi ${currentUser.firstName || 'there'}! Upload your resume`
                      : 'Upload Your Resume for AI Matching'}
                </div>
                <div className="resume-banner-sub">
                  {currentUser?.resumeUploaded 
                    ? currentUser.resumeName 
                    : 'Let AI parse your skills and find best-fit jobs automatically'}
                </div>
              </div>
            </div>
            <button 
              className={`resume-upload-btn ${currentUser?.resumeUploaded ? 'uploaded' : ''}`}
              onClick={() => {
                if (!currentUser) { setIsLoginModalOpen(true); showToast('🔒 Sign in first to upload your resume'); }
                else { setIsResumeModalOpen(true); }
              }}
            >
              <span>{currentUser?.resumeUploaded ? '✅' : '📤'}</span>
              <span>{currentUser?.resumeUploaded ? 'Update Resume' : 'Upload Resume'}</span>
            </button>
          </div>

          <div className="section-header">
            <div className="section-title">Open Positions</div>
            <div className="section-count">{displayedJobs.length} position{displayedJobs.length !== 1 ? 's' : ''}</div>
          </div>

          <div className="jobs-grid">
            {isLoadingJobs && (
              <div className="loading-state">
                <div className="loading-spinner"></div>
                <div className="loading-text">Fetching jobs from database...</div>
              </div>
            )}
            
            {!isLoadingJobs && jobError && (
              <div className="error-state">
                <h3>⚠️ Could not load jobs</h3>
                <p>Backend may be waking up. Please refresh in 30 seconds.</p>
                <p style={{marginTop: '8px', fontSize: '12px', color: 'var(--text-dim)'}}>{jobError}</p>
              </div>
            )}

            {!isLoadingJobs && !jobError && displayedJobs.length === 0 && (
              <div className="no-results">
                <div className="no-results-emoji">🔭</div>
                <h3>No positions found</h3>
                <p>Try different keywords or browse all jobs</p>
              </div>
            )}

            {!isLoadingJobs && !jobError && displayedJobs.map(job => {
              const score = 70 + ((job.title.length + job.company.length) % 28);
              const matchClass = score >= 90 ? 'match-fire' : score >= 80 ? 'match-hot' : 'match-warm';
              const matchLabel = score >= 90 ? `🔥 ${score}% Match` : `${score}% Match`;
              const emoji = getCompanyEmoji(job.company);
              
              return (
                <div key={job.id} className="job-card" onClick={() => setSelectedJob(job)}>
                  <div className="job-card-top">
                    <div className="job-company-row">
                      <div className="company-logo">{emoji}</div>
                      <div>
                        <div className="job-title">{job.title}</div>
                        <div className="job-company">{job.company}</div>
                      </div>
                    </div>
                    <div className={`match-badge ${matchClass}`}>{matchLabel}</div>
                  </div>
                  <div className="job-location-row">
                    <span className="job-loc">📍 {job.location}</span>
                    <span className="job-type">💼 {job.type || 'Full-time'}</span>
                    <span className="job-time">🕒 {getDaysAgo(job.postedDate)}</span>
                  </div>
                  <div className="skills-row">
                    {(job.skills || []).slice(0, 5).map((s, i) => (
                      <span key={i} className="skill-tag">{s}</span>
                    ))}
                  </div>
                  <p className="job-desc">{job.description}</p>
                  <div className="job-card-bottom">
                    <div className="job-salary"><span>{job.salaryRange || 'Competitive'}</span></div>
                    <button className="apply-btn" onClick={(e) => { e.stopPropagation(); applyToJob(job.id); }}>
                      Apply Now →
                    </button>
                  </div>
                </div>
              );
            })}

            {!isLoadingJobs && !jobError && displayedJobs.length > 0 && (
              <div className="features-card">
                <div className="features-title">✦ Platform Capabilities</div>
                <div className="feature-item">
                  <div className="feat-icon">🤖</div>
                  <div>
                    <div className="feat-title">85% ML Matching Accuracy</div>
                    <div className="feat-sub">AI-powered job recommendations tailored to your profile</div>
                  </div>
                </div>
                <div className="feature-item">
                  <div className="feat-icon">📄</div>
                  <div>
                    <div className="feat-title">NLP Resume Parsing</div>
                    <div className="feat-sub">Automatic skill extraction from your resume</div>
                  </div>
                </div>
                <div className="feature-item">
                  <div className="feat-icon">⚡</div>
                  <div>
                    <div className="feat-title">Real-Time Job Updates</div>
                    <div className="feat-sub">40% faster job search with live notifications</div>
                  </div>
                </div>
                <div className="feature-item">
                  <div className="feat-icon">☁️</div>
                  <div>
                    <div className="feat-title">Cloud-Native Architecture</div>
                    <div className="feat-sub">Scalable Spring Boot backend on Render</div>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      </main>

      <footer>
        <div className="footer-logo">SMART<span>HIRE</span></div>
        <div className="footer-sub">AI-Powered Placement Portal • Built for the Future</div>
        <div className="footer-stack">
          {['⚡ Spring Boot', '🍃 MongoDB', '☁️ Render', '📄 HTML/CSS/JS', '🤖 AI Matching'].map((stack, i) => (
            <span key={i} className="stack-pill">{stack}</span>
          ))}
        </div>
        <div className="footer-credit">Personal Project by Adhikari Manohar • 2025</div>
      </footer>

      {/* Login Modal */}
      <div 
        className={`modal-overlay ${isLoginModalOpen ? 'open' : ''}`} 
        onClick={(e) => e.target.classList.contains('modal-overlay') && setIsLoginModalOpen(false)}
      >
        <div className="modal" style={{ maxWidth: '460px' }}>
          <div className="login-modal-header">
            <button className="login-modal-close" onClick={() => setIsLoginModalOpen(false)}>✕</button>
            <div className="login-logo-icon">💼</div>
            <div className="login-modal-title">Welcome to SmartHire</div>
            <div className="login-modal-sub">Sign in to apply and get AI-matched to jobs</div>
          </div>
          <div className="auth-tabs">
            <button className={`auth-tab ${authTab === 'login' ? 'active' : ''}`} onClick={() => setAuthTab('login')}>Sign In</button>
            <button className={`auth-tab ${authTab === 'register' ? 'active' : ''}`} onClick={() => setAuthTab('register')}>Create Account</button>
          </div>
          
          {authTab === 'login' && (
            <div className="auth-form">
              <div className="form-group">
                <label className="form-label">Email Address</label>
                <input type="email" className="form-input" placeholder="you@example.com" value={loginEmail} onChange={e => setLoginEmail(e.target.value)} />
              </div>
              <div className="form-group">
                <label className="form-label">
                  Password
                  <span className="forgot-link" onClick={() => showToast('📧 Password reset link sent!')}>Forgot password?</span>
                </label>
                <input type="password" className="form-input" placeholder="Enter your password" value={loginPassword} onChange={e => setLoginPassword(e.target.value)} />
              </div>
              <button className="auth-submit-btn" onClick={handleLogin}>Sign In →</button>
              <div className="auth-divider">or continue with</div>
              <button className="google-btn" onClick={() => showToast('🔧 Google OAuth coming soon!')}>
                <svg width="18" height="18" viewBox="0 0 24 24">
                  <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                  <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                  <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                  <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
                </svg>
                Continue with Google
              </button>
            </div>
          )}

          {authTab === 'register' && (
            <div className="auth-form">
              <div className="form-row">
                <div className="form-group">
                  <label className="form-label">First Name</label>
                  <input type="text" className="form-input" placeholder="John" value={regFirstName} onChange={e => setRegFirstName(e.target.value)} />
                </div>
                <div className="form-group">
                  <label className="form-label">Last Name</label>
                  <input type="text" className="form-input" placeholder="Doe" value={regLastName} onChange={e => setRegLastName(e.target.value)} />
                </div>
              </div>
              <div className="form-group">
                <label className="form-label">Email Address</label>
                <input type="email" className="form-input" placeholder="you@example.com" value={regEmail} onChange={e => setRegEmail(e.target.value)} />
              </div>
              <div className="form-group">
                <label className="form-label">Password</label>
                <input type="password" className="form-input" placeholder="Min. 8 characters" value={regPassword} onChange={e => setRegPassword(e.target.value)} />
              </div>
              <div className="form-group">
                <label className="form-label">I am a</label>
                <select className="form-input" style={{ cursor: 'pointer' }} value={regRole} onChange={e => setRegRole(e.target.value)}>
                  <option value="candidate">Job Seeker</option>
                  <option value="recruiter">Recruiter / Hiring Manager</option>
                </select>
              </div>
              <button className="auth-submit-btn" onClick={handleRegister}>Create Account →</button>
              <div className="auth-divider">or continue with</div>
              <button className="google-btn" onClick={() => showToast('🔧 Google OAuth coming soon!')}>
                <svg width="18" height="18" viewBox="0 0 24 24">
                  <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                  <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                  <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                  <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
                </svg>
                Continue with Google
              </button>
            </div>
          )}
        </div>
      </div>

      {/* Job Detail Modal */}
      <div 
        className={`modal-overlay ${selectedJob ? 'open' : ''}`}
        onClick={(e) => e.target.classList.contains('modal-overlay') && setSelectedJob(null)}
      >
        <div className="modal">
          {selectedJob && (() => {
            const score = 70 + ((selectedJob.title.length + selectedJob.company.length) % 28);
            const emoji = getCompanyEmoji(selectedJob.company);
            return (
              <>
                <div className="modal-header">
                  <div>
                    <div className="modal-company">{emoji} {selectedJob.company}</div>
                    <div className="modal-title">{selectedJob.title}</div>
                    <div className="modal-location">
                      📍 {selectedJob.location} &nbsp;•&nbsp; 💼 {selectedJob.type || 'Full-time'} &nbsp;•&nbsp; {score}% Match
                    </div>
                  </div>
                  <button className="modal-close" onClick={() => setSelectedJob(null)}>✕</button>
                </div>
                <div className="modal-body">
                  <div className="modal-section">
                    <div className="modal-section-title">Description</div>
                    <div className="modal-desc">{selectedJob.description}</div>
                  </div>
                  <div className="modal-section">
                    <div className="modal-section-title">Required Skills</div>
                    <div className="modal-skills">
                      {(selectedJob.skills || []).map((s, i) => <span key={i} className="modal-skill">{s}</span>)}
                    </div>
                  </div>
                  <div className="modal-section">
                    <div className="modal-section-title">Requirements</div>
                    <ul className="modal-reqs">
                      {(selectedJob.requirements || ['Experience in relevant field', 'Strong communication skills']).map((r, i) => (
                        <li key={i}>{r}</li>
                      ))}
                    </ul>
                  </div>
                </div>
                <div className="modal-footer">
                  <div className="modal-salary"><span>{selectedJob.salaryRange || 'Competitive'}</span></div>
                  <button className="modal-apply-btn" onClick={() => applyToJob(selectedJob.id)}>Apply Now →</button>
                </div>
              </>
            );
          })()}
        </div>
      </div>

      {/* Resume Upload Modal */}
      <div 
        className={`modal-overlay ${isResumeModalOpen ? 'open' : ''}`}
        onClick={(e) => e.target.classList.contains('modal-overlay') && setIsResumeModalOpen(false)}
      >
        <div className="modal" style={{ maxWidth: '520px' }}>
          <div className="modal-header">
            <div>
              <div className="modal-title">📄 Resume Manager</div>
              <div style={{ fontSize: '13px', color: 'var(--text-muted)', marginTop: '3px' }}>Upload for AI-powered skill matching</div>
            </div>
            <button className="modal-close" onClick={() => setIsResumeModalOpen(false)}>✕</button>
          </div>
          <div className="modal-body">
            {!selectedFile && (
              <div 
                className={`drop-zone ${isDragging ? 'drag-over' : ''}`}
                onDragOver={e => { e.preventDefault(); setIsDragging(true); }}
                onDragLeave={() => setIsDragging(false)}
                onDrop={e => { e.preventDefault(); setIsDragging(false); if (e.dataTransfer.files[0]) processFile(e.dataTransfer.files[0]); }}
              >
                <input type="file" accept=".pdf,.doc,.docx" ref={fileInputRef} onChange={handleFileSelect} />
                <div className="drop-zone-icon">📁</div>
                <div className="drop-zone-title">Drop your resume here</div>
                <div className="drop-zone-sub">or click to browse files</div>
                <div className="drop-zone-hint">PDF, DOC, DOCX • Max 5MB</div>
              </div>
            )}
            
            {selectedFile && (
              <div className="file-preview">
                <div className="file-preview-icon">📄</div>
                <div>
                  <div className="file-preview-name">{selectedFile.name}</div>
                  <div className="file-preview-size">
                    {selectedFile.size < 1024 * 1024 ? (selectedFile.size / 1024).toFixed(1) + ' KB' : (selectedFile.size / 1024 / 1024).toFixed(1) + ' MB'}
                  </div>
                </div>
                {!isUploading && parsedSkills.length === 0 && (
                  <div className="file-preview-remove" onClick={() => setSelectedFile(null)} title="Remove">✕</div>
                )}
              </div>
            )}
            
            {isUploading && (
              <div className="upload-progress">
                <div className="progress-bar-wrap">
                  <div className="progress-bar" style={{ width: `${uploadProgress}%` }}></div>
                </div>
                <div className="progress-label">
                  {uploadProgress < 100 ? `Uploading... ${Math.floor(uploadProgress)}%` : '🤖 Analyzing skills...'}
                </div>
              </div>
            )}
            
            {parsedSkills.length > 0 && (
              <div className="parsed-section">
                <div className="parsed-label">🤖 AI-Detected Skills from Your Resume</div>
                <div className="parsed-skills">
                  {parsedSkills.map((s, i) => <span key={i} className="parsed-skill">{s}</span>)}
                </div>
              </div>
            )}
          </div>
          <div className="resume-modal-footer">
            <button className="resume-cancel-btn" onClick={() => setIsResumeModalOpen(false)}>Cancel</button>
            <button 
              className="resume-submit-btn" 
              disabled={!selectedFile || isUploading || parsedSkills.length > 0} 
              onClick={uploadResume}
              style={parsedSkills.length > 0 ? { background: 'linear-gradient(135deg,#4ade80,#16a34a)' } : {}}
            >
              {parsedSkills.length > 0 ? '✅ Resume Uploaded!' : isUploading ? 'Uploading...' : 'Upload & Analyze'}
            </button>
          </div>
        </div>
      </div>

      <div className={`toast ${toast.show ? 'show' : ''}`}>{toast.msg}</div>
    </>
  );
}