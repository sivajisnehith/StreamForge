'use client';

import React, { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { LogOut } from 'lucide-react';

interface NavItem {
  label: string;
  targetId: string;
}

export default function Navbar() {
  const navItems: NavItem[] = [
    { label: 'Overview', targetId: 'overview' },
    { label: 'Pipeline', targetId: 'pipeline' },
    { label: 'Workers', targetId: 'workers' },
    { label: 'Analytics', targetId: 'analytics' },
    { label: 'Database', targetId: 'database' }
  ];

  const [activeLink, setActiveLink] = useState('overview');
  const [username, setUsername] = useState('Snehith');
  const [initials, setInitials] = useState('SN');

  // Parse username from JWT on mount
  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const payload = JSON.parse(window.atob(base64));
        
        let rawUsername = 'Snehith';
        if (payload.username) {
          rawUsername = payload.username;
        } else if (payload.sub) {
          rawUsername = payload.sub.split('@')[0];
        }
        
        setUsername(rawUsername);
        
        // Compute initials
        if (rawUsername.length >= 2) {
          setInitials(rawUsername.substring(0, 2).toUpperCase());
        } else {
          setInitials(rawUsername.charAt(0).toUpperCase() + 'S');
        }
      } catch (e) {
        console.error('Error parsing token payload in navbar:', e);
      }
    }
  }, []);

  // Performant Scroll Spy using Intersection Observer
  useEffect(() => {
    const targetIds = ['overview', 'pipeline', 'workers', 'analytics', 'database'];
    
    const observerOptions = {
      root: null,
      rootMargin: '-120px 0px -60% 0px',
      threshold: 0.1
    };

    const observerCallback = (entries: IntersectionObserverEntry[]) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          setActiveLink(entry.target.id);
        }
      });
    };

    const observer = new IntersectionObserver(observerCallback, observerOptions);

    targetIds.forEach((id) => {
      const el = document.getElementById(id);
      if (el) observer.observe(el);
    });

    return () => {
      targetIds.forEach((id) => {
        const el = document.getElementById(id);
        if (el) observer.unobserve(el);
      });
    };
  }, []);

  const handleNavClick = (e: React.MouseEvent, targetId: string) => {
    e.preventDefault();
    setActiveLink(targetId);
    
    const element = document.getElementById(targetId);
    if (element) {
      const yOffset = -90; // account for fixed header
      const y = element.getBoundingClientRect().top + window.pageYOffset + yOffset;
      window.scrollTo({ top: y, behavior: 'smooth' });
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    window.location.href = '/login.html';
  };

  return (
    <header className="sticky top-0 z-50 w-full border-b border-white/5 bg-[#050816]/80 backdrop-blur-md">
      <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-4">
        {/* Logo */}
        <div className="flex items-center gap-3">
          <div className="relative flex h-8 w-8 items-center justify-center rounded-lg bg-white/5 shadow-inner">
            <svg className="h-5 w-5 text-[#ff6a28] drop-shadow-[0_2px_8px_rgba(255,106,40,0.4)]" viewBox="0 0 24 24" fill="none">
              <path d="M12 2L4 12h5v10l8-10h-5V2z" fill="currentColor" />
            </svg>
          </div>
          <span className="font-sans text-lg font-bold tracking-tight text-white">
            StreamForge
          </span>
        </div>

        {/* Navigation links - Scroll Spy bound */}
        <nav className="hidden md:flex items-center gap-1">
          {navItems.map((item) => {
            const isActive = item.targetId === activeLink;
            return (
              <a
                key={item.targetId}
                href={`#${item.targetId}`}
                onClick={(e) => handleNavClick(e, item.targetId)}
                className={`relative px-4 py-2 font-mono text-xs font-medium transition-colors ${
                  isActive ? 'text-[#ff6a28] font-bold' : 'text-[#a2aebf] hover:text-white'
                }`}
              >
                {isActive && (
                  <motion.span
                    layoutId="activeNavIndicator"
                    className="absolute inset-0 -z-10 rounded-md bg-[#ff6a28]/5 border border-[#ff6a28]/10"
                    transition={{ type: 'spring', stiffness: 380, damping: 30 }}
                  />
                )}
                {item.label}
              </a>
            );
          })}
        </nav>

        {/* Profile Avatar & Logout button */}
        <div className="flex items-center gap-6">
          <div className="flex items-center gap-3">
            <div className="hidden sm:flex flex-col items-end text-right">
              <span className="font-mono text-xs font-semibold text-white">{username}</span>
              <span className="font-mono text-[9px] text-[#34d1c4]">Enterprise Tier</span>
            </div>
            <div className="relative h-9 w-9 rounded-full bg-gradient-to-tr from-[#ff6a28] to-[#9333ea] p-[1px] shadow-lg">
              <div className="h-full w-full rounded-full bg-[#050816] flex items-center justify-center text-[11px] font-bold font-mono text-white">
                {initials}
              </div>
              <span className="absolute bottom-0 right-0 h-2.5 w-2.5 rounded-full border border-[#050816] bg-green-500" />
            </div>
          </div>

          <button
            onClick={handleLogout}
            className="flex items-center justify-center gap-1.5 rounded border border-white/10 hover:border-white/20 bg-white/2 hover:bg-white/5 px-3 py-1.5 font-mono text-[10px] font-bold text-[#ff5f56] active:scale-95 transition-all cursor-pointer"
          >
            <LogOut className="h-3.5 w-3.5" />
            Logout
          </button>
        </div>
      </div>
    </header>
  );
}
