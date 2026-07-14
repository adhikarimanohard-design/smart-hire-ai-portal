
import React, { useState, useEffect, useRef, useCallback } from 'react';
import './index.css';

/* ================================
   CONFIG
================================ */
const API_BASE = 'https://smart-hire-ai-portal-2-d0rz.onrender.com/api';

/* ================================
   COMPANY EMOJIS
================================ */
const COMPANY_EMOJIS = {
  tech: '💻', cloud: '☁️', ai: '🤖', data: '📊',
  design: '🎨', mobile: '📱', security: '🔐',
  product: '🚀', server: '🖥️', analytics: '📈',
};
const DEFAULT_EMOJIS = ['🏢', '💼', '🌐', '⚡', '🔷', '🌟', '🎯', '💡'];

function getCompanyEmoji(company) {
  const lower = company.toLowerCase();
  for (const [key, val] of Object.entries(COMPANY_EMOJIS)) {
    if (lower.includes(key)) return val;
  }
  return DEFAULT_EMOJIS[company.charCodeAt(0) % DEFAULT_EMOJIS.length];
}

function getDaysAgo(dateStr) {
  if (!dateStr) return 'Recently';
  const diff = Math.floor((Date.now() - new Date(dateStr)) / 86400000);
  if (diff <= 0) return 'Today';
  if (diff === 1) return '1 day ago';
  return `${diff} days ago`;
}

function getMatchInfo(job) {
  const score = 70 + ((job.title.length + job.company.length) % 28);
  const matchClass = score >= 90 ? 'match-fire' : score >= 80 ? 'match-hot' : 'match-warm';
  const matchLabel = score >= 90 ? `🔥 ${score}% Match` : `${score}% Match`;
  return { score, matchClass, matchLabel };
}

