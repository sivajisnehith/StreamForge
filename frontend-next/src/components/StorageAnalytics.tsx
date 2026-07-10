'use client';

import React, { useState } from 'react';
import { motion } from 'framer-motion';

interface VideoResponse {
  fileSize: number;
  uploadedAt: string;
}

interface StorageAnalyticsProps {
  videos?: VideoResponse[];
}

export default function StorageAnalytics({ videos = [] }: StorageAnalyticsProps) {
  const [hoveredPoint, setHoveredPoint] = useState<{ x: number; y: number; val: string } | null>(null);

  const dayNames = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
  
  // Calculate upload sizes (in MB) and counts by day of week
  const sizeSums = [0, 0, 0, 0, 0, 0, 0];
  const uploadCounts = [0, 0, 0, 0, 0, 0, 0];

  videos.forEach((v) => {
    try {
      const date = new Date(v.uploadedAt);
      const day = date.getDay(); // 0 = Sun, 1 = Mon...
      const targetIdx = day === 0 ? 6 : day - 1; // map so Mon = 0, Sun = 6
      
      sizeSums[targetIdx] += (v.fileSize || 0) / (1024 * 1024); // convert to MB
      uploadCounts[targetIdx]++;
    } catch (e) {
      console.error('Error grouping analytics data point:', e);
    }
  });

  // Scale the Area Chart coordinates based on dynamic sizing sums
  const maxMb = Math.max(...sizeSums, 50); // min scale 50MB
  const bandwidthData = dayNames.map((day, i) => {
    const valMb = sizeSums[i];
    // Map valMb to y coordinate between 35 (max) and 180 (min/zero)
    const y = 180 - (valMb / maxMb) * 145;
    return {
      day,
      val: valMb >= 1024 ? `${(valMb / 1024).toFixed(1)} GB` : `${valMb.toFixed(0)} MB`,
      x: 60 + i * 78,
      y
    };
  });

  // Construct SVG paths
  const pointsStr = bandwidthData.map(d => `${d.x},${d.y}`).join(' L ');
  const linePath = `M ${pointsStr}`;
  const areaPath = `M 60,180 L ${pointsStr} L 528,180 Z`;

  // Scale Bar Chart counts
  const maxLoads = Math.max(...uploadCounts, 5); // min scale 5 loads
  const uploadStats = dayNames.map((day, i) => ({
    label: day,
    count: uploadCounts[i]
  }));

  return (
    <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
      
      {/* Chart 1: Ingestion Bandwidth Area Chart */}
      <div className="glass-panel rounded-xl p-6">
        <div className="flex justify-between items-center mb-6">
          <div className="flex flex-col gap-0.5">
            <h3 className="font-sans text-sm font-bold text-white">Ingestion Payload Volume</h3>
            <span className="font-mono text-[9px] text-[#636e7f] uppercase">Total raw video sizes uploaded per day</span>
          </div>
          <div className="font-mono text-xs font-bold text-[#34d1c4]">
            Total: {(videos.reduce((acc, v) => acc + (v.fileSize || 0), 0) / (1024 * 1024 * 1024)).toFixed(2)} GB
          </div>
        </div>

        <div className="relative w-full h-[220px]">
          <svg className="w-full h-full" viewBox="0 0 600 220">
            <defs>
              <linearGradient id="chartGlow" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stopColor="#34d1c4" stopOpacity="0.25" />
                <stop offset="100%" stopColor="#34d1c4" stopOpacity="0" />
              </linearGradient>
            </defs>

            {/* Grid Lines */}
            <line x1="60" y1="35" x2="528" y2="35" stroke="rgba(255,255,255,0.03)" strokeWidth="1" />
            <line x1="60" y1="85" x2="528" y2="85" stroke="rgba(255,255,255,0.03)" strokeWidth="1" />
            <line x1="60" y1="135" x2="528" y2="135" stroke="rgba(255,255,255,0.03)" strokeWidth="1" />
            <line x1="60" y1="180" x2="528" y2="180" stroke="rgba(255,255,255,0.06)" strokeWidth="1" />

            {/* Area path fill */}
            {videos.length > 0 && (
              <motion.path
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ duration: 1 }}
                d={areaPath}
                fill="url(#chartGlow)"
              />
            )}

            {/* Line Path stroke */}
            {videos.length > 0 && (
              <motion.path
                initial={{ pathLength: 0 }}
                animate={{ pathLength: 1 }}
                transition={{ duration: 1.5, ease: 'easeOut' }}
                d={linePath}
                fill="none"
                stroke="#34d1c4"
                strokeWidth="2"
                strokeLinecap="round"
              />
            )}

            {/* Nodes */}
            {bandwidthData.map((d) => (
              <g key={d.day}>
                <circle
                  cx={d.x}
                  cy={d.y}
                  r="4"
                  fill="#050816"
                  stroke="#34d1c4"
                  strokeWidth="2"
                  className="cursor-pointer transition-all duration-200 hover:r-6"
                  onMouseEnter={() => setHoveredPoint({ x: d.x, y: d.y, val: d.val })}
                  onMouseLeave={() => setHoveredPoint(null)}
                />
                <text x={d.x} y="205" fill="#636e7f" fontSize="10" fontFamily="monospace" textAnchor="middle">
                  {d.day}
                </text>
              </g>
            ))}

            {/* Tooltip Overlay */}
            {hoveredPoint && (
              <g>
                <rect
                  x={hoveredPoint.x - 45}
                  y={hoveredPoint.y - 35}
                  width="90"
                  height="22"
                  rx="4"
                  fill="rgba(10, 16, 21, 0.95)"
                  stroke="rgba(255, 255, 255, 0.1)"
                  strokeWidth="1"
                />
                <text
                  x={hoveredPoint.x}
                  y={hoveredPoint.y - 20}
                  fill="white"
                  fontSize="9"
                  fontFamily="monospace"
                  textAnchor="middle"
                >
                  {hoveredPoint.val}
                </text>
              </g>
            )}
          </svg>
        </div>
      </div>

      {/* Chart 2: Daily Uploads Bar Chart */}
      <div className="glass-panel rounded-xl p-6">
        <div className="flex justify-between items-center mb-6">
          <div className="flex flex-col gap-0.5">
            <h3 className="font-sans text-sm font-bold text-white">Daily Video Loads</h3>
            <span className="font-mono text-[9px] text-[#636e7f] uppercase">Total ingestion triggers per day</span>
          </div>
          <div className="font-mono text-xs font-bold text-[#ff6a28]">
            Total: {videos.length} jobs
          </div>
        </div>

        <div className="flex h-[180px] items-end justify-between gap-2 px-2">
          {uploadStats.map((item) => {
            const heightPct = (item.count / maxLoads) * 100;
            return (
              <div key={item.label} className="flex flex-col items-center gap-2 flex-1 group">
                <div className="opacity-0 group-hover:opacity-100 transition-opacity duration-200 bg-black/90 border border-white/5 rounded px-1.5 py-0.5 font-mono text-[9px] text-white">
                  {item.count}
                </div>
                <div className="relative w-full h-[120px] bg-white/2 rounded overflow-hidden">
                  <motion.div
                    initial={{ height: 0 }}
                    animate={{ height: `${heightPct}%` }}
                    transition={{ duration: 1, ease: 'easeOut' }}
                    className="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-[#ff6a28] to-[#ff7d42] rounded-t group-hover:brightness-110 transition-all duration-200"
                  />
                </div>
                <span className="font-mono text-[10px] text-[#636e7f]">{item.label}</span>
              </div>
            );
          })}
        </div>
      </div>

    </div>
  );
}
