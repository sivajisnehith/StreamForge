'use client';

import React, { useState, useEffect } from 'react';
import { motion } from 'framer-motion';

interface WelcomeHeroProps {
  processingCount: number;
}

export default function WelcomeHero({ processingCount }: WelcomeHeroProps) {
  const [greeting, setGreeting] = useState('Good Evening');
  const [username, setUsername] = useState('Snehith');

  // Parse username from JWT on mount
  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const payload = JSON.parse(window.atob(base64));
        if (payload.username) {
          const usr = payload.username;
          setUsername(usr.charAt(0).toUpperCase() + usr.slice(1));
        } else if (payload.sub) {
          const sub = payload.sub.split('@')[0];
          setUsername(sub.charAt(0).toUpperCase() + sub.slice(1));
        }
      } catch (e) {
        console.error('Error parsing token payload in header:', e);
      }
    }
  }, []);

  useEffect(() => {
    const hrs = new Date().getHours();
    if (hrs < 12) setGreeting('Good Morning');
    else if (hrs < 18) setGreeting('Good Afternoon');
    else setGreeting('Good Evening');
  }, []);

  const healthItems = [
    { name: 'RabbitMQ', status: 'Connected' },
    { name: 'Workers', status: 'Online' },
    { name: 'FFmpeg', status: 'Healthy' },
    { name: 'MinIO', status: 'Healthy' },
    { name: 'PostgreSQL', status: 'Healthy' },
  ];

  return (
    <div className="flex flex-col gap-6 md:flex-row md:items-center md:justify-between py-8">
      {/* Left Column: Welcome Greeting */}
      <div className="flex flex-col gap-2">
        <div className="flex items-center gap-2">
          <span className="font-mono text-xs text-[#ff6a28] tracking-widest uppercase">system_console</span>
          <span className="h-[1px] w-8 bg-[#ff6a28]/30" />
        </div>
        <h1 className="text-3xl md:text-4xl font-extrabold tracking-tight text-white font-sans">
          {greeting}, <span className="bg-gradient-to-r from-white via-white/90 to-white/60 bg-clip-text text-transparent">{username}</span>
        </h1>
        <p className="text-sm text-[#a2aebf]">
          Welcome back. {processingCount === 0 ? 'No videos' : `${processingCount} video${processingCount === 1 ? '' : 's'}`} currently processing.
        </p>
      </div>

      {/* Right Column: Ingest Health Grid */}
      <div className="glass-panel flex flex-wrap gap-4 rounded-xl p-4 md:p-5">
        <div className="absolute top-0 right-10 -z-10 h-10 w-20 rounded-full bg-[#34d1c4]/5 blur-md" />
        {healthItems.map((item) => (
          <div key={item.name} className="flex items-center gap-2 px-3 py-1.5 rounded bg-white/2 border border-white/5">
            <motion.span
              animate={{ opacity: [0.4, 1, 0.4] }}
              transition={{ repeat: Infinity, duration: 2, ease: 'easeInOut' }}
              className="h-2 w-2 rounded-full bg-[#34d1c4] shadow-[0_0_8px_#34d1c4]"
            />
            <span className="font-mono text-[10px] font-semibold text-white">{item.name}</span>
            <span className="font-mono text-[9px] text-[#636e7f] uppercase">{item.status}</span>
          </div>
        ))}
      </div>
    </div>
  );
}