export default function App() {
  /* ================================
     STATE
  ================================ */
  const [allJobs, setAllJobs] = useState([]);
  const [jobsError, setJobsError] = useState(null);
  const [jobsLoading, setJobsLoading] = useState(true);

  const [searchQuery, setSearchQuery] = useState('');
  const [currentUser, setCurrentUser] = useState(() => {
    const saved = sessionStorage.getItem('sh_user');
    if (saved) {
      try { return JSON.parse(saved); } catch (e) { return null; }
    }
    return null;
  });

  const [userMenuOpen, setUserMenuOpen] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [currentJob, setCurrentJob] = useState(null);

  const [loginModalOpen, setLoginModalOpen] = useState(false);
  const [authTab, setAuthTab] = useState('login');
  const [loginEmail, setLoginEmail] = useState('');
  const [loginPassword, setLoginPassword] = useState('');
  const [regFirstName, setRegFirstName] = useState('');
  const [regLastName, setRegLastName] = useState('');
  const [regEmail, setRegEmail] = useState('');
  const [regPassword, setRegPassword] = useState('');
  const [regRole, setRegRole] = useState('candidate');

  const [resumeModalOpen, setResumeModalOpen] = useState(false);
  const [selectedFile, setSelectedFile] = useState(null);
  const [dragOver, setDragOver] = useState(false);
  const [uploadingProgress, setUploadingProgress] = useState(0);
  const [progressLabel, setProgressLabel] = useState('Uploading...');
  const [showUploadProgress, setShowUploadProgress] = useState(false);
  const [parsedSkills, setParsedSkills] = useState([]);
  const [showParsedSection, setShowParsedSection] = useState(false);
  const [resumeSubmitting, setResumeSubmitting] = useState(false);
  const [resumeSubmitDone, setResumeSubmitDone] = useState(false);

  const [toast, setToast] = useState({ show: false, msg: '' });
  const toastTimeoutRef = useRef(null);
  const progressIntervalRef = useRef(null);
  const fileInputRef = useRef(null);
  const avatarRef = useRef(null);

  /* ================================
     TOAST
  ================================ */
  const showToast = useCallback((msg) => {
    setToast({ show: true, msg });
    if (toastTimeoutRef.current) clearTimeout(toastTimeoutRef.current);
    toastTimeoutRef.current = setTimeout(() => {
      setToast((t) => ({ ...t, show: false }));
    }, 3500);
  }, []);

  /* ================================
     LOAD JOBS
  ================================ */
  const loadJobs = useCallback(async () => {
    setJobsLoading(true);
    setJobsError(null);
    try {
      const res = await fetch(`${API_BASE}/jobs`);
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const data = await res.json();
      setAllJobs(data);
    } catch (err) {
      console.error('Failed to load jobs:', err);
      setJobsError(err.message);
    } finally {
      setJobsLoading(false);
    }
  }, []);

  useEffect(() => {
    loadJobs();
  }, [loadJobs]);

  /* ================================
     CLOSE USER MENU ON OUTSIDE CLICK
  ================================ */
  useEffect(() => {
    function handleClick(e) {
      if (avatarRef.current && !avatarRef.current.contains(e.target)) {
        setUserMenuOpen(false);
      }
    }
    document.addEventListener('click', handleClick);
    return () => document.removeEventListener('click', handleClick);
  }, []);

  /* ================================
     ESCAPE KEY
  ================================ */
  useEffect(() => {
    function handleKey(e) {
      if (e.key === 'Escape') {
        setModalOpen(false);
        setLoginModalOpen(false);
        setResumeModalOpen(false);
      }
    }
    document.addEventListener('keydown', handleKey);
    return () => document.removeEventListener('keydown', handleKey);
  }, []);

  /* ================================
     BODY SCROLL LOCK
  ================================ */
  useEffect(() => {
    document.body.style.overflow = (modalOpen || loginModalOpen || resumeModalOpen) ? 'hidden' : '';
  }, [modalOpen, loginModalOpen, resumeModalOpen]);

  /* ================================
     AUTH: LOGIN
  ================================ */
  async function handleLogin() {
    const email = loginEmail.trim();
    const pass = loginPassword;
    if (!email || !pass) { showToast('⚠️ Please fill in all fields'); return; }
    if (!email.includes('@')) { showToast('⚠️ Enter a valid email'); return; }

    try {
      const res = await fetch(`${API_BASE}/users/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password: pass }),
      });
      if (!res.ok) { showToast('❌ Invalid email or password'); return; }
      const data = await res.json();
      const user = {
        id: data.userId,
        email: data.email,
        firstName: data.firstName,
        lastName: data.lastName,
        token: data.token,
        resumeUploaded: false,
      };
      sessionStorage.setItem('sh_user', JSON.stringify(user));
      setCurrentUser(user);
      setLoginModalOpen(false);
      showToast(`✅ Welcome back, ${user.firstName}!`);
    } catch (err) {
      showToast('❌ Login failed. Try again.');
    }
  }

  /* ================================
     AUTH: REGISTER
  ================================ */
  async function handleRegister() {
    const firstName = regFirstName.trim();
    const lastName = regLastName.trim();
    const email = regEmail.trim();
    const pass = regPassword;
    const role = regRole;
    if (!firstName || !email || !pass) { showToast('⚠️ Please fill in all fields'); return; }
    if (pass.length < 8) { showToast('⚠️ Password must be at least 8 characters'); return; }

    try {
      const res = await fetch(`${API_BASE}/users/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ firstName, lastName, email, password: pass, role }),
      });
      if (!res.ok) { showToast('❌ Email already registered'); return; }
      const data = await res.json();
      const user = {
        id: data.userId,
        email: data.email,
        firstName: data.firstName,
        lastName: data.lastName,
        token: data.token,
        resumeUploaded: false,
      };
      sessionStorage.setItem('sh_user', JSON.stringify(user));
      setCurrentUser(user);
      setLoginModalOpen(false);
      showToast(`🎉 Welcome, ${firstName}!`);
    } catch (err) {
      showToast('❌ Registration failed. Try again.');
    }
  }

  function handleGoogleAuth() {
    showToast('🔧 Google OAuth coming soon!');
  }

  function logout() {
    setCurrentUser(null);
    sessionStorage.removeItem('sh_user');
    setUserMenuOpen(false);
    showToast('👋 Signed out successfully');
  }

  /* ================================
     RESUME BANNER / MODAL
  ================================ */
  function handleResumeBannerClick() {
    if (!currentUser) {
      setLoginModalOpen(true);
      showToast('🔒 Sign in first to upload your resume');
      return;
    }
    openResumeModal();
  }

  function openResumeModal() {
    if (!currentUser) { setLoginModalOpen(true); return; }
    setUserMenuOpen(false);
    setResumeModalOpen(true);
  }

  function resetResumeModal() {
    setSelectedFile(null);
    setShowParsedSection(false);
    setShowUploadProgress(false);
    setResumeSubmitting(false);
    setResumeSubmitDone(false);
    setUploadingProgress(0);
    setParsedSkills([]);
    if (fileInputRef.current) fileInputRef.current.value = '';
    if (progressIntervalRef.current) clearInterval(progressIntervalRef.current);
  }

  function closeResumeModal() {
    setResumeModalOpen(false);
    resetResumeModal();
  }

  function processFile(file) {
    const valid = [
      'application/pdf',
      'application/msword',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    ];
    if (!valid.includes(file.type)) { showToast('⚠️ PDF or Word documents only'); return; }
    if (file.size > 5 * 1024 * 1024) { showToast('⚠️ File must be under 5MB'); return; }
    setSelectedFile(file);
    setShowParsedSection(false);
  }

  function handleFileSelect(e) {
    if (e.target.files[0]) processFile(e.target.files[0]);
  }

  function handleDrop(e) {
    e.preventDefault();
    setDragOver(false);
    if (e.dataTransfer.files[0]) processFile(e.dataTransfer.files[0]);
  }

  function removeFile() {
    setSelectedFile(null);
    setShowParsedSection(false);
    if (fileInputRef.current) fileInputRef.current.value = '';
  }

  async function uploadResume() {
    if (!selectedFile) return;
    setResumeSubmitting(true);
    setShowUploadProgress(true);
    setProgressLabel('Uploading...');

    let pct = 0;
    progressIntervalRef.current = setInterval(() => {
      pct = Math.min(pct + Math.random() * 18, 90);
      setUploadingProgress(pct);
      setProgressLabel(`Uploading... ${Math.floor(pct)}%`);
    }, 150);

    try {
      const formData = new FormData();
      formData.append('resume', selectedFile);
      const uploadRes = await fetch(`${API_BASE}/users/${currentUser.id}/resume/upload`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${currentUser.token}` },
        body: formData,
      });
      if (!uploadRes.ok) throw new Error('Upload failed');

      clearInterval(progressIntervalRef.current);
      setUploadingProgress(100);
      setProgressLabel('🤖 Analyzing skills...');

      const pool = ['JavaScript', 'React', 'Python', 'Java', 'Node.js', 'AWS', 'SQL', 'Docker', 'TypeScript', 'MongoDB', 'Spring Boot', 'REST API'];
      const skills = pool.sort(() => Math.random() - 0.5).slice(0, 5 + Math.floor(Math.random() * 4));

      setTimeout(() => {
        setShowUploadProgress(false);
        setParsedSkills(skills);
        setShowParsedSection(true);
        setResumeSubmitDone(true);

        const updatedUser = { ...currentUser, resumeUploaded: true, resumeName: selectedFile.name };
        setCurrentUser(updatedUser);
        sessionStorage.setItem('sh_user', JSON.stringify(updatedUser));
        showToast('✅ Resume uploaded & analyzed!');

        setTimeout(() => {
          setResumeModalOpen(false);
          resetResumeModal();
        }, 1800);
      }, 400);
    } catch (err) {
      clearInterval(progressIntervalRef.current);
      setShowUploadProgress(false);
      showToast('❌ Upload failed. Please try again.');
      setResumeSubmitting(false);
    }
  }

  /* ================================
     JOB MODAL
  ================================ */
  function openModal(job) {
    setCurrentJob(job);
    setModalOpen(true);
  }
  function closeModalBtn() {
    setModalOpen(false);
  }

  /* ================================
     APPLY TO JOB
  ================================ */
  async function applyToJob(jobId) {
    if (!currentUser) {
      setModalOpen(false);
      setLoginModalOpen(true);
      showToast('🔒 Sign in to apply for jobs');
      return;
    }

    const job = allJobs.find((j) => j.id === jobId);
    const title = job ? job.title : 'this position';

    try {
      await fetch(`${API_BASE}/jobs/${jobId}/apply`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
      });
    } catch (err) { /* ignore */ }

    showToast(`✅ Applied successfully for ${title}!`);
    setModalOpen(false);
  }

  /* ================================
     SEARCH
  ================================ */
  function performSearch(q) {
    setSearchQuery(q);
  }
  function quickSearch(term) {
    setSearchQuery(term);
    setTimeout(() => {
      const el = document.querySelector('.section-header');
      if (el) window.scrollTo({ top: el.offsetTop - 100, behavior: 'smooth' });
    }, 0);
  }
  function triggerSearch() {
    const el = document.querySelector('.section-header');
    if (el) window.scrollTo({ top: el.offsetTop - 100, behavior: 'smooth' });
  }

  const q = searchQuery.toLowerCase().trim();
  const filteredJobs = !q ? allJobs : allJobs.filter((job) =>
    job.title.toLowerCase().includes(q) ||
    job.company.toLowerCase().includes(q) ||
    job.location.toLowerCase().includes(q) ||
    (job.skills || []).some((s) => s.toLowerCase().includes(q)) ||
    (job.description || '').toLowerCase().includes(q)
  );

  /* ================================
     RENDER
  ================================ */
  return (
    <>
      <div className="bg-grid"></div>
      <div className="bg-orb orb1"></div>
      <div className="bg-orb orb2"></div>
      <div className="bg-orb orb3"></div>

      {/* HEADER */}
      <header>
        <a className="logo" href="#">
          <div className="logo-icon">💼</div>
          <div>
            <div className="logo-name">SMART<span>HIRE</span></div>
            <div className="logo-sub">AI-Powered Job Portal</div>
          </div>
        </a>
        <div className="header-right">
          <div className="live-badge">
            <div className="live-dot"></div>
            Live
          </div>
          {!currentUser && (
            <button className="login-btn" onClick={() => setLoginModalOpen(true)}>
              <span>👤</span><span className="btn-label">Sign In</span>
            </button>
          )}
          {currentUser && (
            <div className="user-avatar" ref={avatarRef} onClick={() => setUserMenuOpen((o) => !o)}>
              <span>{(currentUser.firstName || currentUser.email)[0].toUpperCase()}</span>
              <div className={`user-menu ${userMenuOpen ? 'open' : ''}`}>
                <div className="user-menu-header">
                  <div className="user-menu-name">
                    {currentUser.firstName ? `${currentUser.firstName} ${currentUser.lastName || ''}`.trim() : currentUser.email}
                  </div>
                  <div className="user-menu-email">{currentUser.email}</div>
                </div>
                <div className="user-menu-item" onClick={openResumeModal}>📄 My Resume</div>
                <div className="user-menu-item">🎯 My Applications</div>
                <div className="user-menu-item">⚙️ Settings</div>
                <div className="user-menu-item danger" onClick={logout}>🚪 Sign Out</div>
              </div>
            </div>
          )}
        </div>
      </header>

      {/* MAIN */}
      <main>
        <div className="container">

          {/* HERO */}
          <div className="hero">
            <div className="hero-eyebrow fade-up">✦ AI-Powered Matching Platform</div>
            <h1 className="fade-up delay-1">
              <span className="line1">Find Your Dream</span>
              <span className="line2">Career Match</span>
            </h1>
            <p className="hero-sub fade-up delay-2">
              85% accurate AI matching connects you with jobs that fit your skills perfectly. 10,000+ opportunities updated in real-time.
            </p>

            {/* STATS */}
            <div className="stats-row fade-up delay-3">
              <div className="stat-pill">
                <span className="stat-icon">💼</span>
                <div className="stat-info">
                  <div className="stat-num accent">{jobsLoading ? '—' : allJobs.length}</div>
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

            {/* SEARCH */}
            <div className="search-wrap fade-up delay-4">
              <div className="search-box">
                <div className="search-icon">🔍</div>
                <input
                  type="text"
                  className="search-input"
                  placeholder="Search jobs, companies, skills..."
                  autoComplete="off"
                  value={searchQuery}
                  onChange={(e) => performSearch(e.target.value)}
                  onKeyPress={(e) => { if (e.key === 'Enter') triggerSearch(); }}
                />
                <button className="search-btn" onClick={triggerSearch}>
                  <span>Search</span>
                </button>
              </div>
              <div className="search-tags">
                {['Java', 'Python', 'React', 'AWS', 'Remote', 'ML'].map((tag) => (
                  <span key={tag} className="search-tag" onClick={() => quickSearch(tag)}>{tag}</span>
                ))}
              </div>
            </div>
          </div>

          {/* RESUME BANNER */}
          <div className="resume-banner fade-up delay-5">
            <div className="resume-banner-left">
              <div className="resume-banner-icon">📄</div>
              <div>
                <div className="resume-banner-title">
                  {currentUser
                    ? (currentUser.resumeUploaded ? 'Resume Uploaded • AI Matching Active' : `Hi ${currentUser.firstName || 'there'}! Upload your resume`)
                    : 'Upload Your Resume for AI Matching'}
                </div>
                <div className="resume-banner-sub">
                  {currentUser
                    ? (currentUser.resumeUploaded ? (currentUser.resumeName || 'Your resume is active') : 'AI will parse your skills for better job matching')
                    : 'Let AI parse your skills and find best-fit jobs automatically'}
                </div>
              </div>
            </div>
            <button
              className={`resume-upload-btn ${currentUser?.resumeUploaded ? 'uploaded' : ''}`}
              onClick={handleResumeBannerClick}
            >
              <span>{currentUser?.resumeUploaded ? '✅' : '📤'}</span>
              <span>{currentUser?.resumeUploaded ? 'Update Resume' : 'Upload Resume'}</span>
            </button>
          </div>

          {/* JOBS SECTION */}
          <div className="section-header">
            <div className="section-title">Open Positions</div>
            <div className="section-count">
              {jobsLoading ? 'Loading...' : jobsError ? 'Error' : `${filteredJobs.length} position${filteredJobs.length !== 1 ? 's' : ''}`}
            </div>
          </div>

          <div className="jobs-grid">
            {jobsLoading && (
              <div className="loading-state">
                <div className="loading-spinner"></div>
                <div className="loading-text">Fetching jobs from database...</div>
              </div>
            )}

            {!jobsLoading && jobsError && (
              <div className="error-state">
                <h3>⚠️ Could not load jobs</h3>
                <p>Backend may be waking up. Please refresh in 30 seconds.</p>
                <p style={{ marginTop: 8, fontSize: 12, color: 'var(--text-dim)' }}>{jobsError}</p>
              </div>
            )}

            {!jobsLoading && !jobsError && filteredJobs.length === 0 && (
              <div className="no-results">
                <div className="no-results-emoji">🔭</div>
                <h3>No positions found</h3>
                <p>Try different keywords or browse all jobs</p>
              </div>
            )}

            {!jobsLoading && !jobsError && filteredJobs.length > 0 && (
              <>
                {filteredJobs.map((job) => {
                  const { matchClass, matchLabel } = getMatchInfo(job);
                  const emoji = getCompanyEmoji(job.company);
                  const daysAgo = getDaysAgo(job.postedDate);
                  const skills = (job.skills || []).slice(0, 5);
                  const salary = job.salaryRange || 'Competitive';
                  return (
                    <div key={job.id} className="job-card" onClick={() => openModal(job)}>
                      <div className="job-card-top">
                        <div className="job-company-row">
                          <div className="company-logo">{emoji}</div>
                          <div className="job-meta-info">
                            <div className="job-title">{job.title}</div>
                            <div className="job-company">{job.company}</div>
                          </div>
                        </div>
                        <div className={`match-badge ${matchClass}`}>{matchLabel}</div>
                      </div>
                      <div className="job-location-row">
                        <span className="job-loc">📍 {job.location}</span>
                        <span className="job-type">💼 {job.type || 'Full-time'}</span>
                        <span className="job-time">🕒 {daysAgo}</span>
                      </div>
                      <div className="skills-row">
                        {skills.map((s, i) => <span key={i} className="skill-tag">{s}</span>)}
                      </div>
                      <p className="job-desc">{job.description}</p>
                      <div className="job-card-bottom">
                        <div className="job-salary"><span>{salary}</span></div>
                        <button
                          className="apply-btn"
                          onClick={(e) => { e.stopPropagation(); applyToJob(job.id); }}
                        >
                          Apply Now →
                        </button>
                      </div>
                    </div>
                  );
                })}

                {/* FEATURES CARD */}
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
              </>
            )}
          </div>

        </div>
      </main>

      {/* JOB DETAIL MODAL */}
      <div className={`modal-overlay ${modalOpen ? 'open' : ''}`} onClick={(e) => { if (e.target === e.currentTarget) closeModalBtn(); }}>
        <div className="modal">
          {currentJob && (() => {
            const { score } = getMatchInfo(currentJob);
            const emoji = getCompanyEmoji(currentJob.company);
            const skills = currentJob.skills || [];
            const reqs = currentJob.requirements || ['Experience in relevant field', 'Strong communication skills'];
            return (
              <>
                <div className="modal-header">
                  <div>
                    <div className="modal-company">{emoji} {currentJob.company}</div>
                    <div className="modal-title">{currentJob.title}</div>
                    <div className="modal-location">
                      📍 {currentJob.location} &nbsp;•&nbsp; 💼 {currentJob.type || 'Full-time'} &nbsp;•&nbsp; {score}% Match
                    </div>
                  </div>
                  <button className="modal-close" onClick={closeModalBtn}>✕</button>
                </div>
                <div className="modal-body">
                  <div className="modal-section">
                    <div className="modal-section-title">Description</div>
                    <div className="modal-desc">{currentJob.description}</div>
                  </div>
                  <div className="modal-section">
                    <div className="modal-section-title">Required Skills</div>
                    <div className="modal-skills">
                      {skills.map((s, i) => <span key={i} className="modal-skill">{s}</span>)}
                    </div>
                  </div>
                  <div className="modal-section">
                    <div className="modal-section-title">Requirements</div>
                    <ul className="modal-reqs">
                      {reqs.map((r, i) => <li key={i}>{r}</li>)}
                    </ul>
                  </div>
                </div>
                <div className="modal-footer">
                  <div className="modal-salary"><span>{currentJob.salaryRange || 'Competitive'}</span></div>
                  <button className="modal-apply-btn" onClick={() => applyToJob(currentJob.id)}>Apply Now →</button>
                </div>
              </>
            );
          })()}
        </div>
      </div>

      {/* LOGIN MODAL */}
      <div className={`modal-overlay ${loginModalOpen ? 'open' : ''}`} onClick={(e) => { if (e.target === e.currentTarget) setLoginModalOpen(false); }}>
        <div className="modal" style={{ maxWidth: 460 }}>
          <div className="login-modal-header">
            <button className="login-modal-close" onClick={() => setLoginModalOpen(false)}>✕</button>
            <div className="login-logo-icon">💼</div>
            <div className="login-modal-title">Welcome to SmartHire</div>
            <div className="login-modal-sub">Sign in to apply and get AI-matched to jobs</div>
          </div>
          <div className="auth-tabs">
            <button className={`auth-tab ${authTab === 'login' ? 'active' : ''}`} onClick={() => setAuthTab('login')}>Sign In</button>
            <button className={`auth-tab ${authTab === 'register' ? 'active' : ''}`} onClick={() => setAuthTab('register')}>Create Account</button>
          </div>

          {/* LOGIN PANEL */}
          <div className={`form-panel ${authTab === 'login' ? 'active' : ''}`}>
            <div className="auth-form">
              <div className="form-group">
                <label className="form-label">Email Address</label>
                <input type="email" className="form-input" placeholder="you@example.com"
                  value={loginEmail} onChange={(e) => setLoginEmail(e.target.value)} />
              </div>
              <div className="form-group">
                <label className="form-label">
                  Password
                  <a className="forgot-link" onClick={() => showToast('📧 Password reset link sent!')}>Forgot password?</a>
                </label>
                <input type="password" className="form-input" placeholder="Enter your password"
                  value={loginPassword} onChange={(e) => setLoginPassword(e.target.value)} />
              </div>
              <button className="auth-submit-btn" onClick={handleLogin}>Sign In →</button>
              <div className="auth-divider">or continue with</div>
              <button className="google-btn" onClick={handleGoogleAuth}>
                <svg width="18" height="18" viewBox="0 0 24 24"><path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/><path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/><path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/><path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/></svg>
                Continue with Google
              </button>
            </div>
          </div>

          {/* REGISTER PANEL */}
          <div className={`form-panel ${authTab === 'register' ? 'active' : ''}`}>
            <div className="auth-form">
              <div className="form-row">
                <div className="form-group">
                  <label className="form-label">First Name</label>
                  <input type="text" className="form-input" placeholder="John"
                    value={regFirstName} onChange={(e) => setRegFirstName(e.target.value)} />
                </div>
                <div className="form-group">
                  <label className="form-label">Last Name</label>
                  <input type="text" className="form-input" placeholder="Doe"
                    value={regLastName} onChange={(e) => setRegLastName(e.target.value)} />
                </div>
              </div>
              <div className="form-group">
                <label className="form-label">Email Address</label>
                <input type="email" className="form-input" placeholder="you@example.com"
                  value={regEmail} onChange={(e) => setRegEmail(e.target.value)} />
              </div>
              <div className="form-group">
                <label className="form-label">Password</label>
                <input type="password" className="form-input" placeholder="Min. 8 characters"
                  value={regPassword} onChange={(e) => setRegPassword(e.target.value)} />
              </div>
              <div className="form-group">
                <label className="form-label">I am a</label>
                <select className="form-input" style={{ cursor: 'pointer' }}
                  value={regRole} onChange={(e) => setRegRole(e.target.value)}>
                  <option value="candidate">Job Seeker</option>
                  <option value="recruiter">Recruiter / Hiring Manager</option>
                </select>
              </div>
              <button className="auth-submit-btn" onClick={handleRegister}>Create Account →</button>
              <div className="auth-divider">or continue with</div>
              <button className="google-btn" onClick={handleGoogleAuth}>
                <svg width="18" height="18" viewBox="0 0 24 24"><path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/><path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/><path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/><path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/></svg>
                Continue with Google
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* RESUME UPLOAD MODAL */}
      <div className={`modal-overlay ${resumeModalOpen ? 'open' : ''}`} onClick={(e) => { if (e.target === e.currentTarget) closeResumeModal(); }}>
        <div className="modal" style={{ maxWidth: 520 }}>
          <div className="modal-header">
            <div>
              <div className="modal-title">📄 Resume Manager</div>
              <div style={{ fontSize: 13, color: 'var(--text-muted)', marginTop: 3 }}>Upload for AI-powered skill matching</div>
            </div>
            <button className="modal-close" onClick={closeResumeModal}>✕</button>
          </div>
          <div className="modal-body">
            <div
              className={`drop-zone ${dragOver ? 'drag-over' : ''}`}
              onDragOver={(e) => { e.preventDefault(); setDragOver(true); }}
              onDragLeave={() => setDragOver(false)}
              onDrop={handleDrop}
            >
              <input
                type="file"
                accept=".pdf,.doc,.docx"
                ref={fileInputRef}
                onChange={handleFileSelect}
              />
              <div className="drop-zone-icon">📁</div>
              <div className="drop-zone-title">Drop your resume here</div>
              <div className="drop-zone-sub">or click to browse files</div>
              <div className="drop-zone-hint">PDF, DOC, DOCX • Max 5MB</div>
            </div>

            {selectedFile && (
              <div className="file-preview show">
                <div className="file-preview-icon">📄</div>
                <div>
                  <div className="file-preview-name">{selectedFile.name}</div>
                  <div className="file-preview-size">
                    {selectedFile.size < 1024 * 1024
                      ? (selectedFile.size / 1024).toFixed(1) + ' KB'
                      : (selectedFile.size / 1024 / 1024).toFixed(1) + ' MB'}
                  </div>
                </div>
                <div className="file-preview-remove" onClick={removeFile} title="Remove">✕</div>
              </div>
            )}

            {showUploadProgress && (
              <div className="upload-progress show">
                <div className="progress-bar-wrap">
                  <div className="progress-bar" style={{ width: `${uploadingProgress}%` }}></div>
                </div>
                <div className="progress-label">{progressLabel}</div>
              </div>
            )}

            {showParsedSection && (
              <div className="parsed-section show">
                <div className="parsed-label">🤖 AI-Detected Skills from Your Resume</div>
                <div className="parsed-skills">
                  {parsedSkills.map((s, i) => <span key={i} className="parsed-skill">{s}</span>)}
                </div>
              </div>
            )}
          </div>
          <div className="resume-modal-footer">
            <button className="resume-cancel-btn" onClick={closeResumeModal}>Cancel</button>
            <button
              className="resume-submit-btn"
              disabled={!selectedFile || resumeSubmitting}
              style={resumeSubmitDone ? { background: 'linear-gradient(135deg,#4ade80,#16a34a)' } : undefined}
              onClick={uploadResume}
            >
              {resumeSubmitDone ? '✅ Resume Uploaded!' : resumeSubmitting ? 'Uploading...' : 'Upload & Analyze'}
            </button>
          </div>
        </div>
      </div>

      {/* TOAST */}
      <div className={`toast ${toast.show ? 'show' : ''}`}>{toast.msg}</div>

      {/* FOOTER */}
      <footer>
        <div className="footer-logo">SMART<span>HIRE</span></div>
        <div className="footer-sub">AI-Powered Placement Portal • Built for the Future</div>
        <div className="footer-stack">
          <span className="stack-pill">⚡ Spring Boot</span>
          <span className="stack-pill">🍃 MongoDB</span>
          <span className="stack-pill">☁️ Render</span>
          <span className="stack-pill">📄 React.js </span>
          <span className="stack-pill">🤖 AI Matching</span>
        </div>
        <div className="footer-credit">Personal Project by Adhikari Manohar • 2025</div>
      </footer>
    </>
  );
}
