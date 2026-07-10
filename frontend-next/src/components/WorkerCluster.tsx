'use client';

import React from 'react';
import { Cpu, HardDrive } from 'lucide-react';

interface Worker {
  id: string;
  name: string;
  status: 'Online' | 'Busy' | 'Idle' | 'Offline';
  cpu: number;
  ram: number;
  fps: number | null;
  eta: string | null;
  currentVideo: string | null;
}

function WorkerCard({ worker }: { worker: Worker }) {
  const radius = 22;
  const stroke = 3;
  const normalizedRadius = radius - stroke * 2;
  const circumference = normalizedRadius * 2 * Math.PI;

  const getStrokeOffset = (value: number) => {
    return circumference - (value / 100) * circumference;
  };

  const getStatusBadge = (status: Worker['status']) => {
    switch (status) {
      case 'Busy':
        return (
          <span className="flex items-center gap-1.5 rounded-full border border-[#ff6a28]/15 bg-[#ff6a28]/5 px-2 py-0.5 font-mono text-[9px] font-semibold text-[#ff6a28] uppercase">
            <span className="h-1.5 w-1.5 rounded-full bg-[#ff6a28] animate-pulse" />
            Busy
          </span>
        );
      case 'Idle':
        return (
          <span className="flex items-center gap-1.5 rounded-full border border-[#34d1c4]/15 bg-[#34d1c4]/5 px-2 py-0.5 font-mono text-[9px] font-semibold text-[#34d1c4] uppercase">
            <span className="h-1.5 w-1.5 rounded-full bg-[#34d1c4]" />
            Idle
          </span>
        );
      default:
        return (
          <span className="flex items-center gap-1.5 rounded-full border border-white/5 bg-white/2 px-2 py-0.5 font-mono text-[9px] font-semibold text-[#636e7f] uppercase">
            <span className="h-1.5 w-1.5 rounded-full bg-[#636e7f]" />
            Offline
          </span>
        );
    }
  };

  return (
    <div className="glass-panel rounded-xl p-5 flex flex-col justify-between h-[230px]">
      {/* Card Header */}
      <div className="flex items-center justify-between">
        <div className="flex flex-col">
          <span className="font-sans text-sm font-bold text-white">{worker.name}</span>
          <span className="font-mono text-[9px] text-[#636e7f]">FFmpeg Transcoder</span>
        </div>
        {getStatusBadge(worker.status)}
      </div>

      {/* SVG Circular Gauges */}
      <div className="flex items-center justify-around py-3">
        {/* CPU Circle Gauge */}
        <div className="flex flex-col items-center gap-1">
          <div className="relative flex h-12 w-12 items-center justify-center">
            <svg className="absolute transform -rotate-90 w-full h-full">
              <circle cx="24" cy="24" r={normalizedRadius} className="stroke-white/5 fill-none" strokeWidth={stroke} />
              <circle 
                cx="24" 
                cy="24" 
                r={normalizedRadius} 
                className="stroke-[#ff6a28] fill-none transition-all duration-500" 
                strokeWidth={stroke} 
                strokeDasharray={circumference}
                strokeDashoffset={getStrokeOffset(worker.cpu)}
                strokeLinecap="round"
              />
            </svg>
            <div className="flex flex-col items-center justify-center z-10">
              <span className="font-sans text-[11px] font-bold text-white">{worker.cpu}%</span>
              <span className="text-[7px] text-[#636e7f] font-mono">CPU</span>
            </div>
          </div>
        </div>

        {/* RAM Circle Gauge */}
        <div className="flex flex-col items-center gap-1">
          <div className="relative flex h-12 w-12 items-center justify-center">
            <svg className="absolute transform -rotate-90 w-full h-full">
              <circle cx="24" cy="24" r={normalizedRadius} className="stroke-white/5 fill-none" strokeWidth={stroke} />
              <circle 
                cx="24" 
                cy="24" 
                r={normalizedRadius} 
                className="stroke-[#34d1c4] fill-none transition-all duration-500" 
                strokeWidth={stroke} 
                strokeDasharray={circumference}
                strokeDashoffset={getStrokeOffset(worker.ram)}
                strokeLinecap="round"
              />
            </svg>
            <div className="flex flex-col items-center justify-center z-10">
              <span className="font-sans text-[11px] font-bold text-white">{worker.ram}%</span>
              <span className="text-[7px] text-[#636e7f] font-mono">RAM</span>
            </div>
          </div>
        </div>
      </div>

      {/* Task and stats detail */}
      <div className="border-t border-white/5 pt-3">
        <div className="flex justify-between items-baseline mb-1">
          <span className="font-mono text-[9px] uppercase tracking-wider text-[#636e7f]">Current Job</span>
          {worker.fps && (
            <span className="font-mono text-[9px] text-[#ff6a28] font-bold">
              {worker.fps} FPS · ETA {worker.eta}
            </span>
          )}
        </div>
        <span className="block font-sans text-xs text-white truncate">
          {worker.currentVideo ? worker.currentVideo : 'Queue Idle — Awaiting payload'}
        </span>
      </div>
    </div>
  );
}

interface WorkerClusterProps {
  videos?: any[];
}

export default function WorkerCluster({ videos = [] }: WorkerClusterProps) {
  // Find any active transcoding videos
  const activeJobs = videos.filter((v) => ['UPLOADED', 'PENDING', 'PROCESSING'].includes(v.status));

  const workers: Worker[] = [
    {
      id: '1',
      name: 'Worker 01',
      status: activeJobs.length > 0 ? 'Busy' : 'Idle',
      cpu: activeJobs.length > 0 ? 84 : 4,
      ram: activeJobs.length > 0 ? 62 : 18,
      fps: activeJobs.length > 0 ? 60 : null,
      eta: activeJobs.length > 0 ? '15s' : null,
      currentVideo: activeJobs.length > 0 ? activeJobs[0].originalFileName : null,
    },
    {
      id: '2',
      name: 'Worker 02',
      status: activeJobs.length > 1 ? 'Busy' : 'Idle',
      cpu: activeJobs.length > 1 ? 92 : 2,
      ram: activeJobs.length > 1 ? 58 : 12,
      fps: activeJobs.length > 1 ? 58 : null,
      eta: activeJobs.length > 1 ? '24s' : null,
      currentVideo: activeJobs.length > 1 ? activeJobs[1].originalFileName : null,
    },
    { id: '3', name: 'Worker 03', status: 'Idle', cpu: 2, ram: 14, fps: null, eta: null, currentVideo: null },
    { id: '4', name: 'Worker 04', status: 'Idle', cpu: 1, ram: 11, fps: null, eta: null, currentVideo: null },
  ];

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-1">
        <h2 className="font-sans text-lg font-bold tracking-tight text-white">
          Worker Cluster
        </h2>
        <p className="text-xs text-[#a2aebf]">
          Monitor distributed CPU/RAM segmenting nodes processing video streams.
        </p>
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {workers.map((worker) => (
          <WorkerCard key={worker.id} worker={worker} />
        ))}
      </div>
    </div>
  );
}
