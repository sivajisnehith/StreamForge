'use client';

import React from 'react';

export default function Footer() {
  return (
    <footer className="w-full border-t border-white/5 py-12 mt-12">
      <div className="mx-auto flex max-w-7xl flex-col sm:flex-row items-center justify-between gap-4 px-6">
        <div className="flex flex-col gap-1 text-center sm:text-left">
          <span className="font-sans text-sm font-bold text-white">StreamForge</span>
          <span className="font-mono text-[10px] text-[#636e7f]">
            Enterprise adaptive streaming ingestion cluster
          </span>
        </div>
        <div className="flex items-center gap-6 font-mono text-[11px] text-[#636e7f]">
          <a href="#" className="hover:text-white transition-colors">API Docs</a>
          <a href="#" className="hover:text-white transition-colors">System Status</a>
          <a href="#" className="hover:text-white transition-colors">GitHub Repository</a>
        </div>
      </div>
    </footer>
  );
}
